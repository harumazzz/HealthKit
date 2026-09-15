package com.haruma.health.kit.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.haruma.health.kit.MainActivity
import com.haruma.health.kit.R
import com.haruma.health.kit.data.health.HealthConnectManager
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class HealthKitWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun healthConnectManager(): HealthConnectManager
        fun userPreferencesRepository(): UserPreferencesRepository
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val healthConnectManager = entryPoint.healthConnectManager()
                val preferencesRepository = entryPoint.userPreferencesRepository()

                val summary = healthConnectManager.getDailyHealthSummary(LocalDate.now())
                val goals = preferencesRepository.userGoalsFlow.first()

                val bitmap = HealthWidgetBitmapFactory.renderWidgetBitmap(
                    context = context,
                    steps = summary.steps,
                    stepsGoal = goals.stepsGoal,
                    calories = summary.caloriesBurned,
                    sleepMinutes = summary.sleepDurationMinutes
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
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_OPEN_STEP_DETAIL = "com.haruma.health.kit.OPEN_STEP_DETAIL"
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val ROUTE_STEP_DETAIL = "detail/steps"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, HealthKitWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HealthKitWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
