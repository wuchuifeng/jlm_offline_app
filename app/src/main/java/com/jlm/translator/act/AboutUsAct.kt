package com.jlm.translator.act

import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import cn.chawloo.base.ext.appVersionCode
import cn.chawloo.base.ext.appVersionName
import cn.chawloo.base.ext.appendClickable
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.if2Visible
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.cache.GlobalManager.getAppConfig
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.net.NetConstants
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.common.router.openWeb
import com.jlm.translator.databinding.ActAboutUsBinding
import com.jlm.translator.net.HttpRequest
import com.therouter.router.Route

@Route(path = Rt.AboutUsAct)
class AboutUsAct : BaseActCompat() {
    private val vb by binding<ActAboutUsBinding>()
    private var hasNew = false

    override fun initialize() {
        super.initialize()
        immersionBar {
            transparentStatusBar()
            fitsSystemWindows(false)
        }
        vb.tvVersion.text = "v${appVersionName}(${appVersionCode})"
        vb.tvPolicy.text = buildSpannedString {
            appendClickable("《用户协议》", ContextCompat.getColor(this@AboutUsAct, cn.chawloo.base.R.color.theme_color)) {}
            append("和")
            appendClickable("《隐私政策》", ContextCompat.getColor(this@AboutUsAct, cn.chawloo.base.R.color.theme_color)) {}
        }
    }

    override fun onClick() {
        super.onClick()
        vb.clUpdate.doClick {
            getAppConfig(showToast = true) {
                hasNew = this.needUpdate
                vb.ivNew.if2Visible(hasNew)
                update(this, true)
            }
        }
        vb.tvIntro.doClick {
            goto(Rt.CompanyIntroAct)
        }
        vb.tvVersion.doClick {
            // api请求
            HttpRequest.requestAppUpdate{
                update(this, false)
            }
//            getAppConfig(showToast = true) {
//                hasNew = this.needUpdate
//                vb.ivNew.if2Visible(hasNew)
//                update(this, true)
//            }
        }
        vb.tvOfficialWebsite.doClick {
            openWeb("极力米", NetConstants.OFFICIAL_WEBSITE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasNew) {
            getAppConfig(showToast = false) {
                hasNew = this.needUpdate
                vb.ivNew.if2Visible(hasNew)
            }
        }
    }
}