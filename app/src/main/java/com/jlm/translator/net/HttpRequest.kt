package com.jlm.translator.net

import android.bluetooth.BluetoothDevice
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.BaseResult
import cn.chawloo.base.model.Entities
import cn.chawloo.base.net.customCatch
import com.drake.net.Get
import com.drake.net.Net.post
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.jlm.common.entity.AppConfig
import com.jlm.translator.entity.FeedbackDto
import com.jlm.translator.entity.requestDto.CreateDeviceInfoDto
import com.jlm.translator.entity.requestDto.DeviceInfoDto
import com.jlm.translator.entity.requestDto.UpdateDeviceTimeDto
import com.jlm.translator.entity.responseModel.DeviceInfoModel
import com.jlm.translator.entity.responseModel.UpdateDeviceTimeResModel
import kotlin.collections.isNullOrEmpty
import com.jlm.translator.entity.requestDto.DeviceWhitelistDto
import com.jlm.translator.entity.responseModel.DeviceWhitelistModel
import com.jlm.translator.entity.responseModel.AliTokenModel

object HttpRequest {

    /**
     * 设备详情信息
     * */
    fun requestDeviceInfo(deviceId: String, model: String, callBack: DeviceInfoModel.() -> Unit) {
        scopeNet {
            val result = Post<BaseResult<DeviceInfoModel>>("earphone_infos/getInfo") {
                json(DeviceInfoDto(deviceId = deviceId, model = model).toJson())
            }.await()
            val data = result.data
            if (data != null) {
                data.callBack()
            }
        }
    }

    /**
     * 设备详情信息
     * */
    fun requestDeviceDetail(deviceId: String, name: String, callBack: DeviceInfoModel.() -> Unit) {
        scopeNet {
            val result = Post<BaseResult<DeviceInfoModel>>("earphone_infos/detail") {
                json(DeviceInfoDto(deviceId = deviceId, name = name).toJson())
            }.await()
            val data = result.data
            if (data != null) {
                data.callBack()
            }
        }
    }

    /**
     * 创建设备
     * */
    fun requestCreateDevice(deviceId: String, name: String, callBack: () -> Unit) {
        scopeNet {
            val result = Post<BaseResult<DeviceInfoModel>>("earphone_infos/create") {
                json(CreateDeviceInfoDto(deviceId = deviceId, name = name).toJson())
            }.await()
//            val data = result.data
//            if (data != null) {
//                callBack()
//            }
        }
    }

    /**
     * 更新设备使用时间
     * */
    fun requestUpdateDeviceFreeTime(requestModel: UpdateDeviceTimeDto, callBack: UpdateDeviceTimeResModel.() -> Unit) {
        scopeNet {
            val result = Post<BaseResult<UpdateDeviceTimeResModel>>("earphone_infos/update") {
                json(requestModel.toJson())
            }.await()
            val data = result.data
            if (data != null) {
                data.callBack()
            }
        }
    }

    /**
     * 版本更新
     * */
    fun requestAppUpdate(success: AppConfig.() -> Unit) {
        scopeNet {
            val result = Post<BaseResult<AppConfig?>>("appinfos/list").await()
            result.data?.success()
        }
    }

    /**
     * 发送用户反馈
     * */
    fun requestSendFeedback(
        tel: String, 
        feedType: Int, 
        feedDesc: String, 
        feedImg: List<String>,
        success: () -> Unit,
        failure: (String) -> Unit
    ) {
        scopeNet {
            try {
                val result = Post<BaseResult<String>>("feedbacks/create") {
                    json(FeedbackDto(
                        tel = tel,
                        feedType = feedType,
                        feedDesc = feedDesc,
                    ).toJson())
                }.await()
                
                // 根据BaseResult的结构判断是否成功
                if (result.status == 200) {
                    success()
                } else {
                    failure(result.message ?: "反馈失败，请稍后重试")
                }
            } catch (e: Exception) {
                failure("网络异常，请检查网络连接")
                e.printStackTrace()
            }
        }.customCatch { exception ->
            failure("网络异常，请检查网络连接")
            exception.printStackTrace()
        }
    }

    /**
     * 通过型号获取设备白名单
     * */
    fun requestDeviceWhitelist(
        model: String,
        success: (DeviceWhitelistModel) -> Unit,
        failure: (String) -> Unit,
        notInWhitelist: (String) -> Unit
    ) {
        scopeNet {
            try {
                val result = Post<BaseResult<DeviceWhitelistModel>>("devicewhitelist/findByModel") {
                    json(DeviceWhitelistDto(model = model).toJson())
                }.await()
                
                when (result.status) {
                    200 -> {
                        val data = result.data
                        if (data != null) {
                            success(data)
                        } else {
                            failure(result.message ?: "设备白名单数据为空")
                        }
                    }
                    700 -> {
                        // 设备不在白名单内
                        notInWhitelist(result.message ?: "该设备型号不在白名单内")
                    }
                    else -> {
                        failure(result.message ?: "未找到该型号的设备白名单")
                    }
                }
            } catch (e: Exception) {
                failure("网络异常，请检查网络连接")
                e.printStackTrace()
            }
        }.customCatch { exception ->
            failure("网络异常，请检查网络连接")
            exception.printStackTrace()
        }
    }

    /**
     * 获取阿里云智能语音Token
     * @param success Token获取成功回调
     * @param failure Token获取失败回调
     */
    fun requestAliToken(
        success: (AliTokenModel) -> Unit,
        failure: (String) -> Unit
    ) {
        scopeNet {
            try {
                val result = Get<BaseResult<AliTokenModel>>("keyinfos/ali-token").await()
                
                when (result.status) {
                    200 -> {
                        val data = result.data
                        if (data != null) {
                            success(data)
                        } else {
                            failure(result.message ?: "阿里云Token数据为空")
                        }
                    }
                    else -> {
                        failure(result.message ?: "获取阿里云Token失败")
                    }
                }
            } catch (e: Exception) {
                failure("网络异常，请检查网络连接")
                e.printStackTrace()
            }
        }.customCatch { exception ->
            failure("网络异常，请检查网络连接")
            exception.printStackTrace()
        }
    }

}