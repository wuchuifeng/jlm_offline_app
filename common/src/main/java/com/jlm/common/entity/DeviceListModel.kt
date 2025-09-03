package com.jlm.common.entity

import kotlinx.serialization.Serializable

/**
 * 设备列表实体类
 * */
@Serializable
data class DeviceListModel(
    val result: List<DeviceModel> = emptyList(),
    val current: Int = 1,
    val pageSize: Int = 15,
    val total: Int = 2
)