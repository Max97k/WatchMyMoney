package com.watchmymoney.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.watchmymoney.MainActivity
import com.watchmymoney.data.SalaryRepository
import com.watchmymoney.logic.SalaryCalculator
import kotlinx.coroutines.flow.first

class SalaryComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val repository = SalaryRepository(applicationContext)
        val config = repository.userConfig.first()
        
        val now = System.currentTimeMillis()
        val result = SalaryCalculator.calculate(config.annualSalary, now, config.resetHour)
        
        val earnedText = "${config.currencySymbol}${String.format("%.0f", result.earnedToday)}"
        val contentDescription = PlainComplicationText.Builder("Earned today: $earnedText").build()
        
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
                    text = PlainComplicationText.Builder(earnedText).build(),
                    contentDescription = contentDescription
                )
                .setTapAction(tapAction)
                .build()
            }
            
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Today: $earnedText").build(),
                    contentDescription = contentDescription
                )
                .setTapAction(tapAction)
                .build()
            }
            
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = result.progress.coerceIn(0f, 1f),
                    min = 0f,
                    max = 1f,
                    contentDescription = contentDescription
                )
                .setText(PlainComplicationText.Builder(earnedText).build())
                .setTapAction(tapAction)
                .build()
            }
            
            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val earnedText = "$120.50"
        val contentDescription = PlainComplicationText.Builder("Preview").build()
        
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(earnedText).build(),
                contentDescription = contentDescription
            ).build()
            
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("Today: $earnedText").build(),
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
