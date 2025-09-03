package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class DeviceEncyclopediaDto(
    var params: Param? = null,
) {
    @Serializable
    data class Param(
        val device_id: String = ""
    )
}


