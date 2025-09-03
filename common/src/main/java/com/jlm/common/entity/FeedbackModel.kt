package com.jlm.common.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 设备的问题百科实体类
 * */
@Parcelize
@Serializable
data class FeedbackModel(
    var tel: String = "",
    val qaType: Int = 1,
    val desc: String = "",
    val pictures: List<String> = emptyList()

) : Parcelable {
    companion object {
        const val FEED_BACK_MODEL = "feed_back_model"
    }
}
