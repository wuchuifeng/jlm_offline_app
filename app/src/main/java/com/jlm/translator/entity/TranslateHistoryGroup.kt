package com.jlm.translator.entity

import com.drake.brv.item.ItemExpand
import com.jlm.translator.database.table.TranslateHistory

/**
 * TODO
 * @author Create by 鲁超 on 2024/7/2 08:56
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
data class TranslateHistoryGroup(
    val createTime: String = "",
    val historyList: List<TranslateHistory> = emptyList(),
    override var itemExpand: Boolean = true,
    override var itemGroupPosition: Int = 0,
) : ItemExpand {
    override fun getItemSublist() = historyList.distinctBy { it.UUID }
}