package com.haruma.health.kit.ui.components

enum class QuickLogType {
    WATER,
    WEIGHT
}

data class QuickLogUiState(
    val selectedType: QuickLogType = QuickLogType.WATER,
    val isTypeMenuExpanded: Boolean = false,
    val waterAmountMl: Double = 250.0,
    val weightKg: Double = 65.0,
    val isSaving: Boolean = false
)
