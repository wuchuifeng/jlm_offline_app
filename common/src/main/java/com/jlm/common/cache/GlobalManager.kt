package com.jlm.common.cache

import androidx.fragment.app.FragmentActivity
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.BaseResult
import cn.chawloo.base.net.customCatch
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.scopeNetLife
import com.jlm.common.entity.AppConfig

object GlobalManager {
    fun FragmentActivity.getAppConfig(showToast: Boolean = true, success: AppConfig.() -> Unit) {
        if (showToast) {
            toast("检查更新...")
            scopeDialog {
                val result = Post<BaseResult<AppConfig?>>("Client/AppInfos/List").await()
                result.data?.success()
            }.customCatch()
        } else {
            scopeNetLife {
                val result = Post<BaseResult<AppConfig?>>("Client/AppInfos/List").await()
                result.data?.success()
            }.customCatch {
            }
        }
    }
}