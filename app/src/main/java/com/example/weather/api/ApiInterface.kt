package com.example.weather.api

import com.example.weather.models.WeatherReport
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("data/2.5/weather")
    suspend fun getWeatherReport(@Query("lat")latitude : String, @Query("lon") longitude : String, @Query("units") units : String): Response<WeatherReport>


}