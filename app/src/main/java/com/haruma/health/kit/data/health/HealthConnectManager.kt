package com.haruma.health.kit.data.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Volume
import com.haruma.health.kit.data.model.DailyHealthSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val healthConnectClient by lazy {
        if (isSupported()) HealthConnectClient.getOrCreate(context) else null
    }

    fun getAvailabilityStatus(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    fun isSupported(): Boolean {
        return getAvailabilityStatus() != HealthConnectClient.SDK_UNAVAILABLE
    }

    fun isAvailable(): Boolean {
        return getAvailabilityStatus() == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(HealthPermissions.PERMISSIONS)
    }

    fun createRequestPermissionResultContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun getDailyHealthSummary(date: LocalDate): DailyHealthSummary {
        val client = healthConnectClient ?: return DailyHealthSummary(date = date)
        val zoneId = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zoneId).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        val timeRange = TimeRangeFilter.between(startTime, endTime)

        return try {
            val aggregateResponse = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL,
                        HydrationRecord.VOLUME_TOTAL
                    ),
                    timeRangeFilter = timeRange
                )
            )

            val steps = aggregateResponse[StepsRecord.COUNT_TOTAL] ?: 0L
            val calories = aggregateResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
            val distance = aggregateResponse[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
            val water = aggregateResponse[HydrationRecord.VOLUME_TOTAL]?.inMilliliters?.toInt() ?: 0

            val heartRateRecords = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = timeRange,
                    ascendingOrder = false,
                    pageSize = 1
                )
            ).records

            val latestBpm = heartRateRecords.firstOrNull()?.samples?.lastOrNull()?.beatsPerMinute?.toDouble()

            val sleepRecords = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = timeRange
                )
            ).records

            val sleepMinutes = sleepRecords.sumOf { record ->
                Duration.between(record.startTime, record.endTime).toMinutes()
            }

            val weightRecords = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = timeRange,
                    ascendingOrder = false,
                    pageSize = 1
                )
            ).records

            val latestWeight = weightRecords.firstOrNull()?.weight?.inKilograms

            DailyHealthSummary(
                date = date,
                steps = steps,
                caloriesBurned = calories,
                distanceMeters = distance,
                latestHeartRateBpm = latestBpm,
                sleepDurationMinutes = sleepMinutes,
                waterMilliliters = water,
                weightKg = latestWeight
            )
        } catch (_: Exception) {
            DailyHealthSummary(date = date)
        }
    }

    suspend fun getHistoricalDailySummaries(startDate: LocalDate, endDate: LocalDate): List<DailyHealthSummary> {
        val client = healthConnectClient ?: return emptyList()
        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.plusDays(1).atStartOfDay()
        val timeRange = TimeRangeFilter.between(startDateTime, endDateTime)

        val resultMap = mutableMapOf<LocalDate, DailyHealthSummary>()
        var dateIterator = startDate
        while (!dateIterator.isAfter(endDate)) {
            resultMap[dateIterator] = DailyHealthSummary(date = dateIterator)
            dateIterator = dateIterator.plusDays(1)
        }

        try {
            val response = client.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL,
                        HydrationRecord.VOLUME_TOTAL,
                        SleepSessionRecord.SLEEP_DURATION_TOTAL,
                        HeartRateRecord.BPM_AVG
                    ),
                    timeRangeFilter = timeRange,
                    timeRangeSlicer = Period.ofDays(1)
                )
            )

            for (group in response) {
                val groupDate = group.startTime.toLocalDate()
                val steps = group.result[StepsRecord.COUNT_TOTAL] ?: 0L
                val calories = group.result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
                val distance = group.result[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
                val water = group.result[HydrationRecord.VOLUME_TOTAL]?.inMilliliters?.toInt() ?: 0
                val sleepDuration = group.result[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.toMinutes() ?: 0L
                val heartRateAvg = group.result[HeartRateRecord.BPM_AVG]?.toDouble()

                resultMap[groupDate] = DailyHealthSummary(
                    date = groupDate,
                    steps = steps,
                    caloriesBurned = calories,
                    distanceMeters = distance,
                    latestHeartRateBpm = heartRateAvg,
                    sleepDurationMinutes = sleepDuration,
                    waterMilliliters = water
                )
            }
        } catch (_: Exception) {
            var fallbackDate = startDate
            while (!fallbackDate.isAfter(endDate)) {
                val daily = getDailyHealthSummary(fallbackDate)
                resultMap[fallbackDate] = daily
                fallbackDate = fallbackDate.plusDays(1)
            }
        }

        return resultMap.values.sortedBy { it.date }
    }

    suspend fun writeWater(milliliters: Double): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val now = Instant.now()
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(now)
            val record = HydrationRecord(
                startTime = now.minusSeconds(1),
                endTime = now,
                startZoneOffset = zoneOffset,
                endZoneOffset = zoneOffset,
                volume = Volume.milliliters(milliliters)
            )
            client.insertRecords(listOf(record))
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun writeWeight(weightKg: Double): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val now = Instant.now()
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(now)
            val record = WeightRecord(
                time = now,
                zoneOffset = zoneOffset,
                weight = Mass.kilograms(weightKg)
            )
            client.insertRecords(listOf(record))
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openHealthConnectSettings(context: Context) {
        val settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent("android.health.connect.action.MANAGE_HEALTH_PERMISSIONS").apply {
                putExtra(Intent.EXTRA_PACKAGE_NAME, context.packageName)
            }
        } else {
            Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
        }

        try {
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settingsIntent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
            }
        }
    }

    fun openInstallHealthConnect(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (_: Exception) {
            }
        }
    }
}

