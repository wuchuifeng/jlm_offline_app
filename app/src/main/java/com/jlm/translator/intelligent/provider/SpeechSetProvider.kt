package com.jlm.translator.intelligent.provider

import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.translator.entity.SceneModel
import com.jlm.translator.entity.VoicerModel
import com.jlm.translator.intelligent.model.RegionModel
import com.jlm.translator.intelligent.model.RegionPointModel
import com.jlm.translator.manager.IntelligentVersionEnum
import com.jlm.translator.manager.RegionEnum

/**
 * 翻译设置提供类
 * */
object SpeechSetProvider {

    val fontSizeList = arrayOf(12F, 14F, 16F, 19F, 21F)
    val progressFactors = arrayOf(0F, 25F, 50F, 75F, 100F)
    val defaultFontSize = fontSizeList[2]

    //阿里的翻译场景列表
    private val aliTransModelList = listOf(
        SceneModel(showTxt = "通用", scene = "general"),
        SceneModel(showTxt = "医疗", scene = "medical"),
        SceneModel(showTxt = "社交", scene = "social"),
        SceneModel(showTxt = "金融", scene = "finance")
    )
    //微软的音色列表
    private val azureVoicerList = listOf(
        VoicerModel(voicerName = "男声", type = VoicerModel.TYPE_MALE),
        VoicerModel(voicerName = "女声", type = VoicerModel.TYPE_FEMALE),
    )

//    fun getAliRegionModel(code: Int): RegionModel {
//        val regionModel = getAliRegionList().find { it.code == code } ?: getAliDefaultRegion()
//
//        return regionModel
//    }



    fun getVolumnValue(): Int{
        val speakVolume = MK.decodeInt(MKKeys.SpeechSet.key_set_volumn, 100)
        if (speakVolume < 0 || speakVolume > 100) {
            return 99
        } else {
            return speakVolume
        }
    }

    fun getSpeedValue(): Float{
        val speed = MK.decodeFloat(MKKeys.SpeechSet.key_set_speed, 1F)
        return speed
    }

    fun getFontValue(): Float{
        val font = MK.decodeFloat(MKKeys.SpeechSet.key_set_font, defaultFontSize)
        return font
    }

    fun getSilenceValue(): Int{
        val silence = MK.decodeInt(MKKeys.SpeechSet.key_set_silence, 300)
        return silence
    }

    fun getNoiseValue(): Float{
        val noise = MK.decodeFloat(MKKeys.SpeechSet.key_set_noise, 0.2F)
        return noise
    }

    fun getVoiceValue(): Float{
        val voice = MK.decodeFloat(MKKeys.SpeechSet.key_set_voice, 1F)
        return voice
    }

    fun getAliTransSceneValue(): String {
        val model = MK.decodeString(MKKeys.SpeechSet.key_set_ali_scene, "")
        return model
    }

    fun getAzureTransSceneValue(): String {
        val model = MK.decodeString(MKKeys.SpeechSet.key_set_azure_scene, "")
        return model
    }

    fun getAliTransSceneList(): List<SceneModel> {
        return aliTransModelList
    }

    fun getAzureVoicerList(): List<VoicerModel> {
        return azureVoicerList
    }

}