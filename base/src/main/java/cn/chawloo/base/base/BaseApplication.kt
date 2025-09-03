package cn.chawloo.base.base

import android.view.Gravity
import androidx.multidex.MultiDexApplication
import cn.chawloo.base.BuildConfig
import cn.chawloo.base.R
import cn.chawloo.base.ext.OnAppStatusChangedListener
import cn.chawloo.base.ext.activityCache
import cn.chawloo.base.ext.application
import cn.chawloo.base.ext.doOnActivityLifecycle
import cn.chawloo.base.interceptor.ToastInterceptor
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.drake.statelayout.StateConfig
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import com.safframework.log.L
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import me.jessyan.autosize.AutoSizeConfig

/**
 * Application基类
 * @author Create by 鲁超 on 2020/10/9 0009 10:03
 */
open class BaseApplication : MultiDexApplication() {
    private var started = 0

    companion object {
        internal var onAppStatusChangedListener: OnAppStatusChangedListener? = null
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        application.doOnActivityLifecycle(
            onActivityCreated = { activity, _ ->
                activityCache.add(activity)
            },
            onActivityStarted = { activity ->
                started++
                if (started == 1) {
                    onAppStatusChangedListener?.onForeground(activity)
                }
            },
            onActivityStopped = { activity ->
                started--
                if (started == 0) {
                    onAppStatusChangedListener?.onBackground(activity)
                }
            },
            onActivityDestroyed = { activity ->
                activityCache.remove(activity)
            }
        )
        //Bugly crash日志捕捉
        CrashReport.initCrashReport(applicationContext, "536461501f", false)

        L.init("JLM_Translator")
        L.displayThreadInfo(false)

        MMKV.initialize(this)
        MMKV.mmkvWithID("JLM", MMKV.MULTI_PROCESS_MODE)

        val textScale = if (MK.decodeBool(MKKeys.KEY_LARGE_TEXT, false)) 1.2f else 1.0F
        AutoSizeConfig.getInstance()?.apply {
            //是否屏蔽系统字体大小对 AndroidAutoSize 的影响, 如果为 true, App 内的字体的大小将不会跟随系统设置中字体大小的改变
            //如果为 false, 则会跟随系统设置中字体大小的改变, 默认为 false
            isExcludeFontScale = true
            //区别于系统字体大小的放大比例, AndroidAutoSize 允许 APP 内部可以独立于系统字体大小之外，独自拥有全局调节 APP 字体大小的能力
            //当然, 在 APP 内您必须使用 sp 来作为字体的单位, 否则此功能无效, 不设置或将此值设为 0 则取消此功能
            privateFontScale = textScale
        }


        Toaster.init(this, BlackToastStyle())
        Toaster.setGravity(Gravity.BOTTOM, 0, 100)
        Toaster.setInterceptor(ToastInterceptor())

        StateConfig.apply {
            emptyLayout = R.layout.custom_empty_view
            loadingLayout = R.layout.custom_loading_view
            errorLayout = R.layout.custom_error_view
            setRetryIds(R.id.btn_retry)
        }

        SmartRefreshLayout.setDefaultRefreshHeaderCreator { _, _ -> MaterialHeader(this) }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { _, _ -> ClassicsFooter(this) }
    }
}