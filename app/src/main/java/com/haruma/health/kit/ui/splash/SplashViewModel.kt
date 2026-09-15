package com.haruma.health.kit.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.health.kit.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        startInitializationSequence()
    }

    private fun startInitializationSequence() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    statusTextResId = R.string.splash_status_initializing,
                    progress = 0.2f
                )
            }
            delay(500)

            _uiState.update {
                it.copy(
                    statusTextResId = R.string.splash_status_config,
                    progress = 0.6f
                )
            }
            delay(700)

            _uiState.update {
                it.copy(
                    statusTextResId = R.string.splash_status_ready,
                    progress = 1.0f
                )
            }
            delay(300)

            _uiState.update {
                it.copy(isInitialized = true)
            }
        }
    }
}
