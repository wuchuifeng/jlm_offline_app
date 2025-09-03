package cn.chawloo.base.ext

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * 扩展函数集合类
 * @author Create by 鲁超 on 2020/11/20 0020 15:51
 */
fun Int.outOf(range: IntRange): Int {
    return if (this in range) this else 0
}

/**
 * 隐藏软键盘(只适用于Activity，不适用于Fragment)
 */
fun Activity.hideSoftKeyboard() {
    val view = currentFocus
    if (view != null) {
        val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

fun Fragment.hideSoftKeyboard() {
    activity?.run {
        val view = currentFocus
        if (view != null) {
            val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}

fun String?.insertClipboard(context: Context, title: String = "标题已复制") {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, this))
    toast(title)
}
