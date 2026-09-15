package com.haruma.health.kit.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.haruma.health.kit.R

import androidx.compose.material.icons.automirrored.filled.DirectionsWalk

import androidx.compose.material.icons.filled.Feedback

sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", R.string.nav_dashboard, "Dashboard", Icons.Default.Dashboard)
    data object Trends : Screen("trends", R.string.nav_trends, "Trends", Icons.Default.BarChart)
    data object Settings : Screen("settings", R.string.nav_settings, "Settings", Icons.Default.Settings)
    data object StepDetail : Screen("step_detail", R.string.steps, "Step Detail", Icons.AutoMirrored.Filled.DirectionsWalk)
    data object MainTab : Screen("main_tab", R.string.nav_dashboard, "Main Tab", Icons.Default.Dashboard)
    data object FeedbackScreen : Screen("feedback", R.string.feedback_title, "Feedback", Icons.Default.Feedback)

    companion object {
        val Detail = Trends
        val bottomNavItems = listOf(Dashboard, Trends, Settings)
        fun metricDetailRoute(metricType: String) = "detail/$metricType"
        fun feedbackRoute(rating: Int = 0) = "feedback?rating=$rating"
    }
}
