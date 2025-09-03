package com.jlm.translator.database.converter

import androidx.room.TypeConverter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

/**
 * TODO
 * @author Create by 鲁超 on 2021/3/25 0025 17:07
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
class DateConverters {
    @TypeConverter
    fun localDateTime2Long(date: LocalDateTime): Long {
        return date.toDateTime(DateTimeZone.UTC).millis
    }

    @TypeConverter
    fun long2LocalDateTime(value: Long): LocalDateTime {
        return DateTime(value, DateTimeZone.UTC).toLocalDateTime()
    }
}