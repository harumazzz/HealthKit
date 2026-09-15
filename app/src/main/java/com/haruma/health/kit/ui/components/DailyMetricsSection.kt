package com.haruma.health.kit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R
import com.haruma.health.kit.data.model.DailyHealthSummary
import com.haruma.health.kit.data.model.UserGoals
import com.haruma.health.kit.ui.theme.MetricDistance
import com.haruma.health.kit.ui.theme.MetricHeartRate
import com.haruma.health.kit.ui.theme.MetricSleep
import com.haruma.health.kit.ui.theme.MetricWeight
import com.haruma.health.kit.ui.theme.RingMove
import com.haruma.health.kit.ui.theme.RingSteps
import com.haruma.health.kit.ui.theme.RingWater
import java.util.Locale

@Composable
fun DailyMetricsSection(
    healthSummary: DailyHealthSummary,
    userGoals: UserGoals,
    onLogWaterClick: () -> Unit,
    onLogWeightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val steps = healthSummary.steps
        val stepsGoal = userGoals.stepsGoal
        MetricCard(
            icon = Icons.Default.DirectionsWalk,
            iconTint = RingSteps,
            title = stringResource(R.string.steps),
            value = String.format(Locale.US, "%,d", steps),
            unit = stringResource(R.string.unit_steps),
            progress = if (stepsGoal > 0) steps.toFloat() / stepsGoal.toFloat() else null,
            progressColor = RingSteps,
            subtitle = "Goal: ${String.format(Locale.US, "%,d", stepsGoal)}"
        )

        val calories = healthSummary.caloriesBurned
        val caloriesGoal = userGoals.caloriesGoal
        MetricCard(
            icon = Icons.Default.LocalFireDepartment,
            iconTint = RingMove,
            title = stringResource(R.string.calories_burned),
            value = String.format(Locale.US, "%,d", calories.toInt()),
            unit = stringResource(R.string.unit_kcal),
            progress = if (caloriesGoal > 0) (calories / caloriesGoal).toFloat() else null,
            progressColor = RingMove,
            subtitle = "Goal: ${caloriesGoal.toInt()} ${stringResource(R.string.unit_kcal)}"
        )

        val water = healthSummary.waterMilliliters
        val waterGoal = userGoals.waterGoalMilliliters
        MetricCard(
            icon = Icons.Default.WaterDrop,
            iconTint = RingWater,
            title = stringResource(R.string.water),
            value = String.format(Locale.US, "%,d", water),
            unit = stringResource(R.string.unit_ml),
            progress = if (waterGoal > 0) water.toFloat() / waterGoal.toFloat() else null,
            progressColor = RingWater,
            subtitle = "Goal: $waterGoal ${stringResource(R.string.unit_ml)}",
            onClick = onLogWaterClick
        )

        val sleepMinutes = healthSummary.sleepDurationMinutes
        val sleepHours = sleepMinutes / 60
        val sleepRemainingMinutes = sleepMinutes % 60
        val sleepText = if (sleepMinutes > 0) {
            "${sleepHours}h ${sleepRemainingMinutes}m"
        } else {
            stringResource(R.string.no_data)
        }
        MetricCard(
            icon = Icons.Default.Bedtime,
            iconTint = MetricSleep,
            title = stringResource(R.string.sleep),
            value = sleepText,
            unit = "",
            progress = (sleepMinutes.toFloat() / (userGoals.sleepGoalHours * 60).toFloat()),
            progressColor = MetricSleep,
            subtitle = "Goal: ${userGoals.sleepGoalHours.toInt()} ${stringResource(R.string.unit_hours)}"
        )

        val heartRate = healthSummary.latestHeartRateBpm
        MetricCard(
            icon = Icons.Default.Favorite,
            iconTint = MetricHeartRate,
            title = stringResource(R.string.heart_rate),
            value = if (heartRate != null && heartRate > 0) "${heartRate.toInt()}" else stringResource(R.string.no_data),
            unit = if (heartRate != null && heartRate > 0) stringResource(R.string.unit_bpm) else "",
            subtitle = if (heartRate != null && heartRate > 0) "Latest" else null
        )

        val distanceKm = healthSummary.distanceMeters / 1000.0
        MetricCard(
            icon = Icons.Default.Straighten,
            iconTint = MetricDistance,
            title = stringResource(R.string.distance),
            value = String.format(Locale.US, "%.2f", distanceKm),
            unit = stringResource(R.string.unit_km)
        )

        val weightKg = healthSummary.weightKg
        MetricCard(
            icon = Icons.Default.FitnessCenter,
            iconTint = MetricWeight,
            title = stringResource(R.string.weight),
            value = if (weightKg != null && weightKg > 0) String.format(Locale.US, "%.1f", weightKg) else stringResource(R.string.no_data),
            unit = if (weightKg != null && weightKg > 0) stringResource(R.string.unit_kg) else "",
            onClick = onLogWeightClick
        )
    }
}
