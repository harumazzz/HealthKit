package com.haruma.health.kit.data.health

import android.content.Context
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
}
