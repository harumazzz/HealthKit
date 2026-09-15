package com.haruma.health.kit.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.health.kit.R
import com.haruma.health.kit.data.model.MetricType
import com.haruma.health.kit.ui.components.EditMetricGoalSheet
import com.haruma.health.kit.ui.components.HourlyMetricChart
import com.haruma.health.kit.ui.components.MetricDetailHeader
import com.haruma.health.kit.ui.components.MetricPrimaryValueCard
import com.haruma.health.kit.ui.components.SubMetricsGridCard
import com.haruma.health.kit.ui.theme.MetricDistance
import com.haruma.health.kit.ui.theme.MetricHeartRate
import com.haruma.health.kit.ui.theme.MetricSleep
import com.haruma.health.kit.ui.theme.MetricWeight
import com.haruma.health.kit.ui.theme.RingMove
import com.haruma.health.kit.ui.theme.RingWater

@Composable
fun MetricDetailScreen(
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: MetricDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionsAndLoad()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val accentColor = when (uiState.metricType) {
        MetricType.STEPS -> Color(0xFFF26859)
        MetricType.CALORIES -> RingMove
        MetricType.WATER -> RingWater
        MetricType.SLEEP -> MetricSleep
        MetricType.HEART_RATE -> MetricHeartRate
        MetricType.DISTANCE -> MetricDistance
        MetricType.WEIGHT -> MetricWeight
    }

    val cardBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    val titleRes = when (uiState.metricType) {
        MetricType.STEPS -> R.string.steps
        MetricType.CALORIES -> R.string.calories_burned
        MetricType.WATER -> R.string.water
        MetricType.SLEEP -> R.string.sleep
        MetricType.HEART_RATE -> R.string.heart_rate
        MetricType.DISTANCE -> R.string.distance
        MetricType.WEIGHT -> R.string.weight
    }

    val titleText = stringResource(titleRes)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MetricDetailHeader(
                title = titleText,
                selectedDate = uiState.selectedDate,
                onDateSelected = viewModel::selectDate,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HourlyMetricChart(
                        hourlyValues = uiState.hourlyData.hourlyValues,
                        accentColor = accentColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    MetricPrimaryValueCard(
                        title = titleText,
                        formattedMainValue = uiState.hourlyData.formattedMainValue,
                        unit = uiState.hourlyData.unit,
                        formattedGoal = uiState.hourlyData.formattedGoal,
                        accentColor = accentColor,
                        cardBackground = cardBackground,
                        onChangeGoalClick = { viewModel.setGoalSheetVisible(true) }
                    )
                }

                if (uiState.hourlyData.subMetrics.isNotEmpty()) {
                    item {
                        SubMetricsGridCard(
                            subMetrics = uiState.hourlyData.subMetrics,
                            accentColor = accentColor,
                            cardBackground = cardBackground
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(250)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = accentColor,
                    trackColor = Color.Transparent
                )
            }
        }

        if (uiState.showGoalSheet && uiState.hourlyData.goalValue != null) {
            EditMetricGoalSheet(
                metricType = uiState.metricType,
                currentGoalValue = uiState.hourlyData.goalValue ?: 0.0,
                onDismiss = { viewModel.setGoalSheetVisible(false) },
                onGoalSaved = viewModel::updateGoal,
                accentColor = accentColor
            )
        }
    }
}
