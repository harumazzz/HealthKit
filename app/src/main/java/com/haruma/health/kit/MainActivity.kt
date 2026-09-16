package com.haruma.health.kit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import com.haruma.health.kit.ui.HealthKitApp
import com.haruma.health.kit.ui.widget.HealthKitWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.haruma.health.kit.ui.widget.HealthWidgetManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val initialRouteState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractInitialRoute(intent)

        HealthWidgetManager.schedulePeriodicWidgetUpdate(this)

        setContent {
            HealthKitApp(
                preferencesRepository = preferencesRepository,
                initialRoute = initialRouteState.value
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractInitialRoute(intent)
    }

    private fun extractInitialRoute(intent: Intent?) {
        val route = intent?.getStringExtra(HealthKitWidgetProvider.EXTRA_NAVIGATE_TO)
            ?: if (intent?.action == HealthKitWidgetProvider.ACTION_OPEN_STEP_DETAIL) {
                HealthKitWidgetProvider.ROUTE_STEP_DETAIL
            } else {
                null
            }
        initialRouteState.value = route
    }
}