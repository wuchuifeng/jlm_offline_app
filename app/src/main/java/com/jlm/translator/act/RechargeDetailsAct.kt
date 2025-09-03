package com.jlm.translator.act

import android.annotation.SuppressLint
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.databinding.ActRechargeDetailsBinding
import com.therouter.router.Route

/***充值详情*/
@Route(path = Rt.RechargeDetailsAct)
class RechargeDetailsAct : BaseActCompat() {
    private val vb by binding<ActRechargeDetailsBinding>()

    @SuppressLint("SetTextI18n")
    override fun initialize() {
        super.initialize()
        immersionBar {
            statusBarColor(android.R.color.white)
        }
        vb.tvPrice.text = "+35"
    }
}