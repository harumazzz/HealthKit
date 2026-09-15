package com.haruma.health.kit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R
import com.haruma.health.kit.data.model.MetricType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMetricGoalSheet(
    metricType: MetricType,
    currentGoalValue: Double,
    onDismiss: () -> Unit,
    onGoalSaved: (Double) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var goalInput by remember { mutableStateOf(if (currentGoalValue % 1.0 == 0.0) currentGoalValue.toLong().toString() else String.format("%.1f", currentGoalValue)) }
    var isError by remember { mutableStateOf(false) }

    val (titleRes, unitLabel) = when (metricType) {
        MetricType.STEPS -> Pair(R.string.edit_steps_goal_title, R.string.unit_steps)
        MetricType.CALORIES -> Pair(R.string.settings_calories_goal, R.string.unit_kcal)
        MetricType.WATER -> Pair(R.string.settings_water_goal, R.string.unit_ml)
        MetricType.SLEEP -> Pair(R.string.settings_sleep_goal, R.string.unit_hours)
        else -> Pair(R.string.settings_edit_goal, R.string.stat_goal)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = goalInput,
                onValueChange = { input ->
                    goalInput = input
                    isError = input.toDoubleOrNull() == null || (input.toDoubleOrNull() ?: 0.0) <= 0.0
                },
                label = { Text(stringResource(R.string.settings_enter_target)) },
                suffix = { Text(stringResource(unitLabel)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        val parsed = goalInput.toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            onGoalSaved(parsed)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(stringResource(R.string.save), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
