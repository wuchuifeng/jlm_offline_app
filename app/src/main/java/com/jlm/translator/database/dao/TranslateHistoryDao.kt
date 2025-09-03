package com.jlm.translator.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.jlm.translator.database.table.TranslateHistory

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
interface TranslateHistoryDao : BaseDao<TranslateHistory> {

    @Query("DELETE FROM TranslateHistory WHERE UUID = :uuid")
    fun deleteByUUID(uuid: String)

    @Query("DELETE FROM TranslateHistory WHERE UUID in (:uuid)")
    fun deleteByUUIDList(uuid: List<String>)

    @Query("DELETE FROM TranslateHistory WHERE ID in (:ids)")
    fun deleteByIDs(ids: List<Long>)

    /**
     * 删除表中所有数据
     */
    @Query("DELETE FROM TranslateHistory")
    fun deleteAll()

    /**
     * 查询所有数据
     */
    @Query("SELECT * FROM TranslateHistory WHERE sourceText LIKE '%' ||:search|| '%' GROUP BY UUID ORDER BY createTime DESC LIMIT :end OFFSET :start ")
    fun queryAll(start: Int, search: String, end: Int): MutableList<TranslateHistory>

    /**
     * 查询所有数据
     */
    @Query("SELECT * FROM TranslateHistory WHERE type = :mode AND sourceText LIKE '%' ||:search|| '%' GROUP BY UUID ORDER BY createTime DESC LIMIT :end OFFSET :start")
    fun queryAllByMode(mode: Int, search: String, start: Int, end: Int): MutableList<TranslateHistory>

    /**
     * 根据版本查询所有数据
     */
    @Query("SELECT * FROM TranslateHistory WHERE versionCode = :versionCode AND sourceText LIKE '%' ||:search|| '%' GROUP BY UUID ORDER BY createTime DESC LIMIT :end OFFSET :start ")
    fun queryAllByVersion(versionCode: Int, start: Int, search: String, end: Int): MutableList<TranslateHistory>

    /**
     * 根据版本和模式查询数据
     */
    @Query("SELECT * FROM TranslateHistory WHERE versionCode = :versionCode AND type = :mode AND sourceText LIKE '%' ||:search|| '%' GROUP BY UUID ORDER BY createTime DESC LIMIT :end OFFSET :start")
    fun queryAllByVersionAndMode(versionCode: Int, mode: Int, search: String, start: Int, end: Int): MutableList<TranslateHistory>

    /**
     * 根据版本和多个模式查询数据（用于合并查询）
     */
    @Query("SELECT * FROM TranslateHistory WHERE versionCode = :versionCode AND type IN (:modes) AND sourceText LIKE '%' ||:search|| '%' GROUP BY UUID ORDER BY createTime DESC LIMIT :end OFFSET :start")
    fun queryAllByVersionAndModes(versionCode: Int, modes: List<Int>, search: String, start: Int, end: Int): MutableList<TranslateHistory>

    @Query("SELECT * FROM TranslateHistory WHERE UUID = :uuid  ORDER BY createTime DESC LIMIT :end OFFSET :start")
    fun queryByUUID(uuid: String, start: Int, end: Int): MutableList<TranslateHistory>

    /**
     * 通过 UUIDList 获取所有对应数据
     */
    @Query("SELECT * FROM TranslateHistory WHERE UUID IN (:uuid)")
    fun queryByUUIDs(uuid: List<String>): MutableList<TranslateHistory>
}