package com.jlm.common.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 设备描述model
 * */
@Parcelize
@Serializable
data class DeviceModel(
    val _id: String = "",
    val device_name: String = "",
    val device_full_name: String = "",
    val device_type: String = "",
    val thumb_url: String = "",
    val class_id: String = "",
    val OwningAdministratorId: String = "",
    val remark: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val status: Boolean = true
) : Parcelable {
    companion object {
        const val DEVICE_MODEL = "device_model"
    }
}