package com.jlm.translator.intelligent.model

import android.os.Parcelable
import cn.chawloo.base.listener.IWheelEntity
import com.jlm.translator.manager.RegionEnum
import kotlinx.parcelize.Parcelize
/**
 * 智能语音的区域节点实体
 * */
@Parcelize
data class RegionPointModel(
    val regionEnum: RegionEnum
) : Parcelable, IWheelEntity {

    override fun getWheelText(): String {
        return regionEnum.alias
    }

    override fun getWheelDescribtion(): String? {
        return ""
    }

}