package com.haruma.health.kit.ui.trends

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.health.connect.client.PermissionController
import com.haruma.health.kit.data.health.HealthPermissions
import com.haruma.health.kit.ui.components.HealthConnectUnavailableCard
import com.haruma.health.kit.ui.components.HealthPermissionCard
import com.haruma.health.kit.ui.components.MetricChartCard
import com.haruma.health.kit.ui.components.MetricSelectorSection
import com.haruma.health.kit.ui.components.MetricSummaryCards
import com.haruma.health.kit.ui.detail.DetailMetric
import com.haruma.health.kit.ui.theme.MetricHeartRate
import com.haruma.health.kit.ui.theme.MetricSleep
import com.haruma.health.kit.ui.theme.RingMove
import com.haruma.health.kit.ui.theme.RingSteps

@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.checkPermissionsAndLoad()
    }

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

    val activeColor = when (uiState.selectedMetric) {
        DetailMetric.STEPS -> RingSteps
        DetailMetric.CALORIES -> RingMove
        DetailMetric.SLEEP -> MetricSleep
        DetailMetric.HEART_RATE -> MetricHeartRate
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MetricSelectorSection(
                    timeRange = uiState.timeRange,
                    selectedMetric = uiState.selectedMetric,
                    onTimeRangeSelected = viewModel::selectTimeRange,
                    onMetricSelected = viewModel::selectMetric
                )
            }

            if (!uiState.isHealthConnectAvailable) {
                item {
                    HealthConnectUnavailableCard(
                        onInstallClick = {
                            viewModel.openInstallHealthConnect(context)
                        }
                    )
                }
            } else if (!uiState.hasPermissions) {
                item {
                    HealthPermissionCard(
                        onOpenSettingsClick = {
                            viewModel.openHealthConnectSettings(context)
                        },
                        onGrantPermissionsClick = {
                            permissionLauncher.launch(HealthPermissions.PERMISSIONS)
                        }
                    )
                }
            }

            item {
                AnimatedContent(
                    targetState = uiState.summaryStats,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                            fadeOut(animationSpec = tween(180))
                    },
                    label = "summaryStatsContent"
                ) { stats ->
                    MetricSummaryCards(
                        stats = stats,
                        selectedMetric = uiState.selectedMetric,
                        accentColor = activeColor
                    )
                }
            }

            item {
                AnimatedContent(
                    targetState = Triple(uiState.chartBars, uiState.weekdayAverages, uiState.selectedMetric),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(260, delayMillis = 60)) togetherWith
                            fadeOut(animationSpec = tween(180))
                    },
                    label = "chartContent"
                ) { (bars, weekdayAvgs, metric) ->
                    MetricChartCard(
                        selectedMetric = metric,
                        chartBars = bars,
                        selectedBarIndex = uiState.selectedBarIndex,
                        onBarSelected = viewModel::selectBar,
                        goalValue = uiState.summaryStats.goalValue,
                        formattedGoalValue = uiState.summaryStats.formattedGoalValue,
                        accentColor = activeColor,
                        weekdayAverages = weekdayAvgs
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = activeColor,
                trackColor = Color.Transparent
            )
        }
    }
}
