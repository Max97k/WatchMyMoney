package com.watchmymoney.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SalaryCalculatorTest {

    @Test
    fun testDstFallBackClamping() {
        // Set timezone to one that observes DST (e.g., America/New_York)
        val defaultTz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        try {
            // November 3, 2024 is the fall-back date in the US.
            // At 2:00 AM clocks go back to 1:00 AM, meaning the day is 25 hours long.
            val calendar = Calendar.getInstance()
            calendar.set(2024, Calendar.NOVEMBER, 3, 23, 30, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val currentTimeMs = calendar.timeInMillis

            // This is past 24 hours of standard elapsed time (86_400_000 ms)
            // since the "day" started at midnight, but because of fall back,
            // 23:30 is actually 24.5 hours from midnight.
            val result = SalaryCalculator.calculate(36525.0, currentTimeMs) // $100 per day exactly

            // The result progress should not exceed 1.0 due to the clamp
            assertTrue("Progress should be clamped to 1.0, was ${result.progress}", result.progress <= 1.0f)
            assertEquals("Earned amount should be clamped to a maximum of 1 full day's salary ($100.0)", 100.0, result.earnedToday, 0.001)

        } finally {
            TimeZone.setDefault(defaultTz)
        }
    }

    @Test
    fun testNormalDay() {
        // Set timezone to one that observes DST (e.g., America/New_York)
        val defaultTz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        try {
            // Pick a random day without DST changes
            val calendar = Calendar.getInstance()
            calendar.set(2024, Calendar.AUGUST, 15, 12, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val currentTimeMs = calendar.timeInMillis

            val result = SalaryCalculator.calculate(36525.0, currentTimeMs) // $100 per day exactly

            // At 12:00 PM, exactly half the day has passed.
            assertEquals(0.5f, result.progress, 0.001f)
            assertEquals(50.0, result.earnedToday, 0.001)

        } finally {
            TimeZone.setDefault(defaultTz)
        }
    }

    @Test
    fun testZeroSalary() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.AUGUST, 15, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val currentTimeMs = calendar.timeInMillis

        val result = SalaryCalculator.calculate(0.0, currentTimeMs)

        assertEquals(0.0, result.earnedToday, 0.001)
        // Note: progress returns 0f early if salary is <= 0 based on logic class short-circuit
        assertEquals(0.0f, result.progress, 0.001f)
    }
}
