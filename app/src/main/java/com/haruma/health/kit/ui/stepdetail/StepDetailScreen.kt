package com.haruma.health.kit.ui.stepdetail

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.health.kit.ui.components.EditGoalSheet
import com.haruma.health.kit.ui.components.HourlyStepChart
import com.haruma.health.kit.ui.stepdetail.components.StepCountSummaryCard
import com.haruma.health.kit.ui.stepdetail.components.StepDetailHeader
import com.haruma.health.kit.ui.stepdetail.components.StepMetricsGridCard

@Composable
fun StepDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StepDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionsAndLoad()
                viewModel.startPeriodicRefresh()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopPeriodicRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopPeriodicRefresh()
        }
    }

    val accentCoral = Color(0xFFF26859)
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StepDetailHeader(
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
                    HourlyStepChart(
                        hourlySteps = uiState.hourlyStepData.hourlySteps,
                        barColor = accentCoral,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    StepCountSummaryCard(
                        totalSteps = uiState.hourlyStepData.totalSteps,
                        stepsGoal = uiState.stepsGoal,
                        accentColor = accentCoral,
                        cardBackground = cardBackground,
                        onChangeGoalClick = { viewModel.setGoalSheetVisible(true) }
                    )
                }

                item {
                    StepMetricsGridCard(
                        activeDurationSeconds = uiState.hourlyStepData.activeDurationSeconds,
                        distanceKm = uiState.hourlyStepData.distanceKm,
                        caloriesKcal = uiState.hourlyStepData.caloriesKcal,
                        accentColor = accentCoral,
                        cardBackground = cardBackground
                    )
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
                    color = accentCoral,
                    trackColor = Color.Transparent
                )
            }
        }

        if (uiState.showGoalSheet) {
            EditGoalSheet(
                currentGoal = uiState.stepsGoal,
                onDismiss = { viewModel.setGoalSheetVisible(false) },
                onGoalSaved = { newGoal ->
                    viewModel.updateStepsGoal(newGoal)
                },
                accentColor = accentCoral
            )
        }
    }
}
