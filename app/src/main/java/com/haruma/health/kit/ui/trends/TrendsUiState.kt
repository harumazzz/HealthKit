package com.haruma.health.kit.ui.trends

import com.haruma.health.kit.ui.detail.ChartBarData
import com.haruma.health.kit.ui.detail.DayAverage
import com.haruma.health.kit.ui.detail.DetailMetric
import com.haruma.health.kit.ui.detail.DetailTimeRange
import com.haruma.health.kit.ui.detail.SummaryStatistics

data class TrendsUiState(
    val timeRange: DetailTimeRange = DetailTimeRange.PAST_7_DAYS,
    val selectedMetric: DetailMetric = DetailMetric.STEPS,
    val chartBars: List<ChartBarData> = emptyList(),
    val weekdayAverages: List<DayAverage> = emptyList(),
    val summaryStats: SummaryStatistics = SummaryStatistics(),
    val selectedBarIndex: Int? = null,
    val isLoading: Boolean = true,
    val hasPermissions: Boolean = false,
    val isHealthConnectAvailable: Boolean = true
)
