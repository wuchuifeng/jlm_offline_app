package com.jlm.translator.entity

import kotlinx.serialization.Serializable

/**
 * 翻译结果类
 * */
@Serializable
data class TranslateResultModel(
    var RequestId: String? = "",
    var Data: DataEntity? = null,
    var Code: String? = "0",
    var Message: String = "翻译失败"
) {

    companion object {
        const val CODE_INVAILD_DATE = "InvalidDate"
        const val CODE_SUCC = "200"
    }

    @Serializable
    data class DataEntity(
        val WordCount: String = "0",
        val Translated: String = ""
    )
}
