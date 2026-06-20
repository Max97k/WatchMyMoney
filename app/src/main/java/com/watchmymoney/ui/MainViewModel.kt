package com.watchmymoney.ui

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.watchmymoney.complication.SalaryComplicationService
import com.watchmymoney.data.SalaryRepository
import com.watchmymoney.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SalaryRepository(application.dataStore)

    val userConfig = repository.userConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val complicationRequester = ComplicationDataSourceUpdateRequester.create(
        application,
        ComponentName(application, SalaryComplicationService::class.java)
    )

    fun updateSalary(newSalary: Double) {
        viewModelScope.launch {
            repository.updateAnnualSalary(newSalary)
            complicationRequester.requestUpdateAll()
        }
    }
}
