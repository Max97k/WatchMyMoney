package com.watchmymoney.ui

import android.content.Context
import android.os.SystemClock
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
class TickerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()

        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()

        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun testInitialZeroState() {
        // T-1: Initial state before any clock ticks
        composeTestRule.mainClock.autoAdvance = false
        
        composeTestRule.setContent {
            TickerScreen(
                annualSalary = 100000.0,
                currencySymbol = "$",
                resetHour = 0,
                onEditClick = {}
            )
        }

        // Ticking the clock once to run initial layout pass
        composeTestRule.mainClock.advanceTimeBy(16)

        // The earned amount defaults to 0.0 initially before animation loop runs
        composeTestRule.onNodeWithText("$0").assertExists()
        composeTestRule.onNodeWithText(".000000").assertExists()
    }

    @Test
    fun testIntegerAndDecimalSplitDisplay() {
        // T-2: Split display of integer and decimal parts
        composeTestRule.mainClock.autoAdvance = false

        // Base time: 2026-06-20 12:00:00 UTC (Noon)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 20, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetTimeMs = calendar.timeInMillis
        SystemClock.setCurrentTimeMillis(targetTimeMs)

        // Annual salary: 365250.0 (Daily salary: 1000.0)
        // At 12:00:00 UTC (resetHour = 0), progress is exactly 0.5, so earnedToday = 500.0
        composeTestRule.setContent {
            TickerScreen(
                annualSalary = 365250.0,
                currencySymbol = "£",
                resetHour = 0,
                onEditClick = {}
            )
        }

        // Tick compose clock to trigger LaunchedEffect and frame callback
        composeTestRule.mainClock.advanceTimeBy(16)

        // Verify the split display shows £500 and .000000
        composeTestRule.onNodeWithText("£500").assertExists()
        composeTestRule.onNodeWithText(".000000").assertExists()
    }

    @Test
    fun testEditButtonClickResets() {
        // T-3: Edit button click trigger callback
        var editClicked = false
        composeTestRule.setContent {
            TickerScreen(
                annualSalary = 50000.0,
                currencySymbol = "$",
                resetHour = 0,
                onEditClick = { editClicked = true }
            )
        }

        // Click the edit button
        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        // Verify callback was triggered
        assertTrue(editClicked)
    }

    @Test
    fun testRealTimeTickerAnimationLoop() {
        // T-4: Real-time ticker animation loop advancing through frames
        composeTestRule.mainClock.autoAdvance = false

        // Set start time: 2026-06-20 00:00:00 UTC (Midnight)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 20, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        SystemClock.setCurrentTimeMillis(calendar.timeInMillis)

        // Daily salary: 10000.0 (Annual: 3652500.0)
        composeTestRule.setContent {
            TickerScreen(
                annualSalary = 3652500.0,
                currencySymbol = "$",
                resetHour = 0,
                onEditClick = {}
            )
        }

        // Tick first frame: System clock is midnight (progress = 0)
        composeTestRule.mainClock.advanceTimeBy(16)
        composeTestRule.onNodeWithText("$0").assertExists()

        // Advance ShadowSystemClock by 12 hours (43,200,000 ms)
        ShadowSystemClock.advanceBy(Duration.ofHours(12))

        // Tick Compose clock to process the next frame
        composeTestRule.mainClock.advanceTimeBy(16)

        // Should display $5000 and .000000 (half of 10000.0 daily salary)
        composeTestRule.onNodeWithText("$5000").assertExists()

        // Advance ShadowSystemClock by another 6 hours
        ShadowSystemClock.advanceBy(Duration.ofHours(6))
        composeTestRule.mainClock.advanceTimeBy(16)

        // Should display $7500 and .000000 (75% of daily salary)
        composeTestRule.onNodeWithText("$7500").assertExists()
    }

    @Test
    fun testTickerCustomResetHour() {
        // T-5: Ticker screen respects custom resetHour parameter
        composeTestRule.mainClock.autoAdvance = false

        // Set start time: 2026-06-20 00:00:00 UTC (Midnight)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 20, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        SystemClock.setCurrentTimeMillis(calendar.timeInMillis)

        // Annual salary: 365250.0 (Daily salary: 1000.0)
        // Reset hour: 9 (9 AM)
        composeTestRule.setContent {
            TickerScreen(
                annualSalary = 365250.0,
                currencySymbol = "$",
                resetHour = 9,
                onEditClick = {}
            )
        }

        // Tick first frame: System clock is midnight (progress = 15/24 = 0.625 -> $625)
        composeTestRule.mainClock.advanceTimeBy(16)
        composeTestRule.onNodeWithText("$625").assertExists()

        // Advance ShadowSystemClock by 12 hours to reach 12 PM (Noon) today.
        // Today Noon is after 9 AM, so elapsed time is 3 hours (fraction = 0.125 -> $125).
        ShadowSystemClock.advanceBy(Duration.ofHours(12))

        // Tick Compose clock to process the next frame
        composeTestRule.mainClock.advanceTimeBy(16)

        // Should display $125
        composeTestRule.onNodeWithText("$125").assertExists()
    }
}
