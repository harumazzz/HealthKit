package com.haruma.health.kit.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.health.kit.data.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickLogViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickLogUiState())
    val uiState: StateFlow<QuickLogUiState> = _uiState.asStateFlow()

    fun initialize(initialWeight: Double?) {
        if (initialWeight != null && initialWeight > 0.0) {
            _uiState.update { it.copy(weightKg = initialWeight) }
        }
    }

    fun setLogType(type: QuickLogType) {
        _uiState.update { it.copy(selectedType = type, isTypeMenuExpanded = false) }
    }

    fun toggleTypeMenu(expanded: Boolean) {
        _uiState.update { it.copy(isTypeMenuExpanded = expanded) }
    }

    fun setWaterAmount(amount: Double) {
        _uiState.update { it.copy(waterAmountMl = amount.coerceAtLeast(0.0)) }
    }

    fun setWeight(weight: Double) {
        _uiState.update { it.copy(weightKg = weight.coerceAtLeast(0.0)) }
    }

    fun save(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val success = when (currentState.selectedType) {
                QuickLogType.WATER -> {
                    if (currentState.waterAmountMl > 0.0) {
                        healthConnectManager.writeWater(currentState.waterAmountMl)
                    } else false
                }
                QuickLogType.WEIGHT -> {
                    if (currentState.weightKg > 0.0) {
                        healthConnectManager.writeWeight(currentState.weightKg)
                    } else false
                }
            }
            _uiState.update { it.copy(isSaving = false) }
            if (success) {
                onSuccess()
            }
        }
    }
}
