package com.jlm.translator.act.speech

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.toast
import cn.chawloo.base.pop.showSingleWheelViewPopupWindow
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.hjq.shape.view.ShapeTextView
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.common.util.PermissionUtils.requestRecordAudioPermission
import com.jlm.translator.R
import com.jlm.translator.databinding.ActListenerModeBinding
import com.jlm.translator.databinding.IncludeLangheaderDoubleBinding
import com.jlm.translator.databinding.IncludeLangheaderFreeBinding
import com.jlm.translator.databinding.IncludeLangheaderSingleBinding
import com.jlm.translator.databinding.ItemListenerModeBinding
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.RecordModel
import com.jlm.translator.entity.SceneModel
import com.jlm.translator.intelligent.listener.IntelliDataUpdateListener
import com.jlm.translator.intelligent.listener.SpeechSettingInterParams
import com.jlm.translator.listener.SpeechUpdateDataListener
import com.jlm.translator.manager.IntelliFuncInfoEnum
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.IntelligentFuncModeEnum
import com.jlm.translator.manager.IntelligentSettingEnum
import com.jlm.translator.manager.IntelligentVersionEnum
import com.jlm.translator.manager.LanguageModeEnum
import com.jlm.translator.manager.func.SpeechFuncDelegate
import com.jlm.translator.pop.FontSizeSettingPop
import com.jlm.translator.pop.SpeakSpeedSettingPop
import com.jlm.translator.utils.ListenSettingUtil
import com.jlm.translator.widget.DraggableFloatingButton
import com.therouter.router.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.jlm.translator.utils.LoadingHelper
import com.safframework.log.L

@Route(path = Rt.ListenModeAct)
class ListenModeAct : BaseActCompat() {
    private val vb by binding<ActListenerModeBinding>()
    private var curFontSize: Float = 14F //当前显示的字体大小

    private var recordList = mutableListOf<RecordModel>()

    //智能语音通用管理类
    private lateinit var funcDelegate: SpeechFuncDelegate //智能语音通用管理类

    private var tv_sourceLanguage: ShapeTextView ? = null
    private var tv_targetLanguage: ShapeTextView ? = null
    private var tv_targetLeftLanguage: ShapeTextView ? = null
    private var tv_targetRightLanguage: ShapeTextView ? = null

    // EasyFloat标签
    private val FLOAT_TAG = "mode_float"
    
    // 防重复点击时间间隔（毫秒）
    private val CLICK_INTERVAL = 3000L
    private var lastClickTime = 0L

    // Loading工具类
//    private lateinit var loadingHelper: LoadingHelper

    private val intelliDataListener = object : IntelliDataUpdateListener() {
        override fun startAnim() {
            super.startAnim()
            lifecycleScope.launch(Dispatchers.Main) {
                // 语音识别已连接，隐藏loading
//                loadingHelper.hideLoading()
                funcDelegate.hideLoading()
                vb.motionLayout.transitionToState(R.id.recording)
                vb.voiceWave.start()
            }
        }

        override fun stopAnim() {
            super.stopAnim()
            lifecycleScope.launch(Dispatchers.Main) {
                // 停止录音，隐藏loading
//                loadingHelper.hideLoading()
                funcDelegate.hideLoading()
                vb.voiceWave.stop()
                if (recordList.isNotEmpty()) {
                    vb.motionLayout.transitionToState(R.id.has_result)
                } else {
                    vb.motionLayout.transitionToStart()
                }
            }
        }

        override fun addItem(recordModel: RecordModel, position: Int) {
            super.addItem(recordModel, position)
            lifecycleScope.launch(Dispatchers.Main) {
                recordList.add(recordModel)
                vb.srecycler.bindingAdapter.notifyItemInserted(recordList.lastIndex)
                vb.srecycler.scrollToPosition(recordList.lastIndex)
            }
        }

        override fun updateSourceTextItem(content: String, position: Int) {
            super.updateSourceTextItem(content, position)
            lifecycleScope.launch(Dispatchers.Main) {
                if (recordList.isNotEmpty()) {
                    recordList.lastOrNull()?.sourceText = content
                    vb.srecycler.bindingAdapter.notifyItemChanged(position)
                    vb.srecycler.scrollToPosition(recordList.lastIndex)
                }
            }
        }

        override fun updateTargetTextItem(content: String, position: Int, langMode: LanguageModeEnum) {
            super.updateTargetTextItem(content, position, langMode)
            lifecycleScope.launch(Dispatchers.Main) {
                //判断recordList不为空，且position在recordList的索引范围内
                if (recordList.isNotEmpty() && position in recordList.indices) {
                    recordList[position].apply {
                        when (langMode) {
                            LanguageModeEnum.LANG_TARGET, LanguageModeEnum.LANG_TARGET_LEFT -> targetText = "${targetText}${content}"
                            LanguageModeEnum.LANG_TARGET_RIGHT -> targetRightText = "${targetRightText}${content}"
                            else -> {}
                        }
                    }
                    vb.srecycler.bindingAdapter.notifyItemChanged(position)
                    //向底部滚动
                    vb.srecycler.scrollToPosition(position)
                }
            }
        }

        override fun updateLanguageUI(language: Language?, langMode: LanguageModeEnum) {
            super.updateLanguageUI(language, langMode)
            lifecycleScope.launch(Dispatchers.Main) {
                when (langMode) {
                    LanguageModeEnum.LANG_SOURCE -> tv_sourceLanguage?.text  = language?.getLangName() //vb.tvFromLanguage.text = language?.getLangName()
                    LanguageModeEnum.LANG_TARGET -> tv_targetLanguage?.text  = language?.getLangName() //vb.tvToLanguage.text = language?.getLangName()
                    LanguageModeEnum.LANG_TARGET_LEFT -> tv_targetLeftLanguage?.text  = language?.getLangName()
                    LanguageModeEnum.LANG_TARGET_RIGHT -> tv_targetRightLanguage?.text  = language?.getLangName()
                }
            }
        }

        override fun updateSettingData(param: SpeechSettingInterParams) {
            super.updateSettingData(param)
            lifecycleScope.launch(Dispatchers.Main) {
               if(param is SpeechSettingInterParams.FontMode) { //字体
                    param.value.run {
                        curFontSize = ListenSettingUtil.getFontSize(this)
                        updateFontView(curFontSize)
                    }
                }
            }
        }

        override fun recordFinished() {
            super.recordFinished()
            lifecycleScope.launch(Dispatchers.Main) {
                // 录音完成，隐藏loading
//                loadingHelper.hideLoading()
                funcDelegate.hideLoading()
                recordList.lastOrNull()?.run {
                    if (sourceText.isBlank()) {
                        vb.srecycler.bindingAdapter.notifyItemRemoved(recordList.lastIndex)
                        vb.srecycler.mutable.removeLastOrNull()
                    }
                    if (recordList.isNotEmpty()) {
                        vb.motionLayout.transitionToState(R.id.has_result)
                    } else {
                        vb.motionLayout.transitionToState(R.id.start)
                    }
                }
                // 在识别结束后显示悬浮按钮
//                showFloatingButton()
            }
        }
    }

    override fun initialize() {
        super.initialize()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        updateLangSingleUI()
//        loadingHelper = LoadingHelper(this)
        funcDelegate = IntelliVersionManager.getInstance(this).getFuncDelegate()
        funcDelegate.initFuncPlatform(this, IntelliFuncInfoEnum.FUNC_TONGCHUAN ,intelliDataListener)

        //配置语音波纹动画
        funcDelegate.configWaveView(vb.voiceWave)
        
        // 设置MotionLayout状态变化监听器
        setupMotionLayoutListener()
        
//        curFontSize = getFont()
        vb.srecycler.linear()
            .setup {
                itemDifferCallback = object : ItemDifferCallback {
                    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is RecordModel && newItem is RecordModel) {
                            oldItem.key == newItem.key
                        } else {
                            super.areItemsTheSame(oldItem, newItem)
                        }
                    }

                    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is RecordModel && newItem is RecordModel) {
                            oldItem.sourceText == newItem.sourceText
                        } else {
                            super.areContentsTheSame(oldItem, newItem)
                        }
                    }

                    override fun getChangePayload(oldItem: Any, newItem: Any) = true
                }
                addType<RecordModel>(R.layout.item_listener_mode)
                onBind {
                    with(getBinding<ItemListenerModeBinding>()) {
                        with(getModel<RecordModel>()) {
                            //根据type判断UI类型， 单语输出， 双语输出， 自由对话
                            when(rType) {
                                RecordModel.TYPE_SINGLE, RecordModel.TYPE_FREE -> {
                                    llTargetright.visibility = View.GONE
                                    tvSource.apply {
                                        text = sourceText
                                        textSize = curFontSize
                                    }
                                    tvTarget.apply {
                                        text = targetText
                                        textSize = curFontSize
                                    }
                                }

                                RecordModel.TYPE_DOUBLE -> {
                                    llTargetright.visibility = View.VISIBLE
                                    tvSource.apply {
                                        text = sourceText
                                        textSize = curFontSize
                                    }
                                    tvTarget.apply {
                                        text = targetText
                                        textSize = curFontSize
                                    }
                                    tvTargetright.apply {
                                        text = targetRightText
                                        textSize = curFontSize
                                    }
                                }
                            }
                        }
                    }
                }
                onClick(R.id.iv_play) {
                    with(getModel<RecordModel>()) {
//                        speechDelegate.playSpeechSynthesis(this, IntelligentSpeechDelegate.type_all)
                    }
                }
            }
            .setDifferModels(recordList)
        // 初始化字体大小
        vb.tvClickToListener.textSize = curFontSize
    }

    /**
     * 设置MotionLayout状态变化监听器
     */
    private fun setupMotionLayoutListener() {
        vb.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
                // 不需要处理
            }

            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
                // 不需要处理
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                when (currentId) {
                    R.id.start, R.id.has_result -> {
                        // 非录制状态，显示悬浮按钮
//                        showFloatingButton()
                    }
                    R.id.recording -> {
                        // 录制状态，隐藏悬浮按钮
                        hideFloatingButton()
                    }
                }
            }

            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {
                // 不需要处理
            }
        })
    }

    /**
     * 显示悬浮按钮
     */
//    private fun showFloatingButton() {
//        // 离线模式下不显示悬浮按钮
//        val currentVersion = IntelliVersionManager.getInstance(this).getCurVersion()
//        if (currentVersion == IntelligentVersionEnum.OFFLINE) {
//            return
//        }
//        EasyFloat.show(FLOAT_TAG)
//    }

    /**
     * 隐藏悬浮按钮
     */
    private fun hideFloatingButton() {
//        EasyFloat.hide(FLOAT_TAG)
    }

    /**
     * 更新悬浮按钮文字
     */
    private fun updateFloatingButtonText(text: String) {
//        EasyFloat.getFloatView(FLOAT_TAG)?.findViewById<android.widget.TextView>(R.id.tv_floating_mode)?.text = text
    }

    private fun updateLangSingleUI() {
        vb.flHeadercontainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.include_langheader_single, vb.flHeadercontainer, false)
        vb.flHeadercontainer.addView(view)
        //添加点击事件
        IncludeLangheaderSingleBinding.bind(view).apply {
            llFromLanguage.setOnClickListener {
                funcDelegate.showLanguageSelector(LanguageModeEnum.LANG_SOURCE)
            }
            llToLanguage.setOnClickListener {
                funcDelegate.showLanguageSelector(LanguageModeEnum.LANG_TARGET)
            }
            includeBack.flBack.setOnClickListener {
                backPressed()
            }
            //赋值
            tv_sourceLanguage = this.tvFromLanguage
            tv_targetLanguage = this.tvToLanguage
        }
    }

    private fun updateLangDoubleUI() {
        vb.flHeadercontainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.include_langheader_double, vb.flHeadercontainer, false)
        vb.flHeadercontainer.addView(view)
        //添加点击事件
        IncludeLangheaderDoubleBinding.bind(view).apply {
            llFromLanguage.setOnClickListener {
                funcDelegate.showLanguageSelector(LanguageModeEnum.LANG_SOURCE)
            }
            llToLanguage.setOnClickListener {
                funcDelegate.showLanguageSelector(LanguageModeEnum.LANG_TARGET_LEFT)
            }
            llToRightLanguage.setOnClickListener {
                funcDelegate.showLanguageSelector(LanguageModeEnum.LANG_TARGET_RIGHT)
            }
            includeBack.flBack.setOnClickListener {
                backPressed()
            }
            //赋值
            tv_sourceLanguage = this.tvFromLanguage
            tv_targetLeftLanguage = this.tvToLanguage
            tv_targetRightLanguage = this.tvToRightLanguage
        }
    }

    private fun updateLangFreeUI() {
        vb.flHeadercontainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.include_langheader_free, vb.flHeadercontainer, false)
        vb.flHeadercontainer.addView(view)
        //添加点击事件
        IncludeLangheaderFreeBinding.bind(view).apply {
            llFromLanguage.setOnClickListener {
                funcDelegate.showLanguageSelector(LanguageModeEnum.LANG_SOURCE)
            }
            llToLanguage.setOnClickListener {
                funcDelegate.showLanguageSelector(LanguageModeEnum.LANG_TARGET)
            }
            includeBack.flBack.setOnClickListener {
                backPressed()
            }
            //赋值
            tv_sourceLanguage = this.tvFromLanguage
            tv_targetLanguage = this.tvToLanguage
        }
    }

    override fun backPressed() {
        if (vb.motionLayout.currentState == R.id.recording) {
            vb.motionLayout.transitionToState(R.id.has_result)
            funcDelegate.stop()
        } else {
            super.backPressed()
        }
    }

    override fun onClick() {
        super.onClick()
        //点击暂停录制
        vb.slRecording.doClick {
            funcDelegate.stop()
//            if (recordList.isNotEmpty()) {
//                vb.motionLayout.transitionToState(R.id.has_result)
//            } else {
//                vb.motionLayout.transitionToStart()
//            }
        }
        vb.ivRecord.doClick {
            // 检查点击间隔，防止重复点击
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < CLICK_INTERVAL) {
                toast("请勿点击过于频繁")
                return@doClick
            }
            lastClickTime = currentTime
//            vb.motionLayout.transitionToState(R.id.recording)
            requestRecordAudioPermission {
                // 显示loading
//                loadingHelper.showLoading()
                //线程开启识别，先把合成和翻译关掉
                lifecycleScope.launch(Dispatchers.IO) {
                    //开启智能语音
                    funcDelegate.startSpeech(recordList.lastIndex, IntelligentFuncModeEnum.MODE_TONGCHUAN)
                }
            }
        }
        // 语速按钮（原来的模式按钮现在是语速）
        vb.includeBottom.tvMode.doClick {
            funcDelegate.showSettingPop(IntelligentSettingEnum.SETTING_SPEED)
        }
        // 断句按钮（原来的语速按钮现在是断句）
        vb.includeBottom.tvSpeakSpeed.doClick {
            funcDelegate.showSettingPop(IntelligentSettingEnum.SETTING_SILENCE)
        }
        // 字体
        vb.includeBottom.tvFont.doClick {
            funcDelegate.showSettingPop(IntelligentSettingEnum.SETTING_FONT)
        }
        vb.includeBottom.tvSetting.doClick {
            goto(Rt.ListenerModeSettingAct)
        }
    }

    fun updateFontView(fontSize: Float) {
        curFontSize = fontSize
        vb.srecycler.adapter?.notifyDataSetChanged()
        vb.tvClickToListener.textSize = fontSize
    }

    override fun onResume() {
        super.onResume()
//        updateFontView(getFont())
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭loading
//        loadingHelper.release()
        funcDelegate.releaseLoading()
        // 关闭悬浮窗
//        EasyFloat.dismiss(FLOAT_TAG)
        funcDelegate.close()
    }

    private fun getFont(): Float {
        val synthesisFontValue = MK.decodeFloat(MKKeys.KEY_SYNTHESIS_FONT, 14F)
        return synthesisFontValue
    }


}