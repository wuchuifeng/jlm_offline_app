package com.jlm.translator.intelligent.listener

import com.jlm.translator.intelligent.model.SpeechErrorEnum
import com.jlm.translator.manager.TargetModeEnum

internal interface ISpeechliListener {
    fun recordStart() //开始录音回调
    fun recordStop() //暂停录音回调
    fun recordRelease() //释放录音回调
    fun recognizeResult(result: String) //语音识别结果回调
    fun recognizeChange(result: String) //语音识别实时变化的回调
    fun conversationResult(speakerName: String, result: String) //对话识别的结果
    fun conversationChange(speakerName: String, result: String) //对话识别的实时变化
    fun languageIdentiResult(result: String, langKey: String) //语音的语种识别结果的回调0
    fun recogAndTransResult(result: String, transResults: ArrayList<String>) //语音识别并翻译后的回调
    fun translateResult(result: String, position: Int, mode: TargetModeEnum) //翻译结果回调
    fun translateCompleted(result: String, result1: String, position: Int, langType: Int) //所有的翻译结果完成的回调
    fun synthesisStart() //开始合成
    fun synthesisCompleted() //语音合成完成的回调
    fun audioPlayStart(type: Int) //语音合成开始播报的回调
    fun audioPlayOver(type: Int) //语音合成播报结束的回调
    fun taskFinish() //整个识别结束了
    fun taskError(error: SpeechErrorEnum) //通用错误的回调

}

open class IntelliSpeechListener : ISpeechliListener {
    override fun recordStart() {
    }

    override fun recordStop() {
    }

    override fun recordRelease() {
    }

    override fun recognizeResult(result: String) { //语音识别结果回调
    }

    override fun recognizeChange(result: String) { //语音识别的实时变化
    }

    override fun conversationResult(speakerName: String, result: String) { //对话识别的结果
    }

    override fun conversationChange(speakerName: String, result: String) { //对话识别的实时变化
    }

    override fun languageIdentiResult(result: String, langKey: String) { //语音的语种识别结果的回调
    }

    override fun recogAndTransResult(result: String, transResults: ArrayList<String>) { //语音识别并翻译后的回调
    }

    override fun translateResult(result: String, position: Int, mode: TargetModeEnum) {
    }

    override fun translateCompleted(result: String, result1: String, position: Int, langType: Int) {
    }

    override fun synthesisStart() { //开始合成
    }

    override fun synthesisCompleted() { //语音合成完成的回调
    }

    override fun audioPlayStart(type: Int) { //语音合成开始播报的回调
    }

    override fun audioPlayOver(type: Int) { //语音合成播报结束的回调
    }

    override fun taskFinish() { //整个识别结束了
    }

    override fun taskError(error: SpeechErrorEnum) { //通用错误的回调
    }


}