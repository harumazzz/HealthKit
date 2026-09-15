package com.haruma.health.kit.data.health

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.haruma.health.kit.data.model.DailyHealthSummary
import com.haruma.health.kit.data.model.HourlyMetricData
import com.haruma.health.kit.data.model.HourlyStepData
import com.haruma.health.kit.data.model.MetricType
import com.haruma.health.kit.data.model.SubMetricIconType
import com.haruma.health.kit.data.model.SubMetricItem
import com.haruma.health.kit.data.model.UserGoals
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HourlyMetricAggregator @Inject constructor() {

    suspend fun getHourlyStepData(
        client: HealthConnectClient?,
        date: LocalDate,
        getDailySummary: suspend (LocalDate) -> DailyHealthSummary
    ): HourlyStepData {
        if (client == null) return HourlyStepData(date = date)
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        val timeRange = TimeRangeFilter.between(startOfDay, endOfDay)

        return try {
            val stepsRecords = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = timeRange
                )
            ).records

            val hourlyArray = LongArray(24)
            var totalSteps = 0L
            var totalActiveSeconds = 0L

            for (record in stepsRecords) {
                val recordStartLocal = record.startTime.atZone(zoneId)
                val hour = recordStartLocal.hour.coerceIn(0, 23)
                hourlyArray[hour] += record.count
                totalSteps += record.count
                val durationSec = Duration.between(record.startTime, record.endTime).seconds
                totalActiveSeconds += durationSec
            }

            val dailySummary = getDailySummary(date)
            val effectiveSteps = if (totalSteps > 0) totalSteps else dailySummary.steps
            val distanceKm = if (dailySummary.distanceMeters > 0) {
                dailySummary.distanceMeters / 1000.0
            } else {
                effectiveSteps * 0.00075
            }
            val caloriesKcal = if (dailySummary.caloriesBurned > 0) {
                dailySummary.caloriesBurned
            } else {
                effectiveSteps * 0.04
            }

            HourlyStepData(
                date = date,
                hourlySteps = hourlyArray.toList(),
                totalSteps = effectiveSteps,
                activeDurationSeconds = totalActiveSeconds,
                distanceKm = distanceKm,
                caloriesKcal = caloriesKcal
            )
        } catch (_: Exception) {
            val dailySummary = getDailySummary(date)
            HourlyStepData(
                date = date,
                hourlySteps = List(24) { 0L },
                totalSteps = dailySummary.steps,
                activeDurationSeconds = 0L,
                distanceKm = dailySummary.distanceMeters / 1000.0,
                caloriesKcal = dailySummary.caloriesBurned
            )
        }
    }

    suspend fun getHourlyMetricData(
        client: HealthConnectClient?,
        date: LocalDate,
        metricType: MetricType,
        goals: UserGoals,
        getDailySummary: suspend (LocalDate) -> DailyHealthSummary
    ): HourlyMetricData {
        if (client == null) return buildDefaultMetricData(date, metricType, goals)
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        val timeRange = TimeRangeFilter.between(startOfDay, endOfDay)
        val dailySummary = getDailySummary(date)

        return try {
            when (metricType) {
                MetricType.STEPS -> {
                    val stepData = getHourlyStepData(client, date, getDailySummary)
                    val activeSec = stepData.activeDurationSeconds
                    val mins = activeSec / 60
                    val secs = activeSec % 60
                    HourlyMetricData(
                        date = date,
                        metricType = metricType,
                        hourlyValues = stepData.hourlySteps.map { it.toDouble() },
                        mainValue = stepData.totalSteps.toDouble(),
                        formattedMainValue = String.format(Locale.getDefault(), "%,d", stepData.totalSteps),
                        unit = "steps",
                        goalValue = goals.stepsGoal.toDouble(),
                        formattedGoal = String.format(Locale.getDefault(), "%,d steps", goals.stepsGoal),
                        subMetrics = listOf(
                            SubMetricItem("Duration", "${mins}m ${secs}s", "", SubMetricIconType.TIME),
                            SubMetricItem("Distance", String.format(Locale.getDefault(), "%.1f", stepData.distanceKm), "KM", SubMetricIconType.DISTANCE),
                            SubMetricItem("Calories", "${stepData.caloriesKcal.toInt()}", "KCAL", SubMetricIconType.CALORIES)
                        )
                    )
                }
                MetricType.CALORIES -> {
                    val caloriesRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = ActiveCaloriesBurnedRecord::class,
                            timeRangeFilter = timeRange
                        )
                    ).records
                    val hourlyArray = DoubleArray(24)
                    var totalCal = 0.0
                    for (record in caloriesRecords) {
                        val hour = record.startTime.atZone(zoneId).hour.coerceIn(0, 23)
                        val cal = record.energy.inKilocalories
                        hourlyArray[hour] += cal
                        totalCal += cal
                    }
                    val calVal = if (totalCal > 0) totalCal else dailySummary.caloriesBurned
                    val stepsVal = dailySummary.steps
                    val distKm = dailySummary.distanceMeters / 1000.0
                    HourlyMetricData(
                        date = date,
                        metricType = metricType,
                        hourlyValues = hourlyArray.toList(),
                        mainValue = calVal,
                        formattedMainValue = String.format(Locale.getDefault(), "%,d", calVal.toInt()),
                        unit = "kcal",
                        goalValue = goals.caloriesGoal,
                        formattedGoal = String.format(Locale.getDefault(), "%.0f kcal", goals.caloriesGoal),
                        subMetrics = listOf(
                            SubMetricItem("Steps", String.format(Locale.getDefault(), "%,d", stepsVal), "", SubMetricIconType.STEPS),
                            SubMetricItem("Distance", String.format(Locale.getDefault(), "%.1f", distKm), "KM", SubMetricIconType.DISTANCE),
                            SubMetricItem("Goal Met", "${((calVal / goals.caloriesGoal.coerceAtLeast(1.0)) * 100).toInt()}%", "", SubMetricIconType.TARGET)
                        )
                    )
                }
                MetricType.WATER -> {
                    val waterRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = HydrationRecord::class,
                            timeRangeFilter = timeRange
                        )
                    ).records
                    val hourlyArray = DoubleArray(24)
                    var totalWater = 0
                    for (record in waterRecords) {
                        val hour = record.startTime.atZone(zoneId).hour.coerceIn(0, 23)
                        val ml = record.volume.inMilliliters.toInt()
                        hourlyArray[hour] += ml.toDouble()
                        totalWater += ml
                    }
                    val effectiveWater = if (totalWater > 0) totalWater else dailySummary.waterMilliliters
                    val goal = goals.waterGoalMilliliters
                    val remaining = (goal - effectiveWater).coerceAtLeast(0)
                    val goalPercent = if (goal > 0) ((effectiveWater.toDouble() / goal) * 100).toInt() else 0
                    HourlyMetricData(
                        date = date,
                        metricType = metricType,
                        hourlyValues = hourlyArray.toList(),
                        mainValue = effectiveWater.toDouble(),
                        formattedMainValue = String.format(Locale.getDefault(), "%,d", effectiveWater),
                        unit = "ml",
                        goalValue = goal.toDouble(),
                        formattedGoal = "$goal ml",
                        subMetrics = listOf(
                            SubMetricItem("Goal Rate", "$goalPercent%", "", SubMetricIconType.TARGET),
                            SubMetricItem("Remaining", "$remaining", "ML", SubMetricIconType.WATER),
                            SubMetricItem("Logs Count", "${waterRecords.size}", "", SubMetricIconType.TREND)
                        )
                    )
                }
                MetricType.SLEEP -> {
                    val sleepRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = SleepSessionRecord::class,
                            timeRangeFilter = timeRange
                        )
                    ).records
                    val hourlyArray = DoubleArray(24)
                    var totalMins = 0L
                    for (record in sleepRecords) {
                        val duration = Duration.between(record.startTime, record.endTime).toMinutes()
                        val startHour = record.startTime.atZone(zoneId).hour.coerceIn(0, 23)
                        hourlyArray[startHour] += (duration / 60.0)
                        totalMins += duration
                    }
                    val effectiveMins = if (totalMins > 0) totalMins else dailySummary.sleepDurationMinutes
                    val hours = effectiveMins / 60
                    val mins = effectiveMins % 60
                    val totalHoursDouble = effectiveMins / 60.0
                    val goalPercent = if (goals.sleepGoalHours > 0) ((totalHoursDouble / goals.sleepGoalHours) * 100).toInt() else 0
                    HourlyMetricData(
                        date = date,
                        metricType = metricType,
                        hourlyValues = hourlyArray.toList(),
                        mainValue = totalHoursDouble,
                        formattedMainValue = "${hours}h ${mins}m",
                        unit = "",
                        goalValue = goals.sleepGoalHours,
                        formattedGoal = String.format(Locale.getDefault(), "%.1f hrs", goals.sleepGoalHours),
                        subMetrics = listOf(
                            SubMetricItem("Goal Rate", "$goalPercent%", "", SubMetricIconType.TARGET),
                            SubMetricItem("Total Mins", "$effectiveMins", "MIN", SubMetricIconType.TIME),
                            SubMetricItem("Rest Score", if (totalHoursDouble >= 7.0) "Good" else "Fair", "", SubMetricIconType.SLEEP)
                        )
                    )
                }
                MetricType.HEART_RATE -> {
                    val hrRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateRecord::class,
                            timeRangeFilter = timeRange
                        )
                    ).records
                    val hourlySum = DoubleArray(24)
                    val hourlyCount = IntArray(24)
                    val allSamples = mutableListOf<Double>()
                    for (record in hrRecords) {
                        for (sample in record.samples) {
                            val hour = sample.time.atZone(zoneId).hour.coerceIn(0, 23)
                            val bpm = sample.beatsPerMinute.toDouble()
                            hourlySum[hour] += bpm
                            hourlyCount[hour]++
                            allSamples.add(bpm)
                        }
                    }
                    val hourlyAvg = List(24) { h ->
                        if (hourlyCount[h] > 0) hourlySum[h] / hourlyCount[h] else 0.0
                    }
                    val latestBpm = dailySummary.latestHeartRateBpm ?: (allSamples.lastOrNull() ?: 0.0)
                    val minBpm = if (allSamples.isNotEmpty()) allSamples.minOrNull() ?: 0.0 else 0.0
                    val maxBpm = if (allSamples.isNotEmpty()) allSamples.maxOrNull() ?: 0.0 else 0.0
                    val avgBpm = if (allSamples.isNotEmpty()) allSamples.average() else latestBpm
                    HourlyMetricData(
                        date = date,
                        metricType = metricType,
                        hourlyValues = hourlyAvg,
                        mainValue = latestBpm,
                        formattedMainValue = if (latestBpm > 0) "${latestBpm.toInt()}" else "--",
                        unit = "bpm",
                        goalValue = null,
                        formattedGoal = null,
                        subMetrics = listOf(
                            SubMetricItem("Min BPM", if (minBpm > 0) "${minBpm.toInt()}" else "--", "BPM", SubMetricIconType.HEART),
                            SubMetricItem("Max BPM", if (maxBpm > 0) "${maxBpm.toInt()}" else "--", "BPM", SubMetricIconType.HEART),
                            SubMetricItem("Avg BPM", if (avgBpm > 0) "${avgBpm.toInt()}" else "--", "BPM", SubMetricIconType.HEART)
                        )
                    )
                }
                MetricType.DISTANCE -> {
                    val distRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = DistanceRecord::class,
                            timeRangeFilter = timeRange
                        )
                    ).records
                    val hourlyArray = DoubleArray(24)
                    var totalMeters = 0.0
                    for (record in distRecords) {
                        val hour = record.startTime.atZone(zoneId).hour.coerceIn(0, 23)
                        val m = record.distance.inMeters
                        hourlyArray[hour] += (m / 1000.0)
                        totalMeters += m
                    }
                    val effectiveMeters = if (totalMeters > 0) totalMeters else dailySummary.distanceMeters
                    val distKm = effectiveMeters / 1000.0
                    val stepsVal = dailySummary.steps
                    val calVal = dailySummary.caloriesBurned
                    HourlyMetricData(
                        date = date,
                        metricType = metricType,
                        hourlyValues = hourlyArray.toList(),
                        mainValue = distKm,
                        formattedMainValue = String.format(Locale.getDefault(), "%.2f", distKm),
                        unit = "km",
                        goalValue = null,
                        formattedGoal = null,
                        subMetrics = listOf(
                            SubMetricItem("Steps", String.format(Locale.getDefault(), "%,d", stepsVal), "", SubMetricIconType.STEPS),
                            SubMetricItem("Calories", "${calVal.toInt()}", "KCAL", SubMetricIconType.CALORIES),
                            SubMetricItem("Estimated M", "${effectiveMeters.toInt()}", "M", SubMetricIconType.DISTANCE)
                        )
                    )
                }
                MetricType.WEIGHT -> {
                    val weightRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = WeightRecord::class,
                            timeRangeFilter = timeRange,
                            ascendingOrder = false
                        )
                    ).records
                    val latestWeight = weightRecords.firstOrNull()?.weight?.inKilograms ?: dailySummary.weightKg ?: 0.0
                    val bmi = if (latestWeight > 0) latestWeight / (1.75 * 1.75) else 0.0
                    val hourlyArray = List(24) { latestWeight }
                    HourlyMetricData(
                        date = date,
                        metricType = metricType,
                        hourlyValues = hourlyArray,
                        mainValue = latestWeight,
                        formattedMainValue = if (latestWeight > 0) String.format(Locale.getDefault(), "%.1f", latestWeight) else "--",
                        unit = "kg",
                        goalValue = null,
                        formattedGoal = null,
                        subMetrics = listOf(
                            SubMetricItem("Est. BMI", if (bmi > 0) String.format(Locale.getDefault(), "%.1f", bmi) else "--", "", SubMetricIconType.WEIGHT),
                            SubMetricItem("Category", if (bmi in 18.5..24.9) "Normal" else if (bmi > 0) "Monitored" else "--", "", SubMetricIconType.TARGET),
                            SubMetricItem("Logs Today", "${weightRecords.size}", "", SubMetricIconType.TREND)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            buildDefaultMetricData(date, metricType, goals)
        }
    }

    private fun buildDefaultMetricData(date: LocalDate, metricType: MetricType, goals: UserGoals): HourlyMetricData {
        return HourlyMetricData(
            date = date,
            metricType = metricType,
            hourlyValues = List(24) { 0.0 },
            mainValue = 0.0,
            formattedMainValue = "0",
            unit = "",
            goalValue = null,
            formattedGoal = null,
            subMetrics = emptyList()
        )
    }
}
