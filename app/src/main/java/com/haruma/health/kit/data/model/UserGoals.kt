package com.haruma.health.kit.data.model

data class UserGoals(
    val stepsGoal: Long = 10000L,
    val caloriesGoal: Double = 500.0,
    val waterGoalMilliliters: Int = 2000,
    val sleepGoalHours: Double = 8.0
)
