package com.jlm.translator.entity

/***选择翻译模式弹窗Model*/
data class TranslationModeModel(
    var type: Int,
    var isChecked: Boolean,
    var modelName: String
)
