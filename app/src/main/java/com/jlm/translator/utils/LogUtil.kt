package com.jlm.translator.utils

import android.os.Build
import android.os.Debug
import android.util.DebugUtils
import com.jlm.translator.BuildConfig
import com.safframework.log.L
import java.io.File

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/12 09:42
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
object LogUtil {
    private var isDebug = BuildConfig.DEBUG

    fun d(msg: String, forceOutput: Boolean = false) {
        if (forceOutput || isDebug) {
            L.d(msg)
        }
    }

    fun d(tag: String, msg: String, forceOutput: Boolean = false) {
        if (forceOutput || isDebug) {
            L.d(tag, msg)
        }
    }

    fun e(msg: String, forceOutput: Boolean = false) {
        if (forceOutput || isDebug) {
            L.e(msg)
        }
    }

    fun e(tag: String, msg: String, forceOutput: Boolean = false) {
        if (forceOutput || isDebug) {
            L.e(tag, msg)
        }
    }

}