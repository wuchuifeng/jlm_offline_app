package com.jlm.translator.manager

import cn.chawloo.base.ext.topActivity
import com.jlm.common.router.Rt
import com.jlm.translator.R
import com.jlm.translator.database.table.TranslateHistory

// 定义智能语音版本枚举类
enum class IntelligentVersionEnum(val title: String, val code: Int) {
    OFFLINE("离线版", 5); //离线版

    /**
     * 获取版本的中文显示名称
     */
    fun getDisplayName(): String {
        return when (this) {
            OFFLINE -> "离线版"
            else -> "离线版"
        }
    }

    companion object {

        fun getVersionList(): List<IntelligentVersionEnum> {
            return listOf(
//                CHINA,
//                INTERNATIONAL,
                OFFLINE,
//                PROFESSIONAL
            )
        }
    }
}

// 定义智能语音的设置枚举类
enum class IntelligentSettingEnum(val title: String, val icon: Int, val defaultValue: String) {
    SETTING_VOLUMN("播放音量", R.mipmap.ic_set_volumn, "100"), //播放音量
    SETTING_FONT("字体大小", R.mipmap.ic_set_font, "正常"), //字体大小
    SETTING_SPEED("播报语速", R.mipmap.ic_set_playspeed, "1.0"), //语速
    SETTING_SILENCE("静音时间", R.mipmap.ic_set_break, "300"), //静音时间
    SETTING_NOISE("环境噪音", R.mipmap.ic_set_noise, "4.0"),
    SETTING_VOICE("播报声音", R.mipmap.ic_set_voicer, "男声"),
    SETTING_TEMPLATE("行业翻译模版", R.mipmap.ic_set_model, "通用"),
    SETTING_POINT("区域节点", R.mipmap.ic_set_custom, "东亚"),
    SETTING_OUTPUT("同声输出模式", R.mipmap.ic_set_custom, "");

    companion object {

        // 大陆版本所要展示的设置列表
        fun getDefaultSettingList(): List<IntelligentSettingEnum> {
            return listOf(
                SETTING_VOLUMN,
                SETTING_FONT,
                SETTING_SPEED,
                SETTING_SILENCE,
                SETTING_NOISE,
                SETTING_VOICE,
//                SETTING_POINT
            )
        }

        //不同版本下的列表
        fun getSettingList(versionEnum: IntelligentVersionEnum): List<IntelligentSettingEnum> {
            return when (versionEnum) {
                IntelligentVersionEnum.OFFLINE -> {
                    listOf(
                        SETTING_VOLUMN,
                        SETTING_FONT,
                        SETTING_SPEED,
                        SETTING_SILENCE,
//                        SETTING_NOISE,
                        SETTING_VOICE,
                        SETTING_POINT //区域
                    )
                }
                else -> getDefaultSettingList()
            }
        }
    }
}

// 智能语音功能枚举类
enum class IntelligentFuncModeEnum(val code: Int) {
    MODE_TONGCHUAN(1), //单语播报
    MODE_TONGCHUAN_2(2), //双语播报
    MODE_TONGCHUAN_FREE(3), //自由听
    MODE_FREEDIALOG_2(4), //自由对话 双语播报,
    MODE_FREEDIALOG_DOUBLE(5), //两人自由对话
    MODE_TEXT(6), //文字翻译
    MODE_ORIGINAL(7) //原声
}

//定义语言平台, 包括识别 翻译 合成
enum class SpeechPlatformEnum {
    AZURE_recog, //微软识别
    AZURE_recogAndTrans, //微软语音翻译
    AZURE_trans, //微软翻译
    AZURE_synth, //微软合成
    AZURE_identi, //微软语种识别
    AZURE_CHINA_recog, //微软中国识别
    AZURE_CHINA_recogAndTrans, //微软中国语音翻译
    AZURE_CHINA_trans, //微软中国翻译
    AZURE_CHINA_synth, //微软中国合成
    AZURE_CHINA_identi, //微软中国语种识别
    AZURE_identiAndTrans, //微软语种识别+翻译
    ALI_recog, //阿里语音识别
    ALI_trans, //阿里翻译
    ALI_synth, //阿里合成
    MINIMAX_synth, //minimax语音合成
    QWEN_trans, //千问翻译
    NIU_trans, //牛牛翻译
    AZURE_free, // 微软自由听
    ALI_free, // 阿里自由听
    MINIMAX_free, //minimax自由听
    OFFLINE_NIU_recog, //小牛离线识别
    OFFLINE_NIU_trans, //小牛离线翻译
    OFFLINE_NIU_synth, //小牛离线合成
    NONE; //空

}

//翻译记录类型枚举类
enum class TranslateRecordTypeEnum(val title: String, val code: Int, val icon: Int) {
    RECORD_ALL("全部", 0, R.mipmap.ic_home_item1),
    RECORD_TONGCHUAN("同声传译", 100, R.mipmap.ic_home_item1), // 使用特殊code表示合并类型
    RECORD_FREEDIALOG_MULTI("多人自由对话(双语)", 3, R.mipmap.ic_home_item3),
    RECORD_FREEDIALOG_TWOTALK("双组互译对话", 4, R.mipmap.ic_home_item4);

    companion object {
        /**
         * 根据版本获取记录类型列表
         */
        fun getRecordTypesByVersion(version: IntelligentVersionEnum): List<TranslateRecordTypeEnum> {
            return when (version) {
                IntelligentVersionEnum.OFFLINE -> listOf(
                    RECORD_ALL,
                    RECORD_TONGCHUAN, // 包含同声传译、同声传译->双语播报、同声传译->自由听
                )
                else -> listOf(RECORD_ALL)
            }
        }

        /**
         * 根据code获取枚举
         */
        fun getByCode(code: Int): TranslateRecordTypeEnum {
            return values().find { it.code == code } ?: RECORD_ALL
        }

        /**
         * 获取同声传译合并类型包含的具体类型列表
         */
        fun getTongchuanMergedTypes(version: IntelligentVersionEnum): List<Int> {
            return when (version) {

                IntelligentVersionEnum.OFFLINE -> {
                    listOf(
                        TranslateHistory.type_listen,           // 1: 同声传译
                    )
                }
                else -> {
                    listOf(TranslateHistory.type_listen)
                }
            }
        }
    }
}

//智能语音功能信息枚举类
enum class IntelliFuncInfoEnum(val title: String, val desc: String, val icon: Int, val url: String) {
    FUNC_TONGCHUAN( //同声传译
        topActivity.getString(R.string.home_item_tongchuanTitle),
        topActivity.getString(R.string.home_item_tongchuanDesc),
        R.mipmap.ic_home_item1,
        Rt.ListenModeAct
    ),
    FUNC_ORIGINAL( //原声
        topActivity.getString(R.string.home_item_originalTitle),
        topActivity.getString(R.string.home_item_originalDesc),
        R.mipmap.ic_home_item5,
        Rt.ListenOriginalModeAct
    );

    companion object {
        fun getOfflineFuncInfoList(): List<IntelliFuncInfoEnum> {
            return listOf(
                FUNC_TONGCHUAN,
                FUNC_ORIGINAL
            )
        }
    }

}

//语言json文件的信息
enum class LanguageJsonInfoEnum(val fileName: String) {

    TONGCHUAN_OFFLINE_recogNiu("offline/offline_recogNiu.json"), //同传 离线识别
    TONGCHUAN_OFFLINE_transNiu_synthNiu("offline/offline_transNiu_synthNiu.json"); //同传 离线翻译+合成

    companion object  {
        fun getSourceLangJsonInfoEnum(recogPlat: SpeechPlatformEnum, transPlat: SpeechPlatformEnum, synthPlat: SpeechPlatformEnum): LanguageJsonInfoEnum {
            return when (recogPlat) {
                SpeechPlatformEnum.OFFLINE_NIU_recog -> {
                    TONGCHUAN_OFFLINE_recogNiu
                }
                else -> {
                    TONGCHUAN_OFFLINE_recogNiu
                }
            }
        }

        /**
         * 根据传入的平台信息来获取目标语言的json文件信息
         */
        fun getTargetLangJsonInfoEnum(recogPlat: SpeechPlatformEnum, transPlat: SpeechPlatformEnum, synthPlat: SpeechPlatformEnum): LanguageJsonInfoEnum {
            if (transPlat == SpeechPlatformEnum.OFFLINE_NIU_trans && synthPlat == SpeechPlatformEnum.OFFLINE_NIU_synth) { //小牛离线
                return TONGCHUAN_OFFLINE_transNiu_synthNiu
            }
            else {
                return TONGCHUAN_OFFLINE_transNiu_synthNiu
            }
        }
    }

}

enum class TargetModeEnum {
    MODE_ALL, //全声道
    MODE_LEFT, //左声道
    MODE_RIGHT //右声道
}

enum class LanguageModeEnum {
    LANG_SOURCE, //输入源
    LANG_TARGET, //输出源
    LANG_TARGET_LEFT, //左声道输出源
    LANG_TARGET_RIGHT //右声道输出源
}

// 定义区域枚举类
enum class RegionEnum(val code: Int, val alias: String, val point: String, val speechKey: String, val transKey:  String) {


}

// 定义本地语言种类枚举类
enum class SystemLanguageEnum(val code: String) {
    ZH("zh"),
    EN("en"),
    ES("es"),
    DE("de"),
    FR("fr"),
    VI("vi"),
    PT("pt"),
    JA("ja"),
    KO("ko"),
    AR("ar"),
    IT("it")
}