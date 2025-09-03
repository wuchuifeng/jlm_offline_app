package com.jlm.common.compat

import android.content.Context
import android.content.res.Configuration
import cn.chawloo.base.ext.setFontScale

/**
 * TODO
 * @author Create by 鲁超 on 2021/2/18 0018 9:54
 */
abstract class BaseComposeActCompat : BaseActCompat() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        setFontScale()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setFontScale()
    }
}
