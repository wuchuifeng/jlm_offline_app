package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class LoginDto(
    var phone: String? = null,
    var password: String? = null,
    var code: String = "8"
)
