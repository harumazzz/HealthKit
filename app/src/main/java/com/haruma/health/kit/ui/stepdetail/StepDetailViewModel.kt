package com.haruma.health.kit.ui.stepdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.health.kit.data.health.HealthConnectManager
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StepDetailViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StepDetailUiState())
    val uiState: StateFlow<StepDetailUiState> = _uiState.asStateFlow()

    private var periodicRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesRepository.userGoalsFlow.collect { goals ->
                _uiState.update { it.copy(stepsGoal = goals.stepsGoal) }
            }
        }
        checkPermissionsAndLoad()
    }

    fun startPeriodicRefresh() {
        periodicRefreshJob?.cancel()
        periodicRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                if (_uiState.value.hasPermissions && _uiState.value.selectedDate == LocalDate.now()) {
                    loadDataForDate(_uiState.value.selectedDate, isSilentRefresh = true)
                }
            }
        }
    }

    fun stopPeriodicRefresh() {
        periodicRefreshJob?.cancel()
        periodicRefreshJob = null
    }

    fun checkPermissionsAndLoad() {
        viewModelScope.launch {
            val isAvailable = healthConnectManager.isAvailable()
            if (!isAvailable) {
                _uiState.update {
                    it.copy(
                        isHealthConnectAvailable = false,
                        isLoading = false
                    )
                }
                return@launch
            }

            val hasPerms = healthConnectManager.hasAllPermissions()
            _uiState.update {
                it.copy(
                    isHealthConnectAvailable = true,
                    hasPermissions = hasPerms
                )
            }

            if (hasPerms) {
                loadDataForDate(_uiState.value.selectedDate)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        if (_uiState.value.selectedDate == date) return
        _uiState.update { it.copy(selectedDate = date) }
        loadDataForDate(date)
    }

    fun loadDataForDate(date: LocalDate, isSilentRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isSilentRefresh) {
                _uiState.update { it.copy(isLoading = true) }
            }
            val hourlyData = healthConnectManager.getHourlyStepData(date)
            _uiState.update {
                it.copy(
                    hourlyStepData = hourlyData,
                    isLoading = false
                )
            }
        }
    }

    fun updateStepsGoal(newGoal: Long) {
        viewModelScope.launch {
            preferencesRepository.updateStepsGoal(newGoal)
            _uiState.update { it.copy(stepsGoal = newGoal) }
        }
    }

    fun setGoalSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showGoalSheet = visible) }
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicRefresh()
    }
}
