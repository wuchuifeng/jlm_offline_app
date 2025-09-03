package com.jlm.translator.intelligent.speech.offline

import android.content.Context
import android.util.Log
import com.iflytek.aikit.core.*
import com.jlm.translator.entity.Language
import com.jlm.translator.intelligent.provider.SpeechParam
import com.jlm.translator.intelligent.speech.SpeechPlatform
import com.jlm.translator.manager.LanguageModeEnum
import com.jlm.translator.manager.SpeechDataManager
import com.jlm.translator.manager.SpeechState
import com.safframework.log.L
import java.util.*

/**
 * 小牛离线翻译帮助类
 * 基于iFlytek AI SDK实现的离线文本翻译
 */
class OfflineNiuTransHelper(private val context: Context) : SpeechPlatform {
    
    companion object {
        private const val TAG = "OfflineNiuTransHelper"
        private const val ABILITY_ID = "ed4f63e83" // iFlytek翻译能力ID
        private const val LOG_LEVEL = "6"
    }
    
    // 核心组件
    private var currentAiHandle: AiHandle? = null
    private var aiListener: AiListener? = null
    
    // 状态管理
    private var isEngineInitialized = false
    private var isTranslating = false
    private var needUnInit = false
    
    // 配置参数
    private var resDir: String = "/sdcard/iflytek/itrans"
    private var currentTranslateType: String = "cnen"
    
    // 数据管理
    private val speechDataManager = SpeechDataManager.getInstance()
    private val translationResult = StringBuilder()
    
    init {
//        registerAiListener()
    }
    
    /**
     * 注册AI监听器
     */
    private fun registerAiListener() {
        aiListener = object : AiListener {
            override fun onResult(handleID: Int, outputData: List<AiResponse>?, usrContext: Any?) {
                L.i(TAG, "翻译结果回调: handleID=$handleID, dataSize=${outputData?.size}")
                outputData?.forEach { response ->
                    processTranslateResponse(response)
                }
            }
            
            override fun onEvent(handleID: Int, event: Int, eventData: List<AiResponse>?, usrContext: Any?) {
                L.i(TAG, "翻译事件回调: handleID=$handleID, event=$event")
                
                if (event == AeeEvent.AEE_EVENT_END.value) {
                    L.d(TAG, "翻译结束事件")
//                    finishTranslation()
                }
            }
            
            override fun onError(handleID: Int, err: Int, msg: String?, usrContext: Any?) {
                L.e(TAG, "翻译错误回调: handleID=$handleID, err=$err, msg=$msg")
                val errorMsg = msg ?: "翻译未知错误"
//                translateCallback?.onTranslateError(err, errorMsg)
                speechDataManager.tryEmit(SpeechState.Error(errorMsg))
                isTranslating = false
            }
        }
        
        AiHelper.getInst().registerListener(ABILITY_ID, aiListener)
    }
    
    /**
     * 处理翻译响应数据
     */
    private fun processTranslateResponse(response: AiResponse) {
        val key = response.key
        val valueBytes = response.value
        
        if (key == null || valueBytes == null) {
            return
        }
        
        L.d(TAG, "处理翻译响应: key=$key, dataSize=${valueBytes.size}")
        
        try {
            val resultText = String(valueBytes)
            L.d(TAG, "翻译结果片段: $resultText")
            
            if (resultText.isNotEmpty()) {
                translationResult.append(resultText)

                // 发送翻译状态到数据管理器
                speechDataManager.tryEmit(SpeechState.Translated(resultText, 0, LanguageModeEnum.LANG_TARGET))
            }
        } catch (e: Exception) {
            L.e(TAG, "处理翻译响应异常")
        }
    }
    
    /**
     * 完成翻译
     */
    private fun finishTranslation() {
        if (isTranslating) {
            val fullResult = translationResult.toString()
            L.i(TAG, "翻译完成，完整结果: $fullResult")
            // 使用默认参数发送翻译完成状态
            speechDataManager.tryEmit(SpeechState.Translated(fullResult, 0, LanguageModeEnum.LANG_TARGET))
            
            isTranslating = false
            
            // 清理资源
            currentAiHandle?.let { handle ->
                AiHelper.getInst().end(handle)
                currentAiHandle = null
            }
        }
    }

    /**
     * 设置资源目录
     */
    fun setResourceDir(resDir: String) {
        this.resDir = resDir
    }
    
    /**
     * 设置翻译类型
     * @param translateType 翻译类型，如 "cn2en", "en2cn" 等
     */
    fun setTranslateType(sourceLang: Language, targetLang: Language) {
        this.currentTranslateType = "${sourceLang.translate_key}${targetLang.translate_key}"
    }
    
    /**
     * 翻译文本
     * @param text 要翻译的文本
     * @param translateType 翻译类型（可选，不传使用当前设置）
     */
    fun translateText(text: String, translateType: String? = null) {
        if (text.trim().isEmpty()) {
            L.w(TAG, "翻译文本为空")
            return
        }
        
        val type = translateType ?: currentTranslateType
        L.i(TAG, "开始翻译: text=$text, type=$type")
        
        // 重置结果
        translationResult.clear()

        // 初始化翻译引擎
//        if (isTranslating) {
            // 开始翻译
            startTranslation(text, type)
//        }
    }
    
    /**
     * 初始化翻译引擎
     */
    private fun initTranslateEngine() {
        try {
            L.d(TAG, "初始化翻译引擎")
            
            val engineBuilder = AiRequest.builder()
            engineBuilder.param("res_dir", resDir)
            engineBuilder.param("log_level", LOG_LEVEL)
            
            val ret = AiHelper.getInst().engineInit(ABILITY_ID, engineBuilder.build())
            needUnInit = true
            
            if (ret != 0) {
                L.e(TAG, "翻译引擎初始化失败: $ret")
                isTranslating = false
                return
            }
            
            L.i(TAG, "翻译引擎初始化成功")
            // 构建翻译参数
            val paramBuilder = AiRequest.builder()
            val params = "type=$currentTranslateType"
            paramBuilder.param("params", params)
            paramBuilder.param("translateType", currentTranslateType)
//            paramBuilder.text("txt", text)

            // 启动AI Handle
            currentAiHandle = AiHelper.getInst().start(ABILITY_ID, paramBuilder.build(), null)

            if (currentAiHandle == null) {
                L.e(TAG, "翻译任务启动失败，handle为空")
                isTranslating = false
                return
            }

            if (currentAiHandle!!.code != 0) {
                L.e(TAG, "翻译任务启动失败，错误码: ${currentAiHandle!!.code}")
                isTranslating = false
                currentAiHandle = null
                return
            }

        } catch (e: Exception) {
            L.e(TAG, "翻译引擎初始化异常")
//            translateCallback?.onTranslateError(-1, "翻译引擎初始化异常: ${e.message}")
            isTranslating = false
        }
    }
    
    /**
     * 开始翻译
     */
    private fun startTranslation(text: String, translateType: String) {
        try {
            L.d(TAG, "启动翻译任务: type=$translateType")
            
            L.d(TAG, "翻译任务启动成功，handleId: ${currentAiHandle!!.id}")
            
            // 发送文本数据
            sendTextData(text)
            
        } catch (e: Exception) {
            L.e(TAG, "启动翻译任务异常")
            isTranslating = false
        }
    }
    
    /**
     * 发送文本数据
     */
    private fun sendTextData(text: String) {
        currentAiHandle?.let { handle ->
            try {
                L.d(TAG, "发送翻译文本数据")
                
                val dataBuilder = AiRequest.builder()
                val input = AiText.get("txt")
                    .data(text)
                    .valid()
                dataBuilder.payload(input)
                
                val ret = AiHelper.getInst().write(dataBuilder.build(), handle)
                
                if (ret != 0) {
                    L.e(TAG, "发送翻译数据失败: $ret")
                    isTranslating = false
                    return
                }
                
                L.d(TAG, "翻译数据发送成功")
                
            } catch (e: Exception) {
                L.e(TAG, "发送翻译数据异常")
                isTranslating = false
            }
        }
    }
    
    /**
     * 停止翻译
     */
    fun stopTranslation() {
        L.i(TAG, "停止翻译")
        
        if (!isTranslating) {
            L.w(TAG, "翻译未在进行中")
            return
        }
        
        finishTranslation()
    }
    
    // 实现SpeechPlatform接口
    override fun initial(speechParam: SpeechParam) {
        L.i(TAG, "初始化翻译引擎")
        
        // 根据参数设置翻译类型
        when (speechParam) {
            is SpeechParam.TransContent -> {
                // 处理翻译内容参数
                val sourceLang = speechParam.sourceLanguage
                val targetLangs = speechParam.targetLangList
                if (targetLangs.isNotEmpty()) {
                    val targetLang = targetLangs[0]
                    //设置翻译key
                    setTranslateType(sourceLang, targetLang)
                }
            }
            else -> {
                L.w(TAG, "不支持的参数类型: ${speechParam::class.simpleName}")
            }
        }
        //初始化注册x
        registerAiListener()

        initTranslateEngine()
        isEngineInitialized = true
        L.i(TAG, "翻译引擎初始化完成，翻译类型: $currentTranslateType")
    }
    
    override fun start(speechParam: SpeechParam) {
        L.i(TAG, "开始翻译")
        
//        if (!isEngineInitialized) {
//            initial(speechParam)
//        }
        
        // 根据参数执行翻译
        when (speechParam) {
            is SpeechParam.TransContent -> {
                translateText(speechParam.content)
            }
            is SpeechParam.NiuTransContent -> {
                translateText(speechParam.content)
            }
            else -> {
                L.e(TAG, "不支持的翻译参数类型: ${speechParam::class.simpleName}")
            }
        }
    }
    
    override fun stop() {
        L.i(TAG, "停止翻译服务")
        stopTranslation()
    }
    
    override fun close() {
        L.i(TAG, "关闭翻译引擎")
        
        stop()
        
        // 清理AI引擎
        if (needUnInit) {
            try {
                val ret = AiHelper.getInst().engineUnInit(ABILITY_ID)
                if (ret != 0) {
                    L.e(TAG, "翻译引擎反初始化失败: $ret")
                } else {
                    L.i(TAG, "翻译引擎反初始化成功")
                }
                needUnInit = false
            } catch (e: Exception) {
                L.e(TAG, "翻译引擎反初始化异常")
            }
        }
        
        isEngineInitialized = false

        L.i(TAG, "翻译引擎已关闭")
    }
    
    /**
     * 检查是否正在翻译
     */
    fun isTranslating(): Boolean = isTranslating
    
    /**
     * 获取当前翻译类型
     */
    fun getCurrentTranslateType(): String = currentTranslateType
    
    /**
     * 获取支持的翻译类型列表
     */
    fun getSupportedTranslateTypes(): List<String> {
        return listOf(
            "cnen", "encn",
            "cnja", "jacn",
            "cnko", "kocn",
            "cnru", "rucn",
            "cnfr", "frcn",
            "cnes", "escn",
            "cnde", "decn",
            "cnth", "thcn",
            "cnar", "arcn"
        )
    }
}