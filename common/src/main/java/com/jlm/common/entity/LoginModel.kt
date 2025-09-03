package com.jlm.common.entity

import kotlinx.serialization.Serializable

@Serializable
data class LoginModel(
    val token: String? = null,
    val userInfo: UserModel? = null
)