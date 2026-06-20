package com.watchmymoney.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun OnboardingScreen(
    onSalarySet: (Double) -> Unit
) {
    var inputString by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display Area
        Text(
            text = if (inputString.isEmpty()) "Salary?" else "$$inputString",
            style = MaterialTheme.typography.titleMedium,
            color = if (inputString.isEmpty()) Color.Gray else MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        // Number Pad - 3 Columns
        // Layout:
        // 1 2 3
        // 4 5 6
        // 7 8 9
        // ⌫ 0 ✔️

        val buttonSize = 48.dp // Large comfortable touch target
        val spacing = 4.dp

        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            NumButton("1", buttonSize) { inputString += "1" }
            NumButton("2", buttonSize) { inputString += "2" }
            NumButton("3", buttonSize) { inputString += "3" }
        }
        Spacer(modifier = Modifier.height(spacing))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            NumButton("4", buttonSize) { inputString += "4" }
            NumButton("5", buttonSize) { inputString += "5" }
            NumButton("6", buttonSize) { inputString += "6" }
        }
        Spacer(modifier = Modifier.height(spacing))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            NumButton("7", buttonSize) { inputString += "7" }
            NumButton("8", buttonSize) { inputString += "8" }
            NumButton("9", buttonSize) { inputString += "9" }
        }
        Spacer(modifier = Modifier.height(spacing))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            // Backspace
            Button(
                onClick = {
                    if (inputString.isNotEmpty()) {
                        inputString = inputString.dropLast(1)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.size(buttonSize)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Del")
            }

            // 0
            NumButton("0", buttonSize) { if (inputString.isNotEmpty()) inputString += "0" }

            // Confirm
            val isValid = inputString.isNotEmpty() && (inputString.toDoubleOrNull() ?: 0.0) > 0
            Button(
                onClick = {
                    val salary = inputString.toDoubleOrNull()
                    if (salary != null && salary > 0) {
                        onSalarySet(salary)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(buttonSize)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = "Confirm")
            }
        }
    }
}

@Composable
fun NumButton(text: String, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.size(size)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
