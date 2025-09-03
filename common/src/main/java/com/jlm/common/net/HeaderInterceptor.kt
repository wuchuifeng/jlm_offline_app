package com.jlm.common.net

import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import okhttp3.Interceptor

/** 拦截器工具类
 * @author Create by 鲁超 on 2020/11/19 0019 16:46
 */
object HeaderInterceptor {
    fun headerInterceptor(): Interceptor {
        return Interceptor {
            val request = it.request()
            val builder = request.newBuilder()
            val token = MK.decodeString(MKKeys.KEY_TOKEN)
            if (token.isNotBlank()) {
                builder.addHeader("authorization", token)
            }
            builder.addHeader("Content-Type", "application/json")
            it.proceed(builder.build())
        }
    }
}