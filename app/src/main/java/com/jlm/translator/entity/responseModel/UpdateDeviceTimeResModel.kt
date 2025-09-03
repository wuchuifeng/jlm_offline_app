package com.jlm.translator.entity.responseModel

import kotlinx.serialization.Serializable

@Serializable
data class UpdateDeviceTimeResModel(
    val freeTime: Int = 0,
    val deviceId: String = "",
)
