package com.jlm.common.net

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.chawloo.base.ext.toast
import com.jlm.common.router.logout

/**
 * TODO
 * @author Create by 鲁超 on 2021/03/16/0016 20:28
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
class ForceOfflineReceive : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        toast("用户信息过期，请重新登录")
        logout()
    }
}