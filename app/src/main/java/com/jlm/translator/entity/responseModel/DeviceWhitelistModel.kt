package com.jlm.translator.entity.responseModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 设备白名单响应模型
 * @author Created by assistant on 2024/01/01.
 */
@Parcelize
@Serializable
data class DeviceWhitelistModel(
    val _id: String = "",
    val name: String = "",
    val model: String = "",
    val company: String = "",
    val status: Boolean = false,
    val remark: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) : Parcelable {
    
    /**
     * 判断设备型号是否在白名单中且启用状态
     */
    fun isWhitelisted(): Boolean {
        return status
    }
    
    /**
     * 获取状态描述
     */
    fun getStatusDesc(): String {
        return if (status) "已启用" else "已禁用"
    }
} 