package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class RechargeProduct(
    val name: String = "",
    val price: Double = 0.0,
    val currency: String = "CN￥",
    var isChecked: Boolean = false,
)
