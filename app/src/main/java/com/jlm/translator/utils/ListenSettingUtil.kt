package com.jlm.translator.utils

import com.jlm.translator.entity.VoicerModel
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/12 09:42
 *----------Dragon be here!----------/
 *       ┌─┐      ┌─┐
 *     ┌─┘─┴──────┘─┴─┐
 *     │              │
 *     │      ─       │
 *     │  ┬─┘   └─┬   │
 *     │              │
 *     │      ┴       │
 *     │              │
 *     └───┐      ┌───┘
 *         │      │神兽保佑
 *         │      │代码无BUG！
 *         │      └──────┐
 *         │             ├┐
 *         │             ┌┘
 *         └┐ ┐ ┌───┬─┐ ┌┘
 *          │ ┤ ┤   │ ┤ ┤
 *          └─┴─┘   └─┴─┘
 *─────────────神兽出没───────────────/
 */
object ListenSettingUtil {

    const val defaultFontProgress = 50F
    val fontSizeList = arrayOf(18F, 20F, 22F, 24F, 26F)
    val fontProgressFactors = arrayOf(0F, 25F, 50F, 75F, 100F)
    val fontTextList = arrayOf("较小", "小", "正常", "大", "较大")
    val defaultFontSize = fontSizeList[2]


    fun getFontSizeText(fontCode: Int): String {

        return fontTextList[fontCode]
    }

    fun getFontSize(fontCode: Int): Float {
        return fontSizeList[fontCode]
    }

    fun getFontCode(progress: Any): Int {

       var index = fontProgressFactors.indexOf(progress)
       if (index == -1) index = 2

       return index
    }

    fun getFontProgress(fontCode: Int): Float {
        return fontProgressFactors[fontCode]
    }

    fun getSpeechSpeedText(speed: Float): String {
        return "${formatFloat(speed, 2)}倍"
    }

    fun getSpeechBreakText(mills: Int): String {
        return "${mills}毫秒"
    }

    fun getNoiseText(noiseValue: Float): String {
        var v = formatFloat(noiseValue*10, 2) + 2
        return "${v}"
    }

    fun getVoicerText(type:  Int): String {
        return when (type) {
            VoicerModel.TYPE_MALE -> "男声"
            VoicerModel.TYPE_FEMALE -> "女声"
            else -> "女声"
        }
    }

    private fun formatFloat(floatValue: Float, newScale: Int): Float {
        val b = BigDecimal(floatValue.toDouble())
        return b.setScale(newScale, RoundingMode.HALF_UP).toFloat()
    }

}