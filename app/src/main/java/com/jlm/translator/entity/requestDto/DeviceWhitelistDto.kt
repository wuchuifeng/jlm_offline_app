package com.jlm.translator.entity.requestDto

import kotlinx.serialization.Serializable

/**
 * 设备白名单查询请求参数
 * @author Created by assistant on 2024/01/01.
 */
@Serializable
data class DeviceWhitelistDto(
    val model: String = ""
) 