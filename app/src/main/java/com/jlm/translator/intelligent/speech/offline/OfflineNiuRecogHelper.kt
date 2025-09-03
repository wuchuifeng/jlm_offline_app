package com.jlm.translator.intelligent.speech.offline

import android.content.Context
import android.util.Log
import com.iflytek.aikit.core.*
import com.jlm.translator.intelligent.provider.SpeechParam
import com.jlm.translator.intelligent.speech.SpeechPlatform
import com.jlm.translator.manager.SpeechDataManager
import com.jlm.translator.manager.SpeechState
import com.safframework.log.L
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 用户上下文数据类
 */
data class UserContext(
    val index: Int,
    val description: String
)

/**
 * 小牛离线语音识别辅助类
 * 基于iFlytek AI SDK实现的实时语音识别
 */
class OfflineNiuRecogHelper(private val context: Context) : SpeechPlatform, AudioRecorderHelper.AudioDataCallback {
    
    companion object {
        private const val TAG = "OfflineNiuRecogHelper"
        private const val ABILITY_ID = "e0e26945b" // iFlytek能力ID
        private const val BUFFER_SIZE = 320 // 每次发送的音频数据大小（20ms@16kHz）
    }
    
    // 核心组件
    private var audioRecorderHelper: AudioRecorderHelper? = null
    private var currentAiHandle: AiHandle? = null
    private var aiListener: AiListener? = null
    
    // 状态管理
    private var isEngineInitialized = false
    private var isRecording = false
    private var isProcessing = false
    private var isFirstFrame = true
    private var needUnInit = false
    private var requestIndex = 0
    
    // 配置参数
    private var languageType = 0 // 0:中英, 20:日语, 21:韩语, 22:俄语等
    private var enableVad = true
    private var enablePostProc = true
    private var vadThreshold = 0.3
    private var vadEnergyThreshold = 9
    private var vadSpeechEnd = 120000
    private var vadResponseTime = 150000

    private val speechDataManager = SpeechDataManager.getInstance()
    
    // 回调接口
//    private var recognitionCallback: RecognitionCallback? = null
    
    /**
     * 语音识别回调接口
     */
    interface RecognitionCallback {
        /**
         * 实时识别结果（临时结果）
         */
        fun onRealTimeResult(text: String)
        
        /**
         * 最终识别结果
         */
        fun onFinalResult(text: String)
        
        /**
         * 识别开始
         */
        fun onRecognitionStart()
        
        /**
         * 识别结束
         */
        fun onRecognitionEnd()
        
        /**
         * 识别错误
         */
        fun onRecognitionError(errorCode: Int, errorMsg: String)
        
        /**
         * 状态变化
         */
        fun onStateChanged(isRecording: Boolean)
    }
    
    init {
        initAudioRecorder()
        registerAiListener()
    }
    
    /**
     * 初始化音频录制器
     */
    private fun initAudioRecorder() {
        audioRecorderHelper = AudioRecorderHelper(context).apply {
            setAudioDataCallback(this@OfflineNiuRecogHelper)
        }
    }
    
    /**
     * 注册AI监听器
     */
    private fun registerAiListener() {
        aiListener = object : AiListener {
            override fun onResult(handleID: Int, outputData: List<AiResponse>?, usrContext: Any?) {
                Log.i(TAG, "AI结果回调: handleID=$handleID, dataSize=${outputData?.size}")
                outputData?.forEach { response ->
                    processAiResponse(response)
                }
            }
            
            override fun onEvent(handleID: Int, event: Int, eventData: List<AiResponse>?, usrContext: Any?) {
                Log.i(TAG, "AI事件回调: handleID=$handleID, event=$event")
            }
            
            override fun onError(handleID: Int, err: Int, msg: String?, usrContext: Any?) {
                Log.e(TAG, "AI错误回调: handleID=$handleID, err=$err, msg=$msg")
//                recognitionCallback?.onRecognitionError(err, msg ?: "未知错误")
            }
        }
        
        AiHelper.getInst().registerListener(ABILITY_ID, aiListener)
    }
    
    /**
     * 处理AI响应数据
     */
    private fun processAiResponse(response: AiResponse) {
        val key = response.key
        val valueBytes = response.value
        
        if (key == null || valueBytes == null) {
            return
        }
        
        Log.d(TAG, "处理响应: key=$key, dataSize=${valueBytes.size}")
        
        // 只处理plain类型的结果
        if (key.contains("plain")) {
            try {
                // 清理字节数组中的分隔符
                val cleanedBytes = removeSeparators(valueBytes)
                val resultText = cleanedBytes.toString(Charset.forName("GBK"))
                L.d(TAG, "处理结果Plain: $resultText")
                // 提取纯文本
                val plainText = resultText //extractPlainText(resultText)
                if (plainText.isNotEmpty()) {
                    //发送识别结果
                    speechDataManager.tryEmit(SpeechState.Recognized(plainText))
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理AI响应异常", e)
            }
        } else if (key.contains("pgs")) {
            // 清理字节数组中的分隔符
            val cleanedBytes = removeSeparators(valueBytes)
            val resultText = cleanedBytes.toString(Charset.forName("GBK"))
            if (resultText.isNotEmpty()) {
                //发送实时识别内容
                speechDataManager.tryEmit(SpeechState.Recognizing(resultText))
            }
        }
    }
    
    /**
     * 清理字节数组中的分隔符
     */
    private fun removeSeparators(data: ByteArray): ByteArray {
        val cleaned = ByteArrayOutputStream()
        data.forEach { byte ->
            // 过滤掉分隔符：45 是 '-'，126 是 '~'
            if (byte != 45.toByte() && byte != 126.toByte()) {
                cleaned.write(byte.toInt())
            }
        }
        return cleaned.toByteArray()
    }
    
    /**
     * 从结果数据中提取纯文本
     */
    private fun extractPlainText(resultData: String): String {
        if (resultData.trim().isEmpty()) {
            return ""
        }
        
        return try {
            // 尝试解析JSON格式
            if (resultData.trim().startsWith("{")) {
                val jsonObject = JSONObject(resultData)
                
                // 尝试从ws字段提取文本
                if (jsonObject.has("ws")) {
                    val wsArray = jsonObject.getJSONArray("ws")
                    val text = StringBuilder()
                    
                    for (i in 0 until wsArray.length()) {
                        val wsItem = wsArray.getJSONObject(i)
                        if (wsItem.has("cw")) {
                            val cwArray = wsItem.getJSONArray("cw")
                            for (j in 0 until cwArray.length()) {
                                val cwItem = cwArray.getJSONObject(j)
                                if (cwItem.has("w")) {
                                    text.append(cwItem.getString("w"))
                                }
                            }
                        }
                    }
                    return text.toString().trim()
                }
                
                // 尝试获取其他字段
                when {
                    jsonObject.has("text") -> return jsonObject.getString("text").trim()
                    jsonObject.has("result") -> return jsonObject.getString("result").trim()
                }
            }
            
            // 简单文本清理
            resultData.replace(Regex("<[^>]+>"), "").trim()
        } catch (e: JSONException) {
            // JSON解析失败，返回简单清理后的文本
            resultData.replace(Regex("<[^>]+>"), "").trim()
        }
    }
    
    /**
     * 设置识别回调
     */
    fun setRecognitionCallback(callback: RecognitionCallback?) {
//        this.recognitionCallback = callback
    }
    
    /**
     * 设置语言类型
     * @param type 语言类型: 0:中英, 20:日语, 21:韩语, 22:俄语, 23:法语, 24:西班牙语, 25:德语等
     */
    fun setLanguageType(type: Int) {
        this.languageType = type
    }
    
    /**
     * 设置VAD参数
     */
    fun setVadParams(enable: Boolean, threshold: Double = 0.1332, energyThreshold: Int = 9) {
        this.enableVad = enable
        this.vadThreshold = threshold
        this.vadEnergyThreshold = energyThreshold
    }
    
    // 实现SpeechPlatform接口
    override fun initial(speechParam: SpeechParam) {
        Log.i(TAG, "初始化语音识别引擎")
        
        // 首先确保SDK已初始化
//        if (!OfflineManager.isInitialized()) {
//            Log.w(TAG, "离线SDK未初始化，尝试初始化")
//            OfflineManager.initSdkWithResources(context, object : OfflineManager.SdkInitCallback {
//                override fun onAuthSuccess() {
//                    Log.i(TAG, "SDK授权成功，继续语音识别初始化")
//                    completeInitialization(speechParam)
//                }
//
//                override fun onAuthFailed(errorCode: Int, errorMsg: String) {
//                    Log.e(TAG, "SDK授权失败: $errorMsg")
////                    recognitionCallback?.onRecognitionError(errorCode, "SDK授权失败: $errorMsg")
//                }
//
//                override fun onHttpResult(code: Int) {
//                    Log.d(TAG, "HTTP认证结果: $code")
//                }
//
//                override fun onOtherError(code: Int, errorMsg: String) {
//                    Log.e(TAG, "SDK其他错误: $errorMsg")
//                }
//
//                override fun onInitStart() {
//                    Log.d(TAG, "SDK初始化开始")
//                }
//
//                override fun onInitComplete() {
//                    Log.d(TAG, "SDK初始化完成")
//                }
//            })
//            return
//        }
//
//        // 检查SDK授权状态
//        if (!OfflineManager.isAuthorized()) {
//            val errorMsg = "SDK未授权成功: ${OfflineManager.getAuthStatusDescription()}"
//            Log.e(TAG, errorMsg)
//            recognitionCallback?.onRecognitionError(OfflineManager.getAuthResult(), errorMsg)
//            return
//        }
//
//        completeInitialization(speechParam)
    }
    
    /**
     * 完成初始化
     */
    private fun completeInitialization(speechParam: SpeechParam) {
        // 根据参数设置语言类型
        when (speechParam) {
            is SpeechParam.RecognitionStart -> {
                // 根据源语言设置语言类型
                setLanguageFromParam(speechParam.sourceLang.key)
            }
            is SpeechParam.RecogAndTransStart -> {
                setLanguageFromParam(speechParam.sourceLang.key)
            }
            else -> {
                Log.w(TAG, "不支持的参数类型: ${speechParam::class.simpleName}")
            }
        }
        
        isEngineInitialized = true
        Log.i(TAG, "语音识别引擎初始化完成，语言类型: $languageType")
    }
    
    override fun start(speechParam: SpeechParam) {
        Log.i(TAG, "开始语音识别")
        
//        if (!isEngineInitialized) {
//            initial(speechParam)
//        }
        if (speechParam is SpeechParam.RecognitionStart) {
            setLanguageFromParam(speechParam.sourceLang.recognize_key)
        }
        
        if (isRecording) {
            Log.w(TAG, "识别已在进行中")
            return
        }
        
        // 启动AI引擎
        if (startAiEngine()) {
            // 开始录音
            audioRecorderHelper?.let { recorder ->
                if (recorder.startRecording()) {
                    isRecording = true
                    isFirstFrame = true
                    //发送录音开始的flow
                    speechDataManager.tryEmit(SpeechState.RecordStart)
//                    recognitionCallback?.onRecognitionStart()
//                    recognitionCallback?.onStateChanged(true)
                    Log.i(TAG, "语音识别启动成功")
                } else {
                    Log.e(TAG, "录音启动失败")
//                    recognitionCallback?.onRecognitionError(-1, "录音启动失败")
                }
            } ?: run {
                Log.e(TAG, "音频录制器未初始化")
//                recognitionCallback?.onRecognitionError(-1, "音频录制器未初始化")
            }
        }
    }
    
    override fun stop() {
        Log.i(TAG, "停止语音识别")
        
        if (!isRecording) {
            Log.w(TAG, "识别未在进行中")
            return
        }
        
        // 停止录音
        audioRecorderHelper?.stopRecording()
        
        // 发送结束信号并停止AI引擎
        currentAiHandle?.let { handle ->
            sendEndFrame()
            AiHelper.getInst().end(handle)
            currentAiHandle = null
        }
        
        isRecording = false
        isProcessing = false
        
//        recognitionCallback?.onRecognitionEnd()
//        recognitionCallback?.onStateChanged(false)
        
        Log.i(TAG, "语音识别已停止")
    }
    
    override fun close() {
        Log.i(TAG, "关闭语音识别引擎")
        
        stop()
        
        // 释放资源
        audioRecorderHelper?.release()
        audioRecorderHelper = null
        
        // 清理AI引擎
        if (needUnInit && isEngineInitialized) {
            val ret = AiHelper.getInst().engineUnInit(ABILITY_ID)
            if (ret != 0) {
                Log.e(TAG, "AI引擎反初始化失败: $ret")
            } else {
                Log.i(TAG, "AI引擎反初始化成功")
            }
            needUnInit = false
        }
        
        isEngineInitialized = false
//        recognitionCallback = null
        
        Log.i(TAG, "语音识别引擎已关闭")
    }
    
    /**
     * 启动AI引擎
     */
    private fun startAiEngine(): Boolean {
        try {
            Log.d(TAG, "启动AI引擎，语言类型: $languageType")
            
            val paramBuilder = AiInput.builder().apply {
                param("vadOn", enableVad)
                param("rltSep", "-")
                param("vadLinkOn", false)
                param("postprocOn", enablePostProc)
                param("vadThreshold", vadThreshold)
                param("vadEnergyThreshold", vadEnergyThreshold)
                param("vadSpeechEnd", vadSpeechEnd)
                param("vadResponsetime", vadResponseTime)
                param("languageType", languageType)
                param("outputType", 1) // 1:普通文本
                param("puncCache", true)
            }
            
            currentAiHandle = AiHelper.getInst().start(
                ABILITY_ID, 
                paramBuilder.build(),
                UserContext(requestIndex++, "realtime_recognition")
            )
            needUnInit = true
            
            return when {
                currentAiHandle == null -> {
                    Log.e(TAG, "AI引擎启动失败，handle为空")
//                    recognitionCallback?.onRecognitionError(-1, "AI引擎启动失败")
                    false
                }
                currentAiHandle!!.code != 0 -> {
                    Log.e(TAG, "AI引擎启动失败，错误码: ${currentAiHandle!!.code}")
//                    recognitionCallback?.onRecognitionError(currentAiHandle!!.code, "AI引擎启动失败")
                    false
                }
                else -> {
                    Log.i(TAG, "AI引擎启动成功")
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI引擎启动异常", e)
//            recognitionCallback?.onRecognitionError(-1, "AI引擎启动异常: ${e.message}")
            return false
        }
    }
    
    /**
     * 发送结束帧
     */
    private fun sendEndFrame() {
        currentAiHandle?.let { handle ->
            try {
                val dataBuilder = AiRequest.builder()
                val audio = AiAudio.get("input")
                    .encoding(AiAudio.ENCODING_DEFAULT)
                    .data(ByteArray(0))
                audio.status(AiStatus.END)
                dataBuilder.payload(audio.valid())
                
                val ret = AiHelper.getInst().write(dataBuilder.build(), handle)
                if (ret == 0) {
                    AiHelper.getInst().read(ABILITY_ID, handle)
                }
                Log.d(TAG, "发送结束帧完成")
            } catch (e: Exception) {
                Log.e(TAG, "发送结束帧异常", e)
            }
        }
    }
    
    /**
     * 根据语言键设置语言类型
     */
    private fun setLanguageFromParam(langKey: String) {
        languageType = langKey.toInt()
//            when (langKey.lowercase()) {
//            "zh", "cn" -> 0 // 中英
//            "ja" -> 20 // 日语
//            "ko" -> 21 // 韩语
//            "ru" -> 22 // 俄语
//            "fr" -> 23 // 法语
//            "es" -> 24 // 西班牙语
//            "de" -> 25 // 德语
//            "th" -> 27 // 泰语
//            "ar" -> 28 // 阿拉伯语
//            "ug" -> 50 // 维吾尔语
//            else -> 0 // 默认中英
//        }
    }
    
    // 实现AudioRecorderHelper.AudioDataCallback接口
    override fun onAudioData(audioData: ByteArray, length: Int) {
        if (currentAiHandle == null || !isRecording) {
            return
        }
        
        try {
            val status = if (isFirstFrame) AiStatus.BEGIN else AiStatus.CONTINUE
            isFirstFrame = false
            
            val dataBuilder = AiRequest.builder()
            val audio = AiAudio.get("input")
                .encoding(AiAudio.ENCODING_DEFAULT)
                .data(audioData.copyOf(length))
            audio.status(status)
            dataBuilder.payload(audio.valid())
            
            val writeRet = AiHelper.getInst().write(dataBuilder.build(), currentAiHandle)
            if (writeRet == 0) {
                val readRet = AiHelper.getInst().read(ABILITY_ID, currentAiHandle)
                if (readRet != 0) {
                    Log.e(TAG, "AI引擎读取失败: $readRet")
                }
            } else {
                Log.e(TAG, "AI引擎写入失败: $writeRet")
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理音频数据异常", e)
        }
    }
    
    override fun onError(errorMsg: String) {
        Log.e(TAG, "录音错误: $errorMsg")
//        recognitionCallback?.onRecognitionError(-1, "录音错误: $errorMsg")
    }
    
    override fun onRecordingStateChanged(isRecording: Boolean) {
        Log.d(TAG, "录音状态变化: $isRecording")
//        recognitionCallback?.onStateChanged(isRecording)
    }
    
    /**
     * 检查是否正在识别
     */
    fun isRecognizing(): Boolean = isRecording
    
    /**
     * 获取当前语言类型
     */
    fun getCurrentLanguageType(): Int = languageType
}