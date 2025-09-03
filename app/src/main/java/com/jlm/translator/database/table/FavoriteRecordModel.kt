package com.jlm.translator.database.table

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

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
@Parcelize
data class FavoriteRecordModel(
    @PrimaryKey(autoGenerate = true)
    var ID: Long = 0,
    @ColumnInfo(defaultValue = "")
    var sourceKey: String = "",
    @ColumnInfo(defaultValue = "")
    var sourceName: String = "",
    @ColumnInfo(defaultValue = "")
    var targetKey: String = "",
    @ColumnInfo(defaultValue = "")
    var targetName: String = "",
    @ColumnInfo(defaultValue = "")
    var content: String = "",
    @ColumnInfo(defaultValue = "")
    var translationContent: String = "",
    @Ignore
    var isChecked: Boolean = false
) : Parcelable