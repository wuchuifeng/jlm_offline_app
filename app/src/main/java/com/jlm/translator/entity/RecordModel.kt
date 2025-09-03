package com.jlm.translator.entity

data class RecordModel(
    var sourceText: String = "",
    var targetText: String = "", //输出的text或左声道输出的text
    var targetRightText: String = "", //右声道输出的text
    var targetVoicer: String = "becca", //输出的voicer或左声道输出的voicer
    var targetRightVoicer: String = "becca", //输出的右声道的voicer
    var targetKey: String? = "en",
    var targetRightKey: String? = "en", //右声道语言的key
    var rSourceLanguage: Language? = null, //源语言
    var rTargetLanguage: Language? = null, //目标语言 (双语输出时标识左语言)
    var rTargetRightLanguage: Language? = null, //目标语言 右声道
    var key: String = "zh", //唯一标识
    var rType: Int = 0 // 类型
) {

    companion object {
        const val TYPE_SINGLE = 10  //同传-单语
        const val TYPE_DOUBLE = 11 //同传-双语
        const val TYPE_FREE = 12 //同传-自由
    }

}
