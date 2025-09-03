package com.jlm.translator.entity

import kotlinx.serialization.Serializable

/***发现页-产品使用说明项*/
@Serializable
data class ProductInstructionsType(
    val productLinkUrl: String = "",
    val productInstructionName: String = ""
)
