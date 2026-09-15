package com.haruma.health.kit.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.haruma.health.kit.ui.navigation.HealthNavHost

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    initialRoute: String? = null,
    navController: NavHostController = rememberNavController()
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        HealthNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            initialRoute = initialRoute
        )
    }
}
