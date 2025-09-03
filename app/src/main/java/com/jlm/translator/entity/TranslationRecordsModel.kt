package com.jlm.translator.entity

import androidx.databinding.BaseObservable
import com.drake.brv.item.ItemPosition

/***翻译记录数据*/
data class TranslationRecordsModel(
    var type: Int = 0,//布局样式类型: 0时间 1翻译
    var checked: Boolean = false,//是否选中
    var isMarked: Boolean = false,//是否已标记
    var date: Long,//日期
    var content: String = "",//原文
    var translationContent: String = "",//转换后的内容
    val recordModel: Int = 0,//记录模式: 0翻译记录，1对话记录
    override var itemPosition: Int = 0
) : BaseObservable(), ItemPosition
