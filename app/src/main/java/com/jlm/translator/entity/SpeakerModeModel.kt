package com.jlm.translator.entity

data class SpeakerModeModel(
    val sourceKey: String = "",
    var content: String = "",
    val targetKey: String = "",
    var translationContent: String = "",
)
