package com.jlm.translator.entity.requestDto

import kotlinx.serialization.Serializable

/**
 * 获取设备信息传入的params
 * @author Created by liuwen on 2022/11/04.
 */
@Serializable
data class CreateDeviceInfoDto(
    val deviceId: String = "",
    val name: String = ""
)