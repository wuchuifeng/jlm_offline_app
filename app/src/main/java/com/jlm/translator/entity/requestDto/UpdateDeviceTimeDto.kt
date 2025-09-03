package com.jlm.translator.entity.requestDto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateDeviceTimeDto(
    val deviceId: String = "",
    val free_time: Int = 0,
    val versionType: Int = 0, //版本类型， 中国版  国际版
    val funcType: Int = 0  //功能类型  同声传译  多人自由对话  双组自由对话
)
