package com.watchmymoney.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.watchmymoney.R
import com.watchmymoney.data.UserConfig

@Composable
fun SettingsScreen(
    userConfig: UserConfig,
    onEditSalaryClick: () -> Unit,
    onCurrencyChange: (String) -> Unit,
    onResetHourChange: (Int) -> Unit
) {
    val listState = rememberScalingLazyListState()
    val currencyOptions = listOf("$", "NT$", "€", "£", "¥", "₩")
    val resetHourOptions = listOf(0, 6, 8, 9, 12, 18, 21)

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                ListHeader {
                    Text(text = stringResource(R.string.label_settings))
                }
            }

            // 1. 年薪修改
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onEditSalaryClick,
                    label = { Text(text = stringResource(R.string.label_salary_prompt)) },
                    secondaryLabel = {
                        val formattedSalary = userConfig.annualSalary.toLong().toString()
                        Text(text = "${userConfig.currencySymbol}$formattedSalary")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.action_edit)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }

            // 2. 貨幣切換
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val currentIndex = currencyOptions.indexOf(userConfig.currencySymbol)
                        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % currencyOptions.size else 0
                        onCurrencyChange(currencyOptions[nextIndex])
                    },
                    label = { Text(text = stringResource(R.string.label_currency)) },
                    secondaryLabel = { Text(text = userConfig.currencySymbol) },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.AttachMoney,
                            contentDescription = stringResource(R.string.label_currency)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }

            // 3. 重置時間切換
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val currentIndex = resetHourOptions.indexOf(userConfig.resetHour)
                        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % resetHourOptions.size else 0
                        onResetHourChange(resetHourOptions[nextIndex])
                    },
                    label = { Text(text = stringResource(R.string.label_reset_hour)) },
                    secondaryLabel = {
                        val hourStr = stringResource(R.string.format_hour_display, userConfig.resetHour)
                        Text(text = hourStr)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = stringResource(R.string.label_reset_hour)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}
