package cn.chawloo.base.ext

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import cn.chawloo.base.R
import com.drake.brv.PageRefreshLayout
import com.drake.statelayout.StateLayout

/**
 * TODO
 * @author Create by 鲁超 on 2022/6/20 20:15
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
fun StateLayout?.loading(msg: String = "请稍等") {
    this?.showLoading(msg)
}

fun PageRefreshLayout?.loading(msg: String = "请稍等") {
    this?.showLoading(msg)
}

fun StateLayout?.content() {
    this?.showContent()
}

fun PageRefreshLayout?.content() {
    this?.showContent()
}

fun StateLayout?.empty(emptyMsg: String = "暂无数据", @DrawableRes iconId: Int? = null) {
    this?.onEmpty {
        iconId?.run {
            findViewById<ImageView>(R.id.iv_empty_icon).setImageResource(iconId)
        }
        findViewById<TextView>(R.id.tv_empty_msg).text = emptyMsg
    }?.showEmpty()
}

fun PageRefreshLayout?.empty(msg: String = "暂无数据", @DrawableRes iconId: Int? = null) {
    this?.onEmpty {
        iconId?.run {
            findViewById<ImageView>(R.id.iv_empty_icon).setImageResource(iconId)
        }
        findViewById<TextView>(R.id.tv_empty_msg).text = msg
    }?.showEmpty()
}

fun StateLayout?.error(msg: String = "发生错误", retryMsg: String = "重新加载", @DrawableRes iconId: Int? = null, retry: (() -> Unit)? = null) {
    this?.onError {
        iconId?.run {
            findViewById<ImageView>(R.id.iv_error_icon).setImageResource(iconId)
        }
        findViewById<TextView>(R.id.tv_error_msg).text = msg
        findViewById<TextView>(R.id.btn_retry).apply {
            text = retryMsg
            doClick {
                retry?.invoke()
            }
            visible()
        }
    }?.showError()
}

fun StateLayout?.error(throwable: Throwable?, retryMsg: String = "重新加载", @DrawableRes iconId: Int? = null, retry: (() -> Unit)? = null) {
    this?.onError {
        iconId?.run {
            findViewById<ImageView>(R.id.iv_error_icon).setImageResource(iconId)
        }
        findViewById<TextView>(R.id.tv_error_msg).text = throwable?.message?.ifBlank { "发生错误" } ?: "发生错误"
        findViewById<TextView>(R.id.btn_retry).apply {
            text = retryMsg
            doClick {
                retry?.invoke()
            }
            visible()
        }
    }?.showError()
}

fun PageRefreshLayout?.error(throwable: Throwable?, retryMsg: String = "重新加载", @DrawableRes iconId: Int? = null, retry: (() -> Unit)? = null) {
    this?.onError {
        iconId?.run {
            findViewById<ImageView>(R.id.iv_error_icon).setImageResource(iconId)
        }
        findViewById<TextView>(R.id.tv_error_msg).text = throwable?.message?.ifBlank { "发生错误" } ?: "发生错误"
        findViewById<TextView>(R.id.btn_retry).apply {
            text = retryMsg
            doClick {
                retry?.invoke()
            }
            visible()
        }
    }?.showError()
}
