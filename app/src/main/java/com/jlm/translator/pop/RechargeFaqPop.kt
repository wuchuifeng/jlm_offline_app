package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import com.jlm.translator.R
import razerdp.basepopup.BasePopupWindow

class RechargeFaqPop(context: Context) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_recharge_faq)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_dismiss)
        popupGravity = Gravity.CENTER
    }
}