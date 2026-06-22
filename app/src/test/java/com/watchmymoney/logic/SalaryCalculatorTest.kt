package com.watchmymoney.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class SalaryCalculatorTest {

    private val DELTA = 0.0001
    private val FLOAT_DELTA = 0.0001f

    private fun getMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute, second)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    @Test
    fun testZeroOrNegativeSalary() {
        val time = getMillis(2023, Calendar.OCTOBER, 1, 12, 0, 0)

        val resultZero = SalaryCalculator.calculate(0.0, time)
        assertEquals(0.0, resultZero.earnedToday, DELTA)
        assertEquals(0f, resultZero.progress, FLOAT_DELTA)

        val resultNegative = SalaryCalculator.calculate(-50000.0, time)
        assertEquals(0.0, resultNegative.earnedToday, DELTA)
        assertEquals(0f, resultNegative.progress, FLOAT_DELTA)
    }

    @Test
    fun testAtResetTime() {
        // Reset at 0 (midnight)
        val time = getMillis(2023, Calendar.OCTOBER, 1, 0, 0, 0)
        val annualSalary = 36525.0 // 100 per day

        val result = SalaryCalculator.calculate(annualSalary, time, resetHour = 0)
        assertEquals(0.0, result.earnedToday, DELTA)
        assertEquals(0f, result.progress, FLOAT_DELTA)
    }

    @Test
    fun testHalfDay() {
        // Reset at 0 (midnight), current time is 12:00
        val time = getMillis(2023, Calendar.OCTOBER, 1, 12, 0, 0)
        val annualSalary = 36525.0 // 100 per day

        val result = SalaryCalculator.calculate(annualSalary, time, resetHour = 0)
        assertEquals(50.0, result.earnedToday, DELTA)
        assertEquals(0.5f, result.progress, FLOAT_DELTA)
    }

    @Test
    fun testAlmostNextReset() {
        // Reset at 0 (midnight), current time is 23:00 (1 hour before reset)
        val time = getMillis(2023, Calendar.OCTOBER, 1, 23, 0, 0)
        val annualSalary = 36525.0 // 100 per day

        val result = SalaryCalculator.calculate(annualSalary, time, resetHour = 0)
        // 23/24 of 100 = 95.8333...
        assertEquals(100.0 * (23.0 / 24.0), result.earnedToday, DELTA)
        assertEquals((23.0 / 24.0).toFloat(), result.progress, FLOAT_DELTA)
    }

    @Test
    fun testCustomResetHour() {
        // Reset at 6 AM, current time is 18:00 (12 hours elapsed)
        val time = getMillis(2023, Calendar.OCTOBER, 1, 18, 0, 0)
        val annualSalary = 36525.0 // 100 per day

        val result = SalaryCalculator.calculate(annualSalary, time, resetHour = 6)
        assertEquals(50.0, result.earnedToday, DELTA)
        assertEquals(0.5f, result.progress, FLOAT_DELTA)
    }

    @Test
    fun testBeforeResetHour() {
        // Reset at 6 AM, current time is 3 AM (21 hours elapsed since yesterday 6 AM)
        val time = getMillis(2023, Calendar.OCTOBER, 2, 3, 0, 0)
        val annualSalary = 36525.0 // 100 per day

        val result = SalaryCalculator.calculate(annualSalary, time, resetHour = 6)
        assertEquals(100.0 * (21.0 / 24.0), result.earnedToday, DELTA)
        assertEquals((21.0 / 24.0).toFloat(), result.progress, FLOAT_DELTA)
    }
}
