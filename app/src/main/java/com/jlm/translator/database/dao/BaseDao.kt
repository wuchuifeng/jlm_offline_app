package com.jlm.translator.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

/**
 * TODO
 * @author Create by 鲁超 on 2022/4/18 0018 17:15:26
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
interface BaseDao<T> {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveList(list: List<T>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(vararg arr: T)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(arr: List<T>)

    @Transaction
    @Update
    fun upd(vararg arr: T)

    @Transaction
    @Update
    fun upd(arr: List<T>)

    @Transaction
    @Delete
    fun del(vararg arr: T)

    @Transaction
    @Delete
    fun del(arr: List<T>)
}