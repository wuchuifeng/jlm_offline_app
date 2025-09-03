package com.jlm.translator.example

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.jlm.translator.manager.AliTokenManager
import com.safframework.log.L
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 阿里云Token使用示例
 * 展示如何在实际应用中使用AliTokenManager
 */
class AliTokenUsageExample {
    
    private val TAG = "AliTokenUsageExample"
    
    /**
     * 示例1：获取Token用于语音识别
     */
    fun startSpeechRecognition() {
        L.d(TAG, "开始语音识别，先获取Token")
        
        AliTokenManager.getValidToken { token, error ->
            if (token != null) {
                L.d(TAG, "获取Token成功，开始语音识别")
                L.d(TAG, "Token: ${token.token}")
                L.d(TAG, "UserId: ${token.userId}")
                L.d(TAG, "ExpireTime: ${token.expireTime}")
                
                // 在这里使用Token进行语音识别
                // initSpeechRecognition(token)
                
            } else {
                L.e(TAG, "获取Token失败: $error")
                // 处理错误情况
                handleTokenError(error ?: "未知错误")
            }
        }
    }
    
    /**
     * 示例2：在Activity/Fragment中监听Token状态
     */
    fun observeTokenState(context: Context) {
        // 假设这是在Activity或Fragment中
        // lifecycleScope.launch {
        //     AliTokenManager.tokenState.collectLatest { state ->
        //         when (state) {
        //             is AliTokenManager.TokenState.None -> {
        //                 L.d(TAG, "Token状态：未初始化")
        //                 // 可以显示初始状态UI
        //             }
        //             is AliTokenManager.TokenState.Loading -> {
        //                 L.d(TAG, "Token状态：获取中...")
        //                 // 显示加载状态
        //                 showLoading(true)
        //             }
        //             is AliTokenManager.TokenState.Success -> {
        //                 L.d(TAG, "Token状态：获取成功")
        //                 showLoading(false)
        //                 // 可以继续进行需要Token的操作
        //                 onTokenReady(state.token)
        //             }
        //             is AliTokenManager.TokenState.Error -> {
        //                 L.e(TAG, "Token状态：获取失败 - ${state.message}")
        //                 showLoading(false)
        //                 // 显示错误信息
        //                 showError(state.message)
        //             }
        //         }
        //     }
        // }
    }
    
    /**
     * 示例3：强制刷新Token
     */
    fun refreshTokenManually() {
        L.d(TAG, "手动刷新Token")
        
        AliTokenManager.refreshToken { token, error ->
            if (token != null) {
                L.d(TAG, "Token刷新成功")
                // 使用新的Token
            } else {
                L.e(TAG, "Token刷新失败: $error")
            }
        }
    }
    
    /**
     * 示例4：应用退出时清理Token
     */
    fun onAppDestroy() {
        L.d(TAG, "应用退出，清理Token缓存")
        AliTokenManager.clearToken()
    }
    
    // 模拟的辅助方法
    private fun handleTokenError(error: String) {
        // 处理Token获取失败的情况
        // 例如：显示错误提示、重试逻辑等
    }
    
    private fun showLoading(show: Boolean) {
        // 显示或隐藏加载状态
    }
    
    private fun showError(message: String) {
        // 显示错误信息
    }
    
    private fun onTokenReady(token: com.jlm.translator.entity.responseModel.AliTokenModel) {
        // Token准备就绪后的处理逻辑
    }
}