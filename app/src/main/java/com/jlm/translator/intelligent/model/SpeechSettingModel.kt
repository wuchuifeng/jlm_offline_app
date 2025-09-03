package com.jlm.translator.intelligent.model

import android.os.Parcelable
import cn.chawloo.base.listener.IWheelEntity
import kotlinx.parcelize.Parcelize
/**
 * 智能语音的设置信息model
 * */
@Parcelize
data class SpeechSettingModel(
    var volumn: Int = 95,
    var font: Float = 16f,
    var speed: Float = 1.0f,
    var silence: Int = 300,
    var noise: Float = 4.0f,
    var voiceType: Int = 1,
    var regionCode: Int = 1, //节点code 默认东亚
    var sceneCode: Int = 1,
    var fontCode: Int = 2 //0：较小  1：小  2：正常  3：大  4：较大
) : Parcelable {
    

}