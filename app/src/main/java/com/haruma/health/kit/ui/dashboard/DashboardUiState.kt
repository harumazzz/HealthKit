package com.haruma.health.kit.ui.dashboard

import com.haruma.health.kit.data.model.DailyHealthSummary
import com.haruma.health.kit.data.model.UserGoals
import java.time.LocalDate

data class DashboardUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val healthSummary: DailyHealthSummary = DailyHealthSummary(date = LocalDate.now()),
    val userGoals: UserGoals = UserGoals(),
    val hasPermissions: Boolean = false,
    val isHealthConnectAvailable: Boolean = true,
    val isLoading: Boolean = false,
    val isPermissionRequested: Boolean = false,
    val showQuickLogSheet: Boolean = false,
    val errorMessage: String? = null
)
