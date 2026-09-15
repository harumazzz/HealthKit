package com.haruma.health.kit.ui.settings.dialogs

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.components.HealthKitTextField
import com.haruma.health.kit.ui.settings.GoalType

@Composable
fun EditGoalAlertDialog(
    goalType: GoalType,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val goalTitle = when (goalType) {
        GoalType.STEPS -> stringResource(R.string.settings_steps_goal)
        GoalType.CALORIES -> stringResource(R.string.settings_calories_goal)
        GoalType.WATER -> stringResource(R.string.settings_water_goal)
        GoalType.SLEEP -> stringResource(R.string.settings_sleep_goal)
    }
    val unitLabel = when (goalType) {
        GoalType.STEPS -> stringResource(R.string.unit_steps)
        GoalType.CALORIES -> stringResource(R.string.unit_kcal)
        GoalType.WATER -> stringResource(R.string.unit_ml)
        GoalType.SLEEP -> stringResource(R.string.unit_hours)
    }
    val isDecimal = goalType == GoalType.SLEEP

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${stringResource(R.string.settings_edit_goal)}: $goalTitle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HealthKitTextField(
                    value = inputValue,
                    onValueChange = { input ->
                        if (isDecimal) {
                            if (input.matches(Regex("""^\d*\.?\d*$"""))) {
                                onInputValueChange(input)
                            }
                        } else {
                            if (input.all { it.isDigit() }) {
                                onInputValueChange(input)
                            }
                        }
                    },
                    label = stringResource(R.string.settings_enter_target),
                    placeholder = "0",
                    trailingText = unitLabel,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSave()
                        }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
