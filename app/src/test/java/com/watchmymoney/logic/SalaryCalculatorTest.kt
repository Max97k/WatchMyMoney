package com.watchmymoney.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class SalaryCalculatorTest {

    @Test
    fun testZeroSalary() {
        val currentTimeMs = System.currentTimeMillis()
        val result = SalaryCalculator.calculate(0.0, currentTimeMs)
        assertEquals(0.0, result.earnedToday, 0.0)
        assertEquals(0f, result.progress)
    }

    @Test
    fun testNegativeSalary() {
        val currentTimeMs = System.currentTimeMillis()
        val result = SalaryCalculator.calculate(-50000.0, currentTimeMs)
        assertEquals(0.0, result.earnedToday, 0.0)
        assertEquals(0f, result.progress)
    }
}
