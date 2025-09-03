package com.jlm.translator.database.manager

import com.jlm.translator.database.JLMDatabase
import com.jlm.translator.database.table.FavoriteRecordModel

object FavoriteRecordDBManager {
    private val dao by lazy { JLMDatabase.get().favoriteRecordDao() }

    /**
     * 插入一条记录
     * @param history
     */
    fun insertFavoriteRecord(history: FavoriteRecordModel) {
        dao.add(history)
    }

    /**
     * 删除一条记录
     * @param history
     */
    fun deleteFavoriteRecord(history: FavoriteRecordModel) {
        dao.del(history)
    }

    /**
     * 删除一条记录
     * @param history
     */
    fun deleteFavoriteRecord(history: List<FavoriteRecordModel>) {
        dao.del(history)
    }

    /**
     * 删除最后一条记录
     */
    fun deleteLatest() {
        dao.deleteLatest()
    }

    /**
     * 获取记录列表
     */
    fun getFavoriteRecordList(pageIndex: Int = 0, pageSize: Int = 20): MutableList<FavoriteRecordModel> {
        val start = pageIndex * pageSize
        return dao.queryAll(start, pageSize)
    }
}