package com.jlm.translator.intelligent.provider

import android.content.Context
import com.jlm.translator.entity.Language
import com.jlm.translator.intelligent.speech.SpeechPlatform
import com.jlm.translator.intelligent.speech.offline.OfflineNiuRecogHelper
import com.jlm.translator.intelligent.speech.offline.OfflineNiuSynthHelper
import com.jlm.translator.intelligent.speech.offline.OfflineNiuTransHelper
import com.jlm.translator.manager.LanguageJsonInfoEnum
import com.jlm.translator.manager.LanguageModeEnum
import com.jlm.translator.manager.SpeechPlatformEnum

/**
 * 定义语音相关的传参类型
 * */
sealed class SpeechParam {
    data class RecognitionStart(val sourceLang: Language, val targetLangList: List<Language>?): SpeechParam() //适用于阿里语音识别函数
    data class LangIdentifyStart(val langList: List<Language>): SpeechParam() //适用于语种识别
    data class LangIdentifyAndTransStart(val langList: List<Language>, val targetKeys: Array<Language>): SpeechParam()
    data class RecogAndTransStart(val sourceLang: Language, val targetKeys: Array<Language>): SpeechParam() //适用于识别并翻译的功能
    data class TransContent(val content: String, val sourceLanguage: Language, val targetLangList: List<Language>, val position: Int, val langMode: LanguageModeEnum): SpeechParam() //翻译内容
    data class TransContentDouble(val content: String, val sourceLanguage: Language, val targetLeftLanguage: Language, val targetRightLanguage: Language , val position: Int): SpeechParam()
    data class AzureTransContent(val content: String, val sourceLanguage: Language, val targetLangList: List<Language>, val position: Int): SpeechParam() //微软的文本翻译
    data class NiuTransContent(val content: String, val sourceLanguage: Language, val targetLangList: List<Language>, val position: Int): SpeechParam() //小牛的文本翻译
    data class SynthesisStart(val language: Language, val langMode: LanguageModeEnum): SpeechParam() //
    data class SynthContent(val content: String, val langMode: LanguageModeEnum): SpeechParam() //合成内容
    data class SynthContentTwoTalk(val content: String, val langMode: LanguageModeEnum): SpeechParam() //自由对话的合成内容
    data class SpeechInfo(val recogInstance: SpeechPlatform, val transInstance: SpeechPlatform,
                          val synthInstance:  SpeechPlatform, val synth2Instance: SpeechPlatform?,
                          val sourceLangJsonInfo: LanguageJsonInfoEnum, val targetLangJsonInfo: LanguageJsonInfoEnum): SpeechParam()
    object Default: SpeechParam()
}

/**
 * 要调用的语音平台的实例
 * */
object SpeechProvider {

    fun getSpeechInfo(context: Context, recogPlat: SpeechPlatformEnum, transPlat: SpeechPlatformEnum, synthPlat: SpeechPlatformEnum, synth2Plat: SpeechPlatformEnum) : SpeechParam  {
        var recogInstance: SpeechPlatform? = null
        var transInstance: SpeechPlatform? = null
        var synthInstance: SpeechPlatform? = null
        var synth2Instance: SpeechPlatform? = null
        var targetLangJsonInfo:  LanguageJsonInfoEnum? = null
        var sourceLangJsonInfo:  LanguageJsonInfoEnum? = null
        // 识别平台实例
        when (recogPlat) {
            SpeechPlatformEnum.OFFLINE_NIU_recog -> { //小牛离线识别
                recogInstance = getOfflineNiuRecogInstance(context)
            }
            else -> {
                recogInstance = getOfflineNiuRecogInstance(context)
            }
        }
        //翻译平台示例
        when (transPlat) {
            SpeechPlatformEnum.OFFLINE_NIU_trans -> { //小牛离线翻译
                transInstance = getOfflineNiuTransInstance(context)
            }
            else -> {
                transInstance = getOfflineNiuTransInstance(context)
            }
        }
        //合成平台示例
        when(synthPlat) {
            //小牛离线合成
            SpeechPlatformEnum.OFFLINE_NIU_synth -> {
                synthInstance = getOfflineNiuSynthInstance(context)
            }
            else -> {
                synthInstance = getOfflineNiuSynthInstance(context)
            }
        }

        sourceLangJsonInfo = LanguageJsonInfoEnum.getSourceLangJsonInfoEnum(recogPlat, transPlat, synthPlat)
        targetLangJsonInfo = LanguageJsonInfoEnum.getTargetLangJsonInfoEnum(recogPlat, transPlat, synthPlat)

        return SpeechParam.SpeechInfo(
            recogInstance,
            transInstance,
            synthInstance,
            synth2Instance,
            sourceLangJsonInfo,
            targetLangJsonInfo
        )
    }


//    fun getRecogInstance(context: Context, platformEnum: SpeechPlatformEnum): SpeechPlatform {
//        when(platformEnum) {
//            SpeechPlatformEnum.TONGCHUAN_recogAli -> { //阿里识别
//                return getAliRecogInstance(context)
//            }
//            SpeechPlatformEnum.TONGCHUAN_recogAzure -> { //微软识别
//                return getAzureRecogInstance(context)
//            }
//            SpeechPlatformEnum.TONGCHUAN_recogAndTransAzure -> { //微软识别翻译
//                return getAzureRecogAndTransInstance(context)
//            }
//            SpeechPlatformEnum.DIALOG_identiAzure_transAzure_synthAzure, SpeechPlatformEnum.DIALOG_identiAzure_transNiu_synthAzure, SpeechPlatformEnum.DIALOG_identiAzure_transQwen_synthAzure -> { //微软语种识别
//                return getAzureLangIdentifyInstance(context)
//            }
//            SpeechPlatformEnum.DIALOG_identiAndTransAzure_transAzure_synthAzure, SpeechPlatformEnum.DIALOG_identiAndTransAzure_transNiu_synthAzure, SpeechPlatformEnum.DIALOG_identiAndTransAzure_transQwen_synthAzure -> { //微软语种识别翻译
//                return getAzureLangIdentifyAndTransInstance(context)
//            }
//            else -> {
//                toast("语音识别模型异常")
//                return getAliRecogInstance(context)
//            }
////            RecogPlatformEnum.RECOG_IDENTI_TRANS_PLATFORM_AZURE -> { //微软语种识别翻译
////                return getAzureLangIdentifyAndTransInstance(context)
////            }
////            RecogPlatformEnum.RECOG_IDENTI_PLATFORM_QWEN -> { //千问大模型语种识别，默认用微软语种识别SDK
////                return getAzureLangIdentifyInstance(context)
////            }
////            RecogPlatformEnum.RECOG_ORIGINAL_PLATFORM_ALI -> { //阿里识别原声
////                return getAliOriginalRecogInstance(context)
////            }
////            RecogPlatformEnum.RECOG_ORIGINAL_PLATFORM_AZURE -> { //微软识别原声
////                //TODO: 待完善 要使用azure
////                return getAliOriginalRecogInstance(context)
////            }
////            RecogPlatformEnum.RECOG_PLATFORM_QWEN -> { //千问大模型识别
////                //TODO:暂时使用微软语音识别
////                return getAzureRecogInstance(context)
////            }
////            RecogPlatformEnum.RECOG_IDENTI_TRANS_PLATFORM_AZURE_NIU -> { //小牛语音识别
////                //TODO:暂时使用微软语音识别
////                return getAzureLangIdentifyInstance(context)
////            }
////            else -> {
////                return getAliRecogInstance(context)
////            }
//        }
//    }
//
//    fun getTransInstance(context: Context, speechPlatformEnum: SpeechPlatformEnum): SpeechPlatform {
//        when(speechPlatformEnum) {
//            SpeechPlatformEnum.TONGCHUAN_transAli_synthAzure -> {
//                return getAliTransInstance(context)
//            }
//            SpeechPlatformEnum.TONGCHUAN_transAzure_synthAzure -> {
//                return getAzureTransInstance(context)
//            }
//            SpeechPlatformEnum.TONGCHUAN_transNiu_synthAzure -> {
//                return getNiuTransInstance(context)
//            }
//            SpeechPlatformEnum.TONGCHUAN_transQwen_synthAzure -> {
//                return getQwenTransInstance(context)
//            }
//
//        }
////            TransPlatformEnum.TRANS_PLATFORM_ALI -> { //阿里翻译
////                return getAliTransInstance(context)
////            }
////            TransPlatformEnum.TRANS_PLATFORM_AZURE -> { //微软翻译
////                return getAzureTransInstance(context)
////            }
////            TransPlatformEnum.TRANS_PLATFORM_NIU -> { //小牛翻译
////                return getNiuTransInstance(context)
////            }
////            TransPlatformEnum.TRANS_PLATFORM_QWEN_MT -> { //大模型翻译
////                //使用千问大模型翻译
////                return getQwenTransInstance(context)
////            }
////            else -> {
////                return getAliTransInstance(context)
////            }
//
//    }
//
//    fun getSynthInstance(context: Context, platformEnum: SynthPlatformEnum): SpeechPlatform {
//        when(platformEnum) {
//            SynthPlatformEnum.SYNTH_PLATFORM_AZURE -> { //微软合成
//                return AzureSynthHelper(context)
//            }
//            else -> {
//                return AzureSynthHelper(context)
//            }
//        }
//
//    }

    /*************************离线相关*****************************/

    // 小牛离线识别
    fun getOfflineNiuRecogInstance(context: Context): SpeechPlatform {
        return OfflineNiuRecogHelper(context)
    }

    // 小牛离线翻译
    fun getOfflineNiuTransInstance(context: Context): SpeechPlatform {
        return OfflineNiuTransHelper(context)
    }

    // 小牛离线合成
    fun getOfflineNiuSynthInstance(context: Context): SpeechPlatform {
        return OfflineNiuSynthHelper(context)
    }

}