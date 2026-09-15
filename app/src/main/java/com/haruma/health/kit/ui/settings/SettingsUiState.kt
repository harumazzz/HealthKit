package com.haruma.health.kit.ui.settings

import com.haruma.health.kit.data.model.UserGoals

enum class GoalType {
    STEPS,
    CALORIES,
    WATER,
    SLEEP
}

data class SettingsUiState(
    val userGoals: UserGoals = UserGoals(),
    val selectedLanguage: String = "en",
    val selectedTheme: String = "system",
    val isHealthConnectAvailable: Boolean = true,
    val hasAllPermissions: Boolean = false,
    val appVersion: String = "1.0",
    val activeEditGoalType: GoalType? = null,
    val inputGoalValue: String = "",
    val showLanguageDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showAboutDialog: Boolean = false
)
