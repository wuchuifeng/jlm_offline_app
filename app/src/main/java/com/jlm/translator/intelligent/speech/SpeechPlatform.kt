package com.jlm.translator.intelligent.speech

import com.jlm.translator.intelligent.provider.SpeechParam

/**
 * 所有智能语音通用接口
 * */
interface SpeechPlatform {

    fun initial(speechParam: SpeechParam)

    fun start(speechParam: SpeechParam)

    fun stop()

    fun close()
}