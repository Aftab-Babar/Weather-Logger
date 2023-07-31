package com.example.weather.ui.weather_report

import com.example.weather.api.ApiInterface
import com.example.weather.db.AppDatabase
import javax.inject.Inject

class WeatherReportRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val database: AppDatabase
) {

    suspend fun getWeatherReport(latitude: String, longitude: String, units: String) =
        apiInterface.getWeatherReport(latitude, longitude, units)

    fun getReportDao() = database.getReportDao()

}