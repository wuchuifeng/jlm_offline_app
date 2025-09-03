package com.jlm.translator.entity

import android.os.Parcelable
import cn.chawloo.base.listener.IWheelEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class SceneModel(
    val showTxt: String = "",
    val scene: String = "",
    val isSelected: Boolean = false,
    val code: Int = 1
) : Parcelable, IWheelEntity {
    override fun getWheelText(): String {
        return showTxt
    }

    override fun getWheelDescribtion(): String? {
        return ""
    }
}