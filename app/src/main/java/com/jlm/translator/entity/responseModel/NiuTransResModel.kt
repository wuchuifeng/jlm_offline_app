package com.jlm.translator.entity.responseModel

import kotlinx.serialization.Serializable

@Serializable
data class NiuTransResModel(
    val from: String = "",
    val to: String = "",
    val tgtText: String  = "",
    val errorCode: String = "",
    val errorMsg: String = ""
)
