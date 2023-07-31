package com.example.weather.db.dao

import android.database.sqlite.SQLiteException
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weather.db.entities.ReportEntity

@Dao
interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Throws(SQLiteException::class)
    suspend fun insertReport(reportEntity: ReportEntity): Long

    @Query("SELECT * FROM REPORTS ORDER BY id DESC LIMIT 1")
    suspend fun getLatestReport() : ReportEntity?

    @Query("SELECT * FROM REPORTS")
    suspend fun getReports() : List<ReportEntity>

}