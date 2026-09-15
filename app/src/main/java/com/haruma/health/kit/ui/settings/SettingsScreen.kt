package com.haruma.health.kit.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.settings.components.AboutSectionCard
import com.haruma.health.kit.ui.settings.components.DailyGoalsSectionCard
import com.haruma.health.kit.ui.settings.components.GeneralSettingsSectionCard
import com.haruma.health.kit.ui.settings.components.HealthConnectSectionCard
import com.haruma.health.kit.ui.settings.dialogs.AboutAppAlertDialog
import com.haruma.health.kit.ui.settings.dialogs.EditGoalAlertDialog
import com.haruma.health.kit.ui.settings.dialogs.LanguageSelectionDialog
import com.haruma.health.kit.ui.settings.dialogs.ThemeSelectionDialog

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val privacyPolicyUrl = "https://github.com/haruma/health-kit/blob/main/PRIVACY_POLICY.md"
    val termsOfUseUrl = "https://github.com/haruma/health-kit/blob/main/TERMS_OF_USE.md"

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkHealthConnectStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        DailyGoalsSectionCard(
            userGoals = uiState.userGoals,
            onOpenGoalEditor = viewModel::openGoalEditor
        )

        GeneralSettingsSectionCard(
            selectedTheme = uiState.selectedTheme,
            selectedLanguage = uiState.selectedLanguage,
            onShowThemeDialog = { viewModel.showThemeDialog(true) },
            onShowLanguageDialog = { viewModel.showLanguageDialog(true) }
        )

        HealthConnectSectionCard(
            isHealthConnectAvailable = uiState.isHealthConnectAvailable,
            hasAllPermissions = uiState.hasAllPermissions,
            onManagePermissionsClick = { viewModel.openHealthConnectSettings(context) }
        )

        AboutSectionCard(
            appVersion = uiState.appVersion,
            onOpenPrivacyPolicy = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                context.startActivity(intent)
            },
            onOpenTermsOfUse = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsOfUseUrl))
                context.startActivity(intent)
            },
            onShowAboutDialog = { viewModel.showAboutDialog(true) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (uiState.activeEditGoalType != null) {
        EditGoalAlertDialog(
            goalType = uiState.activeEditGoalType!!,
            inputValue = uiState.inputGoalValue,
            onInputValueChange = viewModel::updateInputGoalValue,
            onSave = viewModel::saveGoalValue,
            onDismiss = viewModel::dismissGoalEditor
        )
    }

    if (uiState.showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = uiState.selectedLanguage,
            onLanguageSelected = viewModel::setLanguage,
            onDismiss = { viewModel.showLanguageDialog(false) }
        )
    }

    if (uiState.showThemeDialog) {
        ThemeSelectionDialog(
            selectedTheme = uiState.selectedTheme,
            onThemeSelected = viewModel::setTheme,
            onDismiss = { viewModel.showThemeDialog(false) }
        )
    }

    if (uiState.showAboutDialog) {
        AboutAppAlertDialog(
            appVersion = uiState.appVersion,
            onDismiss = { viewModel.showAboutDialog(false) }
        )
    }
}
