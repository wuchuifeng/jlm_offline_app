package com.jlm.translator.manager

import com.jlm.translator.entity.responseModel.AliTokenModel
import com.jlm.translator.net.HttpRequest
import com.safframework.log.L
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 阿里云Token管理器
 * 负责管理阿里云智能语音Token的获取、缓存和刷新
 */
object AliTokenManager {
    
    private const val TAG = "AliTokenManager"
    
    // Token缓存
    private var cachedToken: AliTokenModel? = null
    
    // Token状态流
    private val _tokenState = MutableStateFlow<TokenState>(TokenState.None)
    val tokenState: StateFlow<TokenState> = _tokenState.asStateFlow()
    
    /**
     * Token状态
     */
    sealed class TokenState {
        object None : TokenState()
        object Loading : TokenState()
        data class Success(val token: AliTokenModel) : TokenState()
        data class Error(val message: String) : TokenState()
    }
    
    /**
     * 获取有效的Token
     * 如果缓存的Token还未过期，直接返回；否则重新获取
     */
    fun getValidToken(callback: (AliTokenModel?, String?) -> Unit) {
        // 检查缓存的Token是否有效
        cachedToken?.let { token ->
            if (isTokenValid(token)) {
                L.d(TAG, "使用缓存的Token")
                callback(token, null)
                return
            }
        }
        
        // 缓存的Token无效或不存在，重新获取
        refreshToken(callback)
    }
    
    /**
     * 强制刷新Token
     */
    fun refreshToken(callback: (AliTokenModel?, String?) -> Unit) {
        L.d(TAG, "开始获取阿里云Token")
        _tokenState.value = TokenState.Loading
        
        HttpRequest.requestAliToken(
            success = { tokenModel ->
                L.d(TAG, "获取阿里云Token成功")
                cachedToken = tokenModel
                _tokenState.value = TokenState.Success(tokenModel)
                callback(tokenModel, null)
            },
            failure = { errorMessage ->
                L.e(TAG, "获取阿里云Token失败: $errorMessage")
                _tokenState.value = TokenState.Error(errorMessage)
                callback(null, errorMessage)
            }
        )
    }
    
    /**
     * 检查Token是否有效（未过期）
     * 提前5分钟判断为过期，避免在使用过程中过期
     */
    private fun isTokenValid(token: AliTokenModel): Boolean {
        val currentTime = System.currentTimeMillis() / 1000 // 转换为秒
        val bufferTime = 5 * 60 // 5分钟缓冲时间
        return token.expireTime > (currentTime + bufferTime)
    }
    
    /**
     * 清除缓存的Token
     */
    fun clearToken() {
        L.d(TAG, "清除缓存的Token")
        cachedToken = null
        _tokenState.value = TokenState.None
    }
    
    /**
     * 获取当前缓存的Token（可能为空或已过期）
     */
    fun getCachedToken(): AliTokenModel? = cachedToken
}