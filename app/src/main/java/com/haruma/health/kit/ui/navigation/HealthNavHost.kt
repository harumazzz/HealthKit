package com.haruma.health.kit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.haruma.health.kit.ui.dashboard.DashboardScreen
import com.haruma.health.kit.ui.detail.MetricDetailScreen
import com.haruma.health.kit.ui.settings.SettingsScreen

@Composable
fun HealthNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Detail.route) {
            MetricDetailScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
