package com.watchmymoney.logic

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Pure logic class for calculating salary earnings.
 */
object SalaryCalculator {

    const val DAYS_PER_YEAR = 365.25

    data class Result(
        val earnedToday: Double,
        val progress: Float // 0.0 to 1.0
    )

    data class LogicalDay(
        val startOfLogicalDay: Instant,
        val logicalDayMs: Long
    )

    private class CachedDay(
        val resetHour: Int,
        val zoneId: ZoneId,
        val logicalDay: LogicalDay
    ) {
        fun isValidFor(timeMs: Long, hour: Int, zone: ZoneId): Boolean {
            if (hour != resetHour || zone != zoneId) return false
            val start = logicalDay.startOfLogicalDay.toEpochMilli()
            return timeMs >= start && timeMs < start + logicalDay.logicalDayMs
        }
    }

    @Volatile
    private var cachedDay: CachedDay? = null

    fun calculateLogicalDay(
        currentTimeMs: Long,
        resetHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LogicalDay {
        val cached = cachedDay
        if (cached != null && cached.isValidFor(currentTimeMs, resetHour, zoneId)) {
            return cached.logicalDay
        }

        val nowInstant = Instant.ofEpochMilli(currentTimeMs)
        val nowZoned = ZonedDateTime.ofInstant(nowInstant, zoneId)

        // Find candidate reset hour today
        val candidateToday = nowZoned.toLocalDate().atTime(resetHour, 0).atZone(zoneId)

        // If current time is before the reset time today, then the logical day started yesterday
        val startOfLogicalDay = if (nowZoned.isBefore(candidateToday)) {
            nowZoned.toLocalDate().minusDays(1).atTime(resetHour, 0).atZone(zoneId)
        } else {
            candidateToday
        }

        // The end of the logical day is the start of the next logical day
        val endOfLogicalDay = startOfLogicalDay.plusDays(1)

        // Calculate dynamic duration of the logical day
        val logicalDayMs = Duration.between(startOfLogicalDay, endOfLogicalDay).toMillis()

        val logicalDay = LogicalDay(startOfLogicalDay.toInstant(), logicalDayMs)
        cachedDay = CachedDay(resetHour, zoneId, logicalDay)
        return logicalDay
    }

    /**
     * Calculates the earned amount and progress for the current day.
     *
     * @param annualSalary The user's annual salary.
     * @param currentTimeMs Current timestamp in milliseconds.
     * @param resetHour The hour of the day when the counter resets (0-23). Default is 0 (midnight).
     * @param zoneId The timezone zone ID. Defaults to ZoneId.systemDefault().
     */
    fun calculate(
        annualSalary: Double,
        currentTimeMs: Long,
        resetHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Result {
        if (annualSalary <= 0) {
            return Result(0.0, 0f)
        }

        val dailySalary = annualSalary / DAYS_PER_YEAR
        
        val logicalDay = calculateLogicalDay(currentTimeMs, resetHour, zoneId)

        // Calculate how much time has elapsed since the start of the logical day
        // Avoid instantiating Instant and Duration on every frame
        val msElapsed = currentTimeMs - logicalDay.startOfLogicalDay.toEpochMilli()

        // Clamp elapsed time to 0..logicalDayMs
        val safeMsElapsed = msElapsed.coerceIn(0, logicalDay.logicalDayMs)

        val fractionOfDay = safeMsElapsed.toDouble() / logicalDay.logicalDayMs
        val earned = dailySalary * fractionOfDay

        return Result(
            earnedToday = earned,
            progress = fractionOfDay.toFloat()
        )
    }
}
