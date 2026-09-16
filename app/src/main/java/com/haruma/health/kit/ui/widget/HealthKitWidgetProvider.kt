package com.haruma.health.kit.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
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

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        HealthWidgetManager.schedulePeriodicWidgetUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        HealthWidgetManager.schedulePeriodicWidgetUpdate(context)

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

                HealthWidgetManager.updateWidgetViews(
                    context = context,
                    steps = summary.steps,
                    stepsGoal = goals.stepsGoal,
                    calories = summary.caloriesBurned,
                    sleepMinutes = summary.sleepDurationMinutes
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_OPEN_STEP_DETAIL = HealthWidgetManager.ACTION_OPEN_STEP_DETAIL
        const val EXTRA_NAVIGATE_TO = HealthWidgetManager.EXTRA_NAVIGATE_TO
        const val ROUTE_STEP_DETAIL = HealthWidgetManager.ROUTE_STEP_DETAIL

        fun updateAllWidgets(context: Context) {
            HealthWidgetManager.requestOneTimeWidgetUpdate(context)
        }
    }
}
