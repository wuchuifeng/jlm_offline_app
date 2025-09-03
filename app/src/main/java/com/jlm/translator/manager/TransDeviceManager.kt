package com.jlm.translator.manager

import android.content.Context
import android.os.Build
import android.provider.Settings
import cn.chawloo.base.ext.toast
import cn.chawloo.base.utils.DeviceUtils
import com.drake.net.utils.scopeNet
import com.jlm.translator.entity.requestDto.UpdateDeviceTimeDto
import com.jlm.translator.entity.responseModel.DeviceInfoModel
import com.jlm.translator.entity.responseModel.DeviceWhitelistModel
import com.jlm.translator.net.HttpRequest
import com.jlm.translator.utils.DeviceUtil
import com.safframework.log.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * 设备管理类，包括设备信息，计时等功能，单例类
 * */
class TransDeviceManager private constructor() {
    private var isTimer = false //是否在计时
    private var secondsCount = 0 //计时秒数
    private var maxTimerCount = 60 //最大计时秒数,到了这个数值就会发送请求，然后清零
    private var minTimerCount = 8 //最小计时秒数，当计时秒数小于这个数值时，不会发送请求

    private var totalFreeTime = 7000 //默认最大免费时间， 秒

    private var jobTimer: Job? = null

    private var deviceInfoModel: DeviceInfoModel? = null //设备信息模型

    private var curFuncModeEnum: IntelligentFuncModeEnum? = IntelligentFuncModeEnum.MODE_TONGCHUAN

    private lateinit var mContext: Context
    private lateinit var versionEnum: IntelligentVersionEnum

    companion object {
        private const val TAG = "TransDeviceManager"
        
        @Volatile
        private var instance: TransDeviceManager? = null
        
        @Synchronized
        fun getInstance(): TransDeviceManager {
            return instance ?: synchronized(this) {
                instance ?: TransDeviceManager().also { instance = it }
            }
        }
    }

    fun initManager(context: Context, versionEnum: IntelligentVersionEnum) {
        mContext = context.applicationContext // 使用applicationContext避免内存泄漏
        this.versionEnum = versionEnum
    }

    fun getDeviceInfoModel(): DeviceInfoModel? {
        return deviceInfoModel
    }

    /**
     * 开启计时，当语音识别开启时
     * */
    fun startFreeTimer(funcModeEnum: IntelligentFuncModeEnum) {
        curFuncModeEnum = funcModeEnum
        //使用协程
        if (jobTimer == null || jobTimer?.isCancelled == true) {
            isTimer = true
            secondsCount = 0 // 重置计数
            
            jobTimer = CoroutineScope(Dispatchers.IO).launch {
                isTimer = true
                while (isTimer) {
//                    if (totalFreeTime <= 0) {
//                        toast("免费时长已用完，请联系供应商")
//                        isTimer = false
//                        jobTimer?.cancel()
//                        return@launch
//                    }
                    delay(1000)
                    secondsCount++
                    totalFreeTime-- // 每秒减少免费时间
                    
                    if (secondsCount >= maxTimerCount) {
                        //当秒数达到最大值，发送请求
                        uploadTime(secondsCount)
                        secondsCount = 0
                    }
                }
            }
        }
    }

    fun stopFreeTimer() {
        isTimer = false
        jobTimer?.cancel()
        //根据时间更新数据
        if (secondsCount > 0) {
            uploadTime(secondsCount)
            secondsCount = 0
        }
    }

    private fun uploadTime(times: Int) {
        if (times <= minTimerCount) return
        
        var timeCounts = times

        scopeNet {
            // 更新设备使用时间
            val deviceId = deviceInfoModel?.deviceId ?: DeviceUtil.getDeviceID(mContext)
            if (deviceId.isNotEmpty()) {
                val updateModel = UpdateDeviceTimeDto(deviceId, timeCounts, versionEnum.code, curFuncModeEnum?.code ?: 0)
                HttpRequest.requestUpdateDeviceFreeTime(updateModel) {
                    totalFreeTime = this.freeTime
                    deviceInfoModel?.free_time = this.freeTime
                    if (this.freeTime <= 0) {
                        toast("VIP时长已不足，请联系服务商")
                    }
                }
                L.d(TAG, "Upload time: $timeCounts seconds")
            }
        }.catch { e ->
            L.e(TAG, "Upload time failed: ${e.message}")
        }
    }

    fun getDeviceInfo(): String {
        //获取设备信息
        val deviceId = Settings.Secure.getString(mContext.contentResolver, Settings.Secure.ANDROID_ID)
        L.d(TAG, "设备信息: ${Build.MODEL} ${deviceId} ${Build.VERSION.SDK_INT} ${Build.SERIAL} ${Build.BRAND} ${Build.MANUFACTURER} ${Build.DEVICE} ${Build.PRODUCT} ${Build.BOARD} ${Build.BOOTLOADER} ${Build.HOST} ${Build.ID}")

        return deviceId
    }

    /**
     * 请求设备信息
     * */
    fun requestDeviceInfoModel(success: (DeviceInfoModel) -> Unit,) {
        val deviceId = DeviceUtil.getDeviceID(mContext)
        val name = DeviceUtil.getDeviceName()
        //获取设备信息
        HttpRequest.requestDeviceInfo(deviceId, name) {
            deviceInfoModel = this
            totalFreeTime = this.free_time
            success(this)
        }
    }

    /**
     * 请求创建设备信息
     * */
    fun requestCreateDeviceInfo() {
        val deviceId = DeviceUtil.getDeviceID(mContext)
        val name = DeviceUtil.getDeviceName()
        HttpRequest.requestCreateDevice(deviceId, name) { 
            L.d(TAG, "Device created successfully")
        }
    }
    
    /**
     * 获取剩余免费时长
     * */
    fun getRemainingFreeTime(): Int {
        return totalFreeTime
    }
    
    /**
     * 是否还有免费时长
     * */
    fun hasFreeTime(): Boolean {
        return totalFreeTime > 0
    }
}