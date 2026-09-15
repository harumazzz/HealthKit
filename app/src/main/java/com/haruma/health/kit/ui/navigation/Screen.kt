package com.haruma.health.kit.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.haruma.health.kit.R

sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", R.string.nav_dashboard, "Dashboard", Icons.Default.Dashboard)
    data object Trends : Screen("trends", R.string.nav_trends, "Trends", Icons.Default.BarChart)
    data object Settings : Screen("settings", R.string.nav_settings, "Settings", Icons.Default.Settings)

    companion object {
        val Detail = Trends
        val bottomNavItems = listOf(Dashboard, Trends, Settings)
    }
}
