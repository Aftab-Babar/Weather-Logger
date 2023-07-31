package com.example.weather.ui.weather_report

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.db.entities.ReportEntity
import com.example.weather.models.WeatherReport
import com.example.weather.utils.Resource
import com.example.weather.utils.SessionManager
import com.example.weather.utils.Tools.convertToJsonString
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherReportViewModel @Inject constructor(
    private val repository: WeatherReportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _weatherReportLiveData by lazy { MutableLiveData<Resource<WeatherReport>>() }
    val weatherReportLiveData: LiveData<Resource<WeatherReport>>
        get() = _weatherReportLiveData


    fun getWeatherReport(latitude: String, longitude: String, units: String) =
        viewModelScope.launch {
            _weatherReportLiveData.postValue(Resource.Loading())
            val response = repository.getWeatherReport(latitude, longitude, units)
            if (response.isSuccessful) {
                response.body()?.let {
                    sessionManager.saveReport(it.convertToJsonString())
                    _weatherReportLiveData.postValue(Resource.Success(it))

                }
            } else {
                _weatherReportLiveData.postValue(
                    Resource.Error(
                        response.message(),
                        response.code()
                    )
                )
            }

        }

    fun setWeatherReport(report: WeatherReport) {
        _weatherReportLiveData.postValue(Resource.Success(report))
    }


    fun getLatestReport() = viewModelScope.launch {
        _weatherReportLiveData.postValue(Resource.Loading())

        try {

            val reportJson = sessionManager.getReport().first()

            if (reportJson.isNullOrBlank()){
                _weatherReportLiveData.postValue(
                    Resource.Error(
                        "No report available. Please connect to the internet.",
                        -1
                    )
                )
                return@launch
            }

            val report = Gson().fromJson(reportJson, WeatherReport::class.java)

            delay(500)
            _weatherReportLiveData.postValue(Resource.Success(report))

        } catch (jse: JsonSyntaxException) {

            _weatherReportLiveData.postValue(Resource.Error(jse.message, -1))

        }



    }

    private val reports = arrayListOf<ReportEntity>()
    private val _weatherReportsLiveData by lazy { MutableLiveData<Resource<List<ReportEntity>>>() }
    val weatherReportsLiveData: LiveData<Resource<List<ReportEntity>>>
        get() = _weatherReportsLiveData

    fun getWeatherReports() = viewModelScope.launch {

        reports.clear()

        _weatherReportsLiveData.postValue(Resource.Loading())

        reports.addAll(repository.getReportDao().getReports())

        delay(500)

        _weatherReportsLiveData.postValue(Resource.Success(reports))

    }

    private val _insertReportLiveData by lazy { MutableLiveData<Resource<ReportEntity>>() }
    val insertReportLiveData: LiveData<Resource<ReportEntity>>
        get() = _insertReportLiveData

    fun insertReport(date: String) = viewModelScope.launch {
        _insertReportLiveData.postValue(Resource.Loading())

        val currentReport = weatherReportLiveData.value?.data

        if (currentReport == null) {
            _insertReportLiveData.postValue(
                Resource.Error(
                    "Unable to save the report. No Current report found.",
                    -1
                )
            )
            return@launch
        }

        val reportEntity = ReportEntity(report = currentReport, storedOn = date)

        try {
            val rowsInserted = repository.getReportDao().insertReport(reportEntity)

            if (rowsInserted == 0L) {
                _insertReportLiveData.postValue(Resource.Error("Unable to save the report.", -1))
                return@launch
            }

            //delay(500)

            reports.add(reportEntity)

            _weatherReportsLiveData.postValue(Resource.Success(reports))
        } catch (sqe: SQLiteException) {
            _insertReportLiveData.postValue(Resource.Error(sqe.message, -1))
        }

        delay(500)
        _insertReportLiveData.postValue(null)

    }

}