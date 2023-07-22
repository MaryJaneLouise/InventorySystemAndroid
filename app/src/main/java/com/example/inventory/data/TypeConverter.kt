package com.example.inventory.data

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date

class DateConverter {
    private val dateFormat = SimpleDateFormat("MM-dd-yyyy HH:mm:ss")

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
