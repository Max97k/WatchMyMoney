package com.watchmymoney.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

@Composable
fun MainScreen(
    viewModel: SalaryViewModel = viewModel()
) {
    val userConfigState by viewModel.userConfig.collectAsStateWithLifecycle()
    val navController = rememberSwipeDismissableNavController()

    if (userConfigState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = remember {
        if ((userConfigState?.annualSalary ?: 0.0) <= 0.0) "onboarding" else "ticker"
    }

    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("ticker") {
                val currentConfig by viewModel.userConfig.collectAsStateWithLifecycle()
                currentConfig?.let { cfg ->
                    ScreenScaffold {
                        TickerScreen(
                            annualSalary = cfg.annualSalary,
                            currencySymbol = cfg.currencySymbol,
                            resetHour = cfg.resetHour,
                            onEditClick = {
                                navController.navigate("settings")
                            }
                        )
                    }
                }
            }

            composable("settings") {
                val currentConfig by viewModel.userConfig.collectAsStateWithLifecycle()
                currentConfig?.let { cfg ->
                    val listState = androidx.wear.compose.foundation.lazy.rememberScalingLazyListState()
                    ScreenScaffold(
                        scrollState = listState
                    ) {
                        SettingsScreen(
                            userConfig = cfg,
                            listState = listState,
                            onEditSalaryClick = {
                                navController.navigate("onboarding")
                            },
                            onCurrencyChange = { newSymbol ->
                                viewModel.updateCurrencySymbol(newSymbol)
                            },
                            onResetHourChange = { newHour ->
                                viewModel.updateResetHour(newHour)
                            }
                        )
                    }
                }
            }

            composable("onboarding") {
                val currentConfig by viewModel.userConfig.collectAsStateWithLifecycle()
                val listState = androidx.wear.compose.foundation.lazy.rememberScalingLazyListState()
                ScreenScaffold(
                    scrollState = listState
                ) {
                    OnboardingScreen(
                        initialSalary = currentConfig?.annualSalary ?: 0.0,
                        currencySymbol = currentConfig?.currencySymbol ?: "$",
                        listState = listState,
                        onSalarySet = { newSalary ->
                            viewModel.updateAnnualSalary(newSalary)
                            navController.navigate("ticker") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        },
                        onCancel = if ((currentConfig?.annualSalary ?: 0.0) > 0.0) {
                            { navController.popBackStack() }
                        } else null
                    )
                }
            }
        }
    }
}
