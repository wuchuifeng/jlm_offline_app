package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class FaqType(
    val typeName: String,
    var isChecked: Boolean = false,
)