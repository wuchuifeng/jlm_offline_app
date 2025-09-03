package com.jlm.translator.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.jlm.translator.database.table.FavoriteRecordModel

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/10 10:36
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

@Dao
interface FavoriteRecordDao : BaseDao<FavoriteRecordModel> {

    /**
     * 查询所有数据
     */
    @Query("SELECT * FROM FavoriteRecordModel ORDER BY ID DESC LIMIT :end OFFSET :start ")
    fun queryAll(start: Int, end: Int): MutableList<FavoriteRecordModel>

    /**
     * 删除表中所有数据
     */
    @Query("DELETE FROM FavoriteRecordModel")
    fun deleteAll()

    @Query("DELETE FROM FavoriteRecordModel WHERE ID = (SELECT MAX(ID) FROM favoriterecordmodel)")
    fun deleteLatest()
}