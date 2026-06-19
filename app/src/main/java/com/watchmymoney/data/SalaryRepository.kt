package com.watchmymoney.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserConfig(
    val annualSalary: Double = 0.0,
    val currencySymbol: String = "$",
    val resetHour: Int = 0,
    val workHourStart: Int = 9,
    val workHourEnd: Int = 18
)

class SalaryRepository(private val context: Context) {

    private object Keys {
        val ANNUAL_SALARY = doublePreferencesKey("annual_salary")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val RESET_HOUR = intPreferencesKey("reset_hour")
        val WORK_HOUR_START = intPreferencesKey("work_hour_start")
        val WORK_HOUR_END = intPreferencesKey("work_hour_end")
    }

    val userConfig: Flow<UserConfig> = context.dataStore.data
        .map { preferences ->
            UserConfig(
                annualSalary = preferences[Keys.ANNUAL_SALARY] ?: 0.0,
                currencySymbol = preferences[Keys.CURRENCY_SYMBOL] ?: "$",
                resetHour = preferences[Keys.RESET_HOUR] ?: 0,
                workHourStart = preferences[Keys.WORK_HOUR_START] ?: 9,
                workHourEnd = preferences[Keys.WORK_HOUR_END] ?: 18
            )
        }

    suspend fun updateAnnualSalary(salary: Double) {
        context.dataStore.edit { settings ->
            settings[Keys.ANNUAL_SALARY] = salary
        }
    }

    suspend fun updateCurrencySymbol(symbol: String) {
        context.dataStore.edit { settings ->
            settings[Keys.CURRENCY_SYMBOL] = symbol
        }
    }
    
    suspend fun updateResetHour(hour: Int) {
        context.dataStore.edit { settings ->
            settings[Keys.RESET_HOUR] = hour
        }
    }

    suspend fun updateWorkHours(start: Int, end: Int) {
        context.dataStore.edit { settings ->
            settings[Keys.WORK_HOUR_START] = start
            settings[Keys.WORK_HOUR_END] = end
        }
    }
}
