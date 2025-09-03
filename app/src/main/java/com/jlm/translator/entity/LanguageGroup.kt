package com.jlm.translator.entity

import android.os.Parcelable
import com.drake.brv.item.ItemExpand
import com.drake.brv.item.ItemHover
import com.jlm.translator.intelligent.locale.LanguageLocale
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class LanguageGroup(
    var name: String = "所有语言",
    var category_key: String = "",
    var languageList: List<Language> = emptyList(),
    override var itemExpand: Boolean = true,
    override var itemGroupPosition: Int = 0
) : ItemExpand, ItemHover, Parcelable {
    override fun getItemSublist() = languageList
    override var itemHover: Boolean
        get() = true
        set(value) {}

    fun getCategoryName(): String {
        return LanguageLocale.getCategoryName(category_key)
    }
}
