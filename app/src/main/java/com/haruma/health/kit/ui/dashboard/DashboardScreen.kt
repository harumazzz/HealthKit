package com.haruma.health.kit.ui.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.health.kit.R
import com.haruma.health.kit.data.health.HealthPermissions
import com.haruma.health.kit.ui.components.ActivityRings
import com.haruma.health.kit.ui.components.DailyMetricsSection
import com.haruma.health.kit.ui.components.DateSelectorBar
import com.haruma.health.kit.ui.components.HealthConnectUnavailableCard
import com.haruma.health.kit.ui.components.HealthPermissionCard
import com.haruma.health.kit.ui.components.QuickLogSheet

import com.haruma.health.kit.data.model.MetricType

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToStepDetail: (() -> Unit)? = null,
    onNavigateToMetricDetail: ((MetricType) -> Unit)? = null,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.permissionContract
    ) { grantedPermissions ->
        viewModel.onPermissionsResult(grantedPermissions)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionsAndLoadData()
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

    LaunchedEffect(uiState.hasPermissions, uiState.isHealthConnectAvailable) {
        if (!uiState.hasPermissions && uiState.isHealthConnectAvailable && !uiState.isPermissionRequested) {
            viewModel.markPermissionRequested()
            permissionLauncher.launch(HealthPermissions.PERMISSIONS)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (uiState.hasPermissions) {
                FloatingActionButton(
                    onClick = { viewModel.setQuickLogSheetVisible(true) },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.quick_log)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val contentAlpha by animateFloatAsState(
                targetValue = if (uiState.isLoading) 0.72f else 1f,
                animationSpec = tween(durationMillis = 200),
                label = "dashboardContentAlpha"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = contentAlpha }
            ) {
                DateSelectorBar(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::selectDate
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    if (!uiState.isHealthConnectAvailable) {
                        item {
                            HealthConnectUnavailableCard(
                                onInstallClick = { viewModel.openInstall(context) }
                            )
                        }
                    } else if (!uiState.hasPermissions) {
                        item {
                            HealthPermissionCard(
                                onOpenSettingsClick = { viewModel.openSettings(context) },
                                onGrantPermissionsClick = {
                                    permissionLauncher.launch(HealthPermissions.PERMISSIONS)
                                }
                            )
                        }
                    } else {
                        item {
                            ActivityRings(
                                calories = uiState.healthSummary.caloriesBurned,
                                caloriesGoal = uiState.userGoals.caloriesGoal,
                                steps = uiState.healthSummary.steps,
                                stepsGoal = uiState.userGoals.stepsGoal,
                                water = uiState.healthSummary.waterMilliliters,
                                waterGoal = uiState.userGoals.waterGoalMilliliters
                            )
                        }

                        item {
                            DailyMetricsSection(
                                healthSummary = uiState.healthSummary,
                                userGoals = uiState.userGoals,
                                onLogWaterClick = { viewModel.setQuickLogSheetVisible(true) },
                                onLogWeightClick = { viewModel.setQuickLogSheetVisible(true) },
                                onStepsClick = onNavigateToStepDetail,
                                onMetricClick = onNavigateToMetricDetail
                            )
                        }
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
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }

        if (uiState.showQuickLogSheet) {
            QuickLogSheet(
                initialWeightKg = uiState.healthSummary.weightKg,
                onDismiss = { viewModel.setQuickLogSheetVisible(false) },
                onDataSaved = {
                    viewModel.loadDailyData(uiState.selectedDate)
                }
            )
        }
    }
}
