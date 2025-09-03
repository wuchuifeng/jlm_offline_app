package com.jlm.translator.database.table

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.jlm.translator.R
import com.jlm.translator.database.converter.DateConverters
import kotlinx.parcelize.Parcelize
import org.joda.time.LocalDateTime

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/10 10:24
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
@Entity
@TypeConverters(DateConverters::class)
@Parcelize
data class TranslateHistory(

    @PrimaryKey(autoGenerate = true)
    var ID: Long = 0,

    /**
     * 单次翻译的ID
     */
    var UUID: String = "",

    /**
     * 记录类型 1：同声   2：同声 双语 3：中英对话  4：短语对话
     */
    var type: Int = 0,

    /**
     * 来源文本（翻以前文本）
     */
    var sourceText: String = "",

    /**
     * 目标文本（翻译后文本）
     */
    var targetText: String = "",

    /**
     * 目标文本，第二种翻译后的文本
     * */
    @ColumnInfo(defaultValue = "")
    var targetSecondText: String = "",

    /**
     * 产生日期 default now()
     */
    var createTime: LocalDateTime = LocalDateTime.now(),

    /**
     * 是否选中
     */
    @Ignore
    var isChecked: Boolean = false,

    /**
     * 是否标记
     */
    @ColumnInfo(defaultValue = "0")
    var isMarked: Boolean = false,

    /**
     * 版本标识：4-中国版，3-国际版，5-专业版
     */
    @ColumnInfo(defaultValue = "4")
    var versionCode: Int = 4,
) : Parcelable {
    companion object {
        const val KEY_TRANSLATE = "key_translate"
        const val type_all: Int = 0 //全部
        const val type_listen: Int = 1 //同声
        const val type_listen_bilingual: Int = 2 //同声 双语
        const val type_listen_freeformulti: Int = 3 //自由多人对话
        const val type_listen_freefortwo: Int = 4 //自由双人对话
        const val type_zh_en_dialogue: Int = 5 //中英对话
        const val type_short_dialogue: Int = 6 //短语对话
        const val type_listen_orignal: Int = 7 //同声 带原声

        const val title_all = "全部"
        const val title_listen = "同声传译"
        const val title_listen_original = "同声传译(自由听)"
        const val title_listen_bilingual = "同声传译(双语播报)"
        const val title_listen_freeformulti = "多人自由对话(双语播报)"
        const val title_listen_freefortwo = "双人自由对话"
    }

    /**
     * 获取type对应的名称
     * */
    fun getTypeTitle(): String {
        return when(type) {
            type_listen -> title_listen
            type_listen_orignal -> title_listen_original
            type_listen_bilingual -> title_listen_bilingual
            type_listen_freeformulti -> title_listen_freeformulti
            type_listen_freefortwo -> title_listen_freefortwo

            else -> title_all
        }
    }

    fun getTypeTitle(type: Int): String {
        return when(type) {
            type_listen -> title_listen
            type_listen_orignal -> title_listen_original
            type_listen_bilingual -> title_listen_bilingual
            type_listen_freeformulti -> title_listen_freeformulti
            type_listen_freefortwo -> title_listen_freefortwo

            else -> title_all
        }
    }

    fun getTypeIcon(): Int {
        return when(type) {
            type_listen -> R.mipmap.ic_home_item1
            type_listen_bilingual -> R.mipmap.ic_home_item2
            type_listen_orignal -> R.mipmap.ic_home_item6
            type_listen_freeformulti -> R.mipmap.ic_home_item3
            type_listen_freefortwo -> R.mipmap.ic_home_item4

            else -> R.mipmap.ic_home_item1
        }
    }
}
