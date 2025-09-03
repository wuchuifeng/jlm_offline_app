package com.jlm.translator.act

import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.databinding.ActContactUsBinding
import com.therouter.router.Route

/***联系我们*/
@Route(path = Rt.ContactUsAct)
class ContactUsAct : BaseActCompat() {
    private val vb by binding<ActContactUsBinding>()
    override fun initialize() {
        super.initialize()
        immersionBar {
            statusBarColor(android.R.color.white)
        }
        vb.tvEmailTitle.text="写邮件"
    }

    override fun onClick() {

    }
}