package com.jlm.translator.intelligent.listener

import cn.chawloo.base.ext.toast
import com.jlm.translator.utils.SpeechErrorResult

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/11 14:34
 *----------Dragon be here!----------/
 *       ┌─┐      ┌─┐
 *     ┌─┘─┴──────┘─┴─┐
 *     │              │
 *     │      ─       │
 *     │  ┬─┘   └─┬   │
 *     │              │
 *     │      ┴       │
 *     │              │
 *     └───┐      ┌───┘
 *         │      │神兽保佑
 *         │      │代码无BUG！
 *         │      └──────┐
 *         │             ├┐
 *         │             ┌┘
 *         └┐ ┐ ┌───┬─┐ ┌┘
 *          │ ┤ ┤   │ ┤ ┤
 *          └─┴─┘   └─┴─┘
 *─────────────神兽出没───────────────/
 */
internal interface IAliListener {
    fun synthesisOverAli()
    fun translateAli(translated: String, position: Int, type: Int)

    fun translateFinish()
    fun recordStart()
    fun recordPause()
    fun recordStop()
    fun recordRelease()
    fun speechRecognizeSentenceEndAli(rText: String)
    fun speechRecognizeChangeAli(rText: String)
    fun onAudioRMSChange(volume: Float)
    fun languageIdentiResult(langKey: String, content: String, position: Int)

    fun azureSpeechLanguageIdentiResult(azureKey: String, content: String)

    fun aliErrorCallback(code: Int, message: String, type: Int);

    fun speechCommonError(error: SpeechErrorResult);

    fun synthesisStartPlay(type: Int);
    fun synthesisPlayOver(type: Int);
}

open class DefaultAliListener : IAliListener {
    override fun synthesisOverAli() {}

    override fun translateAli(translated: String, position: Int, type: Int) {}

    override fun translateFinish() {}

    override fun recordStart() {}
    override fun recordPause() {}
    override fun recordStop() {}
    override fun recordRelease() {}


    override fun speechRecognizeSentenceEndAli(rText: String) {}
    override fun speechRecognizeChangeAli(rText: String) {}

    override fun onAudioRMSChange(volume: Float) {}
    override fun languageIdentiResult(langKey: String, content: String, position: Int) {
    }

    /**
     * azure语种识别返回的callback
     * */
    override fun azureSpeechLanguageIdentiResult(azureKey: String, content: String) {
    }

    override fun aliErrorCallback(code: Int, message: String, type: Int) {
        toast(message)
    }

    /**
     * 语音识别error
     * */
    override fun speechCommonError(error: SpeechErrorResult) {
    }

    override fun synthesisStartPlay(type: Int) {
    }

    override fun synthesisPlayOver(type: Int) {
    }

}