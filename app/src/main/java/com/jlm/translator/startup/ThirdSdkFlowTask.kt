package com.jlm.translator.startup

//import cn.jpush.android.api.JPushInterface
import android.content.Context
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.therouter.TheRouter
import com.therouter.app.flowtask.lifecycle.FlowTask
import com.therouter.flow.TheRouterFlowTask

/**
 * TODO
 * @author Create by 鲁超 on 2022/1/24 0024 13:46:29
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
object ThirdSdkFlowTask : TheRouterFlowTask {
    const val agreePrivacyCache = "agree_privacy_cache"
    const val crashReportInit = "crash_report_init"
    const val tbsInit = "tbs_init"
    const val wechatInit = "wechat_init"

    private var isInitialized = false
    fun startUp() {
        if (!isInitialized) {
            isInitialized = true
            TheRouter.runTask(agreePrivacyCache)
        }
    }
}

@FlowTask(taskName = ThirdSdkFlowTask.crashReportInit, dependsOn = ThirdSdkFlowTask.agreePrivacyCache, async = true)
fun crashReportInit(context: Context) {
}

@FlowTask(taskName = ThirdSdkFlowTask.tbsInit, dependsOn = ThirdSdkFlowTask.agreePrivacyCache, async = true)
fun tbsInit(context: Context) {
    QbSdk.initX5Environment(context, object : QbSdk.PreInitCallback {
        override fun onCoreInitFinished() {
            // 内核初始化完成，可能为系统内核，也可能为系统内核
        }

        /**
         * 预初始化结束
         * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
         * @param p0 是否使用X5内核
         */
        override fun onViewInitFinished(p0: Boolean) {
        }

    })
    val map = HashMap<String, Any>()
    map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
    map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
    QbSdk.initTbsSettings(map)
    QbSdk.setDownloadWithoutWifi(true)
}

@FlowTask(taskName = ThirdSdkFlowTask.wechatInit, dependsOn = ThirdSdkFlowTask.agreePrivacyCache, async = true)
fun wechatApiInit(context: Context) {
}