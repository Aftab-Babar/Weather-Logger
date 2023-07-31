package com.example.weather.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weather.models.WeatherReport

@Entity(
    tableName = "reports",
)
data class ReportEntity(

    @PrimaryKey(autoGenerate = true)
     var id: Int = 0,

    val report: WeatherReport,

    val storedOn: String
)
