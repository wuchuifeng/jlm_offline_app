package com.jlm.translator.intelligent.interf

import com.jlm.translator.entity.Language


/**
 * 语音识别并翻译的帮助类接口
 * */
interface RecogAndTransInter {
    fun startReco(sourceLanguage: Language, targetKeys: Array<Language>) //开始识别并配置翻译

    fun startTrans(content: String)

    fun stop()

    fun close()

}