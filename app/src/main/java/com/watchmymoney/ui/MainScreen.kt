package com.watchmymoney.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val userConfig by viewModel.userConfig.collectAsState()

    if (userConfig == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Wait for data to load
            androidx.wear.compose.material3.CircularProgressIndicator()
        }
        return
    }

    val config = userConfig!!

    if (config.annualSalary <= 0) {
        OnboardingScreen(
            onSalarySet = { newSalary ->
                viewModel.updateSalary(newSalary)
            }
        )
    } else {
        TickerScreen(
            annualSalary = config.annualSalary,
            currencySymbol = config.currencySymbol,
            resetHour = config.resetHour,
            onEditClick = {
                viewModel.updateSalary(0.0)
            }
        )
    }
}
