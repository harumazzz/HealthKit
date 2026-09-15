package com.haruma.health.kit.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    val screenOrder = listOf(Screen.Dashboard.route, Screen.Trends.route, Screen.Settings.route)
    val swiftEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    val swiftDuration = 350

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier,
        enterTransition = {
            val initialIndex = screenOrder.indexOf(initialState.destination.route).takeIf { it >= 0 } ?: 0
            val targetIndex = screenOrder.indexOf(targetState.destination.route).takeIf { it >= 0 } ?: 0
            val direction = if (targetIndex >= initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            slideIntoContainer(
                towards = direction,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 200))
        },
        exitTransition = {
            val initialIndex = screenOrder.indexOf(initialState.destination.route).takeIf { it >= 0 } ?: 0
            val targetIndex = screenOrder.indexOf(targetState.destination.route).takeIf { it >= 0 } ?: 0
            val direction = if (targetIndex >= initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            slideOutOfContainer(
                towards = direction,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
        },
        popEnterTransition = {
            val initialIndex = screenOrder.indexOf(initialState.destination.route).takeIf { it >= 0 } ?: 0
            val targetIndex = screenOrder.indexOf(targetState.destination.route).takeIf { it >= 0 } ?: 0
            val direction = if (targetIndex >= initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            slideIntoContainer(
                towards = direction,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 200))
        },
        popExitTransition = {
            val initialIndex = screenOrder.indexOf(initialState.destination.route).takeIf { it >= 0 } ?: 0
            val targetIndex = screenOrder.indexOf(targetState.destination.route).takeIf { it >= 0 } ?: 0
            val direction = if (targetIndex >= initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            slideOutOfContainer(
                towards = direction,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
        }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Trends.route) {
            MetricDetailScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
