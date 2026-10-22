package com.watchmymoney.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.watchmymoney.R
import com.watchmymoney.logic.SalaryCalculator
import com.watchmymoney.logic.TimeProvider

@Composable
fun TickerScreen(
    annualSalary: Double,
    currencySymbol: String,
    resetHour: Int,
    onEditClick: () -> Unit
) {
    var earnedAmount by remember { mutableDoubleStateOf(0.0) }
    var dailyProgress by remember { mutableFloatStateOf(0f) }

    // Optimized 60Hz loop with zero heap allocation per frame
    LaunchedEffect(annualSalary, resetHour) {
        var cachedDay = SalaryCalculator.calculateLogicalDay(TimeProvider.currentTimeMillis(), resetHour)
        var startMs = cachedDay.startOfLogicalDay.toEpochMilli()
        var durationMs = cachedDay.logicalDayMs
        var endMs = startMs + durationMs
        val dailySalary = annualSalary / SalaryCalculator.DAYS_PER_YEAR

        while (true) {
            withFrameMillis {
                val now = TimeProvider.currentTimeMillis()
                // Recalculate logical day boundary only upon crossing midnight/reset hour
                if (now >= endMs || now < startMs) {
                    cachedDay = SalaryCalculator.calculateLogicalDay(now, resetHour)
                    startMs = cachedDay.startOfLogicalDay.toEpochMilli()
                    durationMs = cachedDay.logicalDayMs
                    endMs = startMs + durationMs
                }

                val elapsed = (now - startMs).coerceIn(0L, durationMs)
                val fraction = if (durationMs > 0L) elapsed.toDouble() / durationMs else 0.0
                earnedAmount = dailySalary * fraction
                dailyProgress = fraction.toFloat().coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Full screen circular progress indicator tracking daily earnings progression
        CircularProgressIndicator(
            progress = { dailyProgress },
            modifier = Modifier.fillMaxSize().padding(4.dp),
            strokeWidth = 3.5.dp,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.label_you_earned),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            // Split integer and decimal for visual hierarchy
            val intPart = earnedAmount.toLong()
            val fraction = kotlin.math.abs(earnedAmount - intPart)
            var fractionalInt = kotlin.math.round(fraction * 1_000_000).toLong()
            var finalIntPart = intPart
            if (fractionalInt >= 1_000_000L) {
                fractionalInt -= 1_000_000L
                finalIntPart += if (earnedAmount >= 0) 1L else -1L
            }
            val decimalPart = fractionalInt.toString().padStart(6, '0')
            val integerPart = if (earnedAmount < 0 && finalIntPart == 0L) "-0" else finalIntPart.toString()

            // Merge accessibility semantics for TalkBack to read calm value instead of 60Hz jitter
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "$currencySymbol$integerPart"
                }
            ) {
                Text(
                    text = "$currencySymbol$integerPart",
                    style = MaterialTheme.typography.numeralLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = ".$decimalPart",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.tertiary,
                        fontFeatureSettings = "tnum"
                    ),
                    modifier = Modifier.clearAndSetSemantics {}
                )
            }
            
            // Edit / Settings button conforming to WO-V2 48x48dp touch target with animated shapes
            FilledTonalIconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(48.dp),
                shapes = IconButtonDefaults.animatedShapes()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.action_edit),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
