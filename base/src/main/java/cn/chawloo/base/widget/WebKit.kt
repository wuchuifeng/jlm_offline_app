package cn.chawloo.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import cn.chawloo.base.pop.showConfirmWindow
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback
import com.tencent.smtt.export.external.interfaces.JsPromptResult
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient


open class WebKit @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : WebView(context, attrs, defStyleAttr) {
    private var mContext: Context = context

    init {
        webSetting()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webSetting() {
        setBackgroundColor(Color.parseColor("#ffffff"))
        requestFocus()
        requestFocusFromTouch()
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
        if (!isInEditMode) {
            settings?.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                allowFileAccess = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportMultipleWindows(true)
                setAppCacheEnabled(true)
                setAppCacheMaxSize(Long.MAX_VALUE)
                //最重要的方法，一定要设置，这就是出不来的主要原因
                domStorageEnabled = true
                //启用地理定位
                setGeolocationEnabled(true)
                //设置定位的数据库路径
                val dir = mContext.applicationContext.getDir("database", Context.MODE_PRIVATE).path
                setGeolocationDatabasePath(dir)
                pluginState = WebSettings.PluginState.ON_DEMAND
                cacheMode = WebSettings.LOAD_NO_CACHE
                defaultFontSize = 16
                displayZoomControls = false
                databaseEnabled = true
            }
        }
        //配置权限（同样在WebChromeClient中实现）
        this.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                view?.loadUrl(url)
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        this.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissionsCallback?) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                callback?.invoke(origin, true, false)
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                showConfirmWindow(mContext, content = message) {
                    result?.confirm()
                }
                return super.onJsAlert(view, url, message, result)
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                showConfirmWindow(mContext, content = message) {
                    result?.confirm()
                }
                return super.onJsConfirm(view, url, message, result)
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                showConfirmWindow(mContext, content = message) {
                    result?.confirm()
                }
                return true
            }
        }
    }
}