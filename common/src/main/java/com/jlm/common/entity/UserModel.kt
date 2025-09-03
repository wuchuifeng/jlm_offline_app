package com.jlm.common.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UserModel(
    val OwningAdministrator: String = "",
    val OwningAdministratorId: String = "",
    val StarLevel: Int = 0,
    val _id: String = "",
    val avatar: String = "",
    val balance: Int = 0,
    val company: String = "",
    val gender: String = "",
    val isVip: Boolean = false,
    val nickname: String = "",
    val phone: String = "",
    val status: Boolean = true,
    val username: String = "",
) : Parcelable {
    val showLevel: String
        get() = when (StarLevel) {
            else -> "普通用户"
        }

    companion object {
        const val USER_MODEL = "user_model"
    }
}