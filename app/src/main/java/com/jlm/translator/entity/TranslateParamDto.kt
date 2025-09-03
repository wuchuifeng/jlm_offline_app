package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class TranslateParamDto (
    val FormatType: String = "text",
    val SourceLanguage: String = "auto",
    val TargetLanguage: String = "en",
    val SourceText: String = "",
    val Scene: String = "general"
)