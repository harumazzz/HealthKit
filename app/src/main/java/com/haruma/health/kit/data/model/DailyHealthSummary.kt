package com.haruma.health.kit.data.model

import java.time.LocalDate

data class DailyHealthSummary(
    val date: LocalDate,
    val steps: Long = 0L,
    val caloriesBurned: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val latestHeartRateBpm: Double? = null,
    val sleepDurationMinutes: Long = 0L,
    val waterMilliliters: Int = 0,
    val weightKg: Double? = null
)
