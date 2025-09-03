package com.jlm.translator.utils

import android.content.Context
import android.os.Build
import android.provider.Settings

object DeviceUtil {

    fun getDeviceName(): String {
        return Build.MODEL
    }

    fun getDeviceID(context: Context): String {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return deviceId
    }
}