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

import androidx.compose.runtime.LaunchedEffect

import com.haruma.health.kit.ui.feedback.FeedbackScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun HealthNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialRoute: String? = null
) {
    val swiftEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    val swiftDuration = 280

    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrBlank()) {
            navController.navigate(initialRoute) {
                launchSingleTop = true
            }
        }
    }

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
                },
                onNavigateToFeedback = { rating ->
                    navController.navigate(Screen.feedbackRoute(rating))
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
        composable(
            route = "feedback?rating={rating}",
            arguments = listOf(
                navArgument("rating") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val rating = backStackEntry.arguments?.getInt("rating") ?: 0
            FeedbackScreen(
                initialRating = rating,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
