package com.haruma.health.kit.data.model

import java.time.LocalDate

data class SubMetricItem(
    val label: String,
    val value: String,
    val unit: String,
    val iconType: SubMetricIconType
)

enum class SubMetricIconType {
    TIME,
    DISTANCE,
    CALORIES,
    STEPS,
    WATER,
    SLEEP,
    HEART,
    WEIGHT,
    TARGET,
    TREND
}

data class HourlyMetricData(
    val date: LocalDate,
    val metricType: MetricType,
    val hourlyValues: List<Double> = List(24) { 0.0 },
    val mainValue: Double = 0.0,
    val formattedMainValue: String = "0",
    val unit: String = "",
    val goalValue: Double? = null,
    val formattedGoal: String? = null,
    val subMetrics: List<SubMetricItem> = emptyList()
)
