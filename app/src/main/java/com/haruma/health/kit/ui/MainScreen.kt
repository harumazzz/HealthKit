package com.haruma.health.kit.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.haruma.health.kit.ui.navigation.HealthNavHost
import com.haruma.health.kit.ui.navigation.HealthNavigationBar

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HealthNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        HealthNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
