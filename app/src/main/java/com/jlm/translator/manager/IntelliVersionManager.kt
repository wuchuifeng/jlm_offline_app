package com.jlm.translator.manager

import android.content.Context
import cn.chawloo.base.ext.toast
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.translator.intelligent.listener.IntelliDataUpdateListener
import com.jlm.translator.intelligent.model.RegionModel
import com.jlm.translator.intelligent.provider.LanguageProvider
import com.jlm.translator.listener.SpeechUpdateDataListener
import com.jlm.translator.manager.func.SpeechFuncDelegate
import com.jlm.translator.manager.setting.SpeechSettingDelegate

/**
 * 智能语音版本管理类, 如果 国际版、大陆版、高级版、普通版等
 * */
class IntelliVersionManager private constructor(val context: Context) {
//    private var versionPlatform: VersionPlatform;
    private lateinit var speechFuncDelegate: SpeechFuncDelegate
    private lateinit var speechSettingDelegate: SpeechSettingDelegate
    private lateinit var translateRecordDelegate: TranslateRecordDelegate

    private var curVersion: IntelligentVersionEnum = IntelligentVersionEnum.OFFLINE

    companion object {
        private var instance: IntelliVersionManager? = null

        fun getInstance(context: Context): IntelliVersionManager {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = IntelliVersionManager(context)
                    }
                }
            }
            return instance!!
        }
    }

    init {
        //从本地获取版本code
        curVersion = IntelligentVersionEnum.OFFLINE

        //初始化delegate
        speechFuncDelegate = SpeechFuncDelegate(curVersion)
        speechSettingDelegate = SpeechSettingDelegate(context, curVersion)
        translateRecordDelegate = TranslateRecordDelegate(context, curVersion)
    }

    /**
     * 初始化配置信息
     * */
    fun initConfig() {
        //初始化devicemanager
        TransDeviceManager.getInstance().initManager(context, curVersion)
        //清除语言缓存（防止类型转换错误）
        LanguageProvider.clearCache()
    }

    /**
     * 切换版本信息
     * @param version 版本
     * */
    fun switchVersion(version: IntelligentVersionEnum) {
        //需要重置所有设置
        if (curVersion != version) {
            curVersion = version
            //切换其他delegate
            speechFuncDelegate = SpeechFuncDelegate(curVersion)
            speechSettingDelegate = SpeechSettingDelegate(context, curVersion)
            translateRecordDelegate = TranslateRecordDelegate(context, curVersion)
            //保存版本code到本地
            MK.encode(MKKeys.IntelliVersion.key_version_code, version.code)
            //重新初始化devicemanager
            TransDeviceManager.getInstance().initManager(context, curVersion)
        }
    }

    /**
     * 获取当前的版本
     * */
    fun getCurVersion(): IntelligentVersionEnum {
        return curVersion
    }

    /**
     * 根据版本获取功能列表
     * */
    fun getFuncInfoList(): List<IntelliFuncInfoEnum> {
        return speechFuncDelegate.getFuncInfoList()
    }

    /**
     * 获取各功能类型的provider
     * */
    fun getFuncDelegate(): SpeechFuncDelegate {
        return speechFuncDelegate
    }

    fun getSettingDelegate(): SpeechSettingDelegate {
        return speechSettingDelegate
    }

    fun getTranslateRecordDelegate(): TranslateRecordDelegate {
        return translateRecordDelegate
    }

    /**
     * 获取要展示的设置列表
     * */
    fun getIntelliSettingList(): List<IntelligentSettingEnum> {
        return speechSettingDelegate.getSettingInfoList()
    }

}