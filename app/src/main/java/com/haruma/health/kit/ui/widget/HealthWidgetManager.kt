package com.haruma.health.kit.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.haruma.health.kit.MainActivity
import com.haruma.health.kit.R
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object HealthWidgetManager {

    private const val WORK_NAME_PERIODIC_WIDGET_UPDATE = "periodic_health_widget_update"
    private const val WORK_NAME_ONETIME_WIDGET_UPDATE = "onetime_health_widget_update"

    const val ACTION_OPEN_STEP_DETAIL = "com.haruma.health.kit.OPEN_STEP_DETAIL"
    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val ROUTE_STEP_DETAIL = "detail/steps"

    private const val PREFS_NAME = "health_widget_cache"
    private const val KEY_STEPS = "cached_steps"
    private const val KEY_STEPS_GOAL = "cached_steps_goal"
    private const val KEY_CALORIES = "cached_calories"
    private const val KEY_SLEEP_MINUTES = "cached_sleep_minutes"
    private const val KEY_CACHED_DATE = "cached_date"

    fun schedulePeriodicWidgetUpdate(context: Context) {
        val periodicRequest = PeriodicWorkRequestBuilder<HealthWidgetWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC_WIDGET_UPDATE,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    fun requestOneTimeWidgetUpdate(context: Context) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<HealthWidgetWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME_ONETIME_WIDGET_UPDATE,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    fun saveCachedWidgetData(
        context: Context,
        steps: Long,
        stepsGoal: Long,
        calories: Double,
        sleepMinutes: Long
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_STEPS, steps)
            .putLong(KEY_STEPS_GOAL, stepsGoal)
            .putString(KEY_CALORIES, calories.toString())
            .putLong(KEY_SLEEP_MINUTES, sleepMinutes)
            .putString(KEY_CACHED_DATE, LocalDate.now().toString())
            .apply()
    }

    data class CachedWidgetData(
        val steps: Long,
        val stepsGoal: Long,
        val calories: Double,
        val sleepMinutes: Long,
        val isFromToday: Boolean
    )

    fun loadCachedWidgetData(context: Context): CachedWidgetData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_STEPS)) return null

        val cachedDate = prefs.getString(KEY_CACHED_DATE, null)
        val isFromToday = cachedDate == LocalDate.now().toString()

        return CachedWidgetData(
            steps = if (isFromToday) prefs.getLong(KEY_STEPS, 0L) else 0L,
            stepsGoal = prefs.getLong(KEY_STEPS_GOAL, 6000L),
            calories = if (isFromToday) {
                prefs.getString(KEY_CALORIES, "0.0")?.toDoubleOrNull() ?: 0.0
            } else 0.0,
            sleepMinutes = if (isFromToday) prefs.getLong(KEY_SLEEP_MINUTES, 0L) else 0L,
            isFromToday = isFromToday
        )
    }

    fun updateWidgetViews(
        context: Context,
        steps: Long,
        stepsGoal: Long,
        calories: Double,
        sleepMinutes: Long
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, HealthKitWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        saveCachedWidgetData(context, steps, stepsGoal, calories, sleepMinutes)

        val bitmap = HealthWidgetBitmapFactory.renderWidgetBitmap(
            context = context,
            steps = steps,
            stepsGoal = stepsGoal,
            calories = calories,
            sleepMinutes = sleepMinutes
        )

        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_STEP_DETAIL
            putExtra(EXTRA_NAVIGATE_TO, ROUTE_STEP_DETAIL)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_health_summary)
            views.setImageViewBitmap(R.id.widget_image, bitmap)
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateWidgetWithCachedData(context: Context) {
        val cached = loadCachedWidgetData(context) ?: return
        updateWidgetViews(
            context = context,
            steps = cached.steps,
            stepsGoal = cached.stepsGoal,
            calories = cached.calories,
            sleepMinutes = cached.sleepMinutes
        )
    }
}
