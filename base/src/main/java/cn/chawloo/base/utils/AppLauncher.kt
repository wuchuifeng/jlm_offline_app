package cn.chawloo.base.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.topActivity

/**
 * App启动相关
 * @author Create by 鲁超 on 2021/3/11 0011 14:10
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
object AppLauncher {

    fun isFirstRun(): Boolean {
        return MK.decodeBool(MKKeys.KEY_IS_FIRST_RUN, true)
    }

    /**
     * 启动当前应用设置页面
     */
    fun Context.startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    fun Context.gotoPermissionSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 重启Activity
     * 此方法会比 recreate() 效果更好
     */
    @Deprecated(message = "该方法会导致startActivityForResult无法正确返回了，如果用recreate()，则会闪屏，且无法删除输入框的内容")
    fun Activity.reload() {
        if (DeviceUtils.isLatest34()) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        if (DeviceUtils.isLatest34()) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
        startActivity(intent)
        System.gc()
        recreate()
    }

    fun gotoMarket() {
        val sb = StringBuilder("market://details?id=")
        val uri = Uri.parse(sb.append(topActivity.applicationContext.packageName).toString())
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val localList = topActivity.applicationContext.packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)
        localList.takeIf { it.isNotEmpty() }?.run {
            try {
                topActivity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                toast("跳转应用市场失败")
            }
        } ?: run {
            toast("未找到手机应用市场")
        }
    }
}