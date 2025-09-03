package com.jlm.translator.entity

import com.jlm.translator.manager.TranslateRecordTypeEnum

/**
 * 翻译记录导航项数据模型
 */
data class TranslateRecordNavModel(
    val recordType: TranslateRecordTypeEnum,
    var isSelected: Boolean = false
) {
    val title: String get() = recordType.title
    val code: Int get() = recordType.code
    val icon: Int get() = recordType.icon
} 