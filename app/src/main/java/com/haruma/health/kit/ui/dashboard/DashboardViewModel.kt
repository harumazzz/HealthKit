package com.haruma.health.kit.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.health.kit.data.health.HealthConnectManager
import com.haruma.health.kit.data.health.HealthPermissions
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import com.haruma.health.kit.ui.widget.HealthKitWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
class DashboardViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val preferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val permissionContract = healthConnectManager.createRequestPermissionResultContract()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var periodicRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesRepository.userGoalsFlow.collect { goals ->
                _uiState.update { it.copy(userGoals = goals) }
                HealthKitWidgetProvider.updateAllWidgets(context)
            }
        }
        checkPermissionsAndLoadData()
    }

    fun startPeriodicRefresh() {
        periodicRefreshJob?.cancel()
        periodicRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                if (_uiState.value.hasPermissions && _uiState.value.selectedDate == LocalDate.now()) {
                    loadDailyData(_uiState.value.selectedDate, isSilentRefresh = true)
                }
            }
        }
    }

    fun stopPeriodicRefresh() {
        periodicRefreshJob?.cancel()
        periodicRefreshJob = null
    }

    fun checkPermissionsAndLoadData() {
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
                loadDailyData(_uiState.value.selectedDate)
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            val hasAll = granted.containsAll(HealthPermissions.PERMISSIONS) || healthConnectManager.hasAllPermissions()
            _uiState.update {
                it.copy(
                    hasPermissions = hasAll,
                    isPermissionRequested = true
                )
            }
            if (hasAll) {
                loadDailyData(_uiState.value.selectedDate)
            }
        }
    }

    fun markPermissionRequested() {
        _uiState.update { it.copy(isPermissionRequested = true) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        if (_uiState.value.hasPermissions) {
            loadDailyData(date)
        }
    }

    fun loadDailyData(date: LocalDate = _uiState.value.selectedDate, isSilentRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isSilentRefresh) {
                _uiState.update { it.copy(isLoading = true) }
            }
            val summary = healthConnectManager.getDailyHealthSummary(date)
            _uiState.update {
                it.copy(
                    healthSummary = summary,
                    isLoading = false
                )
            }
            HealthKitWidgetProvider.updateAllWidgets(context)
        }
    }

    fun logWater(milliliters: Double) {
        viewModelScope.launch {
            val success = healthConnectManager.writeWater(milliliters)
            if (success) {
                loadDailyData(_uiState.value.selectedDate)
            }
        }
    }

    fun logWeight(weightKg: Double) {
        viewModelScope.launch {
            val success = healthConnectManager.writeWeight(weightKg)
            if (success) {
                loadDailyData(_uiState.value.selectedDate)
            }
        }
    }

    fun setQuickLogSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showQuickLogSheet = visible) }
    }

    fun openSettings(context: Context) {
        healthConnectManager.openHealthConnectSettings(context)
    }

    fun openInstall(context: Context) {
        healthConnectManager.openInstallHealthConnect(context)
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicRefresh()
    }
}
