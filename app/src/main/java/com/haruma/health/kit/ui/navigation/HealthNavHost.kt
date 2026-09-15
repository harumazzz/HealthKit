package com.haruma.health.kit.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.haruma.health.kit.ui.detail.MetricDetailScreen
import com.haruma.health.kit.ui.stepdetail.StepDetailScreen

@Composable
fun HealthNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val swiftEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    val swiftDuration = 280

    NavHost(
        navController = navController,
        startDestination = Screen.MainTab.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = swiftDuration, easing = swiftEasing)
            )
        }
    ) {
        composable(Screen.MainTab.route) {
            MainTabScreen(
                onNavigateToStepDetail = {
                    navController.navigate("detail/steps")
                },
                onNavigateToMetricDetail = { metricType ->
                    navController.navigate("detail/${metricType.name.lowercase()}")
                }
            )
        }
        composable(Screen.StepDetail.route) {
            StepDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("detail/{metricType}") {
            MetricDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
