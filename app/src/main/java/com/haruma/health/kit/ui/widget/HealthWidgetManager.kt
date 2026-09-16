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
import java.util.concurrent.TimeUnit

object HealthWidgetManager {

    private const val WORK_NAME_PERIODIC_WIDGET_UPDATE = "periodic_health_widget_update"
    private const val WORK_NAME_ONETIME_WIDGET_UPDATE = "onetime_health_widget_update"

    const val ACTION_OPEN_STEP_DETAIL = "com.haruma.health.kit.OPEN_STEP_DETAIL"
    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val ROUTE_STEP_DETAIL = "detail/steps"

    fun schedulePeriodicWidgetUpdate(context: Context) {
        val periodicRequest = PeriodicWorkRequestBuilder<HealthWidgetWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC_WIDGET_UPDATE,
            ExistingPeriodicWorkPolicy.KEEP,
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
}
