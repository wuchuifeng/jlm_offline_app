package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class LogoutDto(
    val userId: String?
)
