package com.watchmymoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme
import com.watchmymoney.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val dynamicColors = dynamicColorScheme(LocalContext.current)
            MaterialTheme(
                colorScheme = dynamicColors ?: ColorScheme()
            ) {
                MainScreen()
            }
        }
    }
}
