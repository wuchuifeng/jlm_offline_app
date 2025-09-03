package com.jlm.translator.database

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec

object DatabaseUpgrade {
    @DeleteTable(tableName = "test")
    class DemoAutoUpgrade0To1 : AutoMigrationSpec

    class AutoUpgrade2To3 : AutoMigrationSpec

    @DeleteColumn(tableName = "FavoriteRecordModel", columnName = "isChecked")
    @RenameColumn(tableName = "FavoriteRecordModel", fromColumnName = "translationType", toColumnName = "targetKey")
    class AutoUpgrade3To4 : AutoMigrationSpec

    class AutoUpgrade4To5 : AutoMigrationSpec
    class AutoUpgrade5To6 : AutoMigrationSpec
    class AutoUpgrade6To7 : AutoMigrationSpec
}