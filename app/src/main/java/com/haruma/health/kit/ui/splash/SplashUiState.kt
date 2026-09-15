package com.haruma.health.kit.ui.splash

import androidx.annotation.StringRes
import com.haruma.health.kit.R

data class SplashUiState(
    val isInitialized: Boolean = false,
    @StringRes val statusTextResId: Int = R.string.splash_status_initializing,
    val progress: Float = 0f
)
