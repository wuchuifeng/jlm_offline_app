package com.jlm.translator.intelligent.interf

import com.jlm.translator.entity.Language


interface SynthesisInter {
    fun config(targetLanguage: Language)

    fun startSpeaking(content: String)

    fun stop()

    fun close()

}