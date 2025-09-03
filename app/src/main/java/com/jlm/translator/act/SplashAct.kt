package com.jlm.translator.act

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import cn.chawloo.base.ext.appVersionCode
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.pop.PrivacyPolicyPop
import com.jlm.common.router.Rt
import com.jlm.common.router.goHome
import com.jlm.common.router.isLogin
import com.jlm.translator.startup.ThirdSdkFlowTask
import com.safframework.log.L
import com.therouter.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Route(path = Rt.SplashAct)
class SplashAct : BaseActCompat() {
    private lateinit var mCoroutineScope: CoroutineScope
    private var mKeepOnAtomicBool = AtomicBoolean(true)

    /*最终的方法是否调用*/
    private val dialogShowInvoke = AtomicBoolean(false)

    private val shouldAvoidSplashScreen = Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2
    private var splashScreen: SplashScreen? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldAvoidSplashScreen)
            setTheme(com.jlm.common.R.style.Theme_AppTheme)
        else
            splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (shouldAvoidSplashScreen.not()) {
            splashScreen?.setKeepOnScreenCondition { mKeepOnAtomicBool.get() }
            splashScreen?.setOnExitAnimationListener { startExitSplash(it) }
            // 创建 CoroutineScope （用于管理CoroutineScope中的所有协程）
            mCoroutineScope = CoroutineScope(Job() + Dispatchers.Main)
            mCoroutineScope.launch(Dispatchers.IO) {
                mKeepOnAtomicBool.compareAndSet(true, false)
                delay(100)
                if (!dialogShowInvoke.get()) {
                    launch(Dispatchers.Main) {
                        startExitSplash(null)
                    }
                }
            }
        } else {
            startExitSplash(null)
        }
    }

    @Synchronized
    private fun startExitSplash(splashScreenViewProvider: SplashScreenViewProvider?) {
        if (dialogShowInvoke.get()) {
            return
        }
        dialogShowInvoke.compareAndSet(false, true)
        PrivacyPolicyPop.show(this) {
            MK.encode(MKKeys.KEY_POLICY, false)
            splashScreenViewProvider?.view?.run {
                val splashAlphaAnim = ObjectAnimator.ofFloat(this, View.ALPHA, 1F, 0F)
                splashAlphaAnim.duration = 500
                splashAlphaAnim.interpolator = FastOutLinearInInterpolator()
                splashAlphaAnim.start()
            }
            jump(it)
        }
    }

    private fun jump(isAgree: Boolean) {
        ThirdSdkFlowTask.startUp()
        val versionCode = MK.decodeInt(MKKeys.KEY_VERSION_CODE)
        if (appVersionCode - versionCode > 50) {
            MK.encode(MKKeys.KEY_VERSION_CODE, appVersionCode)
            L.e("版本相差过大需要清空用户登录信息，重新登录")
            MK.removeKeys(MKKeys.KEY_USER, MKKeys.KEY_TOKEN)
        }
        setTheme(com.jlm.common.R.style.Theme_AppTheme)
        if (isLogin(true)) {
            goHome()
        }
    }
}