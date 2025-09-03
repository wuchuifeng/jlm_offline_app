package com.jlm.translator.manager.func

import android.app.AlertDialog
import android.content.Context
import cn.chawloo.base.ext.toast
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.jlm.translator.intelligent.listener.IntelliDataUpdateListener
import com.jlm.translator.intelligent.model.SpeechSettingModel
import com.jlm.translator.manager.TransDeviceManager
import com.jlm.translator.manager.IntelliFuncInfoEnum
import com.jlm.translator.manager.IntelligentFuncModeEnum
import com.jlm.translator.manager.IntelligentSettingEnum
import com.jlm.translator.manager.IntelligentVersionEnum
import com.jlm.translator.manager.LanguageModeEnum
import com.jlm.translator.manager.SpeechDataManager
import com.jlm.translator.manager.SpeechState
import com.jlm.translator.utils.LoadingHelper
import com.safframework.log.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.xfans.lib.voicewaveview.VoiceWaveView

/**
 * 语音功能代理类, 同传、自由对话、原声输出等
 * 统一管理语音功能的生命周期和设备使用时长
 * */
class SpeechFuncDelegate(val versionEnum: IntelligentVersionEnum) {
    
    companion object {
        private const val TAG = "SpeechFuncDelegate"
    }

    lateinit var funcPlatform: SpeechFuncPlatform
    private var transDeviceManager: TransDeviceManager? = null
    private var isTimerRunning = false // 标记计时器是否正在运行
    private lateinit var currentContext: Context // 保存当前上下文

    // Loading工具类
    private var loadingHelper: LoadingHelper? = null

    private lateinit var scope: CoroutineScope
    private var job: Job? = null

    init {
        // 初始化设备管理器
        initDeviceManager()
    }

    /**
     * 初始化设备管理器
     */
    private fun initDeviceManager() {
        try {
            transDeviceManager = TransDeviceManager.getInstance()
            L.d(TAG, "TransDeviceManager initialized")
        } catch (e: Exception) {
            L.e(TAG, "Failed to initialize TransDeviceManager: ${e.message}")
        }
    }

    //TODO: flow流监听移到delegate上, 使用funcPlatform.Recognized....这样的来传递数据
    private fun startFlow() {
        job = scope.launch {
            SpeechDataManager.getInstance().speechDataFlow
                .distinctUntilChanged()
                .onStart {
                    L.d("YourDelegate", "Flow collection started")
                }
                .onEach { value ->
                }
                .collect() {
                    when (it) {
                        is SpeechState.RecordStart -> { //开始录制
                        }
                        is SpeechState.RecordStop -> { //停止录制
                        }
                        is SpeechState.RecordRelease -> { //录音释放
                        }
                        is SpeechState.Recognizing -> { //识别中，实时文字返回
                        }
                        is SpeechState.Recognized -> { //一句话识别结束
                        }
                        is SpeechState.RecognizedAndTranslated -> { //一句话识别并翻译后的结果,主要用在微软的语音翻译上
                        }
                        is SpeechState.Translated -> { //翻译后的结果
                        }
                        is SpeechState.TranslationCompleted -> { //翻译完成
                        }
                        is SpeechState.TaskFinish -> { //任务完成
                        }
                        is SpeechState.TaskError -> { //任务出错
                        }
                        else -> {
                        }
                    }
                }
        }
    }

    fun initFuncPlatform(context: Context, funcInfoEnum: IntelliFuncInfoEnum, updateDataListener: IntelliDataUpdateListener) {
        // 保存上下文
        currentContext = context
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob()) //初始化scope
        L.d(TAG, "initFuncPlatform: $currentContext")
        
        // 确保设备管理器已初始化
//        if (transDeviceManager == null) {
//            initDeviceManager()
//        }
        
        // 初始化设备管理器的上下文和版本
//        transDeviceManager?.initManager(context, versionEnum)
        
        //根据功能枚举选择对应的平台
        when (funcInfoEnum) {
            IntelliFuncInfoEnum.FUNC_TONGCHUAN -> { //同传
                funcPlatform = TongchuanPlat(context, versionEnum, updateDataListener)
            }

            else -> {
                funcPlatform = TongchuanPlat(context, versionEnum, updateDataListener)
            }
        }

        L.d(TAG, "FuncPlatform initialized: $funcInfoEnum")
    }

    /**
     * 功能列表
     * */
    fun getFuncInfoList(): List<IntelliFuncInfoEnum> {
        return IntelliFuncInfoEnum.getOfflineFuncInfoList()
    }

    fun showLoading() {
        if (loadingHelper == null) {
            loadingHelper = LoadingHelper(currentContext)
        }
        loadingHelper?.showLoading()
    }

    fun hideLoading() {
        if (loadingHelper != null) {
            loadingHelper?.hideLoading()
        }
    }

    fun releaseLoading() {
        if (loadingHelper != null) {
            loadingHelper?.release()
        }
    }

    /**
     * 开始语音识别和计时
     * @param lastIndex 最后的索引
     * @param context 当前活跃的context，用于显示对话框
     */
    fun startSpeech(lastIndex: Int, funcModeEnum: IntelligentFuncModeEnum) {

        // 先检查是否有免费时长
        if (transDeviceManager?.hasFreeTime() == false) {
//            toast("VIP时长已用完，请联系服务商。15813703532")
            showNoTimeDialog(currentContext)
            return
        }
        //当剩余时长不足5分钟时，弹出提示框
        transDeviceManager?.getRemainingFreeTime().let {
            if (it != null && it < 300) {
                toast("剩余时长不足5分钟，请及时服务商。15813703532")
            }
        }

        //先开启loading
//        CoroutineScope(Dispatchers.Main).launch {
//            showLoading()
//        }
        // 启动语音功能
        funcPlatform.startSpeech(lastIndex)
        
        // 开始计时
        if (!isTimerRunning) {
            transDeviceManager?.startFreeTimer(funcModeEnum)
            isTimerRunning = true
            L.d(TAG, "Timer started")
        }
    }
    
    /**
     * 显示没有剩余时间的对话框
     * @param context 用于显示对话框的context
     */
    private fun showNoTimeDialog(context: Context?) {
        context?.let { ctx ->
            // 确保在主线程中显示Dialog
            scope.launch(Dispatchers.Main) {
                try {
                    // 检查context是否仍然有效
                    AlertDialog.Builder(ctx)
                        .setTitle("提示")
                        .setMessage("VIP时长已用完，请联系服务商。15813703532")
                        .setPositiveButton("确定") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                } catch (e: Exception) {
                    toast("VIP时长已用完，请联系服务商。15813703532")
                }
            }
        } ?: run {
            L.e(TAG, "Context is null, showing toast instead")
            // 如果context为空，使用toast作为备选方案
            toast("已没有剩余时间，请联系13673668675")
        }
    }

    fun getFunPlatform(): SpeechFuncPlatform {
        return funcPlatform
    }

    fun showLanguageSelector(langModeEnum: LanguageModeEnum) {
        funcPlatform.showLanguageSelector(langModeEnum)
    }

    /**
     * 根据类型来弹出相应的设置弹窗
     * */
    fun showSettingPop(settingEnum: IntelligentSettingEnum) {
        funcPlatform.showSettingPop(settingEnum)
    }

    fun start() {
        funcPlatform.start()
    }

    /**
     * 停止语音识别和计时
     */
    fun stop() {
        // 停止语音功能
        funcPlatform.stop()
        
        // 停止计时
        if (isTimerRunning) {
            transDeviceManager?.stopFreeTimer()
            isTimerRunning = false
            L.d(TAG, "Timer stopped")
        }
    }

    /**
     * 关闭并释放资源
     */
    fun close() {
        // 确保计时器已停止
        if (isTimerRunning) {
            transDeviceManager?.stopFreeTimer()
            isTimerRunning = false
        }
        
        // 关闭语音功能
        funcPlatform.close()
        
        // 释放协程
        job?.cancel()
        scope.cancel()
        
        // 清空上下文引用
//        currentContext = null
        
        L.d(TAG, "SpeechFuncDelegate closed")
    }

    fun configWaveView(waveView: VoiceWaveView) {
        funcPlatform.voiceWaveAnimConfig(waveView)
    }
    
    /**
     * 获取剩余免费时长
     */
    fun getRemainingFreeTime(): Int {
        return transDeviceManager?.getRemainingFreeTime() ?: 0
    }
    
    /**
     * 是否还有免费时长
     */
    fun hasFreeTime(): Boolean {
        return transDeviceManager?.hasFreeTime() ?: false
    }
}