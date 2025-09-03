package com.jlm.translator.act

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.ViewGroup
import cn.chawloo.base.ext.bundleExtra
import cn.chawloo.base.ext.content
import cn.chawloo.base.ext.empty
import cn.chawloo.base.ext.string
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.openWeb
import com.jlm.translator.databinding.ActRichTextBinding
import com.safframework.log.L
import com.tencent.smtt.sdk.CookieManager
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.therouter.router.Route

/**
 * 内置浏览器
 * @author Create by 鲁超 on 2021/2/10 0010 13:53
 */
@Route(path = Rt.RichTextAct)
class RichTextAct : BaseActCompat() {
    companion object {
        const val WEB_TITLE = "web_title"
        const val WEB_URL = "web_url"
        const val WEB_HTML = "web_html"
    }

    private val vb by binding<ActRichTextBinding>()
    private lateinit var bundle: Bundle

    /**
     * 标题,如果为空,则默认使用网页内容的标题
     */
    private var mTitle: String? = null
    private var mContent: String? = ""

    /**
     * 返回是否直接退出Activity
     */
    private var backCanFinish = false

    /**
     * progress缓冲效果
     */
    private var count = 0
    private var max = 0
    private var speed = 30
    private var objectAnimator: ObjectAnimator? = null

    override fun initialize() {
        super.initialize()
        bundle = intent.bundleExtra() ?: Bundle()
        handleIntent()
        initUi()
//        loadContent(handleIntent())
        loadData()
        immersionBar {
            transparentStatusBar()
            statusBarColor(android.R.color.white)
            navigationBarColor(android.R.color.white)
            navigationBarDarkIcon(true)
            autoDarkModeEnable(true)
            fitsSystemWindows(true)
        }
    }

    private fun handleIntent() {
        bundle.run {
            mTitle = string(WEB_TITLE)
            mContent = string(WEB_HTML)
        }
    }

    private fun initUi() {
        if (mTitle.isNullOrBlank()) {
            vb.headBanner.title = ""
        } else {
            vb.headBanner.title = mTitle
        }
//        vb.progressBar1.max = 100
//        vb.webview.webViewClient = object : WebViewClient() {
//            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                super.onPageStarted(view, url, favicon)
//                vb.progressBar1.visible()
//                mHandler.postDelayed(run, speed.toLong()) //开始缓冲
//                onPageStart()
//            }
//
//            override fun onPageFinished(view: WebView, url: String) {
//                super.onPageFinished(view, url)
//                max = 100
//                mHandler.postDelayed(run, speed.toLong()) //开始缓冲,其实已经结束
//                setWebTitle(vb.webview.title)
//            }
//
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                L.w("跳转URL=$url")
//                val isOverride: Boolean = loadUrlFirst(url)
//                if (isOverride) {
//                    return true
//                }
//                if (url.startsWith("http:") || url.startsWith("https:")) {
//                    return super.shouldOverrideUrlLoading(view, url)
//                }
//                browse(url = url, newTask = true)
//                finish()
//                return true
//            }
//        }
//        //下载监听
//        vb.webview.setDownloadListener { url: String?, _: String?, _: String?, _: String?, contentLength: Long ->
//            L.w("文件大小=$contentLength")
//            url?.takeIf { it.isNotBlank() }?.run {
//                browse(url = this, newTask = true)
//                finish()
//            }
//        }
//        vb.webview.webChromeClient = object : WebChromeClient() {
//            override fun onReceivedTitle(view: WebView, title: String) {
//                super.onReceivedTitle(view, title)
//                setWebTitle(title)
//            }
//
//            override fun onProgressChanged(view: WebView, newProgress: Int) {
//                super.onProgressChanged(view, newProgress)
//                max = newProgress //获得当前最大进度
//                if (newProgress == 100) { //100的时候网页加载完成,一切还原
//                    speed = 10
//                } else {
//                    if (!vb.progressBar1.isShown) {
//                        vb.progressBar1.visible()
//                    }
//                }
//            }
//
//            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissionsCallback) {
//                super.onGeolocationPermissionsShowPrompt(origin, callback)
//                callback.invoke(origin, true, false)
//            }
//        }
//        vb.webview.setOnLongClickListener {
//            val hitTestResult = vb.webview.hitTestResult
//            when (hitTestResult.type) {
//                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
//                }
//
//                WebView.HitTestResult.IMAGE_TYPE -> {
//                    hitTestResult.extra
//                }
//            }
//            false
//        }
        WebView.setWebContentsDebuggingEnabled(true)
    }

    private fun setWebTitle(title: String?) {
        title?.takeIf { it.isNotBlank() }?.run {
            vb.headBanner.title = this
        }
    }

    /**
     * 若要拦截url请重写该方法 并返回true
     *
     * @param url
     * @return
     */
    fun loadUrlFirst(url: String): Boolean {
        if (TextUtils.isEmpty(url)) {
            return true
        }
        if (url.lowercase().startsWith("aijk://")) { //应用内部链接...
            if (TextUtils.isEmpty(url)) {
                return true
            }
            L.i("Action=$url")
            if (url.startsWith("http")) {
                openWeb(webTitle = "", webUrl = url)
                return true
            }
            return true
        }
        return false
    }


    /**
     * 加载内容
     *
     * @param url
     */
    private fun loadContent(url: String) {
        var ur = url
        if (TextUtils.isEmpty(ur)) {
            vb.state.empty()
            return
        }
        if (ur.startsWith("www.")) {
            ur = "https://$ur"
        }
        loadUrl(ur)
        vb.state.content()
    }

    private fun loadUrl(url: String) {
        val token = MK.decodeString(MKKeys.KEY_TOKEN)
        val uri = Uri.parse(url)
        syncCookie(uri.host, "access_token=$token")
        vb.webview.loadUrl(url)
    }

    private fun loadData() {
        vb.webview.loadDataWithBaseURL(null, mContent, "text/html", "utf-8", null)
    }

    private fun syncCookie(url: String?, cookie: String?) {
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        manager.setCookie(url, cookie)
        manager.flush()
    }

    override fun onDestroy() {
        vb.webview.stopLoading()
        val view = vb.webview.parent as ViewGroup
        view.removeView(vb.webview)
        vb.webview.destroy()
        super.onDestroy()
    }

    /* 改写物理按键返回的逻辑 */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && vb.webview.canGoBack()) {
            // 返回上一页面
            vb.webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            vb.webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun backPressed() {
        if (vb.webview.canGoBack() && !backCanFinish) {
            vb.webview.goBack()
        } else {
            super.backPressed()
        }
    }
}