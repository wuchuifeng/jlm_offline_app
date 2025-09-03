package com.jlm.common.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 设备的问题百科实体类
 * */
@Parcelize
@Serializable
data class DeviceEncyclopediaModel(
    val _id: String = "",
    val title: String = "",
    val content: String = "",
    val device_id: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val devices: DeviceModel? = null

) : Parcelable {
    companion object {
        const val DEVICE_ENCYCLOPEDIA_MODEL = "device_encyclopedia_model"
    }
}
