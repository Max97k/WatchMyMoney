package com.watchmymoney.complication

import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.DynamicComplicationText
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.watchmymoney.MainActivity
import com.watchmymoney.R
import com.watchmymoney.data.SalaryRepository
import com.watchmymoney.data.dataStore
import com.watchmymoney.logic.SalaryCalculator
import kotlinx.coroutines.flow.first

class SalaryComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val repository = SalaryRepository(applicationContext.dataStore)
        val config = repository.userConfig.first()
        
        val now = com.watchmymoney.logic.TimeProvider.currentTimeMillis()
        val logicalDay = SalaryCalculator.calculateLogicalDay(now, config.resetHour)
        val result = SalaryCalculator.calculate(config.annualSalary, now, config.resetHour)
        
        // Use optimized string building (no String.format) for static fallback
        val staticEarnedVal = result.earnedToday.toLong().toString()
        val earnedTextStr = "${config.currencySymbol}$staticEarnedVal"

        // Dynamic Expressions
        val startOfLogicalDaySec = DynamicInstant.withSecondsPrecision(logicalDay.startOfLogicalDay)
        val platformTimeSec = DynamicInstant.platformTimeWithSecondsPrecision()
        val elapsedSec = startOfLogicalDaySec.durationUntil(platformTimeSec).toIntSeconds()

        val logicalDaySec = logicalDay.logicalDayMs / 1000.0f
        val dailySalary = (config.annualSalary / SalaryCalculator.DAYS_PER_YEAR).toFloat()

        val progressDynamic = elapsedSec.asFloat().times(DynamicFloat.constant(1.0f / logicalDaySec))
        val earnedDynamicFloat = elapsedSec.asFloat().times(DynamicFloat.constant(dailySalary / logicalDaySec))

        val floatFormatter = DynamicFloat.FloatFormatter.Builder()
            .setMaxFractionDigits(0)
            .setGroupingUsed(false)
            .build()

        val earnedTextDynamicStr = DynamicString.constant(config.currencySymbol).concat(earnedDynamicFloat.format(floatFormatter))
        val earnedTextDynamic = DynamicComplicationText(earnedTextDynamicStr, earnedTextStr)

        val shortContentDescFallback = applicationContext.getString(R.string.complication_earned_today, earnedTextStr)
        val shortContentDescDynamicStr = applicationContext.getString(R.string.complication_earned_today_dynamic)
        val shortContentDescStr = DynamicString.constant(shortContentDescDynamicStr).concat(earnedTextDynamicStr)
        val shortContentDesc = DynamicComplicationText(shortContentDescStr, shortContentDescFallback)

        val longTextFallback = applicationContext.getString(R.string.complication_today, earnedTextStr)
        val longTextDynamicPrefixStr = applicationContext.getString(R.string.complication_today_dynamic)
        val longTextDynamicStr = DynamicString.constant(longTextDynamicPrefixStr).concat(earnedTextDynamicStr)
        val longTextDynamic = DynamicComplicationText(longTextDynamicStr, longTextFallback)
        
        // Create tap action to open the app
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val tapAction = PendingIntent.getActivity(
            this, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = earnedTextDynamic,
                    contentDescription = shortContentDesc
                )
                .setTapAction(tapAction)
                .build()
            }
            
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = longTextDynamic,
                    contentDescription = shortContentDesc
                )
                .setTapAction(tapAction)
                .build()
            }
            
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    dynamicValue = progressDynamic,
                    fallbackValue = result.progress.coerceIn(0f, 1f),
                    min = 0f,
                    max = 1f,
                    contentDescription = shortContentDesc
                )
                .setText(earnedTextDynamic)
                .setTapAction(tapAction)
                .build()
            }
            
            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val earnedText = "$120"
        val contentDescription = PlainComplicationText.Builder(applicationContext.getString(R.string.complication_preview)).build()
        
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(earnedText).build(),
                contentDescription = contentDescription
            ).build()
            
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(applicationContext.getString(R.string.complication_today, earnedText)).build(),
                contentDescription = contentDescription
            ).build()
            
            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 0.5f,
                min = 0f,
                max = 1f,
                contentDescription = contentDescription
            ).setText(PlainComplicationText.Builder(earnedText).build()).build()
            
            else -> null
        }
    }
}
