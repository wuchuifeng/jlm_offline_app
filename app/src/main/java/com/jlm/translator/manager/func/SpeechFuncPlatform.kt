package com.jlm.translator.manager.func

import cn.chawloo.base.model.BaseResult
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.jlm.translator.entity.SceneModel
import com.jlm.translator.intelligent.model.SpeechSettingModel
import com.jlm.translator.manager.IntelligentSettingEnum
import com.jlm.translator.manager.LanguageModeEnum
import me.xfans.lib.voicewaveview.VoiceWaveView

/**
 * 功能模块抽象类
 * */
abstract class SpeechFuncPlatform {
//    abstract fun getLanguageGroupList(langModeEnum: LanguageModeEnum): List<LanguageGroup>
//    abstract fun getLanguage(langModeEnum: LanguageModeEnum): Language
    abstract fun showLanguageSelector(langModeEnum: LanguageModeEnum)
    abstract fun start()
    abstract fun stop()
    abstract fun close()
    open fun initLanguage(){}
    abstract fun startSpeech(lastIndex: Int)
    abstract fun showSettingPop(settingEnum: IntelligentSettingEnum)

    //保存选中的语言
    fun saveLanguageToLocal(key: String, language: Language) {
        MK.encode(key, language)
    }

    /**
     * wave相关
     * */
    fun voiceWaveAnimConfig(waveView: VoiceWaveView) {
        waveView.apply {
            lineWidth = 6f
            lineSpace = 10f
            duration = 300
            addBody(40)
            addBody(80)
            addBody(40)
            addBody(60)
//            addBody(30)
        }
    }

    /**
     * 每个item最大的字符数量
     * */
    open fun getItemTextMaxLimit(key: String): Int {
        //双语自由对话模式下不需要限制字符
        var len = 100
        if (key != "zh") {
            len = 150
        }
        return len
    }

    /**
     * 上传使用时间
     * */
    private fun uploadTime() {
        scopeNet {
            val result = Post<BaseResult<String>>("earphone_infos/update") {
                param("macAddress", "")
                param("free_time", 100)
            }.await()
        }
    }

    private fun getMKSceneValue(): String {
        var key = "general"
        val model = MK.decodeParcelable(MKKeys.KEY_TRANSLATE_SCENE, SceneModel::class.java)
        if (model != null) {
            key = model.scene
        }
        return key
    }
}