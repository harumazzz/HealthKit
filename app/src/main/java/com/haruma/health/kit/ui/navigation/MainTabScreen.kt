package com.haruma.health.kit.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.data.model.MetricType
import com.haruma.health.kit.ui.dashboard.DashboardScreen
import com.haruma.health.kit.ui.detail.MetricDetailScreen
import com.haruma.health.kit.ui.settings.SettingsScreen
import com.haruma.health.kit.ui.trends.TrendsScreen

@Composable
fun MainTabScreen(
    onNavigateToStepDetail: () -> Unit,
    onNavigateToMetricDetail: (MetricType) -> Unit,
    onNavigateToFeedback: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val itemTitle = stringResource(screen.titleResId)
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = itemTitle) },
                        label = { Text(itemTitle) },
                        selected = selectedTab == screen,
                        onClick = {
                            if (selectedTab != screen) {
                                selectedTab = screen
                            }
                        }
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
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 200),
                label = "tabCrossfade"
            ) { tab ->
                when (tab) {
                    Screen.Dashboard -> DashboardScreen(
                        onNavigateToStepDetail = onNavigateToStepDetail,
                        onNavigateToMetricDetail = onNavigateToMetricDetail
                    )
                    Screen.Trends -> TrendsScreen()
                    Screen.Settings -> SettingsScreen(
                        onNavigateToFeedback = onNavigateToFeedback
                    )
                    else -> DashboardScreen(
                        onNavigateToStepDetail = onNavigateToStepDetail,
                        onNavigateToMetricDetail = onNavigateToMetricDetail
                    )
                }
            }
        }
    }
}
