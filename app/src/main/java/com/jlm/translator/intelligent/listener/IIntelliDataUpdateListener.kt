package com.jlm.translator.intelligent.listener

import com.jlm.translator.entity.Language
import com.jlm.translator.entity.RecordModel
import com.jlm.translator.intelligent.model.SpeechSettingModel
import com.jlm.translator.manager.IntelligentSettingEnum
import com.jlm.translator.manager.LanguageModeEnum

sealed class SpeechSettingInterParams {
    class SilenceMode(val mode: Int):  SpeechSettingInterParams()
    class FontMode(val value: Int):  SpeechSettingInterParams()
    class SpeedMode(val value: Float):  SpeechSettingInterParams()
}

internal interface IIntelliDataUpdateListener {
    fun startAnim() //开启录音动画
    fun stopAnim() //停止录音动画
//    fun updateSetting(speechSettingEnum: IntelligentSettingEnum, speechSettingModel: SpeechSettingModel) //设置的数据更新
    fun addItem(recordModel: RecordModel, position: Int) //新增一行
    fun updateSourceTextItem(content: String, position: Int) //更新输入的文字
    fun updateTargetTextItem(content: String, position: Int, languageMode: LanguageModeEnum) //更新输出的文字
    fun updateLanguageUI(language: Language?, languageMode: LanguageModeEnum) //更新语音数据
    fun updateSettingData(param: SpeechSettingInterParams)
    fun recordFinished()
}
open class IntelliDataUpdateListener : IIntelliDataUpdateListener {
    override fun startAnim() {
    }

    override fun stopAnim() {
    }

    override fun addItem(recordModel: RecordModel, position: Int) {
    }

    override fun updateSourceTextItem(content: String, position: Int) {
    }
    override fun updateTargetTextItem(content: String, position: Int, languageMode: LanguageModeEnum) {
    }

    override fun updateLanguageUI(language: Language?, languageMode: LanguageModeEnum) {
    }

    override fun updateSettingData(param: SpeechSettingInterParams) {
    }

    override fun recordFinished() {
    }


}