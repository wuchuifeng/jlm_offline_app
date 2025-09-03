package com.jlm.translator.entity.responseModel

import kotlinx.serialization.Serializable

/**
 * 阿里云智能语音Token响应模型
 */
@Serializable
data class AliTokenModel(
    /** Token值 */
    val token: String,
    
    /** Token过期时间 */
    val expireTime: Long,
    
    /** 用户ID */
    val userId: String
)