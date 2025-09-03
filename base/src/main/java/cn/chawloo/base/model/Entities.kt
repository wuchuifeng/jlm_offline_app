package cn.chawloo.base.model

import kotlinx.serialization.Serializable

@Serializable
data class Entities<T>(
    val result: List<T> = emptyList(),
    val current: Int = 0,
    val pageSize: Int = 20,
    val total: Int = 0,
)
