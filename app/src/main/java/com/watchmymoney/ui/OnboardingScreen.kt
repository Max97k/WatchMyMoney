package com.watchmymoney.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextButtonDefaults
import com.watchmymoney.R

@Composable
fun OnboardingScreen(
    initialSalary: Double = 0.0,
    currencySymbol: String = "$",
    onSalarySet: (Double) -> Unit,
    onCancel: (() -> Unit)? = null,
    listState: androidx.wear.compose.foundation.lazy.ScalingLazyListState = rememberScalingLazyListState()
) {
    var inputString by rememberSaveable {
        mutableStateOf(if (initialSalary > 0) initialSalary.toLong().toString() else "")
    }

    // Minimum 48x48dp touch target conforming to Wear OS Material 3 guidelines
    val buttonWidth = 48.dp
    val buttonHeight = 48.dp
    val spacing = 4.dp
    val maxDigits = 10

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Display Area
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    val displayText = if (inputString.isEmpty()) "Salary?" else "$currencySymbol$inputString"
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (inputString.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            // Row 1: 1, 2, 3
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    NumButton("1", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "1"
                    }
                    NumButton("2", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "2"
                    }
                    NumButton("3", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "3"
                    }
                }
            }

            // Row 2: 4, 5, 6
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    NumButton("4", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "4"
                    }
                    NumButton("5", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "5"
                    }
                    NumButton("6", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "6"
                    }
                }
            }

            // Row 3: 7, 8, 9
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    NumButton("7", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "7"
                    }
                    NumButton("8", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "8"
                    }
                    NumButton("9", buttonWidth, buttonHeight) {
                        if (inputString.length < maxDigits) inputString += "9"
                    }
                }
            }

            // Row 4: Del, 0, Confirm
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    // Backspace
                    FilledTonalIconButton(
                        onClick = {
                            if (inputString.isNotEmpty()) {
                                inputString = inputString.dropLast(1)
                            }
                        },
                        modifier = Modifier.size(width = buttonWidth, height = buttonHeight),
                        shapes = IconButtonDefaults.animatedShapes()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                            contentDescription = stringResource(R.string.action_delete)
                        )
                    }

                    // 0 (with leading zero prevention)
                    NumButton("0", buttonWidth, buttonHeight) {
                        if (inputString.isNotEmpty() && inputString.length < maxDigits) {
                            inputString += "0"
                        }
                    }

                    // Confirm
                    val isValid = inputString.isNotEmpty() && (inputString.toDoubleOrNull() ?: 0.0) > 0
                    FilledIconButton(
                        onClick = {
                            val salary = inputString.toDoubleOrNull()
                            if (salary != null && salary > 0) {
                                onSalarySet(salary)
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier.size(width = buttonWidth, height = buttonHeight),
                        shapes = IconButtonDefaults.animatedShapes()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.action_confirm)
                        )
                    }
                }
            }

            // Optional Cancel Button when editing an existing salary
            if (onCancel != null && initialSalary > 0) {
                item {
                    FilledTonalButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(top = 4.dp),
                        label = {
                            Text(text = stringResource(R.string.action_cancel))
                        },
                        icon = {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.action_cancel)
                            )
                        }
                    )
                }
            }
        }

        // Material 3 ScrollIndicator
        ScrollIndicator(
            state = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun NumButton(
    text: String,
    width: Dp,
    height: Dp,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.size(width = width, height = height),
        shapes = TextButtonDefaults.animatedShapes()
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
