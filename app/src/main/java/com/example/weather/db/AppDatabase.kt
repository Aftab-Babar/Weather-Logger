package com.example.weather.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weather.db.dao.ReportDao
import com.example.weather.db.entities.ReportEntity

@Database(
    entities = [ReportEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Convertors::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getReportDao() : ReportDao

}