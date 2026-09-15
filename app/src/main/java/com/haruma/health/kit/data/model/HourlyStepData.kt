package com.haruma.health.kit.data.model

import java.time.LocalDate

data class HourlyStepData(
    val date: LocalDate,
    val hourlySteps: List<Long> = List(24) { 0L },
    val totalSteps: Long = 0L,
    val activeDurationSeconds: Long = 0L,
    val distanceKm: Double = 0.0,
    val caloriesKcal: Double = 0.0
)
