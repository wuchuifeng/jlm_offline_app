package com.jlm.translator.entity.requestDto

import kotlinx.serialization.Serializable

/**
 * 获取设备信息传入的params
 * @author Created by liuwen on 2022/11/04.
 */
@Serializable
data class DeviceInfoDto(
//    var params: Param? = null,
    val deviceId: String = "",
    val name: String? = null,
    val model: String? = null
) {
//    @Serializable
//    data class Param(
//        val deviceId: String = "",
//        val name: String = ""
//    )
}