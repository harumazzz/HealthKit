package com.haruma.health.kit.ui.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.haruma.health.kit.data.health.HealthConnectManager
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HealthWidgetWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetWorkerEntryPoint {
        fun healthConnectManager(): HealthConnectManager
        fun userPreferencesRepository(): UserPreferencesRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                WidgetWorkerEntryPoint::class.java
            )
            val healthConnectManager = entryPoint.healthConnectManager()
            val preferencesRepository = entryPoint.userPreferencesRepository()

            val summary = healthConnectManager.getDailyHealthSummary(LocalDate.now())
            val goals = preferencesRepository.userGoalsFlow.first()

            HealthWidgetManager.updateWidgetViews(
                context = appContext,
                steps = summary.steps,
                stepsGoal = goals.stepsGoal,
                calories = summary.caloriesBurned,
                sleepMinutes = summary.sleepDurationMinutes
            )

            Result.success()
        } catch (_: SecurityException) {
            HealthWidgetManager.updateWidgetWithCachedData(appContext)
            Result.success()
        } catch (_: Throwable) {
            HealthWidgetManager.updateWidgetWithCachedData(appContext)
            Result.success()
        }
    }
}
