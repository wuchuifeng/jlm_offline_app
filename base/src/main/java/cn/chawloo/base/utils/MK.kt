package cn.chawloo.base.utils

import android.os.Parcelable
import com.tencent.mmkv.MMKV

object MK {
    private var mmkv: MMKV? = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)

    fun encode(key: String, value: Any?) {
        when (value) {
            is String -> mmkv?.encode(key, value)
            is Float -> mmkv?.encode(key, value)
            is Boolean -> mmkv?.encode(key, value)
            is Int -> mmkv?.encode(key, value)
            is Long -> mmkv?.encode(key, value)
            is Double -> mmkv?.encode(key, value)
            is ByteArray -> mmkv?.encode(key, value)
            is Parcelable -> mmkv?.encode(key, value)
            else -> return
        }
    }

    fun encode(key: String, sets: Set<String>) {
        mmkv?.encode(key, sets)
    }

    fun decodeInt(key: String, defaultValue: Int = 0): Int {
        return mmkv?.decodeInt(key, defaultValue) ?: defaultValue
    }

    fun decodeDouble(key: String, defaultValue: Double = 0.00): Double {
        return mmkv?.decodeDouble(key, defaultValue) ?: defaultValue
    }

    fun decodeLong(key: String, defaultValue: Long = 0L): Long {
        return mmkv?.decodeLong(key, defaultValue) ?: defaultValue
    }

    fun decodeBool(key: String, defaultValue: Boolean = false): Boolean {
        return mmkv?.decodeBool(key, defaultValue) ?: defaultValue
    }

    fun decodeFloat(key: String, defaultValue: Float = 0F): Float {
        return mmkv?.decodeFloat(key, defaultValue) ?: defaultValue
    }

    fun decodeByteArray(key: String, defaultValue: ByteArray = byteArrayOf()): ByteArray {
        return mmkv?.decodeBytes(key, defaultValue) ?: defaultValue
    }

    fun decodeString(key: String, defaultValue: String = ""): String {
        return mmkv?.decodeString(key, defaultValue) ?: defaultValue
    }

    fun <T : Parcelable> decodeParcelable(key: String, tClass: Class<T>): T? {
        return mmkv?.decodeParcelable(key, tClass)
    }

    fun decodeStringSet(key: String, defaultValue: Set<String> = setOf()): Set<String> {
        return mmkv?.decodeStringSet(key, defaultValue) ?: defaultValue
    }

    fun removeKeys(vararg key: String) {
        mmkv?.removeValuesForKeys(key)
    }

    fun clearAll() {
        mmkv?.clearAll()
    }
}

object MKKeys {
    const val KEY_IS_FIRST_RUN = "key_is_first_run"
    const val KEY_TOKEN = "key_token"
    const val KEY_POLICY = "key_policy"
    const val KEY_USER = "key_user"
    const val KEY_DEVICE_ID = "key_device_id"
    const val KEY_VERSION_CODE = "key_app_version_code"
    const val KEY_LARGE_TEXT = "key_global_is_large_text_size"

    const val KEY_SOURCE_LANGUAGE = "key_source_language"
    const val KEY_TARGET_LANGUAGE = "key_target_language"
    //文字翻译的语言选择
    const val KEY_TEXTTRANSLATE_SOURCE_LANGUAGE = "key_texttranslate_source_language"
    const val KEY_TEXTTRANSLATE_TARGET_LANGUAGE = "key_texttranslate_target_language"
    //短语对话的语言选择
    const val KEY_DIALOGUE_LEFT_LANGUAGE = "key_dialogue_left_language"
    const val KEY_DIALOGUE_RIGHT_LANGUAGE = "key_dialogue_right_language"
    // 同声翻译的语言选择
    const val KEY_LISTENMODE_SOURCE_LANGUAGE = "key_listenmode_source_language"
    const val KEY_LISTENMODE_TARGET_LANGUAGE = "key_listenmode_target_language"
    // 同声翻译带原声的语言选择
    const val KEY_LISTENORIGNAL_SOURCE_LANGUAGE = "key_listenorignal_source_language"
    const val KEY_LISTENORIGNAL_TARGET_LANGUAGE = "key_listenorignal_target_language"

    // 同声模式双语输出的语言选择
    const val KEY_LISTENMODEBILINGGUAL_SOURCE_LANGUAGE = "key_listenmodebilingual_source_language"
    const val KEY_LISTENMODEBILINGGUAL_TARGET_LEFT_LANGUAGE = "key_listenmodebilingual_target_left_language"
    const val KEY_LISTENMODEBILINGGUAL_TARGET_RIGHT_LANGUAGE = "key_listenmodebilingual_target_right_language"

    //自由对话模式的语言选择
    const val KEY_FREEDIALOGUE_SOURCE_LANGUAGE = "key_freedialogue_source_language"
    const val KEY_FREEDIALOGUE_TARGET_LEFT_LANGUAGE = "key_freedialogue_target_left_language"
    const val KEY_FREEDIALOGUE_TARGET_RIGHT_LANGUAGE = "key_freedialogue_target_right_language"

    // 自由对话多人模式
    const val KEY_LISTENFREEFORMULTI_SOURCE_LANGUAGE = "key_listenfreeformulti_source_language"
    const val KEY_LISTENFREEFORMULTI_TARGET_LEFT_LANGUAGE = "key_listenfreeformulti_target_left_language"
    const val KEY_LISTENFREEFORMULTI_TARGET_RIGHT_LANGUAGE = "key_listenfreeformulti_target_right_language"

    //双人自由对话模式的语言选择
    const val KEY_FREEFORTWO_LEFT_LANGUAGE = "key_freefortwo_left_language"
    const val KEY_FREEFORTWO_RIGHT_LANGUAGE = "key_freefortwo_right_language"

    const val KEY_LISTENFREEFORTWO_LEFT_LANGUAGE = "key_listenfreefortwo_left_language"
    const val KEY_LISTENFREEFORTWO_RIGHT_LANGUAGE = "key_listenfreefortwo_right_language"

    const val KEY_SYNTHESIS_INTONATION = "synthesisIntonation"
    const val KEY_SYNTHESIS_VOLUME = "synthesisVolume" //声音
    const val KEY_SYNTHESIS_SPEED = "synthesisSpeed" //语速
    const val KEY_SYNTHESIS_PITCH = "synthesisPitch" //语调
    const val KEY_RECOGNIZE_BREAK = "recognizeBreak" //语音断句
    const val KEY_RECOGNIZE_noise = "recognizeNoise" //噪音
    const val KEY_SYNTHESIS_FONT = "synthesisFont" //字体

    const val KEY_TRANSLATE_SCENE = "translateScene" //翻译场景选择
    const val KEY_VOICER_TYPE = "voicerType" //语音播报的声音类型选择

    const val KEY_SYNTHESIS_INTONATION_DIALOGUE = "synthesis_intonation_dialogue" //语调
    const val KEY_SYNTHESIS_VOLUME_DIALOGUE = "synthesis_volume_dialogue" //声音
    const val KEY_SYNTHESIS_SPEED_DIALOGUE = "synthesis_speed_dialogue" //语速

    const val KEY_TRANSLATE_SCENE_DIALOGUE = "translate_scene_dialogue" //翻译场景选择

    const val KEY_ALI_TOKEN_EXPIRE_TIME = "ali_expire_time"

    const val KEY_ALI_TOKEN = "ali_token"

    object IntelliVersion {
        const val key_version_code = "key_intelli_version_code"
    }

    object Language {
        const val key_china_tongchuan_source = "key_china_tongchuan_source_1" //同传源语言
        const val key_china_tongchuan_target = "key_china_tongchuan_target_1" //同传目标语言
        const val key_china_tongchuan_2_source = "key_china_tongchuan_2_source_1"
        const val key_china_tongchuan_2_targetleft = "key_china_tongchuan_2_targetleft_1" //同传双语播报 目标左声道语言
        const val key_china_tongchuan_2_targetright = "key_china_tongchuan_2_targetright_1" //同传双语播报 目标右声道语言
        const val key_china_tongchuan_free_target = "key_china_tongchuan_free_target_1" //同传自由听 目标语言

        const val key_offline_tongchuan_source = "key_offline_tongchuan_source_1" //同传源语言
        const val key_offline_tongchuan_target = "key_offline_tongchuan_target_1" //同传目标语言
        const val key_offline_tongchuan_2_source = "key_offline_tongchuan_2_source_1"
        const val key_offline_tongchuan_2_targetleft = "key_offline_tongchuan_2_targetleft_1" //同传双语播报 目标左声道语言
        const val key_offline_tongchuan_2_targetright = "key_offline_tongchuan_2_targetright_1" //同传双语播报 目标右声道语言
        const val key_offline_tongchuan_free_target = "key_offline_tongchuan_free_target_1" //同传自由听 目标语言

        const val key_international_tongchuan_source = "key_international_tongchuan_source" //同传源语言
        const val key_international_tongchuan_target = "key_international_tongchuan_target" //同传目标语言
        const val key_international_tongchuan_2_source = "key_international_tongchuan_2_source"
        const val key_international_tongchuan_2_targetleft = "key_international_tongchuan_2_targetleft" //同传双语播报 目标左声道语言
        const val key_international_tongchuan_2_targetright = "key_international_tongchuan_2_targetright" //同传双语播报 目标右声道语言
        const val key_international_tongchuan_free_target = "key_international_tongchuan_free_target" //同传自由听 目标语言

        const val key_profession_tongchuan_source = "key_profession_tongchuan_source" //同传源语言
        const val key_profession_tongchuan_target = "key_profession_tongchuan_target" //同传目标语言
        const val key_profession_tongchuan_2_source = "key_profession_tongchuan_2_source"
        const val key_profession_tongchuan_2_targetleft = "key_profession_tongchuan_2_targetleft" //同传双语播报 目标左声道语言
        const val key_profession_tongchuan_2_targetright = "key_profession_tongchuan_2_targetright" //同传双语播报 目标右声道语言
        const val key_profession_tongchuan_free_target = "key_profession_tongchuan_free_target" //同传自由听 目标语言


        const val key_freedialog_source = "key_freedialog_source" //自由识别 源语言
        const val key_freedialog_targetleft = "key_freedialog_targetleft" //自由识别 目标左声道语言
        const val key_freedialog_targetright = "key_freedialog_targetright" //自由识别 目标右声道语言

        //自由识别，双语输出相关
        const val key_china_freedialog_twosynth_source = "key_china_freedialog_twosynth_source_1"
        const val key_china_freedialog_twosynth_targetleft = "key_china_freedialog_twosynth_targetleft_1"
        const val key_china_freedialog_twosynth_targetright = "key_china_freedialog_twosynth_targetright_1"

        const val key_international_freedialog_twosynth_source = "key_international_freedialog_twosynth_source"
        const val key_international_freedialog_twosynth_targetleft = "key_international_freedialog_twosynth_targetleft"
        const val key_international_freedialog_twosynth_targetright = "key_international_freedialog_twosynth_targetright"

        const val key_profession_freedialog_twosynth_source = "key_profession_freedialog_twosynth_source"
        const val key_profession_freedialog_twosynth_targetleft = "key_profession_freedialog_twosynth_targetleft"
        const val key_profession_freedialog_twosynth_targetright = "key_profession_freedialog_twosynth_targetright"

        //自由对话，双人模式相关
        const val key_china_freedialog_twotalk_left = "key_china_freedialog_twotalk_left_1" //双人自由对话 左声道
        const val key_china_freedialog_twotalk_right = "key_china_freedialog_twotalk_right_1" //右声道
        const val key_international_freedialog_twotalk_left = "key_international_freedialog_twotalk_left"
        const val key_international_freedialog_twotalk_right = "key_international_freedialog_twotalk_right"
        const val key_profession_freedialog_twotalk_left = "key_profession_freedialog_twotalk_left" //双人自由对话 左声道
        const val key_profession_freedialog_twotalk_right = "key_profession_freedialog_twotalk_right" //右声道
    }

    /**
     * 翻译设置
     * */
    object SpeechSet {
//        const val key_speechoutput_mode_code = "key_speechoutput_mode_code"
        const val key_china_speechoutput_mode_code = "key_china_speechoutput_mode_code"
        const val key_international_speechoutput_mode_code = "key_international_speechoutput_mode_code"
        const val key_offline_speechoutput_mode_code = "key_offline_speechoutput_mode_code"

        const val key_azure_selectRegion_code = "key_azure_selectRegion_code"
        const val key_ali_selectRegion_code = "key_ali_selectRegion_code"
        const val key_set_volumn = "key_set_volumn" //音量
        const val key_set_font = "key_set_font" //字体
        const val key_set_speed = "key_set_speed" //语速
        const val key_set_silence = "key_set_silence" //静音时间
        const val key_set_noise = "key_set_noise" //环境噪音
        const val key_set_voice = "key_set_voice" //播报声音
        const val key_set_ali_scene = "key_set_ali_scene" //翻译场景
        const val key_set_azure_scene = "key_set_azure_scene"

        const val key_set_ali_region_code = "key_set_ali_region_code" //code
        const val key_set_azure_region_code = "key_set_azure_region_code"

        const val key_set_china_model = "key_set_china_object" //中国版模型
        const val key_set_internal_model = "key_set_internal_object" //国际版
        const val key_set_profession_model = "key_set_profession_model"
        const val key_set_offline_model = "key_set_offline_model"
    }

    object Region {
        const val key_region = "key_region"
        const val key_azure_regionname = "key_azure_regionname"
        const val key_ali_regionname = "key_ali_regionname"
    }

}