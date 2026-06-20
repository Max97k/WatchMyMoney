package com.watchmymoney.complication

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.watchmymoney.MainActivity
import com.watchmymoney.data.SalaryRepository
import com.watchmymoney.data.dataStore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Instant
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
class SalaryComplicationServiceTest {

    private lateinit var context: Context
    private lateinit var service: SalaryComplicationService
    private lateinit var repository: SalaryRepository
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clean up datastore directory to prevent state leakage
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()

        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        // Instantiate repository using context
        repository = SalaryRepository(context.dataStore)

        // Build service under Robolectric context
        service = Robolectric.buildService(SalaryComplicationService::class.java).create().get()
    }

    @After
    fun tearDown() {
        // Clean up datastore directory to prevent state leakage
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()

        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun testShortTextComplicationFormatting() = runTest {
        // C-11 equivalent: SHORT_TEXT complication formatting
        // Setup user configuration in DataStore
        repository.updateAnnualSalary(365250.0) // Daily salary: 1000.0
        repository.updateCurrencySymbol("$")
        repository.updateResetHour(0)

        // Set system clock to 2026-06-20 12:00:00 UTC (Noon -> progress = 0.5 -> earned = 500)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 20, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        SystemClock.setCurrentTimeMillis(calendar.timeInMillis)

        // Request Short Text complication
        val request = ComplicationRequest(101, ComplicationType.SHORT_TEXT, false)
        val data = service.onComplicationRequest(request) as? ShortTextComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        val text = data!!.text.getTextAt(context.resources, java.time.Instant.ofEpochMilli(0)).toString()
        assertEquals("$500", text)
    }

    @Test
    fun testLongTextComplicationFormatting() = runTest {
        // C-12 equivalent: LONG_TEXT complication formatting
        // Setup user configuration in DataStore
        repository.updateAnnualSalary(365250.0) // Daily salary: 1000.0
        repository.updateCurrencySymbol("€")
        repository.updateResetHour(0)

        // Set system clock to 2026-06-20 06:00:00 UTC (6 AM -> progress = 0.25 -> earned = 250)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 20, 6, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        SystemClock.setCurrentTimeMillis(calendar.timeInMillis)

        // Request Long Text complication
        val request = ComplicationRequest(102, ComplicationType.LONG_TEXT, false)
        val data = service.onComplicationRequest(request) as? LongTextComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        val text = data!!.text.getTextAt(context.resources, java.time.Instant.ofEpochMilli(0)).toString()
        assertEquals("Today: €250", text)
    }

    @Test
    fun testRangedValueComplicationFormatting() = runTest {
        // C-13 equivalent: RANGED_VALUE complication formatting
        // Setup user configuration in DataStore
        repository.updateAnnualSalary(365250.0) // Daily salary: 1000.0
        repository.updateCurrencySymbol("£")
        repository.updateResetHour(0)

        // Set system clock to 2026-06-20 18:00:00 UTC (6 PM -> progress = 0.75 -> earned = 750)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 20, 18, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        SystemClock.setCurrentTimeMillis(calendar.timeInMillis)

        // Request Ranged Value complication
        val request = ComplicationRequest(103, ComplicationType.RANGED_VALUE, false)
        val data = service.onComplicationRequest(request) as? RangedValueComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        assertEquals(0.75f, data!!.value, 1e-4f)
        assertEquals(0f, data.min, 1e-4f)
        assertEquals(1f, data.max, 1e-4f)
        val text = data.text!!.getTextAt(context.resources, java.time.Instant.ofEpochMilli(0)).toString()
        assertEquals("£750", text)
    }

    @Test
    fun testTapActionIntentTargetAndFlags() = runTest {
        // C-14 equivalent: tap action intent target and flags
        repository.updateAnnualSalary(100000.0)

        val request = ComplicationRequest(104, ComplicationType.SHORT_TEXT, false)
        val data = service.onComplicationRequest(request) as? ShortTextComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        val pendingIntent = data!!.tapAction
        assertNotNull("Tap action should be defined", pendingIntent)

        // Verify using Robolectric ShadowPendingIntent
        val shadowPendingIntent = shadowOf(pendingIntent)
        assertTrue(shadowPendingIntent.isActivityIntent)
        
        val savedIntent = shadowPendingIntent.savedIntent
        assertEquals(MainActivity::class.java.name, savedIntent.component?.className)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, savedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun testStaticPreviewData() {
        // C-15 equivalent: static preview data validation
        val shortPreview = service.getPreviewData(ComplicationType.SHORT_TEXT) as? ShortTextComplicationData
        assertNotNull(shortPreview)
        assertEquals("$120.50", shortPreview!!.text.getTextAt(context.resources, java.time.Instant.ofEpochMilli(0)).toString())

        val longPreview = service.getPreviewData(ComplicationType.LONG_TEXT) as? LongTextComplicationData
        assertNotNull(longPreview)
        assertEquals("Today: $120.50", longPreview!!.text.getTextAt(context.resources, java.time.Instant.ofEpochMilli(0)).toString())

        val rangedPreview = service.getPreviewData(ComplicationType.RANGED_VALUE) as? RangedValueComplicationData
        assertNotNull(rangedPreview)
        assertEquals(0.5f, rangedPreview!!.value, 1e-4f)
        assertEquals("$120.50", rangedPreview.text!!.getTextAt(context.resources, java.time.Instant.ofEpochMilli(0)).toString())
    }
}
