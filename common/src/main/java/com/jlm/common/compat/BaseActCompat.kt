package com.jlm.common.compat

import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.chawloo.base.base.BaseAct
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.common.entity.UserModel
import com.jlm.common.net.ForceOfflineReceive

abstract class BaseActCompat : BaseAct() {
    private var lostTokenReceive: ForceOfflineReceive? = null

    val userModel: UserModel?
        get() = MK.decodeParcelable(MKKeys.KEY_USER, UserModel::class.java)

    override fun initialize() {
        onClick()
    }

    override fun onResume() {
        super.onResume()
        if (MK.decodeString(MKKeys.KEY_TOKEN).isNotBlank()) {
            val intentFilter = IntentFilter()
            intentFilter.addAction("$packageName.FORCE_OFFLINE")
            lostTokenReceive = ForceOfflineReceive().apply {
                LocalBroadcastManager.getInstance(this@BaseActCompat).registerReceiver(this, intentFilter)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lostTokenReceive?.run {
            LocalBroadcastManager.getInstance(this@BaseActCompat).unregisterReceiver(this)
            lostTokenReceive = null
        }
    }
}