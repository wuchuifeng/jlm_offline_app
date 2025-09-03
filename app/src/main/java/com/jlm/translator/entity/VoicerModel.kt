package com.jlm.translator.entity

import android.os.Parcelable
import cn.chawloo.base.listener.IWheelEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class VoicerModel(
    val voicerName: String = "女声",
    val type: Int = TYPE_FEMALE,
    val isSelected: Boolean = false
) : Parcelable, IWheelEntity {

    companion object{
        const val TYPE_MALE = 1 //男声
        const val TYPE_FEMALE = 2 //女声
    }

    override fun getWheelText(): String {
        return voicerName
    }

    override fun getWheelDescribtion(): String? {
        return ""
    }
}