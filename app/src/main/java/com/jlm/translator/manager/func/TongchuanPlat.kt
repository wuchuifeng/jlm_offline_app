package com.jlm.translator.manager.func

import android.content.Context
import cn.chawloo.base.ext.toast
import cn.chawloo.base.utils.MKKeys
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.jlm.translator.entity.RecordModel
import com.jlm.translator.intelligent.listener.IntelliDataUpdateListener
import com.jlm.translator.intelligent.listener.SpeechSettingInterParams
import com.jlm.translator.intelligent.model.SpeechErrorEnum
import com.jlm.translator.intelligent.model.SpeechSettingModel
import com.jlm.translator.intelligent.provider.LanguageProvider
import com.jlm.translator.intelligent.provider.SpeechParam
import com.jlm.translator.intelligent.provider.SpeechProvider
import com.jlm.translator.intelligent.speech.SpeechPlatform
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.IntelligentSettingEnum
import com.jlm.translator.manager.IntelligentVersionEnum
import com.jlm.translator.manager.LanguageModeEnum
import com.jlm.translator.manager.SpeechDataManager
import com.jlm.translator.manager.SpeechPlatformEnum
import com.jlm.translator.manager.SpeechState
import com.jlm.translator.manager.setting.SpeechSettingDelegate
import com.jlm.translator.pop.DoubleLangSelectorPop
import com.jlm.translator.pop.DoubleSelectorParams
import com.jlm.translator.pop.FontSizeSettingPop
import com.jlm.translator.pop.SpeakSpeedSettingPop
import com.jlm.translator.pop.SpeechBreakThresholdSettingPop
import com.jlm.translator.pop.ThreeLangSelectorPop
import com.jlm.translator.pop.ThreeSelectorParams
import com.jlm.translator.utils.LogUtil
import com.jlm.translator.database.manager.TranslateHistoryDBManager
import com.jlm.translator.database.table.TranslateHistory
import com.safframework.log.L
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 同声传译功能平台
 * 优化版本：消除重复代码，提高可维护性
 */
class TongchuanPlat(
    val context: Context, 
    val version: IntelligentVersionEnum, 
    val updateDataListener: IntelliDataUpdateListener
): SpeechFuncPlatform() {
    
    companion object {
        private const val TAG = "TongchuanPlat"
        private const val TEXT_LIMIT_THRESHOLD = 100
    }

    // 协程相关
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null

    // 语音平台实例
    private var speechPlatforms = SpeechPlatforms()
    
    // 文字记录
    private val textBuilders = TextBuilders()

    // 语言相关
    private var languageConfig = LanguageConfig()

    // 当前状态
    private var curRecordPosition = -1

    // 设置相关
    private lateinit var speechSettingDelegate: SpeechSettingDelegate
    private lateinit var settingModel: SpeechSettingModel

    //翻译记录相关
    private var translateUUID = UUID.randomUUID()
    private lateinit var translateHistory: TranslateHistory

    init {
        initializeSettings()
        initSpeechAndLanguage()
        initTranslateHistory()
        collectData()
    }

    // 数据类定义
    private data class SpeechPlatforms(
        var recogSpeechPlatform: SpeechPlatform? = null,
        var transSpeechPlatform: SpeechPlatform? = null,
        var synthSpeechPlatform: SpeechPlatform? = null,
        var synthRightSpeechPlatform: SpeechPlatform? = null
    ) {

        fun isValidSpeech(): Boolean = recogSpeechPlatform != null && synthSpeechPlatform != null

        fun stopAll() {
            recogSpeechPlatform?.stop()
            transSpeechPlatform?.stop()
            synthSpeechPlatform?.stop()
            synthRightSpeechPlatform?.stop()
        }
        
        fun closeAll() {
            recogSpeechPlatform?.close()
            transSpeechPlatform?.close()
            synthSpeechPlatform?.close()
            synthRightSpeechPlatform?.close()
        }
        
        fun clear() {
            recogSpeechPlatform = null
            transSpeechPlatform = null
            synthSpeechPlatform = null
            synthRightSpeechPlatform = null
        }
    }

    private data class TextBuilders(
        val speechResultSb: StringBuilder = StringBuilder(),
        val translateResultSb: StringBuilder = StringBuilder(),
        val translateSecondResultSb: StringBuilder = StringBuilder()
    ) {
        fun clearAll() {
            speechResultSb.clear()
            translateResultSb.clear()
            translateSecondResultSb.clear()
        }
    }

    private data class LanguageConfig(
        var sourceLanguage: Language? = null,
        var targetLanguage: Language? = null,
        var targetRightLanguage: Language? = null,
        var sourceLangGroupList: List<LanguageGroup>? = null,
        var targetLangGroupList: List<LanguageGroup>? = null,
        var targetRightLangGroupList: List<LanguageGroup>? = null
    ) {
        fun clear() {
            sourceLanguage = null
            targetLanguage = null
            targetRightLanguage = null
            sourceLangGroupList = null
            targetLangGroupList = null
            targetRightLangGroupList = null
        }

        fun isValidLanguageGroup(): Boolean = sourceLangGroupList != null && targetLangGroupList != null

        fun isValidLanguage(): Boolean = sourceLanguage != null &&
                targetLanguage != null &&
                sourceLanguage!!.getItemKey().isNotEmpty()  &&
                targetLanguage!!.getItemKey().isNotEmpty()
        
        fun isValidForSingle(): Boolean = sourceLanguage != null && targetLanguage != null
        fun isValidForDouble(): Boolean = isValidForSingle() && targetRightLanguage != null
    }

    private data class VersionModeConfig(
        val recogPlatform: SpeechPlatformEnum,
        val transPlatform: SpeechPlatformEnum,
        val synthPlatform: SpeechPlatformEnum,
        val synth2Platform: SpeechPlatformEnum,
        val sourceKey: String,
        val targetKey: String,
        val targetRightKey: String
    )

    private fun initializeSettings() {
        speechSettingDelegate = IntelliVersionManager.getInstance(context).getSettingDelegate()
        settingModel = speechSettingDelegate.getSpeechSettingModel()
        //更新字体
        updateDataListener.updateSettingData(SpeechSettingInterParams.FontMode(settingModel.fontCode))
    }

    private fun initSpeechAndLanguage() {
        try {
            val config = getVersionModeConfig(version)
            if (config != null) {
                setupSpeechPlatforms(config)
            }
        } catch (e: Exception) {
            L.e(TAG, "Error initializing speech and language")
            toast("语音和语言初始化失败")
        }
    }

    private fun getVersionModeConfig(version: IntelligentVersionEnum): VersionModeConfig? {
        return VersionModeConfig(
            SpeechPlatformEnum.OFFLINE_NIU_recog, SpeechPlatformEnum.OFFLINE_NIU_trans,
            SpeechPlatformEnum.OFFLINE_NIU_synth, SpeechPlatformEnum.NONE,
            MKKeys.Language.key_offline_tongchuan_source,
            MKKeys.Language.key_offline_tongchuan_target, ""
        )
    }

    private fun setupSpeechPlatforms(config: VersionModeConfig) {
        //根据platform信息 从speechprovider里获取对应的speechplatform
        val speechInfo = SpeechProvider.getSpeechInfo(
            context, config.recogPlatform, config.transPlatform, 
            config.synthPlatform, config.synth2Platform
        )
        
        if (speechInfo is SpeechParam.SpeechInfo) {
            speechPlatforms.apply {
                recogSpeechPlatform = speechInfo.recogInstance
                transSpeechPlatform = speechInfo.transInstance
                synthSpeechPlatform = speechInfo.synthInstance
                synthRightSpeechPlatform = speechInfo.synth2Instance
            }
            
            setupLanguageGroups(speechInfo, config)
        }
    }

    /**
     * 设置语言信息
     * */
    private fun setupLanguageGroups(speechInfo: SpeechParam.SpeechInfo, config: VersionModeConfig) {
        languageConfig.apply {
            try {
                sourceLangGroupList = LanguageProvider.getLanguageGroupList(context, speechInfo.sourceLangJsonInfo)
                targetLangGroupList = LanguageProvider.getLanguageGroupList(context, speechInfo.targetLangJsonInfo)
                targetRightLangGroupList = targetLangGroupList

                if (sourceLangGroupList.isNullOrEmpty() || targetLangGroupList.isNullOrEmpty()) {
                    toast("语言列表异常，请退出重新进入")
                    return
                }

                sourceLanguage = LanguageProvider.getLanguage(context, config.sourceKey, "zh", sourceLangGroupList!!)
                targetLanguage = LanguageProvider.getLanguage(context, config.targetKey, "en", targetLangGroupList!!)

                if (config.targetRightKey.isNotEmpty()) {
                    targetRightLanguage = LanguageProvider.getLanguage(context, config.targetRightKey, "es", targetRightLangGroupList!!)
                }

                // 验证语言对象的有效性
                if (sourceLanguage?.getItemKey().isNullOrEmpty() || targetLanguage?.getItemKey().isNullOrEmpty()) {
                    L.w(TAG, "Language validation failed, clearing cache and retrying...")
                    // 清除缓存并重试
                    LanguageProvider.clearCacheForType(speechInfo.sourceLangJsonInfo)
                    LanguageProvider.clearCacheForType(speechInfo.targetLangJsonInfo)
                    
                    // 重新加载
                    sourceLangGroupList = LanguageProvider.getLanguageGroupList(context, speechInfo.sourceLangJsonInfo)
                    targetLangGroupList = LanguageProvider.getLanguageGroupList(context, speechInfo.targetLangJsonInfo)
                    
                    if (sourceLangGroupList.isNullOrEmpty() || targetLangGroupList.isNullOrEmpty()) {
                        toast("语言重新加载失败，请退出重新进入")
                        return
                    }
                    
                    sourceLanguage = LanguageProvider.getLanguage(context, config.sourceKey, "zh", sourceLangGroupList!!)
                    targetLanguage = LanguageProvider.getLanguage(context, config.targetKey, "en", targetLangGroupList!!)
                    
                    if (config.targetRightKey.isNotEmpty()) {
                        targetRightLanguage = LanguageProvider.getLanguage(context, config.targetRightKey, "es", targetLangGroupList!!)
                    }
                }
            } catch (e: Exception) {
                L.e(TAG, "Error in setupLanguageGroups: ${e.message}")
                toast("语言配置异常：${e.message}")
                // 尝试清除所有缓存
                LanguageProvider.clearCache()
                return
            }
        }
        
        updateLanguageUI()
    }

    private fun updateLanguageUI() {
        languageConfig.apply {
            updateDataListener.updateLanguageUI(sourceLanguage, LanguageModeEnum.LANG_SOURCE)
            updateDataListener.updateLanguageUI(targetLanguage, LanguageModeEnum.LANG_TARGET)
        }
    }

    override fun showLanguageSelector(langModeEnum: LanguageModeEnum) {
        showDoubleLangSelector(langModeEnum)
    }

    private fun showDoubleLangSelector(langModeEnum: LanguageModeEnum) {
        languageConfig.apply {
            if (!isValidForSingle() || sourceLangGroupList == null || targetLangGroupList == null) return
            
            DoubleLangSelectorPop(
                context, langModeEnum,
                DoubleSelectorParams.InputParam(sourceLanguage!!, targetLanguage!!, sourceLangGroupList!!, targetLangGroupList!!),
                { handleLanguageSelection(language, langMode) }
            ).showPopupWindow()
        }
    }

    private fun showTripleLangSelector(langModeEnum: LanguageModeEnum) {
        languageConfig.apply {
            if (!isValidForDouble() || sourceLangGroupList == null || targetLangGroupList == null || targetRightLangGroupList == null) return
            
            ThreeLangSelectorPop(
                context, langModeEnum,
                ThreeSelectorParams.InputParam(
                    sourceLanguage!!, targetLanguage!!, targetRightLanguage!!,
                    sourceLangGroupList!!, targetLangGroupList!!, targetRightLangGroupList!!
                ),
                { handleLanguageSelection(language, langMode) }
            ).showPopupWindow()
        }
    }

    private fun handleLanguageSelection(language: Language, langMode: LanguageModeEnum) {
        updateDataListener.updateLanguageUI(language, langMode)
        
        val saveKey = getSaveKey(langMode)
        if (saveKey.isNotEmpty()) {
            updateLanguageConfig(language, langMode)
            saveLanguageToLocal(saveKey, language)
        }
    }

    private fun updateLanguageConfig(language: Language, langMode: LanguageModeEnum) {
        languageConfig.apply {
            when (langMode) {
                LanguageModeEnum.LANG_SOURCE -> sourceLanguage = language
                LanguageModeEnum.LANG_TARGET -> targetLanguage = language
                LanguageModeEnum.LANG_TARGET_LEFT -> {
                    // 双语模式下，LEFT对应targetLanguage（因为config.targetKey对应LEFT的存储key）
                    targetLanguage = language  
                }
                LanguageModeEnum.LANG_TARGET_RIGHT -> {
                    // 双语模式下，RIGHT对应targetRightLanguage
                    targetRightLanguage = language
                }
                else -> {}
            }
        }
    }

    private fun getSaveKey(langMode: LanguageModeEnum): String {
        return when (version) {
            IntelligentVersionEnum.OFFLINE -> when (langMode) {
                LanguageModeEnum.LANG_SOURCE -> MKKeys.Language.key_offline_tongchuan_source
                LanguageModeEnum.LANG_TARGET -> MKKeys.Language.key_offline_tongchuan_target
                LanguageModeEnum.LANG_TARGET_LEFT -> MKKeys.Language.key_offline_tongchuan_2_targetleft
                LanguageModeEnum.LANG_TARGET_RIGHT -> MKKeys.Language.key_offline_tongchuan_2_targetright
                else -> ""
            }
            else -> ""
        }
    }

    override fun start() {}

    override fun stop() {
        speechPlatforms.stopAll()
    }

    override fun close() {
        speechPlatforms.closeAll()
        job?.cancel()
        scope.cancel()
    }

    override fun startSpeech(lastIndex: Int) {
        if (!languageConfig.isValidLanguage()) {
            toast("语言异常")
            return
        }
        if (!speechPlatforms.isValidSpeech()) {
            toast("语音初始化异常")
            return
        }
        textBuilders.clearAll()
        curRecordPosition = lastIndex
        startRecog()
        //初始化翻译，暂时通过transcontent传入语言相关
        val transParam = SpeechParam.TransContent(
            "", languageConfig.sourceLanguage!!, listOf(languageConfig.targetLanguage!!),
            curRecordPosition, LanguageModeEnum.LANG_TARGET
        )
        speechPlatforms.transSpeechPlatform?.initial(transParam)

//        speechPlatforms.transSpeechPlatform?.initial(SpeechParam.Default)
        initialSynth()
        callAddItem()
    }

    override fun showSettingPop(settingEnum: IntelligentSettingEnum) {
        when (settingEnum) {
            IntelligentSettingEnum.SETTING_SILENCE -> showSilenceSettingPop()
            IntelligentSettingEnum.SETTING_SPEED -> showSpeedSettingPop()
            IntelligentSettingEnum.SETTING_FONT -> showFontSettingPop()
            else -> {}
        }
    }

    private fun showSilenceSettingPop() {
        SpeechBreakThresholdSettingPop(context, settingModel.silence) {
            updateDataListener.updateSettingData(SpeechSettingInterParams.SilenceMode(this))
            settingModel.silence = this
            speechSettingDelegate.saveSettingModelValue(settingModel)
        }.showPopupWindow()
    }

    private fun showSpeedSettingPop() {
        SpeakSpeedSettingPop(context, settingModel.speed) {
            updateDataListener.updateSettingData(SpeechSettingInterParams.SpeedMode(this))
            settingModel.speed = this
            speechSettingDelegate.saveSettingModelValue(settingModel)
        }.showPopupWindow()
    }

    private fun showFontSettingPop() {
        FontSizeSettingPop(context, settingModel.fontCode) {
            updateDataListener.updateSettingData(SpeechSettingInterParams.FontMode(this))
            settingModel.fontCode = this
            speechSettingDelegate.saveSettingModelValue(settingModel)
        }.showPopupWindow()
    }

    private fun collectData() {
        scope.launch {
            SpeechDataManager.getInstance().speechDataFlow
                .distinctUntilChanged()
                .onStart { L.d(TAG, "Flow collection started") }
                .catch { e -> L.e(TAG, "Error in speech data flow") }
                .collect { speechState ->
                    handleSpeechState(speechState)
                }
        }
    }

    private fun handleSpeechState(speechState: SpeechState) {
        when (speechState) {
            is SpeechState.RecordStart -> {
                L.d(TAG, "RecordStart")
                updateDataListener.startAnim()
            }
            is SpeechState.RecordStop, is SpeechState.RecordRelease -> {
                updateDataListener.stopAnim()
            }
            is SpeechState.Recognizing -> handleRecognizing(speechState)
            is SpeechState.Recognized -> handleRecognized(speechState)
            is SpeechState.RecognizedAndTranslated -> handleRecognizedAndTranslated(speechState)
            is SpeechState.Translated -> handleTranslated(speechState)
            is SpeechState.TranslationCompleted -> handleTranslationCompleted(speechState)
            is SpeechState.TaskFinish -> handleTaskFinish()
            is SpeechState.TaskError -> handleTaskError(speechState)
            else -> {}
        }
    }

    private fun handleRecognizing(state: SpeechState.Recognizing) {
        val result = state.result
        if (result.isNotEmpty()) {
            updateDataListener.updateSourceTextItem("${textBuilders.speechResultSb}$result", curRecordPosition)
        }
    }

    private fun handleRecognized(state: SpeechState.Recognized) {
        val result = state.result
        if (result.isNotEmpty()) {
            startTranslate(result)
            textBuilders.speechResultSb.append(result)
            updateDataListener.updateSourceTextItem(textBuilders.speechResultSb.toString(), curRecordPosition)
            
            if (textBuilders.speechResultSb.length > TEXT_LIMIT_THRESHOLD) {
                // 1.将文字记录到历史记录中 2.清空文字记录 3. 新增一个item
                recordSourceTranslateHistory(textBuilders.speechResultSb.toString())
                callAddItem()
                textBuilders.speechResultSb.clear()
            }
        }
    }

    private fun handleRecognizedAndTranslated(state: SpeechState.RecognizedAndTranslated) {
        val result = state.result
        val transResults = state.transResults
        if (result.isEmpty()) return

        textBuilders.speechResultSb.append(result)
        updateDataListener.updateSourceTextItem(textBuilders.speechResultSb.toString(), curRecordPosition)
        
        val prePosition = curRecordPosition
        if (textBuilders.speechResultSb.length > getItemTextMaxLimit("zh")) {
            // 1.将文字记录到历史记录中 2.清空文字记录 3. 新增一个item
            recordSourceTranslateHistory(textBuilders.speechResultSb.toString())
            callAddItem()
            textBuilders.speechResultSb.clear()
        }
        
        handleRecogAndTranslate(transResults, prePosition)
    }

    private fun handleTranslated(state: SpeechState.Translated) {
        handleTranslted(state.result, state.index, state.langMode)
    }

    private fun handleTranslationCompleted(state: SpeechState.TranslationCompleted) {
        handleTranslateCompleted(state.result, state.result1, state.index)
    }

    private fun handleTaskFinish() {
        LogUtil.d(TAG, "speechFinish--$curRecordPosition")
        updateDataListener.recordFinished()
        
        // 插入数据库信息
        if (textBuilders.speechResultSb.toString().isNotEmpty()) {
            recordSourceTranslateHistory(textBuilders.speechResultSb.toString())
            insertTranslateHistory(textBuilders.translateResultSb.toString(), textBuilders.translateSecondResultSb.toString())
        }
        
        textBuilders.clearAll()
    }

    private fun handleTaskError(state: SpeechState.TaskError) {
        val error = state.error
        if (error.needStop()) {
            finishTask()
            when (error) {
                SpeechErrorEnum.recog_params_error -> {
                }
                else -> {}
            }
            stop()
        }
    }

    fun startTranslate(content: String) {
        languageConfig.apply {
            if (sourceLanguage == null || targetLanguage == null) return
            
            val targetLanguages = if (targetRightLanguage == null) {
                listOf(targetLanguage!!)
            } else {
                listOf(targetLanguage!!, targetRightLanguage!!)
            }
            val speechParam = SpeechParam.TransContent(
                content, sourceLanguage!!, targetLanguages,
                curRecordPosition, LanguageModeEnum.LANG_TARGET
            )
            speechPlatforms.transSpeechPlatform?.start(speechParam)

        }
    }

    fun startSynthesis(content: String, langMode: LanguageModeEnum) {
        val synth = SpeechParam.SynthContent(content, langMode)
        when (langMode) {
            LanguageModeEnum.LANG_TARGET -> speechPlatforms.synthSpeechPlatform?.start(synth)
            LanguageModeEnum.LANG_TARGET_LEFT -> speechPlatforms.synthSpeechPlatform?.start(synth)
            LanguageModeEnum.LANG_TARGET_RIGHT -> speechPlatforms.synthRightSpeechPlatform?.start(synth)
            else -> {}
        }
    }

    fun handleRecogAndTranslate(transResults: ArrayList<String>, prePosition: Int) {
        var text1 = ""
        var text2 = ""

        if (transResults.isNotEmpty()) {
            text1 = transResults[0]
            if (text1.isNotEmpty()) {
                handleTranslted(text1, prePosition, LanguageModeEnum.LANG_TARGET)
            }
        }
        handleTranslateCompleted(text1, text2, prePosition)
    }

    fun handleTranslted(result: String, position: Int, langMode: LanguageModeEnum) {
        startSynthesis(result, langMode)
        updateDataListener.updateTargetTextItem(result, position, langMode)
    }

    fun handleTranslateCompleted(result: String, result1: String, position: Int) {
        L.d(TAG, "handleTranslateCompleted: curRecordPosition=$curRecordPosition, position=$position, result=$result, result1=$result1")
        
        textBuilders.apply {
            translateResultSb.append(result)
            translateSecondResultSb.append(result1)
        }
        
        // 根据position判断是否要插入信息到翻译记录的数据库中
        if (curRecordPosition > position) {
            // 插入数据库信息
            L.d(TAG, "保存翻译记录: targetText=${textBuilders.translateResultSb}, targetSecondText=${textBuilders.translateSecondResultSb}")
            insertTranslateHistory(textBuilders.translateResultSb.toString(), textBuilders.translateSecondResultSb.toString())
            textBuilders.translateResultSb.clear()
            textBuilders.translateSecondResultSb.clear()
        } else {
            L.d(TAG, "翻译结果累积中: curRecordPosition=$curRecordPosition <= position=$position")
        }
    }

    private fun startRecog() {
        val targetLanguages = if (languageConfig.targetRightLanguage == null) {
            listOf(languageConfig.targetLanguage!!)
        } else {
            listOf(languageConfig.targetLanguage!!, languageConfig.targetRightLanguage!!)
        }
        L.d(TAG, "startRecog: sourceLanguage=${languageConfig.sourceLanguage}")
        val speechParam = SpeechParam.RecognitionStart(languageConfig.sourceLanguage!!, targetLanguages)
        speechPlatforms.recogSpeechPlatform!!.start(speechParam)
    }

    fun initialSynth() {
        speechPlatforms.synthSpeechPlatform?.initial(
            SpeechParam.SynthesisStart(languageConfig.targetLanguage!!, LanguageModeEnum.LANG_TARGET)
        )
    }

    private fun callAddItem() {
        languageConfig.apply {
            if (sourceLanguage == null || targetLanguage == null) return
            
            curRecordPosition++
            val recordModel = RecordModel().apply {
                key = sourceLanguage!!.getItemKey()
                rSourceLanguage = sourceLanguage
                rTargetLanguage = targetLanguage

                rType = RecordModel.TYPE_SINGLE
            }
            
            updateDataListener.addItem(recordModel, curRecordPosition)
        }
    }

    private fun clearSpeechData() {
        speechPlatforms.clear()
        languageConfig.clear()
        textBuilders.clearAll()
    }

    private fun finishTask() {
        LogUtil.d(TAG, "speechFinish--$curRecordPosition")
        updateDataListener.recordFinished()
        textBuilders.clearAll()
    }

    private fun initTranslateHistory() {
        // 🔑 每次初始化时生成新的UUID
        translateUUID = UUID.randomUUID()

        val historyType = TranslateHistory.type_listen
        
        translateHistory = TranslateHistory(
            UUID = translateUUID.toString(), 
            type = historyType,
            versionCode = version.code
        )
    }

    /**
     * 记录源文本到翻译历史对象
     */
    fun recordSourceTranslateHistory(content: String) {
        translateHistory.sourceText = content
        L.d(TAG, "记录源文本到翻译历史: UUID=${translateUUID}, content=$content")
    }

    /**
     * 插入翻译记录到数据库
     * @param targetContent 第一种翻译结果
     * @param targetSecondContent 第二种翻译结果
     */
    fun insertTranslateHistory(targetContent: String, targetSecondContent: String) {
        translateHistory.targetText = targetContent
        translateHistory.targetSecondText = targetSecondContent
        L.d(TAG, "插入翻译记录: UUID=${translateUUID}, versionCode=${version.code}, type=${translateHistory.type}")
        L.d(TAG, "源文本: ${translateHistory.sourceText}")
        L.d(TAG, "目标文本1: $targetContent")
        L.d(TAG, "目标文本2: $targetSecondContent")
        TranslateHistoryDBManager.insertTranslateHistory(translateHistory)
        L.d(TAG, "翻译记录插入完成")
    }
}