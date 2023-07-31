package com.example.weather.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFERENCES)


    private val reportDataPreferences = stringPreferencesKey(Constants.WEATHER_REPORT)


    suspend fun saveReport(report: String) {
        context.dataStore.edit {
            it[reportDataPreferences] = report
        }
    }

    fun getReport() = context.dataStore.data.map {
        it[reportDataPreferences]
    }


}