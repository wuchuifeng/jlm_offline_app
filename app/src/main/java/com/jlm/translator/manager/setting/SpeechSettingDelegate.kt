package com.jlm.translator.manager.setting

import android.content.Context
import cn.chawloo.base.ext.toast
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.translator.intelligent.model.RegionPointModel
import com.jlm.translator.intelligent.model.SpeechSettingModel
import com.jlm.translator.manager.IntelligentSettingEnum
import com.jlm.translator.manager.IntelligentVersionEnum

class SpeechSettingDelegate(val context: Context, val versionEnum: IntelligentVersionEnum) {
    val TAG = javaClass.name

    var settingModel: SpeechSettingModel? = null
    var regionModel: RegionPointModel? = null
    var regionList:  List<RegionPointModel>? = null

    init {
        initModel()
    }

    private fun initModel() {
        settingModel  = getSpeechSettingModel()
        regionList = getRegionPointList()
        if (settingModel == null || regionList == null) {
            toast("翻译配置异常")
            return
        }
    }

    /**
     * 获取设置展示列表信息
     * */
    fun getSettingInfoList(): List<IntelligentSettingEnum> {

        return IntelligentSettingEnum.getSettingList(IntelligentVersionEnum.OFFLINE)
    }

    /**
     * 获取区域节点列表
     * */
    fun getRegionPointList(): List<RegionPointModel> {
        return  emptyList()
    }

    fun saveSettingModelValue(settingModel: SpeechSettingModel) {
        this.settingModel = settingModel
        MK.encode(MKKeys.SpeechSet.key_set_offline_model, settingModel)
    }

    fun getSpeechSettingModel(): SpeechSettingModel {
        if (settingModel != null) return settingModel!!
        val model = MK.decodeParcelable(MKKeys.SpeechSet.key_set_offline_model, SpeechSettingModel::class.java)
        return model?: SpeechSettingModel()
    }

    fun clearCache() {
        settingModel = null
        regionModel = null
        regionList = emptyList()
    }

}