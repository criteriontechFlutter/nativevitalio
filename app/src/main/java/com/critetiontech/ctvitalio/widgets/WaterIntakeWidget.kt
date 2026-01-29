package com.critetiontech.ctvitalio.widgets

import com.critetiontech.ctvitalio.R


import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class WaterIntakeWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val ACTION_ADD_WATER = "com.critetiontech.ctvitalio.ADD_WATER"
        private const val ACTION_RESET = "com.critetiontech.ctvitalio.RESET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.waterintakewidget)
            val prefs = context.getSharedPreferences("water_intake", Context.MODE_PRIVATE)

            val waterIntake = prefs.getInt("water_intake_ml", 0)
            val dailyGoal = 2000
            val percentage = (waterIntake * 100) / dailyGoal
            val lastUpdated = prefs.getLong("last_updated", System.currentTimeMillis())

            // Check if it's a new day
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val lastDate = prefs.getString("last_date", today)

            if (today != lastDate) {
                prefs.edit().apply {
                    putInt("water_intake_ml", 0)
                    putString("last_date", today)
                    commit()
                }
                views.setTextViewText(R.id.percentageText, "0%")
                views.setTextViewText(R.id.amountText, "0ml")
                views.setTextViewText(R.id.lastUpdatedText, "Reset for today")
            } else {
                views.setTextViewText(R.id.percentageText, "$percentage%")
                views.setTextViewText(R.id.amountText, "${waterIntake}ml")

                val timeAgo = getTimeAgo(lastUpdated)
                views.setTextViewText(R.id.lastUpdatedText, "Last updated $timeAgo")
            }

            // Set up button intents
            val addWaterIntent = Intent(context, WaterIntakeWidget::class.java).apply {
                action = ACTION_ADD_WATER
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val addWaterPending = PendingIntent.getBroadcast(
                context, appWidgetId, addWaterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.addButton, addWaterPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "just now"
                diff < 3600000 -> "${diff / 60000} minutes ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                else -> "${diff / 86400000} days ago"
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        when (intent.action) {
            "com.critetiontech.ctvitalio.ADD_WATER" -> {
                val prefs = context.getSharedPreferences("water_intake", Context.MODE_PRIVATE)
                val currentIntake = prefs.getInt("water_intake_ml", 0)
                val newIntake = (currentIntake + 200).coerceAtMost(2000)

                prefs.edit().apply {
                    putInt("water_intake_ml", newIntake)
                    putLong("last_updated", System.currentTimeMillis())
                    commit()
                }

                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}