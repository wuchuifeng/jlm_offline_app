package com.jlm.translator.entity.responseModel

import kotlinx.serialization.Serializable

@Serializable
data class AzureTransResModel(
    val translations: List<TranslateResult>,
    val detectedLanguage: DetectLanguage
)

@Serializable
data class DetectLanguage(val language: String, val score: Float)
@Serializable
data class TranslateResult(val text: String,  val to: String)