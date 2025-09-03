package com.jlm.translator.act.speech

import androidx.core.content.ContextCompat
import cn.chawloo.base.ext.context
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.visible
import cn.chawloo.base.pop.showSingleWheelViewPopupWindow
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.databinding.ActListenerModeSettingBinding
import com.jlm.translator.entity.SceneModel
import com.jlm.translator.entity.VoicerModel
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.IntelligentSettingEnum
import com.jlm.translator.pop.EnvironmentalNoiseSettingPop
import com.jlm.translator.pop.FontSizeSettingPop
import com.jlm.translator.pop.FontSizeSettingPop.Companion.defaultFontSize
import com.jlm.translator.pop.SpeakPitchSettingPop
import com.jlm.translator.pop.SpeakSpeedSettingPop
import com.jlm.translator.pop.SpeechBreakThresholdSettingPop
import com.jlm.translator.utils.ListenSettingUtil
import com.therouter.router.Route
import com.jlm.translator.R
import com.jlm.translator.databinding.ItemTransSetBinding
import com.jlm.translator.intelligent.model.RegionModel
import com.jlm.translator.intelligent.model.RegionPointModel
import com.jlm.translator.intelligent.model.SpeechSettingModel
import com.jlm.translator.manager.setting.SpeechSettingDelegate
import com.safframework.log.L

@Route(path = Rt.ListenerModeSettingAct)
class ListenerModeSettingAct : BaseActCompat() {
    private val vb by binding<ActListenerModeSettingBinding>()
    private lateinit var speechSettingDelegate: SpeechSettingDelegate
    private lateinit var settingModel: SpeechSettingModel

    private val sceneList = listOf(
        SceneModel(showTxt = "通用", scene = "general", code = 1),
        SceneModel(showTxt = "医疗", scene = "medical", code = 2),
        SceneModel(showTxt = "社交", scene = "social", code = 3),
        SceneModel(showTxt = "金融", scene = "finance", code = 4),
        SceneModel(showTxt = "商品标题", scene = "title", code = 5),
        SceneModel(showTxt = "商品描述", scene = "description", code = 6),
        SceneModel(showTxt = "商品沟通", scene = "communication", code = 7),
    )
    private var selectedScene: SceneModel = sceneList.first()
        set(value) {
//            vb.tvScene.text = value.showTxt
            field = value
        }

    private val voicerList = listOf(
        VoicerModel(voicerName = "男声", type = VoicerModel.TYPE_MALE),
        VoicerModel(voicerName = "女声", type = VoicerModel.TYPE_FEMALE),
    )
//    val voicerType = MK.decodeInt(MKKeys.KEY_VOICER_TYPE, VoicerModel.TYPE_FEMALE)

    private var selectedVoicer: VoicerModel = voicerList[0]
        set(value) {
//            vb.tvVoicer.text = value.voicerName
            field = value
        }

    //区域列表
    private var regionList = emptyList<RegionPointModel>()
    private lateinit var selectedRegion: RegionPointModel //IntelliVersionManager.getInstance(this).getSelectedRegion()

    private var settingList = emptyList<IntelligentSettingEnum>()

    override fun initialize() {
        super.initialize()
        initSettingData()

//        initValueUi()

        vb.recycler.linear()
            .setup {
                addType<IntelligentSettingEnum>(R.layout.item_trans_set)
                onBind {
                    val binding = getBinding<ItemTransSetBinding>()
                    with(getModel<IntelligentSettingEnum>()){
                        if (this == IntelligentSettingEnum.SETTING_VOLUMN) {
                            binding.slRoot.visible()
                            binding.slRoot1.gone()

                            binding.tvTitle.text = title

                            binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, icon),
                                null,  // top
                                null,  // right
                                null   // bottom
                            )
                        } else {
                            binding.slRoot1.visible()
                            binding.slRoot.gone()

                            binding.tvTitle1.text = title
                            // 设置图标
                            binding.tvTitle1.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, icon),
                                null,  // top
                                null,  // right
                                null   // bottom
                            )
                        }
                        // 更新内容
                        updateSettingValue(this, binding)
                    }
                }
                onClick(R.id.sl_root1) {
                    with(getModel<IntelligentSettingEnum>()) {
                        itemClick(this, position)
                    }
                }

            }
        vb.recycler.models = settingList
    }

    fun initSettingData() {
        speechSettingDelegate = IntelliVersionManager.getInstance(this).getSettingDelegate() //获取代理类
        settingList = speechSettingDelegate.getSettingInfoList() //获取设置列表
        regionList = speechSettingDelegate.getRegionPointList() //获取区域列表
        //获取设置数据model
        settingModel = speechSettingDelegate.getSpeechSettingModel()
        val voiceType = settingModel.voiceType //声音类型
        selectedVoicer= if(voiceType == VoicerModel.TYPE_FEMALE) voicerList[1] else voicerList[0]
    }

    fun updateSettingValue(enum: IntelligentSettingEnum, binding: ItemTransSetBinding) {
        when (enum) {
            IntelligentSettingEnum.SETTING_VOLUMN -> { // 音量调整
//                val speakVolume = MK.decodeInt(MKKeys.KEY_SYNTHESIS_VOLUME, 100)
                val speakVolume = settingModel.volumn //从model中拿音量值
                binding.rsVolume.setProgress(speakVolume.toFloat())
                binding.rsVolume.setOnRangeChangedListener(object : OnRangeChangedListener {
                    override fun onRangeChanged(
                        view: RangeSeekBar?,
                        leftValue: Float,
                        rightValue: Float,
                        isFromUser: Boolean
                    ) {
                        MK.encode(MKKeys.KEY_SYNTHESIS_VOLUME, leftValue.toInt())
                    }

                    override fun onStartTrackingTouch(
                        view: RangeSeekBar?,
                        isLeft: Boolean
                    ) {
                    }

                    override fun onStopTrackingTouch(
                        view: RangeSeekBar?,
                        isLeft: Boolean
                    ) {
                        val value = view?.leftSeekBar?.progress
                        value?.let {
                            settingModel.volumn = it.toInt()
                            speechSettingDelegate.saveSettingModelValue(settingModel)
                        }
                    }
                })
            }
            IntelligentSettingEnum.SETTING_FONT -> { //字体大小
                val synthesisFontValue = settingModel.fontCode //从model中拿字体值
                binding.tvContent1.text = ListenSettingUtil.getFontSizeText(synthesisFontValue)
            }
            IntelligentSettingEnum.SETTING_SPEED -> { //播报语速
                var speedValue = settingModel.speed //从model中拿语速值
                binding.tvContent1.text = ListenSettingUtil.getSpeechSpeedText(speedValue)
            }
            IntelligentSettingEnum.SETTING_SILENCE -> { //静音时间
                var breakValue = settingModel.silence //从model中拿静音时间值
                binding.tvContent1.text = ListenSettingUtil.getSpeechBreakText(breakValue)
            }
            IntelligentSettingEnum.SETTING_NOISE -> { //环境噪音
                val noiseValue = settingModel.noise //从model中拿环境噪音值
                binding.tvContent1.text = ListenSettingUtil.getNoiseText(noiseValue)
            }
            IntelligentSettingEnum.SETTING_VOICE -> { //声音
                val voiceType = settingModel.voiceType
                binding.tvContent1.text = ListenSettingUtil.getVoicerText(voiceType)
            }
            IntelligentSettingEnum.SETTING_TEMPLATE -> { //行业翻译模版

            }
            IntelligentSettingEnum.SETTING_POINT -> { // 区域节点
                binding.tvContent1.text = selectedRegion.regionEnum.alias
            }
            else -> {}
        }
    }

    fun itemClick(enum: IntelligentSettingEnum, position: Int) {
        when (enum) {
            IntelligentSettingEnum.SETTING_FONT -> { //字体大小
                FontSizeSettingPop(this@ListenerModeSettingAct, settingModel.fontCode) {
//                    vb.tvFontSize.text = ListenSettingUtil.getFontSizeText(this)

                    settingModel.fontCode = this
                    refreshSettingData()
                }.showPopupWindow()
            }
            IntelligentSettingEnum.SETTING_SPEED -> { //播报语速
                SpeakSpeedSettingPop(this@ListenerModeSettingAct, settingModel.speed) {
//                    vb.tvPlaySpeed.text = ListenSettingUtil.getSpeechSpeedText(this)
                    settingModel.speed = this
                    refreshSettingData()
                }.showPopupWindow()
            }
            IntelligentSettingEnum.SETTING_SILENCE -> { //静音时间
                SpeechBreakThresholdSettingPop(this@ListenerModeSettingAct, settingModel.silence) {
//                    vb.tvSpeechBreak.text = ListenSettingUtil.getSpeechBreakText(this)
                    settingModel.silence = this
                    refreshSettingData()
                }.showPopupWindow()
            }
            IntelligentSettingEnum.SETTING_NOISE -> { //环境噪音
                EnvironmentalNoiseSettingPop(this@ListenerModeSettingAct, settingModel.noise) {
//                    vb.tvEnvironmentNoise.text = ListenSettingUtil.getNoiseText(this)
                    settingModel.noise = this
                    refreshSettingData()
                }.showPopupWindow()
            }
            IntelligentSettingEnum.SETTING_VOICE -> { //声音
                showSingleWheelViewPopupWindow(context, "请选择声音类型", voicerList, voicerList.indexOf(selectedVoicer)) { _, result, _ ->
                    selectedVoicer = result
                    //将选择声音的类型存储
//                    MK.encode(MKKeys.SpeechSet.key_set_volumn, result.type)
                    settingModel.voiceType = result.type
                    refreshSettingData()
                }
            }
            IntelligentSettingEnum.SETTING_TEMPLATE -> { //行业翻译模版
                showSingleWheelViewPopupWindow(this@ListenerModeSettingAct, "请选择场景", sceneList, sceneList.indexOf(selectedScene)) { _, result, _ ->
                    selectedScene = result
//                    MK.encode(MKKeys.KEY_TRANSLATE_SCENE, selectedScene)
                    settingModel.sceneCode = result.code
                }
            }
            IntelligentSettingEnum.SETTING_POINT -> { // 区域节点
                showSingleWheelViewPopupWindow(context, "请选择区域节点", regionList, regionList.indexOf(selectedRegion)) { _, result, _ ->
                    selectedRegion = result
//                    //将选择声音的类型存储
                    settingModel.regionCode = result.regionEnum.code
                    refreshSettingData()
                }
            }
            else -> {}
        }

    }

    fun refreshSettingData() {
        //保存setting信息到本地
        speechSettingDelegate.saveSettingModelValue(settingModel)
        //刷新list
        vb.recycler.models = settingList
    }

//    override fun onClick() {
//        super.onClick()
//        // 播报语速
//        vb.slPlaySpeed.doClick {
//            SpeakSpeedSettingPop(this@ListenerModeSettingAct) {
//                vb.tvPlaySpeed.text = ListenSettingUtil.getSpeechSpeedText(this)
//            }.showPopupWindow()
//        }
//        //语调
//        vb.llPitch.doClick {
//            SpeakPitchSettingPop(this@ListenerModeSettingAct, 0).showPopupWindow()
//        }
//        //停顿
//        vb.slSpeakBreak.doClick {
//            SpeechBreakThresholdSettingPop(this@ListenerModeSettingAct) {
//                vb.tvSpeechBreak.text = ListenSettingUtil.getSpeechBreakText(this)
//            }.showPopupWindow()
//        }
//        //环境噪音
//        vb.slNoise.doClick {
//            EnvironmentalNoiseSettingPop(this@ListenerModeSettingAct) {
//                vb.tvEnvironmentNoise.text = ListenSettingUtil.getNoiseText(this)
//            }.showPopupWindow()
//        }
//        //自定义词库
//        vb.slCustom.doClick {
//            toast("如有需求，请联系我们")
//        }
//
////        vb.llSpeakSpeed.doClick {
////            PlaySpeedSettingPop(this@ListenerModeSettingAct).showPopupWindow()
////        }
////        vb.tvEnvironmentNoise.doClick {
////            EnvironmentalNoiseSettingPop(this@ListenerModeSettingAct).showPopupWindow()
////        }
//        vb.slFontSize.doClick {
//            FontSizeSettingPop(this@ListenerModeSettingAct, {
//                vb.tvFontSize.text = ListenSettingUtil.getFontSizeText(this)
//
//            }).showPopupWindow()
//        }
//        vb.slScene.doClick {
//            showSingleWheelViewPopupWindow(this@ListenerModeSettingAct, "请选择场景", sceneList, sceneList.indexOf(selectedScene)) { _, result, _ ->
//                selectedScene = result
//                MK.encode(MKKeys.KEY_TRANSLATE_SCENE, selectedScene)
//            }
//        }
//        vb.slVoicer.doClick {
//            showSingleWheelViewPopupWindow(context, "请选择声音类型", voicerList, voicerList.indexOf(selectedVoicer)) { _, result, _ ->
//                selectedVoicer = result
//                //将选择声音的类型存储
//                MK.encode(MKKeys.KEY_VOICER_TYPE, result.type)
//            }
//        }
//    }
}

