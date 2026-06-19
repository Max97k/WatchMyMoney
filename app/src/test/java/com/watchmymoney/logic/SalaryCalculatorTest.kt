package com.watchmymoney.logic

import org.junit.Test
import org.junit.Assert.assertEquals

class SalaryCalculatorTest {

    @Test
    fun calculate_zeroSalary_returnsZero() {
        val result = SalaryCalculator.calculate(
            annualSalary = 0.0,
            currentTimeMs = 1000L
        )

        assertEquals(0.0, result.earnedToday, 0.0)
        assertEquals(0f, result.progress)
    }

    @Test
    fun calculate_negativeSalary_returnsZero() {
        val result = SalaryCalculator.calculate(
            annualSalary = -50000.0,
            currentTimeMs = 1000L
        )

        assertEquals(0.0, result.earnedToday, 0.0)
        assertEquals(0f, result.progress)
    }
}
