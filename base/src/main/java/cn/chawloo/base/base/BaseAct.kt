package cn.chawloo.base.base

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import cn.chawloo.base.R
import cn.chawloo.base.delegate.IUpdate
import cn.chawloo.base.delegate.UpdateDelegate
import cn.chawloo.base.utils.DeviceUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.therouter.TheRouter
import me.jessyan.autosize.AutoSizeCompat

const val BUNDLE_NAME = "bundle_name"

abstract class BaseAct : AppCompatActivity(), IBaseView, IUpdate by UpdateDelegate {
    private var mStateSaved = false
    private lateinit var onBackInvokedCallback: OnBackInvokedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBaseCreate()
        TheRouter.inject(this)
        if (DeviceUtils.isLatestT()) {
            onBackInvokedCallback = OnBackInvokedCallback { backPressed() }.also {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, it)
            }
        } else {
            onBackPressedDispatcher.addCallback(owner = this) {
                backPressed()
            }
        }
        mStateSaved = false
        initialize()
    }

    open fun onClick() {}

    override fun getResources(): Resources {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources()) //如果没有自定义需求用这个方法
        }
        return super.getResources()
    }

    private fun onBaseCreate() {
        immersionBar {
            transparentStatusBar()
            statusBarColor(R.color.bg_color)
            navigationBarColor(android.R.color.white)
            navigationBarDarkIcon(true)
            statusBarDarkFont(true)
        }
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            return
        }
        super.setRequestedOrientation(requestedOrientation)
    }

    /**
     * 是否是透明的Activity
     *
     * @return
     */
    private fun isTranslucentOrFloating(): Boolean {
        var isTranslucentOrFloating = false
        try {
            val styleableRes = Class.forName("com.android.internal.R\$styleable").getField("Window")[null] as IntArray
            val ta = obtainStyledAttributes(styleableRes)
            val m = ActivityInfo::class.java.getMethod("isTranslucentOrFloating", TypedArray::class.java)
            m.isAccessible = true
            isTranslucentOrFloating = m.invoke(null, ta) as Boolean
            m.isAccessible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isTranslucentOrFloating
    }


    override fun onResume() {
        super.onResume()
        mStateSaved = false
    }

    val activity: BaseAct get() = this

    open fun backPressed() {
        if (DeviceUtils.isLatest34()) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.anim_activity_slide_left_in, R.anim.anim_activity_slide_right_out)
        } else {
            overridePendingTransition(R.anim.anim_activity_slide_left_in, R.anim.anim_activity_slide_right_out)
        }
        finish()
    }

    override fun onStart() {
        super.onStart()
        mStateSaved = false
    }

    override fun onStop() {
        super.onStop()
        mStateSaved = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (DeviceUtils.isLatestT()) {
            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCallback)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (!mStateSaved) {
            super.onKeyDown(keyCode, event)
        } else {
            true
        }
    }


    protected abstract fun initialize()

    /**
     * 登录成功后的每个View的统一回调接口
     */
    override fun loginOk() {}
}