package com.jlm.translator.entity

import kotlinx.serialization.Serializable

@Serializable
data class KeyModel(
    val _id: String = "",
    val appid: List<Appid> = emptyList(),
    val createdAt: String = "",
    val remark: String = "",
    val secret: String = "",
    val token: String = "",
    val updatedAt: String = "",
    val type: Int = 0
) {
    companion object {
        const val KEY_MODEL = "key_model"
        const val TYPE_ALI = 1 //阿里智能语音
        const val TYPE_AZURE = 2 //微软智能语音
    }
}

@Serializable
data class Appid(
    val id: String = "",
    val lang: String = "",
    val type: String = "",
)