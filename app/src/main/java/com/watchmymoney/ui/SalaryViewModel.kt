package com.watchmymoney.ui

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.watchmymoney.complication.SalaryComplicationService
import com.watchmymoney.data.SalaryRepository
import com.watchmymoney.data.UserConfig
import com.watchmymoney.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalaryViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: SalaryRepository = SalaryRepository(application.dataStore)
) : AndroidViewModel(application) {

    private val complicationRequester: ComplicationDataSourceUpdateRequester by lazy {
        ComplicationDataSourceUpdateRequester.create(
            getApplication(),
            ComponentName(getApplication(), SalaryComplicationService::class.java)
        )
    }

    val userConfig: StateFlow<UserConfig?> = repository.userConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateAnnualSalary(salary: Double) {
        viewModelScope.launch {
            repository.updateAnnualSalary(salary)
            requestComplicationUpdate()
        }
    }

    fun updateCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            repository.updateCurrencySymbol(symbol)
            requestComplicationUpdate()
        }
    }

    fun updateResetHour(hour: Int) {
        viewModelScope.launch {
            repository.updateResetHour(hour)
            requestComplicationUpdate()
        }
    }

    fun requestComplicationUpdate() {
        try {
            complicationRequester.requestUpdateAll()
        } catch (_: Exception) {
            // 忽略無 Complication Host 環境下的例外
        }
    }
}
