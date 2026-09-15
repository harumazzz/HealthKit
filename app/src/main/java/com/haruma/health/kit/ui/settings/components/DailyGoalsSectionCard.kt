package com.haruma.health.kit.ui.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.haruma.health.kit.R
import com.haruma.health.kit.data.model.UserGoals
import com.haruma.health.kit.ui.components.GoalItemRow
import com.haruma.health.kit.ui.components.SettingsGroupCard
import com.haruma.health.kit.ui.settings.GoalType
import com.haruma.health.kit.ui.theme.MetricSleep
import com.haruma.health.kit.ui.theme.RingMove
import com.haruma.health.kit.ui.theme.RingSteps
import com.haruma.health.kit.ui.theme.RingWater

@Composable
fun DailyGoalsSectionCard(
    userGoals: UserGoals,
    onOpenGoalEditor: (GoalType) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGroupCard(
        title = stringResource(R.string.settings_section_daily_goals),
        modifier = modifier
    ) {
        GoalItemRow(
            icon = Icons.Default.DirectionsWalk,
            iconTint = RingSteps,
            title = stringResource(R.string.settings_steps_goal),
            value = "${userGoals.stepsGoal} ${stringResource(R.string.unit_steps)}",
            onClick = { onOpenGoalEditor(GoalType.STEPS) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        GoalItemRow(
            icon = Icons.Default.LocalFireDepartment,
            iconTint = RingMove,
            title = stringResource(R.string.settings_calories_goal),
            value = "${userGoals.caloriesGoal.toInt()} ${stringResource(R.string.unit_kcal)}",
            onClick = { onOpenGoalEditor(GoalType.CALORIES) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        GoalItemRow(
            icon = Icons.Default.WaterDrop,
            iconTint = RingWater,
            title = stringResource(R.string.settings_water_goal),
            value = "${userGoals.waterGoalMilliliters} ${stringResource(R.string.unit_ml)}",
            onClick = { onOpenGoalEditor(GoalType.WATER) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        GoalItemRow(
            icon = Icons.Default.Bedtime,
            iconTint = MetricSleep,
            title = stringResource(R.string.settings_sleep_goal),
            value = "${userGoals.sleepGoalHours} ${stringResource(R.string.unit_hours)}",
            onClick = { onOpenGoalEditor(GoalType.SLEEP) }
        )
    }
}
