package com.haruma.health.kit.ui.detail

import java.time.LocalDate

enum class DetailTimeRange(val days: Long) {
    PAST_7_DAYS(7L),
    PAST_30_DAYS(30L)
}

enum class DetailMetric {
    STEPS,
    CALORIES,
    SLEEP,
    HEART_RATE
}

data class ChartBarData(
    val date: LocalDate,
    val value: Double,
    val label: String,
    val formattedValue: String,
    val isPeak: Boolean = false
)

data class SummaryStatistics(
    val dailyAverage: Double = 0.0,
    val formattedDailyAverage: String = "0",
    val peakValue: Double = 0.0,
    val formattedPeakValue: String = "0",
    val peakDate: LocalDate? = null,
    val totalValue: Double = 0.0,
    val formattedTotalValue: String = "0",
    val goalMetPercentage: Int = 0,
    val goalValue: Double? = null,
    val formattedGoalValue: String? = null
)

data class MetricDetailUiState(
    val timeRange: DetailTimeRange = DetailTimeRange.PAST_7_DAYS,
    val selectedMetric: DetailMetric = DetailMetric.STEPS,
    val chartBars: List<ChartBarData> = emptyList(),
    val selectedBarIndex: Int? = null,
    val summaryStats: SummaryStatistics = SummaryStatistics(),
    val isLoading: Boolean = true,
    val hasPermissions: Boolean = false,
    val isHealthConnectAvailable: Boolean = true
)
