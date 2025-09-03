package com.jlm.translator.entity

import androidx.annotation.DrawableRes

data class PayMethod(
    val name: String = "",
    @DrawableRes val icon: Int = 0,
    var isChecked: Boolean = false
)
