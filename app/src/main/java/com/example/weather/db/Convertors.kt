package com.example.weather.db

import androidx.room.TypeConverter
import com.example.weather.models.WeatherReport
import com.example.weather.utils.Tools.convertToJsonString
import com.google.gson.Gson

class Convertors {

    @TypeConverter
    fun fromWeatherReport(report: WeatherReport) =
        report.convertToJsonString()

    @TypeConverter
    fun toWeatherReport(reportJson: String): WeatherReport {

        return Gson().fromJson(reportJson, WeatherReport::class.java)
    }


}
