package cn.chawloo.base.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 接口回调基类
 *
 * @author Create by 鲁超 on 2020/4/8 0008 16:34
 */
@Serializable
data class BaseResult<T>(
    @SerialName("code")
    var status: Int = 0,
    var message: String = "",
    var time: Long = 0,
    var data: T? = null,
)