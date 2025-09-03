package cn.chawloo.base.delegate

import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.topActivity
import cn.chawloo.base.model.IUpdateContract
import cn.chawloo.base.pop.UpdateAppPop

/**
 * TODO
 * @author Create by 鲁超 on 2023/6/30 16:08
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
object UpdateDelegate : IUpdate {
    override fun update(appConfig: IUpdateContract, isShowNewest: Boolean) {
        if (appConfig.needUpdate) {
            appConfig.url?.takeIf { it.isNotBlank() }?.run {
                UpdateAppPop(topActivity, appConfig).show()
            } ?: toast("下载地址为空")
        } else {
            if (isShowNewest) toast("当前为最新版本")
        }
    }
}

interface IUpdate {
    fun update(appConfig: IUpdateContract, isShowNewest: Boolean = true)
}