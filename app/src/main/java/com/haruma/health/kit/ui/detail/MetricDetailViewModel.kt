package com.haruma.health.kit.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.health.kit.data.health.HealthConnectManager
import com.haruma.health.kit.data.model.MetricType
import com.haruma.health.kit.data.model.UserGoals
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
class MetricDetailViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val preferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricDetailUiState())
    val uiState: StateFlow<MetricDetailUiState> = _uiState.asStateFlow()

    private var currentUserGoals: UserGoals = UserGoals()
    private var periodicRefreshJob: Job? = null

    init {
        val typeArg = savedStateHandle.get<String>("metricType")
        val parsedType = typeArg?.let { arg ->
            runCatching { MetricType.valueOf(arg.uppercase()) }.getOrNull()
        } ?: MetricType.STEPS

        _uiState.update { it.copy(metricType = parsedType) }

        viewModelScope.launch {
            preferencesRepository.userGoalsFlow.collect { goals ->
                currentUserGoals = goals
                loadDataForDate(_uiState.value.selectedDate)
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

    fun setMetricType(metricType: MetricType) {
        if (_uiState.value.metricType == metricType) return
        _uiState.update { it.copy(metricType = metricType) }
        loadDataForDate(_uiState.value.selectedDate)
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
            val metricData = healthConnectManager.getHourlyMetricData(
                date = date,
                metricType = _uiState.value.metricType,
                goals = currentUserGoals
            )
            _uiState.update {
                it.copy(
                    hourlyData = metricData,
                    isLoading = false
                )
            }
        }
    }

    fun updateGoal(newGoal: Double) {
        viewModelScope.launch {
            when (_uiState.value.metricType) {
                MetricType.STEPS -> preferencesRepository.updateStepsGoal(newGoal.toLong())
                MetricType.CALORIES -> preferencesRepository.updateCaloriesGoal(newGoal)
                MetricType.WATER -> preferencesRepository.updateWaterGoal(newGoal.toInt())
                MetricType.SLEEP -> preferencesRepository.updateSleepGoal(newGoal)
                else -> {}
            }
            loadDataForDate(_uiState.value.selectedDate)
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
