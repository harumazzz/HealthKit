package com.haruma.health.kit.ui.stepdetail

import com.haruma.health.kit.data.model.HourlyStepData
import java.time.LocalDate

data class StepDetailUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val hourlyStepData: HourlyStepData = HourlyStepData(date = LocalDate.now()),
    val stepsGoal: Long = 8000L,
    val isLoading: Boolean = true,
    val showGoalSheet: Boolean = false,
    val hasPermissions: Boolean = false,
    val isHealthConnectAvailable: Boolean = true
)
