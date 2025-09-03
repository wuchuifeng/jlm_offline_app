package com.jlm.translator.entity

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

/**
 * 产品实体
 */
@Serializable
data class ProductType(
    /**
     * 产品ID
     */
    val productId: Int = -1,
    /**
     * 产品图标
     */
    @DrawableRes val productIcon: Int = 0,
    /**
     * 产品图标的url
     * */
    val productIconUrl: String? = "",
    /**
     * 产品名称
     */
    val productName: String = "",
    /**
     * 产品描述，用语显示二级内容或标题
     */
    val productDesc: String = "",
    /**
     * 选中状态
     */
    var isSelected: Boolean = false,
)