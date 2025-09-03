package com.jlm.translator.entity

data class RechargeRecordModel(
    val rechargeName: String,//名称
    val date: Long,//日期
    val price: String,//金额
    val unit: String = "Fish"//单位
)
