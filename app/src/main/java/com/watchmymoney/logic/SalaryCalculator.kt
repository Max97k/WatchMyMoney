package com.watchmymoney.logic

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Pure logic class for calculating salary earnings.
 */
object SalaryCalculator {

    private const val MS_PER_DAY = 86_400_000L
    private const val DAYS_PER_YEAR = 365.25

    data class Result(
        val earnedToday: Double,
        val progress: Float // 0.0 to 1.0
    )

    /**
     * Calculates the earned amount and progress for the current day.
     *
     * @param annualSalary The user's annual salary.
     * @param currentTimeMs Current timestamp in milliseconds.
     * @param resetHour The hour of the day when the counter resets (0-23). Default is 0 (midnight).
     */
    fun calculate(annualSalary: Double, currentTimeMs: Long, resetHour: Int = 0): Result {
        if (annualSalary <= 0) {
            return Result(0.0, 0f)
        }

        val dailySalary = annualSalary / DAYS_PER_YEAR
        
        // Calculate start of the "day" based on resetHour using java.time
        val zoneId = ZoneId.systemDefault()
        val instant = Instant.ofEpochMilli(currentTimeMs)
        val zdt = instant.atZone(zoneId)

        // Determine the logical start date for the current "day"
        val date = if (zdt.hour >= resetHour) zdt.toLocalDate() else zdt.toLocalDate().minusDays(1)
        val time = LocalTime.of(resetHour, 0)

        val startOfDay = ZonedDateTime.of(date, time, zoneId).toInstant().toEpochMilli()

        val msElapsed = currentTimeMs - startOfDay
        
        // Clamp elapsed time to 0..MS_PER_DAY (handle potential DST shifts or slight drifts safely)
        val safeMsElapsed = msElapsed.coerceIn(0, MS_PER_DAY)

        val fractionOfDay = safeMsElapsed.toDouble() / MS_PER_DAY
        val earned = dailySalary * fractionOfDay

        return Result(
            earnedToday = earned,
            progress = fractionOfDay.toFloat()
        )
    }
}
