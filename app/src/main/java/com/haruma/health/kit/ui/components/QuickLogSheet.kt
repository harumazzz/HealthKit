package com.haruma.health.kit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.theme.MetricWeight
import com.haruma.health.kit.ui.theme.RingWater

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogSheet(
    initialWeightKg: Double?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDataSaved: (() -> Unit)? = null,
    viewModel: QuickLogViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(initialWeightKg) {
        viewModel.initialize(initialWeightKg)
    }

    var waterInput by remember(uiState.waterAmountMl) {
        mutableStateOf(
            if (uiState.waterAmountMl % 1.0 == 0.0) {
                uiState.waterAmountMl.toLong().toString()
            } else {
                uiState.waterAmountMl.toString()
            }
        )
    }

    var weightInput by remember(uiState.weightKg) {
        mutableStateOf(
            if (uiState.weightKg % 1.0 == 0.0) {
                uiState.weightKg.toLong().toString()
            } else {
                uiState.weightKg.toString()
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.quick_log),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.log_type),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.toggleTypeMenu(true) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (uiState.selectedType) {
                                            QuickLogType.WATER -> RingWater.copy(alpha = 0.15f)
                                            QuickLogType.WEIGHT -> MetricWeight.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (uiState.selectedType) {
                                        QuickLogType.WATER -> Icons.Default.WaterDrop
                                        QuickLogType.WEIGHT -> Icons.Default.FitnessCenter
                                    },
                                    contentDescription = null,
                                    tint = when (uiState.selectedType) {
                                        QuickLogType.WATER -> RingWater
                                        QuickLogType.WEIGHT -> MetricWeight
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(
                                    when (uiState.selectedType) {
                                        QuickLogType.WATER -> R.string.water
                                        QuickLogType.WEIGHT -> R.string.weight
                                    }
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.select_type),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = uiState.isTypeMenuExpanded,
                        onDismissRequest = { viewModel.toggleTypeMenu(false) },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.water)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = RingWater
                                )
                            },
                            onClick = {
                                viewModel.setLogType(QuickLogType.WATER)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.weight)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MetricWeight
                                )
                            },
                            onClick = {
                                viewModel.setLogType(QuickLogType.WEIGHT)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (uiState.selectedType) {
                QuickLogType.WATER -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        listOf(250L, 500L, 750L).forEach { preset ->
                            FilterChip(
                                selected = uiState.waterAmountMl.toLong() == preset,
                                onClick = {
                                    viewModel.setWaterAmount(preset.toDouble())
                                },
                                label = { Text(stringResource(R.string.quick_add_water, preset)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HealthKitTextField(
                        value = waterInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                waterInput = input
                                viewModel.setWaterAmount(input.toDoubleOrNull() ?: 0.0)
                            }
                        },
                        label = stringResource(R.string.amount_ml),
                        placeholder = "250",
                        leadingIcon = Icons.Default.WaterDrop,
                        trailingText = stringResource(R.string.unit_ml),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.save {
                                onDataSaved?.invoke()
                                onDismiss()
                            }
                        },
                        enabled = !uiState.isSaving && uiState.waterAmountMl > 0.0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
                QuickLogType.WEIGHT -> {
                    HealthKitTextField(
                        value = weightInput,
                        onValueChange = { input ->
                            if (input.matches(Regex("""^\d*\.?\d*$"""))) {
                                weightInput = input
                                val parsed = input.toDoubleOrNull()
                                if (parsed != null) {
                                    viewModel.setWeight(parsed)
                                }
                            }
                        },
                        label = stringResource(R.string.weight_kg),
                        placeholder = "65.0",
                        leadingIcon = Icons.Default.FitnessCenter,
                        trailingText = stringResource(R.string.unit_kg),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.save {
                                onDataSaved?.invoke()
                                onDismiss()
                            }
                        },
                        enabled = !uiState.isSaving && uiState.weightKg > 0.0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
