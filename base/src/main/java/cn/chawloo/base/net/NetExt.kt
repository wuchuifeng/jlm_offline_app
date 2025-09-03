package cn.chawloo.base.net

import cn.chawloo.base.BuildConfig
import cn.chawloo.base.ext.toast
import com.drake.net.exception.ConvertException
import com.drake.net.exception.NetSocketTimeoutException
import com.drake.net.exception.NetUnknownHostException
import com.drake.net.exception.NetworkingException
import com.drake.net.scope.AndroidScope
import org.json.JSONException

fun AndroidScope.customCatch(block: (ApiException) -> Unit = { toast(it) }): AndroidScope {
    return this.catch {
        if (BuildConfig.DEBUG) {
            it.printStackTrace()
        }
        val childException = it.cause
        if (childException != null && childException is ApiException) {
            block(childException)
            return@catch
        }
        block(ApiException(when (it) {
            is NetUnknownHostException, is NetSocketTimeoutException -> {
                "发生网络错误，请重试"
            }

            is JSONException -> {
                "数据解析失败，请重试"
            }

            is ConvertException -> {
                "数据解析失败，请重试"
            }

            is NetworkingException -> {
                "网络连接错误，请重试"
            }

            else -> {
                it.message?.takeIf { msg -> msg.isNotBlank() } ?: "请求失败，请重试"
            }
        }))
    }
}