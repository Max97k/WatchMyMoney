package com.watchmymoney.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class SalaryCalculatorTest {

    private val zoneNy = ZoneId.of("America/New_York")
    private val zoneUtc = ZoneId.of("UTC")

    @Test
    fun testZeroSalary() {
        val result = SalaryCalculator.calculate(
            annualSalary = 0.0,
            currentTimeMs = System.currentTimeMillis(),
            resetHour = 0
        )
        assertEquals(0.0, result.earnedToday, 0.0)
        assertEquals(0f, result.progress)
    }

    @Test
    fun testNegativeSalary() {
        val result = SalaryCalculator.calculate(
            annualSalary = -50000.0,
            currentTimeMs = System.currentTimeMillis(),
            resetHour = 0
        )
        assertEquals(0.0, result.earnedToday, 0.0)
        assertEquals(0f, result.progress)
    }

    @Test
    fun testStandardDayCalculation() {
        // Standard non-DST day: March 10, 2026 (Tuesday)
        // Let's set start of day (midnight)
        val startOfToday = LocalDateTime.of(2026, 3, 10, 0, 0, 0)
            .atZone(zoneUtc)
            .toInstant()
            .toEpochMilli()

        // 12 hours later (noon)
        val noon = LocalDateTime.of(2026, 3, 10, 12, 0, 0)
            .atZone(zoneUtc)
            .toInstant()
            .toEpochMilli()

        val annualSalary = 365.25 * 100.0 // daily salary is 100.0

        val result = SalaryCalculator.calculate(
            annualSalary = annualSalary,
            currentTimeMs = noon,
            resetHour = 0,
            zoneId = zoneUtc
        )

        // Since it's exactly 12 hours out of a 24 hour day, progress should be 0.5 and earned should be 50.0
        assertEquals(0.5f, result.progress, 1e-6f)
        assertEquals(50.0, result.earnedToday, 1e-6)
    }

    @Test
    fun testLogicalDayTransitionResetHour() {
        // Reset hour at 08:00
        // Current time is March 10, 2026 at 06:00
        val timeMs = LocalDateTime.of(2026, 3, 10, 6, 0, 0)
            .atZone(zoneUtc)
            .toInstant()
            .toEpochMilli()

        val annualSalary = 365.25 * 100.0

        val result = SalaryCalculator.calculate(
            annualSalary = annualSalary,
            currentTimeMs = timeMs,
            resetHour = 8,
            zoneId = zoneUtc
        )

        // Start of logical day was yesterday (March 9) at 08:00
        // Elapsed time is from March 9 08:00 to March 10 06:00 (22 hours)
        // Total day duration is 24 hours
        // Progress should be 22 / 24 = 0.9166667
        assertEquals(22f / 24f, result.progress, 1e-5f)
        assertEquals(100.0 * (22.0 / 24.0), result.earnedToday, 1e-5)
    }

    @Test
    fun testDstSpringForward() {
        // America/New_York DST Spring Forward: March 8, 2026
        // At 02:00, clocks spring forward to 03:00.
        // Start of day is midnight: March 8, 2026 at 00:00 (EST)
        // End of day is midnight next day: March 9, 2026 at 00:00 (EDT)
        // Actual day duration: 23 hours.

        val annualSalary = 365.25 * 100.0

        // Test at noon on March 8: March 8, 2026 at 12:00 EDT
        // Physical duration since midnight EST is:
        // 00:00 to 02:00 (2 hours)
        // 03:00 to 12:00 (9 hours)
        // Total elapsed: 11 hours.
        val noonDst = ZonedDateTime.of(2026, 3, 8, 12, 0, 0, 0, zoneNy)
            .toInstant()
            .toEpochMilli()

        val result = SalaryCalculator.calculate(
            annualSalary = annualSalary,
            currentTimeMs = noonDst,
            resetHour = 0,
            zoneId = zoneNy
        )

        // Progress should be 11 / 23 = 0.47826087
        assertEquals(11f / 23f, result.progress, 1e-5f)
        assertEquals(100.0 * (11.0 / 23.0), result.earnedToday, 1e-5)
    }

    @Test
    fun testDstFallBack() {
        // America/New_York DST Fall Back: November 1, 2026
        // At 02:00, clocks fall back to 01:00.
        // Start of day is midnight: November 1, 2026 at 00:00 (EDT)
        // End of day is midnight next day: November 2, 2026 at 00:00 (EST)
        // Actual day duration: 25 hours.

        val annualSalary = 365.25 * 100.0

        // Test at 12:00 EST (noon after fallback has occurred)
        // Physical duration since midnight EDT is:
        // 00:00 EDT to 02:00 EDT (2 hours)
        // 01:00 EST to 12:00 EST (11 hours)
        // Total elapsed: 13 hours.
        val noonStandard = ZonedDateTime.of(2026, 11, 1, 12, 0, 0, 0, zoneNy)
            .toInstant()
            .toEpochMilli()

        val result = SalaryCalculator.calculate(
            annualSalary = annualSalary,
            currentTimeMs = noonStandard,
            resetHour = 0,
            zoneId = zoneNy
        )

        // Progress should be 13 / 25 = 0.52
        assertEquals(13f / 25f, result.progress, 1e-5f)
        assertEquals(100.0 * (13.0 / 25.0), result.earnedToday, 1e-5)
    }
}
