package com.jlm.common.net

import android.app.Application
import android.content.Intent
import android.text.TextUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.chawloo.base.ext.fromJson
import cn.chawloo.base.ext.getInt
import cn.chawloo.base.ext.getString
import cn.chawloo.base.ext.packageName
import cn.chawloo.base.ext.toast
import cn.chawloo.base.widget.LoadingDialog
import com.drake.net.NetConfig
import com.drake.net.convert.NetConverter
import com.drake.net.exception.ConvertException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ServerResponseException
import com.drake.net.interceptor.RetryInterceptor
import com.drake.net.interfaces.NetErrorHandler
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDialogFactory
import com.drake.net.okhttp.setErrorHandler
import com.drake.net.request.kType
import com.safframework.http.interceptor.AndroidLoggingInterceptor
import com.safframework.log.L
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Cache
import okhttp3.Response
import java.io.File
import java.lang.reflect.Type
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.reflect.KType

/**
 * TODO
 * @author Create by 鲁超 on 2022/6/13 0013 10:25:04
 *----------Dragon be here!----------/
 *       ┌─┐      ┌─┐
 *     ┌─┘─┴──────┘─┴─┐
 *     │              │
 *     │      ─       │
 *     │  ┬─┘   └─┬   │
 *     │              │
 *     │      ┴       │
 *     │              │
 *     └───┐      ┌───┘
 *         │      │神兽保佑
 *         │      │代码无BUG！
 *         │      └──────┐
 *         │             ├┐
 *         │             ┌┘
 *         └┐ ┐ ┌───┬─┐ ┌┘
 *          │ ┤ ┤   │ ┤ ┤
 *          └─┴─┘   └─┴─┘
 *─────────────神兽出没───────────────/
 */
object NetConstants {
    const val BASE_API = "BaseApiUrl"
    const val DEFAULT_TIMEOUT: Long = 60

    const val OFFICIAL_WEBSITE = "www.jlmjs.com"
    const val USER_AGREEMENT = ""
    const val PRIVACY_POLICY = ""
    const val THIRD_SHARE = ""
    const val PERSONAL_INFORMATION_COLLECTION = ""
    const val PERMISSION_CHECKLIST = ""
    const val MEMBER_AGREEMENT = ""

    const val devUrl = "https://tmapi.jlmjs.com/v1/app/" //开发环境地址
    const val prodUrl = "https://tmapi.jlmjs.com/v1/app/" //生产环境地址

    const val TAG = "NetConstants"

    val api: String
        get() {
            val api = getUrl(BASE_API)
            return api.ifBlank {
                //"http://8.134.165.18:3099/v1/"
//                "http://txbapi.jlmjs.com/v1/"
                "https://tmapi.jlmjs.com/v1/app/" //新环境地址
            }
        }


    private var mBaseUrl: ArrayList<Pair<String, String>> = ArrayList()

    /**
     * 设定访问地址
     *
     * @param key 配置名
     * @param url 地址详情
     */
    fun addBaseUrl(key: String, url: String) {
        mBaseUrl.takeIf { it.isNotEmpty() }?.forEach {
            if (it.first == key) {
                L.e("API地址修改，删除后初始化")
                mBaseUrl.remove(it)
            }
            mBaseUrl.add(Pair(key, url))
        } ?: mBaseUrl.add(Pair(key, url))
    }

    private fun getUrl(key: String?): String {
        mBaseUrl.forEach {
            if (TextUtils.equals(key, it.first)) {
                return it.second
            }
        }
        return ""
    }


    @OptIn(ExperimentalSerializationApi::class)
    private val jsonDecoder = Json {
        encodeDefaults = true//编码默认值   如果没有对象则会显示默认值
        ignoreUnknownKeys = true//忽略未知键
        explicitNulls = true //序列化时是否忽略null
        coerceInputValues = true //强制输入值
        isLenient = true //宽松解析，json格式异常也可解析，如：{name:"小红",age:"18"} + Person(val name:String,val age:Int) ->Person("小红",18)
    }

    fun initApi(app: Application, debug: Boolean = false, showLog: Boolean = false) {
        val baseApiUrl: String = if (debug) {
            //"http://8.134.165.18:3099/v1/" //测试环境
//            "http://txbapi.jlmjs.com/v1/"
            devUrl
        } else {
//            "http://8.134.165.18:3099/v1/" //正式环境
//            "http://txbapi.jlmjs.com/v1/"
            prodUrl
        }
        addBaseUrl(BASE_API, baseApiUrl)
        NetConfig.initialize(baseApiUrl, app) {
//            setDebug(BuildConfig.DEBUG)
            proxy(Proxy.NO_PROXY)
            cache(Cache(File(app.cacheDir, "net_cache"), 50 * 1024L * 1024L))
            connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            addInterceptor(HeaderInterceptor.headerInterceptor())
            addInterceptor(AndroidLoggingInterceptor.build(isDebug = showLog, hideVerticalLine = true))
            addInterceptor(RetryInterceptor(2))
            retryOnConnectionFailure(true)
            setConverter(serializationConverter)
            setErrorHandler(object : NetErrorHandler {
                override fun onError(e: Throwable) {
                    if (e is RequestParamsException && e.tag == 401) {
                        LocalBroadcastManager.getInstance(app).sendBroadcast(Intent("$packageName.FORCE_OFFLINE"))
                    } else if (e is RequestParamsException && e.tag == 400) {
                        toast(e)
//                        super.onError(e)
                    } else {
                        super.onError(e)
                    }
                    L.e(TAG, "errorHandler>>>${e.message}")
                }
            })
            this.build().apply {
                dispatcher.maxRequestsPerHost = 20
                dispatcher.maxRequests = 128
            }
            setDialogFactory {
                LoadingDialog(it)
            }
        }
    }

    private val serializationConverter = object : NetConverter {
        override fun <R> onConvert(succeed: Type, response: Response): R? {
            try {
                return NetConverter.onConvert<R>(succeed, response)
            } catch (e: ConvertException) {
                val code = response.code
                when {
                    code in 200..300 -> {
                        var bodyString = response.body?.string() ?: return null
                        val kType = response.request.kType ?: throw ConvertException(response, "Request does not contain KType")
                        return try {
                            L.d(TAG, "bodyString>>>$bodyString>>>${kType?.arguments}")
                            val json = bodyString.fromJson() // 获取JSON中后端定义的错误码和错误信息
                            val businessCode = json.getInt("code")
                            if (businessCode == 200) { // 对比后端自定义错误码
                                bodyString.parseBody<R>(kType)
                            } else {
                                bodyString = "{\"code\": ${businessCode}, \"message\" : \"${json.getString("message")}\"}"
                                bodyString.parseBody<R>(kType)
//                                throw ResponseException(response, json.getString("message"))
                            }
                        } catch (e: Exception) {
//                            throw ResponseException(response,e.message)
                            bodyString.parseBody<R>(kType)
                        }
                    }

                    code in 400..499 -> throw RequestParamsException(response, code.toString()).apply {
                        L.d(TAG, "400...499>>>${code.toString()}")
                        tag = code
                    }

                    code >= 500 -> throw ServerResponseException(response, code.toString())
                    else -> throw ConvertException(response)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <R> String.parseBody(succeed: KType): R? {
        return jsonDecoder.decodeFromString(Json.serializersModule.serializer(succeed), this) as? R
    }
}