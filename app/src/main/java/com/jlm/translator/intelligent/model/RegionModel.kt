package com.jlm.translator.intelligent.model

import android.os.Parcelable
import cn.chawloo.base.listener.IWheelEntity
import kotlinx.parcelize.Parcelize
/**
 * 智能语音的区域节点实体
 * */
@Parcelize
data class RegionModel(
    val mainKey: String = "",
    val subKey: String = "",
    val region: String = "",
    val code: Int = 1,
    val name: String = ""
) : Parcelable, IWheelEntity {

    override fun getWheelText(): String {
        return name
    }

    override fun getWheelDescribtion(): String? {
        return ""
    }

}