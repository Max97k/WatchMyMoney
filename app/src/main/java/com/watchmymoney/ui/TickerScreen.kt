package com.watchmymoney.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.watchmymoney.logic.SalaryCalculator
import kotlinx.coroutines.delay

@Composable
fun TickerScreen(
    annualSalary: Double,
    currencySymbol: String,
    resetHour: Int,
    onEditClick: () -> Unit
) {
    var earnedAmount by remember { mutableStateOf(0.0) }

    // Coroutine Loop with delay for battery optimization
    LaunchedEffect(annualSalary, resetHour) {
        while (true) {
            val now = System.currentTimeMillis()
            val result = SalaryCalculator.calculate(annualSalary, now, resetHour)
            earnedAmount = result.earnedToday
            if(earnedAmount < 0) earnedAmount = 0.0
            delay(1000L) // Update once per second
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You earned",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        
        // Use mathematical operations to avoid expensive String.format in compose loops
        val integerPart = earnedAmount.toLong()
        val decimalPart = ((earnedAmount - integerPart) * 1000000).toLong().toString().padStart(6, '0')

        Text(
            text = "$currencySymbol$integerPart",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = ".$decimalPart",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        Button(
            onClick = onEditClick,
            modifier = Modifier.padding(top = 8.dp).size(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
