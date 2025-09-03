package com.jlm.translator.intelligent.speech.offline

import android.content.Context
import android.util.Log
import com.iflytek.aikit.core.*
import com.jlm.translator.intelligent.provider.SpeechParam
import com.jlm.translator.intelligent.speech.AudioPlayer
import com.jlm.translator.intelligent.speech.AudioPlayer.ChannelType
import com.jlm.translator.intelligent.speech.SpeechPlatform
import com.jlm.translator.manager.LanguageModeEnum
import com.jlm.translator.manager.SpeechDataManager
import com.jlm.translator.manager.SpeechState
import com.safframework.log.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * 小牛离线语音合成帮助类
 * 基于iFlytek AI SDK实现的离线文本转语音合成
 */
class OfflineNiuSynthHelper(private val context: Context) : SpeechPlatform {
    
    companion object {
        private const val TAG = "OfflineNiuSynthHelper"
        private const val ABILITY_ID = "e09712bcb" // iFlytek语音合成能力ID
        private const val OUTPUT_DIR_NAME = "xtts30/output"
        private const val LOG_LEVEL = "6"
    }
    
    // 核心组件
    private var currentAiHandle: AiHandle? = null
    private var aiListener: AiListener? = null
    private var synthJob: Job? = null
    
    // 状态管理
    private var isEngineInitialized = false
    private var isSynthesizing = false
    private var needUnInit = false
    
    // 配置参数
    private var outputDir: String = ""
    private var currentFileName: String = ""
    private var currentLanguageMode: LanguageModeEnum = LanguageModeEnum.LANG_TARGET
    
    // 合成参数
    private var vcn: String = "xiaoyi"        // 声音类型
    private var language: Int = 0              // 语言 (0:中文, 1:英文等)
    private var textEncoding: String = "UTF-8"  // 文本编码
    private var pitch: Int = 50                // 音调 (0-100)
    private var volume: Int = 50               // 音量 (0-100)
    private var speed: Int = 50                // 语速 (0-100)
    private var reg: Int = 0                   // 区域
    private var rdn: Int = 0                   // 随机数
    
    // 数据管理
    private val speechDataManager = SpeechDataManager.getInstance()

    init {
        initOutputDir()
    }
    
    /**
     * 初始化输出目录
     */
    private fun initOutputDir() {
        outputDir = context.getExternalFilesDir(null)?.absolutePath + "/" + OUTPUT_DIR_NAME
        val dir = File(outputDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        L.i(TAG, "输出目录: $outputDir")
    }
    
    /**
     * 注册AI监听器
     */
    private fun registerAiListener() {
        aiListener = object : AiListener {
            override fun onResult(handleID: Int, outputData: List<AiResponse>?, usrContext: Any?) {
                L.i(TAG, "合成结果回调: handleID=$handleID, dataSize=${outputData?.size}")
                outputData?.forEach { response ->
                    processSynthesisResponse(response)
                }
            }
            
            override fun onEvent(handleID: Int, event: Int, eventData: List<AiResponse>?, usrContext: Any?) {
                L.i(TAG, "合成事件回调: handleID=$handleID, event=$event")
                
                when (event) {
                    AeeEvent.AEE_EVENT_END.value -> {
                        L.d(TAG, "合成结束事件")
//                        finishSynthesis()
                    }
                    AeeEvent.AEE_EVENT_PROGRESS.value -> {
                        L.d(TAG, "合成进度事件")
                        processProgressEvent(eventData)
                    }
                }
            }
            
            override fun onError(handleID: Int, err: Int, msg: String?, usrContext: Any?) {
                L.e(TAG, "合成错误回调: handleID=$handleID, err=$err, msg=$msg")
                val errorMsg = msg ?: "合成未知错误"
                speechDataManager.tryEmit(SpeechState.Error(errorMsg))
                isSynthesizing = false
            }
        }
        
        AiHelper.getInst().registerListener(ABILITY_ID, aiListener)
    }
    
    /**
     * 处理合成响应数据
     */
    private fun processSynthesisResponse(response: AiResponse) {
        val key = response.key
        val valueBytes = response.value
        
        if (key == null || valueBytes == null) {
            return
        }
        
        L.d(TAG, "处理合成响应: key=$key, dataSize=${valueBytes.size}")
        
        try {
            // 保存音频数据到文件
            val outputFile = getOutputFilePath()
            FileOutputStream(outputFile, true).use { fos ->
                fos.write(valueBytes)
            }
            
            // 实时播放音频数据
            if (valueBytes.isNotEmpty()) {
                when (currentLanguageMode) {
                    LanguageModeEnum.LANG_TARGET -> AudioPlayer.feedAudioData(valueBytes, ChannelType.STEREO)
                    LanguageModeEnum.LANG_TARGET_LEFT -> AudioPlayer.feedAudioData(valueBytes, ChannelType.LEFT)
                    LanguageModeEnum.LANG_TARGET_RIGHT -> AudioPlayer.feedAudioData(valueBytes, ChannelType.RIGHT)
                    else -> {}
                }
            }
        } catch (e: Exception) {
            L.e(TAG, "处理合成响应异常")
        }
    }
    
    /**
     * 处理进度事件
     */
    private fun processProgressEvent(eventData: List<AiResponse>?) {
        if (eventData == null) return
        
        var progress = -1
        var total = -1
        
        eventData.forEach { response ->
            when (response.key) {
                "progress_pos" -> progress = bytesToInt(response.value)
                "progress_len" -> total = bytesToInt(response.value)
            }
        }
        
        if (progress >= 0 && total >= 0) {
            L.d(TAG, "合成进度: $progress/$total")
        }
    }
    
    /**
     * 字节数组转整数
     */
    private fun bytesToInt(bytes: ByteArray?): Int {
        if (bytes == null || bytes.size < 4) return -1
        return (bytes[0].toInt() and 0xFF) or
               ((bytes[1].toInt() and 0xFF) shl 8) or
               ((bytes[2].toInt() and 0xFF) shl 16) or
               ((bytes[3].toInt() and 0xFF) shl 24)
    }
    
    /**
     * 完成合成
     */
    private fun finishSynthesis() {
//        if (isSynthesizing) {
            val outputFile = getOutputFilePath()
            L.i(TAG, "合成完成，输出文件: $outputFile")
            
            speechDataManager.tryEmit(SpeechState.SynthesisCompleted)
            
            isSynthesizing = false
            
            // 清理资源
            currentAiHandle?.let { handle ->
                AiHelper.getInst().end(handle)
                currentAiHandle = null
            }
//        }
    }
    
    /**
     * 获取输出文件路径
     */
    private fun getOutputFilePath(): String {
        return "$outputDir/Output_$currentFileName.pcm"
    }
    
    /**
     * 设置声音参数
     */
    fun setVoiceParams(vcn: String, language: Int) {
        this.vcn = vcn
        this.language = language
    }
    
    /**
     * 设置合成参数
     */
    fun setSynthesisParams(pitch: Int, volume: Int, speed: Int) {
        this.pitch = pitch.coerceIn(0, 100)
        this.volume = volume.coerceIn(0, 100)
        this.speed = speed.coerceIn(0, 100)
    }
    
    /**
     * 合成文本
     * @param text 要合成的文本
     */
    fun synthesizeText(text: String) {
        if (text.trim().isEmpty()) {
            L.w(TAG, "合成文本为空")
            return
        }
        
        L.i(TAG, "开始合成文本: $text")
        
        currentFileName = System.currentTimeMillis().toString()
        isSynthesizing = true
        
//        speechDataManager.tryEmit(SpeechState.SynthesisStart)
        
        // 清理旧的输出文件
        cleanupOldFiles()
        
        // 启动合成
        startSynthesis(text)
    }
    
    /**
     * 清理旧文件
     */
    private fun cleanupOldFiles() {
        try {
            val dir = File(outputDir)
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".pcm")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            L.e(TAG, "清理旧文件异常")
        }
    }

    private fun initSynth() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                L.d(TAG, "构建合成参数")

                // 构建合成参数
                val paramBuilder = AiInput.builder()
                    .param("vcn", vcn)
                    .param("language", language)
                    .param("textEncoding", textEncoding)
                    .param("pitch", pitch)
                    .param("volume", volume)
                    .param("speed", speed)
                    .param("reg", reg)
                    .param("rdn", rdn)

                // 启动AI Handle
                currentAiHandle = AiHelper.getInst().start(ABILITY_ID, paramBuilder.build(), null)

                if (currentAiHandle == null) {
                    L.e(TAG, "合成任务启动失败，handle为空")
                    isSynthesizing = false
                    return@launch
                }

                if (currentAiHandle!!.code != 0) {
                    L.e(TAG, "合成任务启动失败，错误码: ${currentAiHandle!!.code}")
                    isSynthesizing = false
                    currentAiHandle = null
                    return@launch
                }
            } catch (e: Exception) {

            }

        }
    }
    
    /**
     * 启动合成
     */
    private fun startSynthesis(text: String) {
        synthJob = CoroutineScope(Dispatchers.IO).launch {
            try {
//                L.d(TAG, "构建合成参数")
//
//                // 构建合成参数
//                val paramBuilder = AiInput.builder()
//                    .param("vcn", vcn)
//                    .param("language", language)
//                    .param("textEncoding", textEncoding)
//                    .param("pitch", pitch)
//                    .param("volume", volume)
//                    .param("speed", speed)
//                    .param("reg", reg)
//                    .param("rdn", rdn)
//
//                // 启动AI Handle
//                currentAiHandle = AiHelper.getInst().start(ABILITY_ID, paramBuilder.build(), null)
//
//                if (currentAiHandle == null) {
//                    L.e(TAG, "合成任务启动失败，handle为空")
//                    isSynthesizing = false
//                    return@launch
//                }
//
//                if (currentAiHandle!!.code != 0) {
//                    L.e(TAG, "合成任务启动失败，错误码: ${currentAiHandle!!.code}")
//                    isSynthesizing = false
//                    currentAiHandle = null
//                    return@launch
//                }
//
//                L.d(TAG, "合成任务启动成功，handleId: ${currentAiHandle!!.id}")
                
                // 发送文本数据
                sendTextData(text)
                
            } catch (e: Exception) {
                L.e(TAG, "启动合成任务异常")
                isSynthesizing = false
            }
        }
    }
    
    /**
     * 发送文本数据
     */
    private fun sendTextData(text: String) {
        currentAiHandle?.let { handle ->
            try {
                L.d(TAG, "发送合成文本数据")
                
                val dataBuilder = AiRequest.builder()
                val input = AiText.get("text")
                    .data(text)
                    .valid()
                dataBuilder.payload(input)
                
                val ret = AiHelper.getInst().write(dataBuilder.build(), handle)
                
                if (ret != 0) {
                    L.e(TAG, "发送合成数据失败: $ret")
                    isSynthesizing = false
                    return
                }
                
                L.d(TAG, "合成数据发送成功")
                
            } catch (e: Exception) {
                L.e(TAG, "发送合成数据异常")
                isSynthesizing = false
            }
        }
    }
    
    /**
     * 停止合成
     */
    fun stopSynthesis() {
        L.i(TAG, "停止合成")
        
        if (!isSynthesizing) {
            L.w(TAG, "合成未在进行中")
            return
        }
        
        finishSynthesis()
        
        // 停止音频播放
        AudioPlayer.stop(getChannelType())
    }
    
    // 实现SpeechPlatform接口
    override fun initial(speechParam: SpeechParam) {
        L.i(TAG, "初始化语音合成引擎")
        
        when (speechParam) {
            is SpeechParam.SynthesisStart -> {
                currentLanguageMode = speechParam.langMode
                
                // 初始化AudioPlayer
                AudioPlayer.initialize(getChannelType(), object : AudioPlayer.Callback {
                    override fun onPlayStart() {
                        L.d(TAG, "音频播放开始")
                    }
                    
                    override fun onPlayOver() {
                        L.d(TAG, "音频播放结束")
                    }
                })
                
                val language = speechParam.language
                // 根据语言设置默认参数
                setVoiceParams(getDefaultVcn(language.getCurVoicer()), getLanguageCode(language.synthesis_key))
                //初始化
                initSynth()
                registerAiListener()
                // 开启播放
                AudioPlayer.startPlayback(getChannelType())
            }
            else -> {
                L.w(TAG, "不支持的参数类型: ${speechParam::class.simpleName}")
            }
        }
        
        isEngineInitialized = true
        L.i(TAG, "语音合成引擎初始化完成")
    }

    override fun start(speechParam: SpeechParam) {
        L.i(TAG, "开始语音合成")
        when (speechParam) {
            is SpeechParam.SynthContent -> {
                currentLanguageMode = speechParam.langMode
                synthesizeText(speechParam.content)
            }
            is SpeechParam.SynthContentTwoTalk -> {
                currentLanguageMode = speechParam.langMode
                synthesizeText(speechParam.content)
            }
            else -> {
                L.e(TAG, "不支持的合成参数类型: ${speechParam::class.simpleName}")
            }
        }
    }

    override fun stop() {
        L.i(TAG, "停止语音合成服务")
        stopSynthesis()
    }

    override fun close() {
        L.i(TAG, "关闭语音合成引擎")
        
        stop()
        
        // 取消合成任务
        synthJob?.cancel()
        synthJob = null
        
        // 清理AI引擎
//        if (needUnInit) {
            try {
                val ret = AiHelper.getInst().engineUnInit(ABILITY_ID)
                if (ret != 0) {
                    L.e(TAG, "合成引擎反初始化失败: $ret")
                } else {
                    L.i(TAG, "合成引擎反初始化成功")
                }
                needUnInit = false
            } catch (e: Exception) {
                L.e(TAG, "合成引擎反初始化异常")
            }
//        }
        
        // 释放AudioPlayer
        AudioPlayer.releaseTrack(getChannelType())
        
        isEngineInitialized = false
        
        L.i(TAG, "语音合成引擎已关闭")
    }
    
    /**
     * 获取声道类型
     */
    private fun getChannelType(): ChannelType {
        return when (currentLanguageMode) {
            LanguageModeEnum.LANG_TARGET_LEFT -> ChannelType.LEFT
            LanguageModeEnum.LANG_TARGET_RIGHT -> ChannelType.RIGHT
            else -> ChannelType.STEREO
        }
    }
    
    /**
     * 获取默认声音
     */
    private fun getDefaultVcn(langKey: String): String {
        return langKey
    }
    
    /**
     * 获取语言代码
     */
    private fun getLanguageCode(langKey: String): Int {
        return langKey.toInt()
    }
    
    /**
     * 检查是否正在合成
     */
    fun isSynthesizing(): Boolean = isSynthesizing
    
    /**
     * 获取当前合成参数
     */
    fun getCurrentParams(): Map<String, Any> {
        return mapOf(
            "vcn" to vcn,
            "language" to language,
            "textEncoding" to textEncoding,
            "pitch" to pitch,
            "volume" to volume,
            "speed" to speed,
            "outputDir" to outputDir
        )
    }
}