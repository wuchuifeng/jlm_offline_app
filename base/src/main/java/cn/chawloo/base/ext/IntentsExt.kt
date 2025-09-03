package cn.chawloo.base.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import cn.chawloo.base.base.BUNDLE_NAME
import cn.chawloo.base.utils.DeviceUtils

/**
 * TODO
 * @author Create by 鲁超 on 2023/7/13 16:05
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
inline fun <reified T> Context.intentOf(vararg pairs: Pair<String, *>): Intent = intentOf<T>(bundleOf(*pairs))

inline fun <reified T> Context.intentOf(bundle: Bundle): Intent =
    Intent(this, T::class.java).putExtras(bundle)

fun dial(phoneNumber: String): Boolean =
    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
        .startForActivity()

fun makeCall(phoneNumber: String): Boolean =
    Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
        .startForActivity()

fun sendSMS(phoneNumber: String, content: String): Boolean =
    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(phoneNumber)}"))
        .putExtra("sms_body", content)
        .startForActivity()

fun browse(url: String, newTask: Boolean = false): Boolean =
    Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .apply { if (newTask) newTask() }
        .startForActivity()

fun email(email: String, subject: String? = null, text: String? = null): Boolean =
    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        .putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_TEXT, text)
        .startForActivity()

fun installAPK(uri: Uri): Boolean =
    Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/vnd.android.package-archive")
        .newTask()
        .grantReadUriPermission()
        .startForActivity()

fun Intent.startForActivity(): Boolean =
    try {
        topActivity.startActivity(this)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

fun Intent.clearTask(): Intent = addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

fun Intent.clearTop(): Intent = addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

fun Intent.newDocument(): Intent = addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)

fun Intent.excludeFromRecents(): Intent = addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

fun Intent.multipleTask(): Intent = addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

fun Intent.newTask(): Intent = addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

fun Intent.noAnimation(): Intent = addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

fun Intent.noHistory(): Intent = addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

fun Intent.singleTop(): Intent = addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

fun Intent.grantReadUriPermission(): Intent = apply {
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun Intent?.bundleExtra(key: String = BUNDLE_NAME): Bundle? = this?.getBundleExtra(key)
fun Intent?.boolExtra(key: String, default: Boolean = false): Boolean = this?.getBooleanExtra(key, default) ?: default
fun Intent?.longExtra(key: String, default: Long = 0): Long = this?.getLongExtra(key, default) ?: default
fun Intent?.intExtra(key: String, default: Int = 0): Int = this?.getIntExtra(key, default) ?: default
fun Intent?.stringExtra(key: String, default: String = ""): String = this?.getStringExtra(key) ?: default

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent?.parcelableExtra(key: String): T? {
    return if (DeviceUtils.isLatestT()) {
        this?.getParcelableExtra(key, T::class.java)
    } else {
        this?.getParcelableExtra(key)
    }
}