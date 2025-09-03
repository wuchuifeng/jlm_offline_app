package com.jlm.translator.entity

data class DeviceItemModel(
    val title: String? = "", //标题
    val content: String? = "", //内容
    val editAble:  Boolean = false, //是否可编辑
    val type: Int = 0 //类型
)
