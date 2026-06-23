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
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
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
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()

        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        repository = SalaryRepository(context.dataStore)
        service = Robolectric.buildService(SalaryComplicationService::class.java).create().get()
    }

    @After
    fun tearDown() {
        context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()
        TimeZone.setDefault(originalTimeZone)
        SalaryComplicationService.testClock = null
    }

    private fun setMockClock(isoString: String) {
        val fixedInstant = Instant.parse(isoString)
        SalaryComplicationService.testClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    }

    @Test
    fun testShortTextComplicationFormatting() = runTest {
        repository.updateAnnualSalary(365250.0)
        repository.updateCurrencySymbol("$")
        repository.updateResetHour(0)

        // 2026-06-20 12:00:00 UTC
        setMockClock("2026-06-20T12:00:00Z")

        val request = ComplicationRequest(101, ComplicationType.SHORT_TEXT, false)
        val data = service.onComplicationRequest(request) as? ShortTextComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        // With explicit testClock, platformTime is simulated correctly and statically evaluates on getTextAt
        val text = data!!.text.getTextAt(context.resources, Instant.ofEpochMilli(0)).toString()
        assertEquals("$500", text)
    }

    @Test
    fun testLongTextComplicationFormatting() = runTest {
        repository.updateAnnualSalary(365250.0)
        repository.updateCurrencySymbol("€")
        repository.updateResetHour(0)

        // 2026-06-20 06:00:00 UTC
        setMockClock("2026-06-20T06:00:00Z")

        val request = ComplicationRequest(102, ComplicationType.LONG_TEXT, false)
        val data = service.onComplicationRequest(request) as? LongTextComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        val text = data!!.text.getTextAt(context.resources, Instant.ofEpochMilli(0)).toString()
        assertEquals("Today: €250", text)
    }

    @Test
    fun testRangedValueComplicationFormatting() = runTest {
        repository.updateAnnualSalary(365250.0)
        repository.updateCurrencySymbol("£")
        repository.updateResetHour(0)

        // 2026-06-20 18:00:00 UTC
        setMockClock("2026-06-20T18:00:00Z")

        val request = ComplicationRequest(103, ComplicationType.RANGED_VALUE, false)
        val data = service.onComplicationRequest(request) as? RangedValueComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        assertEquals(0.75f, data!!.value, 1e-4f)
        assertEquals(0f, data.min, 1e-4f)
        assertEquals(1f, data.max, 1e-4f)

        val text = data.text!!.getTextAt(context.resources, Instant.ofEpochMilli(0)).toString()
        assertEquals("£750", text)
    }

    @Test
    fun testTapActionIntentTargetAndFlags() = runTest {
        repository.updateAnnualSalary(100000.0)

        val request = ComplicationRequest(104, ComplicationType.SHORT_TEXT, false)
        val data = service.onComplicationRequest(request) as? ShortTextComplicationData
        
        assertNotNull("ComplicationData should not be null", data)
        val pendingIntent = data!!.tapAction
        assertNotNull("Tap action should be defined", pendingIntent)

        val shadowPendingIntent = shadowOf(pendingIntent)
        assertTrue(shadowPendingIntent.isActivityIntent)
        
        val savedIntent = shadowPendingIntent.savedIntent
        assertEquals(MainActivity::class.java.name, savedIntent.component?.className)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, savedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun testStaticPreviewData() {
        val shortPreview = service.getPreviewData(ComplicationType.SHORT_TEXT) as? ShortTextComplicationData
        assertNotNull(shortPreview)
        assertEquals("$120", shortPreview!!.text.getTextAt(context.resources, Instant.ofEpochMilli(0)).toString())

        val longPreview = service.getPreviewData(ComplicationType.LONG_TEXT) as? LongTextComplicationData
        assertNotNull(longPreview)
        assertEquals("Today: $120", longPreview!!.text.getTextAt(context.resources, Instant.ofEpochMilli(0)).toString())

        val rangedPreview = service.getPreviewData(ComplicationType.RANGED_VALUE) as? RangedValueComplicationData
        assertNotNull(rangedPreview)
        assertEquals(0.5f, rangedPreview!!.value, 1e-4f)
        assertEquals("$120", rangedPreview.text!!.getTextAt(context.resources, Instant.ofEpochMilli(0)).toString())
    }
}
