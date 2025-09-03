package com.jlm.translator.entity

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
data class ProductMode(
    val productModeName: String = "",
    val productModeDesc: String = "",
    @DrawableRes val productModeIcon: Int = 0,
    val productModeUrl: String = "",
)
