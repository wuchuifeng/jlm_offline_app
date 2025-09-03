package com.xhj.translator.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import cn.chawloo.base.utils.MK
import java.util.Locale

const val KEY_LANGUAGE = "key_language"

object LocaleUtil {
    /**
     * 初始化应用语言
     * 在 Application 的 onCreate 中调用
     */
    fun initializeLanguage() {
        val savedLanguage = MK.decodeString(KEY_LANGUAGE, "zh")
        applyLanguage(savedLanguage)
    }

    /**
     * 切换语言
     * @param language 要设置的语言（"zh" 或 "en"）
     */
    fun switchLanguage(language: String) {
        MK.encode(KEY_LANGUAGE, language)
        applyLanguage(language)
        //清空缓存的语言列表、区域列表
//        LanguageManager.clearLanguageList()
//        RegionManager.clearRegionCache()
    }

    /**
     * 应用语言配置
     * @param language 要设置的语言
     */
    private fun applyLanguage(language: String) {
        val locale = when (language) {
            "zh" -> Locale.SIMPLIFIED_CHINESE // 简体中文
            "en" -> Locale.ENGLISH // 英文
            "ar" -> Locale("ar") // 阿拉伯语
            "fr" -> Locale.FRENCH // 法语
            "de" -> Locale.GERMAN // 德语
            "it" -> Locale.ITALIAN // 意大利语
            "ja" -> Locale.JAPANESE // 日语
            "ko" -> Locale.KOREAN // 韩语
            "pt" -> Locale("pt", "PT") // 葡萄牙语（葡萄牙）
            "es" -> Locale("es", "ES") // 西班牙语（西班牙）
            "vi" -> Locale("vi") // 越南语
            else -> Locale.ENGLISH //Locale.getDefault() // 默认英文
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
    }

    fun getCurrentLocale(): Locale {
        val language = MK.decodeString(KEY_LANGUAGE, "zh") // 默认语言为简体中文
        return when (language) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            "ar" -> Locale("ar")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "pt" -> Locale("pt", "PT")
            "es" -> Locale("es", "ES")
            "vi" -> Locale("vi")
            else -> Locale.ENGLISH //Locale.getDefault()
        }
    }

    fun getShowLanguageText(locale: Locale): String {
        return when (locale) {
            Locale.SIMPLIFIED_CHINESE -> "中文（简体）"
            Locale.ENGLISH -> "English"
            Locale("ar") -> "العربية" // 阿拉伯语
            Locale.FRENCH -> "Français" // 法语
            Locale.GERMAN -> "Deutsch" // 德语
            Locale.ITALIAN -> "Italiano" // 意大利语
            Locale.JAPANESE -> "日本語" // 日语
            Locale.KOREAN -> "한국어" // 韩语
            Locale("pt", "PT") -> "Português" // 葡萄牙语（葡萄牙）
            Locale("es", "ES") -> "Español" // 西班牙语（西班牙）
            Locale("vi") -> "Tiếng Việt" // 越南语
            else -> "English" // 默认返回英文
        }
    }
}