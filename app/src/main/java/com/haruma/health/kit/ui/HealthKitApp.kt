package com.haruma.health.kit.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.haruma.health.kit.data.preferences.UserPreferencesRepository
import com.haruma.health.kit.ui.theme.HealthKitTheme
import java.util.Locale

private class LocalizedContextWrapper(
    base: Context,
    private val configContext: Context
) : ContextWrapper(base) {
    override fun getResources(): Resources = configContext.resources
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HealthKitApp(
    preferencesRepository: UserPreferencesRepository,
    initialRoute: String? = null
) {
    val appLanguage by preferencesRepository.languageFlow.collectAsState(initial = "en")
    val themeMode by preferencesRepository.themeModeFlow.collectAsState(initial = "system")
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = remember(themeMode, systemDark) {
        when (themeMode) {
            "light" -> false
            "dark" -> true
            else -> systemDark
        }
    }

    val context = LocalContext.current
    DisposableEffect(isDarkTheme, context) {
        context.findActivity()?.enableEdgeToEdge(
            statusBarStyle = if (isDarkTheme) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
            navigationBarStyle = if (isDarkTheme) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
        )
        onDispose {}
    }

    val locale = remember(appLanguage) { Locale.forLanguageTag(appLanguage) }
    val currentConfig = LocalConfiguration.current
    val updatedConfig = remember(appLanguage, currentConfig) {
        Configuration(currentConfig).apply {
            setLocale(locale)
        }
    }
    val localizedContext = remember(appLanguage, context) {
        val configContext = context.createConfigurationContext(updatedConfig)
        LocalizedContextWrapper(context, configContext)
    }

    DisposableEffect(appLanguage, context) {
        Locale.setDefault(locale)
        val activity = context.findActivity()
        if (activity != null) {
            val config = activity.resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        }
        onDispose {}
    }

    CompositionLocalProvider(
        LocalConfiguration provides updatedConfig,
        LocalContext provides localizedContext
    ) {
        HealthKitTheme(darkTheme = isDarkTheme) {
            MainScreen(initialRoute = initialRoute)
        }
    }
}
