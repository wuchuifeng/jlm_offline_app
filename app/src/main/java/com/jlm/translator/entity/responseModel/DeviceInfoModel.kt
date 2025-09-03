package com.jlm.translator.entity.responseModel

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import androidx.annotation.DrawableRes
import cn.chawloo.base.ext.topActivity
import com.jlm.translator.R
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DeviceInfoModel(
    var _id: String = "",
    var uuid: String = "", //设备的UUID，目前空置
    var sin: Long = 0, //设备sin码，唯一标识
    var deviceId: String = "",
    var free_time: Int = 0, //剩余免费时长
    var deviceType: Int = 0, //设备类型
    var name: String = "",
    var alias: String = "", //设备别名
    var remark: String = "",
    var version: Int = VERSION_UNKNOWN, //1:普通版 2：专业版
    var status: Boolean = false, //在server上是否被禁用
    var buyer_phone: String = "", //购买人电话
    var buyer_info: String = "", //购买人信息
    var buyer_time: String = "", //购买时间
    val vip_level: String = "", //会员等级  普通 钻石 黄金
    var headphones_count: Int = 0, //耳机数量
    val modelInfo: DeviceModel? = null, //设备型号信息
    val agencyInfo: DeviceAgency? = null, //设备代理商信息
) : Parcelable {

    companion object {
        const val VERSION_NORMAL = 1
        const val VERSION_PROFESSIONAL = 2
        const val VERSION_UNKNOWN = -1 //未知

        const val TYPE_TEST = 1 //测试设备
        const val TYPE_PRO = 2 //专业设备
    }

    /**
     * 获取剩余免费时长 不足一小时显示分钟数,不足一分钟显示不足一分钟
     */
    fun getFreeTime(): String {
        return when {
            free_time >= 3600 -> {
                val hours = free_time / 3600
                val minutes = (free_time % 3600) / 60
                topActivity.getString(R.string.free_time_hours, hours, minutes)
            }

            free_time >= 60 -> {
                val minutes = free_time / 60
                topActivity.getString(R.string.free_time_minutes, minutes)
            }

            free_time > 0 && free_time < 60 -> {
                topActivity.getString(R.string.clear_vip_duration_less_than_1_minute)
            }

            else -> topActivity.getString(R.string.vip_duration_0_minute)
        }
    }

    fun getStatusName(): String {
        return when {
            status -> "已激活"
            else -> "被禁用"
        }
    }

    fun getDeviceTypeName(): String {
        return when (deviceType) {
            TYPE_TEST -> "测试设备"
            TYPE_PRO -> "专业设备"
            else -> "未知设备"
        }
    }

    fun getEarphoneCountInfo(): String {
        return when (headphones_count) {
            0 -> "无"
            else -> "${headphones_count}个"
        }
    }

    /**
     * 获取剩余免费时长 不足一小时显示分钟数,不足一分钟显示不足一分钟
     */
    fun getClearFreeTime(): String {
        return when {
            free_time >= 3600 -> {
                val hours = free_time / 3600
                val minutes = (free_time % 3600) / 60
                topActivity.getString(R.string.clear_free_time_hours, hours, minutes)
            }

            free_time >= 60 -> {
                val minutes = free_time / 60
                topActivity.getString(R.string.clear_free_time_minutes, minutes)
            }

            else -> topActivity.getString(R.string.clear_vip_duration_less_than_1_minute)
        }
    }

    /**
     * 设备是否授权
     * */
    fun isAuthorized(): Boolean {

        return deviceId.isNotEmpty() && status
    }

    fun isConnected(): Boolean {
        return deviceId.isNotEmpty()
    }

    fun clear() {
        _id = ""
        deviceId = ""
        name = ""
        remark = ""
        status = false
    }

    fun isAuthorized(address: String): Boolean {
        return address.isNotEmpty() && deviceId.isNotEmpty() && status && address == deviceId
    }

    fun getVersionName(): String {
        return when (version) {
            VERSION_NORMAL -> "版本：普通版"
            VERSION_PROFESSIONAL -> "版本：专业版"
            else -> "未知"
        }
    }

    fun getVersionType(): Int {
        return version //VERSION_PROFESSIONAL
    }

    /**
     * 设备全名 = 设备名-版本名
     * */
    fun getDeviceFullName(): String {
        return alias //name
    }

    @Parcelize
    @Serializable
    data class VersionModel(
        var version: Int,
        var name: String,
        @androidx.annotation.DrawableRes var drawableId: Int = R.mipmap.ic_set_model,
        val describ: String = ""
    ) : Parcelable

    @Parcelize
    @Serializable
    data class DeviceModel(
        var _id: String = "",
        var name: String = "",
        var remark: String = "",
//        var colors: List<String> = emptyList(), //服务器数据结构有变化，暂时不解析
    ) : Parcelable

    @Parcelize
    @Serializable
    data class DeviceAgency(
        var _id: String = "",
        var name: String = "",
        var phone: String = "",
        var address: String = "",
        var remark: String = "",
    ) : Parcelable

}