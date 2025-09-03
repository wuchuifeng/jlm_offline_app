package com.jlm.translator.act

import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.ext.appVersionCode
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.goHome
import com.jlm.common.router.isLogin
import com.jlm.translator.databinding.ActSplashnewActBinding
import com.safframework.log.L
import com.therouter.router.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = Rt.SplashNewAct)
class SplashNewAct : BaseActCompat() {
    private val vb by binding<ActSplashnewActBinding>()

    override fun initialize() {
        super.initialize()
        immersionBar {
            transparentStatusBar()
            fitsSystemWindows(false)
        }
        vb.tvNotice.text = "AI智能同声翻译"
        lifecycleScope.launch {
            delay(1000)
            jump()
        }
    }

    private fun jump() {
        //默认跳转到主页面
        goHome()
//        val versionCode = MK.decodeInt(MKKeys.KEY_VERSION_CODE)
//        if (appVersionCode - versionCode > 50) {
//            MK.encode(MKKeys.KEY_VERSION_CODE, appVersionCode)
//            L.e("版本相差过大需要清空用户登录信息，重新登录")
//            MK.removeKeys(MKKeys.KEY_USER, MKKeys.KEY_TOKEN)
//        }
//        if (isLogin(true)) {
//            goHome()
//        } else {
//            finish()
//        }
    }
}