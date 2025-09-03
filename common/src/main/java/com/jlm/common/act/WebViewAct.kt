package com.jlm.common.act

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.ViewGroup
import cn.chawloo.base.ext.browse
import cn.chawloo.base.ext.bundleExtra
import cn.chawloo.base.ext.content
import cn.chawloo.base.ext.empty
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.loading
import cn.chawloo.base.ext.string
import cn.chawloo.base.ext.visible
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.databinding.ActWebviewBinding
import com.jlm.common.router.Rt
import com.jlm.common.router.openWeb
import com.safframework.log.L
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback
import com.tencent.smtt.sdk.CookieManager
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.therouter.router.Route

/**
 * 内置浏览器
 * @author Create by 鲁超 on 2021/2/10 0010 13:53
 */
@Route(path = Rt.WebViewAct)
class WebViewAct : BaseActCompat() {
    companion object {
        const val WEB_TITLE = "web_title"
        const val WEB_URL = "web_url"
        const val WEB_HTML = "web_html"
    }

    private val vb by binding<ActWebviewBinding>()
    private lateinit var bundle: Bundle

    /**
     * 标题,如果为空,则默认使用网页内容的标题
     */
    private var mTitle: String? = null

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
        vb.state.loading()
        initUi()
        loadContent(handleIntent())
        immersionBar {
            transparentStatusBar()
            statusBarColor(android.R.color.transparent)
            navigationBarColor(android.R.color.transparent)
            navigationBarDarkIcon(true)
            autoDarkModeEnable(true)
            fitsSystemWindows(true)
        }
    }

    private fun handleIntent(): String {
        var url: String
        bundle.run {
            mTitle = string(WEB_TITLE)
            url = string(WEB_URL)
        }
        return url
    }

    private fun initUi() {
        if (mTitle.isNullOrBlank()) {
            vb.headBanner.title = ""
        } else {
            vb.headBanner.title = mTitle
        }
        vb.progressBar1.max = 100
        vb.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                vb.progressBar1.visible()
                mHandler.postDelayed(run, speed.toLong()) //开始缓冲
                onPageStart()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                max = 100
                mHandler.postDelayed(run, speed.toLong()) //开始缓冲,其实已经结束
                setWebTitle(vb.webview.title)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                L.w("跳转URL=$url")
                val isOverride: Boolean = loadUrlFirst(url)
                if (isOverride) {
                    return true
                }
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return super.shouldOverrideUrlLoading(view, url)
                }
                browse(url = url, newTask = true)
                finish()
                return true
            }
        }
        //下载监听
        vb.webview.setDownloadListener { url: String?, _: String?, _: String?, _: String?, contentLength: Long ->
            L.w("文件大小=$contentLength")
            url?.takeIf { it.isNotBlank() }?.run {
                browse(url = this, newTask = true)
                finish()
            }
        }
        vb.webview.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                setWebTitle(title)
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                max = newProgress //获得当前最大进度
                if (newProgress == 100) { //100的时候网页加载完成,一切还原
                    speed = 10
                } else {
                    if (!vb.progressBar1.isShown) {
                        vb.progressBar1.visible()
                    }
                }
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissionsCallback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                callback.invoke(origin, true, false)
            }
        }
        vb.webview.setOnLongClickListener {
            val hitTestResult = vb.webview.hitTestResult
            when (hitTestResult.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                }

                WebView.HitTestResult.IMAGE_TYPE -> {
                    hitTestResult.extra
                }
            }
            false
        }
        WebView.setWebContentsDebuggingEnabled(true)
    }

    private val mHandler = Handler(Looper.getMainLooper()) {
        count = 0
        speed = 30
        vb.progressBar1.progress = 0
        vb.progressBar1.gone()
        vb.progressBar1.alpha = 1.0f
        remove()
        false
    }

    private fun remove() {
        mHandler.removeCallbacks(run)
    }

    private val run: Runnable = object : Runnable {
        override fun run() {
            if (count == max && count == 100) { //此时网页加载完成。
                if (!vb.webview.canGoBack()) { //说明已经退回到顶级页面了
                    loadDone()
                }
                if (objectAnimator == null) {
                    objectAnimator = ObjectAnimator.ofFloat(vb.progressBar1, "alpha", 1.0f, 0.0f).setDuration(500)
                    objectAnimator?.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mHandler.sendEmptyMessage(0)
                        }
                    })
                }
                if (objectAnimator?.isRunning == false) {
                    objectAnimator?.start()
                }
                return
            }
            if (count == max) { //如果已经缓冲到最大进度条,则停止
            } else { //进行缓冲
                if (count > 100) {
                    count = 100
                    max = 100
                } else {
                    count++
                }
                vb.progressBar1.progress = count
                mHandler.postDelayed(this, speed.toLong())
            }
        }
    }

    fun onPageStart() {}

    fun loadDone() {}

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