package com.watchmymoney.logic

import java.util.Calendar

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
     * @param workHourStart The start of working hours (0-23). Default is 9 (9 AM).
     * @param workHourEnd The end of working hours (0-23). Default is 18 (6 PM).
     */
    fun calculate(
        annualSalary: Double, 
        currentTimeMs: Long, 
        resetHour: Int = 0,
        workHourStart: Int = 9,
        workHourEnd: Int = 18
    ): Result {
        if (annualSalary <= 0 || workHourStart >= workHourEnd) {
            return Result(0.0, 0f)
        }

        val dailySalary = annualSalary / DAYS_PER_YEAR
        
        // Calculate start of the "day" based on resetHour
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMs
            set(Calendar.HOUR_OF_DAY, resetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If current time is before the reset time today, then the "day" started yesterday
        var startOfDay = calendar.timeInMillis
        if (currentTimeMs < startOfDay) {
            startOfDay -= MS_PER_DAY
        }

        // Calculate work start and end timestamps based on startOfDay
        val workStartMs = startOfDay + (workHourStart - resetHour) * 3600_000L
        val workEndMs = startOfDay + (workHourEnd - resetHour) * 3600_000L

        // Clamp current time to within the working hours
        val currentWorkTimeMs = currentTimeMs.coerceIn(workStartMs, workEndMs)
        val totalWorkDurationMs = workEndMs - workStartMs
        
        val msElapsed = currentWorkTimeMs - workStartMs
        
        val fractionOfDay = msElapsed.toDouble() / totalWorkDurationMs
        val earned = dailySalary * fractionOfDay

        return Result(
            earnedToday = earned,
            progress = fractionOfDay.toFloat()
        )
    }
}
