package com.jlm.translator.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cn.chawloo.base.ext.application
import com.jlm.translator.database.dao.FavoriteRecordDao
import com.jlm.translator.database.dao.TranslateHistoryDao
import com.jlm.translator.database.table.FavoriteRecordModel
import com.jlm.translator.database.table.TranslateHistory

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/10 10:23
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
@Database(
    version = 7,
    entities = [
        TranslateHistory::class,
        FavoriteRecordModel::class,
    ],
    autoMigrations = [
        AutoMigration(from = 2, to = 3, spec = DatabaseUpgrade.AutoUpgrade2To3::class),
        AutoMigration(from = 3, to = 4, spec = DatabaseUpgrade.AutoUpgrade3To4::class),
        AutoMigration(from = 4, to = 5, spec = DatabaseUpgrade.AutoUpgrade4To5::class),
        AutoMigration(from = 5, to = 6, spec = DatabaseUpgrade.AutoUpgrade5To6::class),
        AutoMigration(from = 6, to = 7, spec = DatabaseUpgrade.AutoUpgrade6To7::class),
    ]
)
abstract class JLMDatabase : RoomDatabase() {
    abstract fun translateHistoryDao(): TranslateHistoryDao
    abstract fun favoriteRecordDao(): FavoriteRecordDao

    companion object {
        fun get(): JLMDatabase {
            return Room.databaseBuilder(application, JLMDatabase::class.java, "jlm_translate.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}