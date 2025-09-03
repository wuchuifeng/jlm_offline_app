package cn.chawloo.base.ext

import androidx.annotation.StringRes
import com.hjq.toast.Toaster

/**
 * TODO
 * @author Create by 鲁超 on 2022/8/22 14:25
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

fun toast(message: CharSequence?) = message?.takeIf { it.isNotBlank() }?.let { Toaster.show(it) }
fun toast(message: String?) = message?.takeIf { it.isNotBlank() }?.let { Toaster.show(it) }
fun toast(@StringRes message: Int) = Toaster.show(message)
fun toast(t: Throwable?) = t?.message?.takeIf { it.isNotBlank() }?.let { Toaster.show(it) }