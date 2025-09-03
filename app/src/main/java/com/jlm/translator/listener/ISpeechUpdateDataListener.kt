package com.jlm.translator.listener

import com.jlm.translator.entity.Language
import com.jlm.translator.entity.RecordModel

internal interface ISpeechUpdateDataListener {
    fun startAnim() //开启录音动画
    fun stopAnim() //停止录音动画
    fun addItem(recordModel: RecordModel, position: Int) //新增一行
    fun updateSourceTextItem(content: String, position: Int) //更新输入的文字
    fun updateTargetTextItem(content: String, position: Int, type: Int) //更新输出的文字
    fun updateLanguageUI(language: Language?, type: Int) //更新语音数据
    fun recordFinished()
}
open class SpeechUpdateDataListener : ISpeechUpdateDataListener {
    override fun startAnim() {
    }

    override fun stopAnim() {
    }

    override fun addItem(recordModel: RecordModel, position: Int) {
    }

    override fun updateSourceTextItem(content: String, position: Int) {
    }
    override fun updateTargetTextItem(content: String, position: Int, type: Int) {
    }

    override fun updateLanguageUI(language: Language?, type: Int) {
    }

    override fun recordFinished() {
    }


}