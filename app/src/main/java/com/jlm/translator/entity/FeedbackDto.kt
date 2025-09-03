package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class FeedbackDto(
    var tel: String = "",
    val feedType: Int = 1,
    val feedDesc: String = "",
    val feedImg: List<String>? = null
)
