package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class TranslateRecordNavItem(
    var name: String = "全部",
    var index: Int = 0
)
