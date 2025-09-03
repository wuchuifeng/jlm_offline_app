package com.jlm.translator.intelligent.interf

import com.jlm.translator.entity.Language


/**
 * 语音识别公共基类
 * */
interface RecognizeInter {
    fun start(sourceLanguage: Language, targetKeys: Array<Language>)

    fun start(sourceLanguage: Language, targetKeys: Array<Language>, audioSourceModel: Int, isUpdate: Boolean)

    fun stop()

    fun close()

}