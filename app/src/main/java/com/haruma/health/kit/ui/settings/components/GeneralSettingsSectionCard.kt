package com.haruma.health.kit.ui.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.components.SettingsActionRow
import com.haruma.health.kit.ui.components.SettingsGroupCard

@Composable
fun GeneralSettingsSectionCard(
    selectedTheme: String,
    selectedLanguage: String,
    onShowThemeDialog: () -> Unit,
    onShowLanguageDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGroupCard(
        title = stringResource(R.string.settings_section_general),
        modifier = modifier
    ) {
        val themeDisplay = when (selectedTheme) {
            "light" -> stringResource(R.string.settings_theme_light)
            "dark" -> stringResource(R.string.settings_theme_dark)
            else -> stringResource(R.string.settings_theme_system)
        }

        SettingsActionRow(
            icon = Icons.Default.DarkMode,
            iconTint = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.settings_theme),
            subtitle = themeDisplay,
            trailingIcon = Icons.Default.ChevronRight,
            onClick = onShowThemeDialog
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        val languageDisplay = if (selectedLanguage == "vi") {
            stringResource(R.string.settings_language_vi)
        } else {
            stringResource(R.string.settings_language_en)
        }

        SettingsActionRow(
            icon = Icons.Default.Language,
            iconTint = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.settings_language),
            subtitle = languageDisplay,
            trailingIcon = Icons.Default.ChevronRight,
            onClick = onShowLanguageDialog
        )
    }
}
