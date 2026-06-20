package com.watchmymoney.ui

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()
    }

    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()
    }

    @Test
    fun testFirstLaunchState() {
        // A-1 equivalent: First launch state (empty input, confirm disabled)
        composeTestRule.setContent {
            OnboardingScreen(onSalarySet = {})
        }

        // Verify initial placeholder "Salary?" is displayed
        composeTestRule.onNodeWithText("Salary?").assertExists()

        // Verify confirm button is disabled initially
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsNotEnabled()
    }

    @Test
    fun testNumpadInputFormatting() {
        // A-2 equivalent: Numpad input formatting (digits append properly)
        composeTestRule.setContent {
            OnboardingScreen(onSalarySet = {})
        }

        // Tap numbers 1, 5, 8
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("8").performClick()

        // Verify the display updates to "$158"
        composeTestRule.onNodeWithText("$158").assertExists()
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsEnabled()
    }

    @Test
    fun testLeadingZeroPrevention() {
        // A-3 equivalent: Leading zero prevention
        composeTestRule.setContent {
            OnboardingScreen(onSalarySet = {})
        }

        // Tap "0" first
        composeTestRule.onNodeWithText("0").performClick()

        // Input should remain empty, displaying "Salary?"
        composeTestRule.onNodeWithText("Salary?").assertExists()
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsNotEnabled()

        // Tap "2", then "0"
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("0").performClick()

        // Should display "$20"
        composeTestRule.onNodeWithText("$20").assertExists()
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsEnabled()
    }

    @Test
    fun testBackspaceBehavior() {
        // A-4 equivalent: Backspace behavior
        composeTestRule.setContent {
            OnboardingScreen(onSalarySet = {})
        }

        // Tap "7", "9"
        composeTestRule.onNodeWithText("7").performClick()
        composeTestRule.onNodeWithText("9").performClick()
        composeTestRule.onNodeWithText("$79").assertExists()

        // Tap Backspace
        composeTestRule.onNodeWithContentDescription("Del").performClick()
        composeTestRule.onNodeWithText("$7").assertExists()

        // Tap Backspace again
        composeTestRule.onNodeWithContentDescription("Del").performClick()
        composeTestRule.onNodeWithText("Salary?").assertExists()
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsNotEnabled()
    }

    @Test
    fun testConfirmClickTrigger() {
        // A-5 equivalent: Confirm click trigger calls callback with double salary
        var capturedSalary: Double? = null
        composeTestRule.setContent {
            OnboardingScreen(onSalarySet = { salary ->
                capturedSalary = salary
            })
        }

        // Enter 50000
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText("0").performClick()

        composeTestRule.onNodeWithText("$50000").assertExists()
        
        // Tap Confirm
        composeTestRule.onNodeWithContentDescription("Confirm").performClick()

        // Verify the callback received 50000.0
        assertEquals(50000.0, capturedSalary)
    }
}
