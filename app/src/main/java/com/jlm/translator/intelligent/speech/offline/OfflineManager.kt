package com.jlm.translator.intelligent.speech.offline

import android.content.Context
import android.util.Log
import com.iflytek.aikit.core.AiHelper
import com.iflytek.aikit.core.BaseLibrary
import com.iflytek.aikit.core.CoreListener
import com.iflytek.aikit.core.ErrType
import com.safframework.log.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 离线SDK管理器
 * 负责iFlytek AI SDK的初始化、授权管理和生命周期管理
 */
object OfflineManager {
    
    private const val TAG = "OfflineManager"
    
    // SDK配置参数
    private var appId: String = "f569ee85"
    private var apiKey: String = "421f1a1ec179614a4d0da04caf9d941a"
    private var apiSecret: String = "ZjVkNDdmNGI4YTRmOGVkZTAwZWQ0YWY3"
    private var workDir: String = "/sdcard/iflytek/"
    private var customDeviceId: String = "ABC123def456ghi7"//"NiuTransTest006" //"ABC123def456ghi7" //"JLMTranslator001"
    private var authInterval: Int = 555
    
    // SDK状态
    private var isInitialized = false
    private var authResult = -1
    private var sdkInitCallback: SdkInitCallback? = null
    
    /**
     * SDK初始化回调接口
     */
    interface SdkInitCallback {
        /**
         * 授权成功
         */
        fun onAuthSuccess()
        
        /**
         * 授权失败
         * @param errorCode 错误码
         * @param errorMsg 错误信息
         */
        fun onAuthFailed(errorCode: Int, errorMsg: String)
        
        /**
         * HTTP认证结果
         * @param code 结果码
         */
        fun onHttpResult(code: Int)
        
        /**
         * 其他错误
         * @param code 错误码
         * @param errorMsg 错误信息
         */
        fun onOtherError(code: Int, errorMsg: String)
        
        /**
         * 初始化开始
         */
        fun onInitStart()
        
        /**
         * 初始化完成
         */
        fun onInitComplete()
    }
    
    /**
     * 授权结果监听器
     */
    private val coreListener = object : CoreListener {
        override fun onAuthStateChange(type: ErrType, code: Int) {
            L.i(TAG, "SDK授权状态变化: type=$type, code=$code")
            
            when (type) {
                ErrType.AUTH -> {
                    authResult = code
                    if (code == 0) {
                        L.i(TAG, "SDK授权成功")
                        sdkInitCallback?.onAuthSuccess()
                    } else {
                        L.e(TAG, "SDK授权失败，错误码: $code")
                        sdkInitCallback?.onAuthFailed(code, "SDK授权失败，错误码: $code")
                    }
                }
                ErrType.HTTP -> {
                    L.i(TAG, "HTTP认证结果: $code")
                    sdkInitCallback?.onHttpResult(code)
                }
                else -> {
                    L.e(TAG, "SDK其他错误: type=$type, code=$code")
                    sdkInitCallback?.onOtherError(code, "SDK其他错误: type=$type, code=$code")
                }
            }
        }
    }
    
    /**
     * 初始化SDK
     * @param context 应用上下文
     * @param appId 应用ID
     * @param apiKey API密钥
     * @param apiSecret API密钥
     * @param workDir 工作目录
     * @param callback 初始化回调
     */
    fun initSdk(
        context: Context,
//        appId: String,
//        apiKey: String,
//        apiSecret: String,
//        workDir: String,
        callback: SdkInitCallback? = null
    ) {
        L.i(TAG, "开始初始化SDK")
        
        if (isInitialized) {
            L.w(TAG, "SDK已经初始化")
            callback?.onAuthSuccess()
            return
        }
        
        // 验证参数
        if (appId.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
            val errorMsg = "SDK初始化参数不完整，请检查appId、apiKey、apiSecret"
            L.e(TAG, errorMsg)
            callback?.onAuthFailed(-1, errorMsg)
            return
        }
        
        this.appId = appId
        this.apiKey = apiKey
        this.apiSecret = apiSecret
        this.workDir = workDir
        this.sdkInitCallback = callback
        
        callback?.onInitStart()
        
        // 在子线程中执行SDK初始化
        CoroutineScope(Dispatchers.IO).launch {
            try {
                L.d(TAG, "开始构建SDK参数")
                
                // 构建初始化参数
                val params = BaseLibrary.Params.builder()
                    .appId(appId)
                    .apiKey(apiKey)
                    .apiSecret(apiSecret)
                    .customDeviceId(customDeviceId)
                    .workDir(workDir)
                    .authInterval(authInterval)
                    .build()
                
                L.d(TAG, "SDK参数构建完成，开始初始化")
                
                // 注册授权状态监听器
                AiHelper.getInst().registerListener(coreListener)
                
                // 执行SDK初始化
                AiHelper.getInst().initEntry(context.applicationContext, params)
                
                isInitialized = true
                
                withContext(Dispatchers.Main) {
                    callback?.onInitComplete()
                    L.i(TAG, "SDK初始化完成")
                }
                
            } catch (e: Exception) {
                L.e(TAG, "SDK初始化异常")
                withContext(Dispatchers.Main) {
                    callback?.onAuthFailed(-1, "SDK初始化异常: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 使用默认资源配置初始化SDK
     * @param context 应用上下文
     * @param callback 初始化回调
     */
    fun initSdkWithResources(context: Context, callback: SdkInitCallback? = null) {
        try {
//            val appId = context.getString(context.resources.getIdentifier("appId", "string", context.packageName))
//            val apiKey = context.getString(context.resources.getIdentifier("apiKey", "string", context.packageName))
//            val apiSecret = context.getString(context.resources.getIdentifier("apiSecret", "string", context.packageName))
//            val workDir = context.getString(context.resources.getIdentifier("workDir", "string", context.packageName))
            
            initSdk(context, callback)
        } catch (e: Exception) {
            L.e(TAG, "从资源文件读取SDK配置失败>>${e}")
            callback?.onAuthFailed(-1, "从资源文件读取SDK配置失败: ${e.message}")
        }
    }
    
    /**
     * 设置自定义设备ID
     * @param deviceId 设备ID
     */
    fun setCustomDeviceId(deviceId: String) {
        this.customDeviceId = deviceId
    }
    
    /**
     * 设置授权间隔
     * @param interval 间隔时间（毫秒）
     */
    fun setAuthInterval(interval: Int) {
        this.authInterval = interval
    }
    
    /**
     * 检查SDK是否已初始化
     * @return 是否已初始化
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * 检查SDK是否已授权成功
     * @return 是否已授权成功
     */
    fun isAuthorized(): Boolean = authResult == 0
    
    /**
     * 获取授权结果码
     * @return 授权结果码
     */
    fun getAuthResult(): Int = authResult
    
    /**
     * 获取授权状态描述
     * @return 授权状态描述
     */
    fun getAuthStatusDescription(): String {
        return when {
            !isInitialized -> "SDK未初始化"
            authResult == 0 -> "SDK授权成功"
            authResult == -1 -> "SDK授权中..."
            else -> "SDK授权失败，错误码: $authResult"
        }
    }
    
    /**
     * 重新授权
     * @param callback 回调
     */
    fun reauth(callback: SdkInitCallback? = null) {
        L.i(TAG, "重新执行授权")
        if (!isInitialized) {
            callback?.onAuthFailed(-1, "SDK未初始化，无法重新授权")
            return
        }
        
        this.sdkInitCallback = callback
        authResult = -1
        
        // 触发重新授权逻辑
        try {
            // 可以通过重新注册监听器来触发授权检查
            AiHelper.getInst().registerListener(coreListener)
        } catch (e: Exception) {
            L.e(TAG, "重新授权异常")
            callback?.onAuthFailed(-1, "重新授权异常: ${e.message}")
        }
    }
    
    /**
     * 取消注册SDK回调
     */
    fun unregisterCallback() {
        this.sdkInitCallback = null
    }
    
    /**
     * 反初始化SDK
     */
    fun uninitSdk() {
        L.i(TAG, "开始反初始化SDK")
        
        try {
            if (isInitialized) {
                AiHelper.getInst().unInit()
                isInitialized = false
                authResult = -1
                sdkInitCallback = null
                L.i(TAG, "SDK反初始化完成")
            } else {
                L.w(TAG, "SDK未初始化，无需反初始化")
            }
        } catch (e: Exception) {
            L.e(TAG, "SDK反初始化异常")
        }
    }
    
    /**
     * 获取SDK配置信息
     */
    fun getSdkConfig(): Map<String, String> {
        return mapOf(
            "appId" to appId,
            "apiKey" to if (apiKey.isNotEmpty()) "***${apiKey.takeLast(4)}" else "",
            "apiSecret" to if (apiSecret.isNotEmpty()) "***${apiSecret.takeLast(4)}" else "",
            "workDir" to workDir,
            "customDeviceId" to customDeviceId,
            "authInterval" to authInterval.toString(),
            "isInitialized" to isInitialized.toString(),
            "authResult" to authResult.toString()
        )
    }
    
    /**
     * 打印SDK状态信息
     */
    fun printSdkStatus() {
        L.i(TAG, "====== SDK状态信息 ======")
        getSdkConfig().forEach { (key, value) ->
            L.i(TAG, "$key: $value")
        }
        L.i(TAG, "授权状态: ${getAuthStatusDescription()}")
        L.i(TAG, "========================")
    }
}