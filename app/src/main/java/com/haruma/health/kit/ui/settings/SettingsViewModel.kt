package com.haruma.health.kit.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.health.kit.data.health.HealthConnectManager
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val preferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAppVersion()
        observePreferences()
        checkHealthConnectStatus()
    }

    private fun loadAppVersion() {
        val version = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "1.0"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
            }
        } catch (_: Exception) {
            "1.0"
        }
        _uiState.update { it.copy(appVersion = version) }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.userGoalsFlow.collect { goals ->
                _uiState.update { it.copy(userGoals = goals) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.languageFlow.collect { lang ->
                _uiState.update { it.copy(selectedLanguage = lang) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.themeModeFlow.collect { theme ->
                _uiState.update { it.copy(selectedTheme = theme) }
            }
        }
    }

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            val isAvailable = healthConnectManager.isAvailable()
            val hasPerms = if (isAvailable) healthConnectManager.hasAllPermissions() else false
            _uiState.update {
                it.copy(
                    isHealthConnectAvailable = isAvailable,
                    hasAllPermissions = hasPerms
                )
            }
        }
    }

    fun openGoalEditor(type: GoalType) {
        val currentValue = when (type) {
            GoalType.STEPS -> _uiState.value.userGoals.stepsGoal.toString()
            GoalType.CALORIES -> _uiState.value.userGoals.caloriesGoal.toInt().toString()
            GoalType.WATER -> _uiState.value.userGoals.waterGoalMilliliters.toString()
            GoalType.SLEEP -> _uiState.value.userGoals.sleepGoalHours.toString()
        }
        _uiState.update {
            it.copy(
                activeEditGoalType = type,
                inputGoalValue = currentValue
            )
        }
    }

    fun dismissGoalEditor() {
        _uiState.update {
            it.copy(
                activeEditGoalType = null,
                inputGoalValue = ""
            )
        }
    }

    fun updateInputGoalValue(value: String) {
        _uiState.update { it.copy(inputGoalValue = value) }
    }

    fun saveGoalValue() {
        val type = _uiState.value.activeEditGoalType ?: return
        val valueStr = _uiState.value.inputGoalValue.trim()
        viewModelScope.launch {
            when (type) {
                GoalType.STEPS -> {
                    val steps = valueStr.toLongOrNull()
                    if (steps != null && steps > 0) {
                        preferencesRepository.updateStepsGoal(steps)
                    }
                }
                GoalType.CALORIES -> {
                    val calories = valueStr.toDoubleOrNull()
                    if (calories != null && calories > 0) {
                        preferencesRepository.updateCaloriesGoal(calories)
                    }
                }
                GoalType.WATER -> {
                    val water = valueStr.toIntOrNull()
                    if (water != null && water > 0) {
                        preferencesRepository.updateWaterGoal(water)
                    }
                }
                GoalType.SLEEP -> {
                    val sleep = valueStr.toDoubleOrNull()
                    if (sleep != null && sleep > 0) {
                        preferencesRepository.updateSleepGoal(sleep)
                    }
                }
            }
            dismissGoalEditor()
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.updateLanguage(languageCode)
            _uiState.update {
                it.copy(
                    selectedLanguage = languageCode,
                    showLanguageDialog = false
                )
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(theme)
            _uiState.update {
                it.copy(
                    selectedTheme = theme,
                    showThemeDialog = false
                )
            }
        }
    }

    fun showLanguageDialog(show: Boolean) {
        _uiState.update { it.copy(showLanguageDialog = show) }
    }

    fun showThemeDialog(show: Boolean) {
        _uiState.update { it.copy(showThemeDialog = show) }
    }

    fun showAboutDialog(show: Boolean) {
        _uiState.update { it.copy(showAboutDialog = show) }
    }

    fun openHealthConnectSettings(ctx: Context) {
        healthConnectManager.openHealthConnectSettings(ctx)
    }
}
