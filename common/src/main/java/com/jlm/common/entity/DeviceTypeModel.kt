package com.jlm.common.entity

import kotlinx.serialization.Serializable

/**
 * 设备分类列表实体类
 * */
@Serializable
data class DeviceTypeModel(
    val result: List<DeviceEncyclopediaModel> = emptyList(),
    val current: Int = 1,
    val pageSize: Int = 15,
    val total: Int = 2
)