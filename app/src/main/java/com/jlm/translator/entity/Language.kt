package com.jlm.translator.entity

import android.os.Parcelable
import cn.chawloo.base.ext.toast
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.translator.intelligent.locale.LanguageLocale
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Language(
    var sortLetters: String = "",
    var voicer: String = "",
    var key: String = "",
    var name: String = "",
    val country: String = "",
    var azure_key_identification: String = "",
    var name_key: String = "", //语言名称key
    var country_key:  String = "", //城市名key
    var recognize_key: String = "", //识别的语言标识符
    var translate_key: String = "", //翻译的语言标识符
    var synthesis_key: String = "", //合成的语言标识符
    var azure_male_voicer: Array<String>? = null,
    var azure_female_voicer: Array<String>? = null,
    var isChecked: Boolean = false,
    var curSelectLangType: Int = 0  // 当前语言所属类型， 1.输入源  2.输出源或左声道  3. 右声道
) : Parcelable {
    fun getCurVoicer(): String {
        val type = MK.decodeInt(MKKeys.KEY_VOICER_TYPE, VoicerModel.TYPE_FEMALE) // 默认为女声
        if(voicer.isEmpty()) {
            when(type) {
                VoicerModel.TYPE_MALE -> {
                    if(!azure_male_voicer.isNullOrEmpty()) {
                        return azure_male_voicer!![0]
                    }
                }
                VoicerModel.TYPE_FEMALE -> {
                    if(!azure_female_voicer.isNullOrEmpty()) {
                        return azure_female_voicer!![0]
                    }
                }
            }
        }
        return voicer
    }

    fun getCurVoicer(type: Int): String {
        when(type) {
            VoicerModel.TYPE_MALE -> {
                if(!azure_male_voicer.isNullOrEmpty()) {
                    return azure_male_voicer!![0]
                }
            }
            VoicerModel.TYPE_FEMALE -> {
                if(!azure_female_voicer.isNullOrEmpty()) {
                    return azure_female_voicer!![0]
                }
            }
        }
        if(!azure_male_voicer.isNullOrEmpty()) {
            return azure_male_voicer!![0]
        }
        if(!azure_female_voicer.isNullOrEmpty()) {
            return azure_female_voicer!![0]
        }

        toast("播报声音人异常")
        return "en-US-BrianMultilingualNeural"

    }


    fun getCurVoicerName(): String {
        val type = MK.decodeInt(MKKeys.KEY_VOICER_TYPE, VoicerModel.TYPE_FEMALE) // 默认为女声
        when(type) {
            VoicerModel.TYPE_MALE -> return "男声"
            VoicerModel.TYPE_FEMALE -> return "女声"
        }
        return "女声"
    }

    /**
     * 获取当前language唯一标识符
     * */
    fun getItemKey(): String {
        return key
    }

    /**
     * 获取语音识别key
     * */
    fun getRecognizeKey(): String {
        if(recognize_key == zh_en_key || recognize_key == southeast_asia_key) {
            return "auto"
        }
        return recognize_key
    }

    fun getTransKey(): String {
        if(translate_key == zh_en_key || translate_key == southeast_asia_key) {
            return "auto"
        }
        return translate_key
    }

    fun getSynthesisKey(): String {
        return synthesis_key
    }

    fun getLangName(): String {
        return LanguageLocale.getLanguageName(name_key)
    }

    fun getCountryName(): String {
        return LanguageLocale.getCountryName(country_key)
    }

    fun checkKey(otherKey: String): Boolean {
        return otherKey == key
    }

    companion object {
        const val KEY_SOURCE_LANGUAGE = "key_source_language"
        const val KEY_TARGET_LANGUAGE = "key_target_language"

        const val southeast_asia_key = "asia-southeast" //东南亚混合语言key
        const val zh_en_key = "zh-en" //中英混合key
    }
}
