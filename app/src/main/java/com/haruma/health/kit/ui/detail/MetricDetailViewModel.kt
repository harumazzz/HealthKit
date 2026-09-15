package com.haruma.health.kit.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.health.kit.data.health.HealthConnectManager
import com.haruma.health.kit.data.model.DailyHealthSummary
import com.haruma.health.kit.data.model.UserGoals
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MetricDetailViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricDetailUiState())
    val uiState: StateFlow<MetricDetailUiState> = _uiState.asStateFlow()

    private var currentUserGoals: UserGoals = UserGoals()

    init {
        viewModelScope.launch {
            preferencesRepository.userGoalsFlow.collect { goals ->
                currentUserGoals = goals
                refreshData()
            }
        }
        checkPermissionsAndLoad()
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
                refreshData()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectTimeRange(timeRange: DetailTimeRange) {
        if (_uiState.value.timeRange == timeRange) return
        _uiState.update {
            it.copy(
                timeRange = timeRange,
                selectedBarIndex = null
            )
        }
        refreshData()
    }

    fun selectMetric(metric: DetailMetric) {
        if (_uiState.value.selectedMetric == metric) return
        _uiState.update {
            it.copy(
                selectedMetric = metric,
                selectedBarIndex = null
            )
        }
        refreshData()
    }

    fun selectBar(index: Int?) {
        _uiState.update { it.copy(selectedBarIndex = index) }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(_uiState.value.timeRange.days - 1)

            val rawSummaries = if (healthConnectManager.hasAllPermissions()) {
                healthConnectManager.getHistoricalDailySummaries(startDate, endDate)
            } else {
                emptyList()
            }

            val selectedMetric = _uiState.value.selectedMetric
            val timeRange = _uiState.value.timeRange

            val goalValue = when (selectedMetric) {
                DetailMetric.STEPS -> currentUserGoals.stepsGoal.toDouble()
                DetailMetric.CALORIES -> currentUserGoals.caloriesGoal
                DetailMetric.SLEEP -> currentUserGoals.sleepGoalHours
                DetailMetric.HEART_RATE -> null
            }

            val formattedGoalValue = when (selectedMetric) {
                DetailMetric.STEPS -> String.format(Locale.US, "%,d steps", currentUserGoals.stepsGoal)
                DetailMetric.CALORIES -> String.format(Locale.US, "%.0f kcal", currentUserGoals.caloriesGoal)
                DetailMetric.SLEEP -> String.format(Locale.US, "%.1f hrs", currentUserGoals.sleepGoalHours)
                DetailMetric.HEART_RATE -> null
            }

            val maxVal = rawSummaries.maxOfOrNull { extractMetricValue(it, selectedMetric) } ?: 0.0

            val chartBars = rawSummaries.map { summary ->
                val value = extractMetricValue(summary, selectedMetric)
                val label = if (timeRange == DetailTimeRange.PAST_7_DAYS) {
                    summary.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
                } else {
                    "${summary.date.monthValue}/${summary.date.dayOfMonth}"
                }
                val formatted = formatMetricValue(value, selectedMetric)
                val isPeak = value > 0.0 && value == maxVal

                ChartBarData(
                    date = summary.date,
                    value = value,
                    label = label,
                    formattedValue = formatted,
                    isPeak = isPeak
                )
            }

            val summaryStats = computeStatistics(
                bars = chartBars,
                metric = selectedMetric,
                goalValue = goalValue,
                formattedGoalValue = formattedGoalValue
            )

            _uiState.update {
                it.copy(
                    chartBars = chartBars,
                    summaryStats = summaryStats,
                    isLoading = false
                )
            }
        }
    }

    private fun extractMetricValue(summary: DailyHealthSummary, metric: DetailMetric): Double {
        return when (metric) {
            DetailMetric.STEPS -> summary.steps.toDouble()
            DetailMetric.CALORIES -> summary.caloriesBurned
            DetailMetric.SLEEP -> summary.sleepDurationMinutes / 60.0
            DetailMetric.HEART_RATE -> summary.latestHeartRateBpm ?: 0.0
        }
    }

    private fun formatMetricValue(value: Double, metric: DetailMetric): String {
        return when (metric) {
            DetailMetric.STEPS -> String.format(Locale.US, "%,d steps", value.toLong())
            DetailMetric.CALORIES -> String.format(Locale.US, "%.0f kcal", value)
            DetailMetric.SLEEP -> String.format(Locale.US, "%.1f hrs", value)
            DetailMetric.HEART_RATE -> if (value > 0.0) String.format(Locale.US, "%.0f bpm", value) else "No data"
        }
    }

    private fun computeStatistics(
        bars: List<ChartBarData>,
        metric: DetailMetric,
        goalValue: Double?,
        formattedGoalValue: String?
    ): SummaryStatistics {
        if (bars.isEmpty()) return SummaryStatistics()

        val total = bars.sumOf { it.value }
        val daysCount = bars.size

        val dailyAvg = if (daysCount > 0) total / daysCount else 0.0

        val peakBar = bars.filter { it.value > 0.0 }.maxByOrNull { it.value }
        val peakValue = peakBar?.value ?: 0.0
        val peakDate = peakBar?.date

        val goalMetCount = if (goalValue != null && goalValue > 0.0) {
            bars.count { it.value >= goalValue }
        } else {
            0
        }
        val goalMetPercentage = if (daysCount > 0 && goalValue != null && goalValue > 0.0) {
            ((goalMetCount.toDouble() / daysCount) * 100).toInt()
        } else {
            0
        }

        val formattedAvg = formatMetricValue(dailyAvg, metric)
        val formattedPeak = formatMetricValue(peakValue, metric)
        val formattedTotal = when (metric) {
            DetailMetric.STEPS -> String.format(Locale.US, "%,d steps", total.toLong())
            DetailMetric.CALORIES -> String.format(Locale.US, "%,d kcal", total.toLong())
            DetailMetric.SLEEP -> String.format(Locale.US, "%.1f hrs", total)
            DetailMetric.HEART_RATE -> formattedAvg
        }

        return SummaryStatistics(
            dailyAverage = dailyAvg,
            formattedDailyAverage = formattedAvg,
            peakValue = peakValue,
            formattedPeakValue = formattedPeak,
            peakDate = peakDate,
            totalValue = total,
            formattedTotalValue = formattedTotal,
            goalMetPercentage = goalMetPercentage,
            goalValue = goalValue,
            formattedGoalValue = formattedGoalValue
        )
    }
}
