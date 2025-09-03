package com.jlm.translator.database.manager

import com.jlm.translator.database.JLMDatabase
import com.jlm.translator.database.table.TranslateHistory
import com.safframework.log.L

/***记录历史管理*/
object TranslateHistoryDBManager {
    val TAG = javaClass.name
    private val dao by lazy { JLMDatabase.get().translateHistoryDao() }

    /**
     * 插入一条记录
     * @param history
     */
    fun insertTranslateHistory(history: TranslateHistory) {
        dao.add(history)
    }

    /**
     * 获取记录列表
     */
    fun getTranslateHistoryList(mode: Int = 0, search: String = "", pageIndex: Int = 0, pageSize: Int = 20): MutableList<TranslateHistory> {
        val start = pageIndex * pageSize
        L.d(TAG, "getTranslateHistoryList---mode=$mode-----search=$search-----start=$start-----pageSize=$pageSize")
        return if (mode > 0) {
            dao.queryAllByMode(mode, search, start, pageSize)
        } else {
            dao.queryAll(start, search, pageSize)
        }
    }

    /**
     * 根据版本获取记录列表
     */
    fun getTranslateHistoryListByVersion(versionCode: Int, mode: Int = 0, search: String = "", pageIndex: Int = 0, pageSize: Int = 20): MutableList<TranslateHistory> {
        val start = pageIndex * pageSize
        L.d(TAG, "getTranslateHistoryListByVersion---versionCode=$versionCode---mode=$mode-----search=$search-----start=$start-----pageSize=$pageSize")
        return if (mode > 0) {
            dao.queryAllByVersionAndMode(versionCode, mode, search, start, pageSize)
        } else {
            dao.queryAllByVersion(versionCode, start, search, pageSize)
        }
    }

    /**
     * 根据版本和多个模式获取记录列表（用于合并查询）
     */
    fun getTranslateHistoryListByVersionAndModes(versionCode: Int, modes: List<Int>, search: String = "", pageIndex: Int = 0, pageSize: Int = 20): MutableList<TranslateHistory> {
        val start = pageIndex * pageSize
        L.d(TAG, "getTranslateHistoryListByVersionAndModes---versionCode=$versionCode---modes=$modes-----search=$search-----start=$start-----pageSize=$pageSize")
        return if (modes.isNotEmpty()) {
            dao.queryAllByVersionAndModes(versionCode, modes, search, start, pageSize)
        } else {
            dao.queryAllByVersion(versionCode, start, search, pageSize)
        }
    }

    /**
     * 获取记录列表
     */
    fun getTranslateHistoryListByUUID(uuid: String, pageIndex: Int = 0, pageSize: Int = 20): MutableList<TranslateHistory> {
        val start = pageIndex * pageSize
        return dao.queryByUUID(uuid, start, pageSize)
    }

    fun getTranslateHistoryListByUUIDs(uuids: List<String>): MutableList<TranslateHistory> {
        return dao.queryByUUIDs(uuids)
    }

    /**
     * 删除UUID相关的翻译记录、相当于删除一整组
     */
    fun deleteByUUID(uuid: String) {
        dao.deleteByUUID(uuid)
    }

    /**
     * 删除UUID相关的翻译记录、相当于删除一整组
     */
    fun deleteByUUIDList(uuid: List<String>) {
        dao.deleteByUUIDList(uuid)
    }

    /**
     * 删除ID相关的翻译记录
     */
    fun deleteByIDs(ids: List<Long>) {
        dao.deleteByIDs(ids)
    }

    fun update(data: List<TranslateHistory>) {
        dao.upd(data)
    }
}