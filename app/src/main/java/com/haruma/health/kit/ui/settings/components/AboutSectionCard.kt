package com.haruma.health.kit.ui.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.components.SettingsActionRow
import com.haruma.health.kit.ui.components.SettingsGroupCard
import com.haruma.health.kit.ui.components.SettingsValueRow

import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color

@Composable
fun AboutSectionCard(
    appVersion: String,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsOfUse: () -> Unit,
    onShowAboutDialog: () -> Unit,
    onShowRateUsDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGroupCard(
        title = stringResource(R.string.settings_section_about),
        modifier = modifier
    ) {
        SettingsActionRow(
            icon = Icons.Default.Star,
            iconTint = Color(0xFFFFB800),
            title = stringResource(R.string.settings_rate_us),
            subtitle = stringResource(R.string.settings_rate_us_subtitle),
            trailingIcon = Icons.Default.ChevronRight,
            onClick = onShowRateUsDialog
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        SettingsActionRow(
            icon = Icons.Default.PrivacyTip,
            iconTint = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.privacy_policy),
            trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
            onClick = onOpenPrivacyPolicy
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        SettingsActionRow(
            icon = Icons.Default.Description,
            iconTint = MaterialTheme.colorScheme.secondary,
            title = stringResource(R.string.settings_terms_of_use),
            trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
            onClick = onOpenTermsOfUse
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        SettingsActionRow(
            icon = Icons.Default.Info,
            iconTint = MaterialTheme.colorScheme.tertiary,
            title = stringResource(R.string.settings_about_app),
            trailingIcon = Icons.Default.ChevronRight,
            onClick = onShowAboutDialog
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        SettingsValueRow(
            icon = Icons.Default.Verified,
            title = stringResource(R.string.settings_app_version),
            value = appVersion
        )
    }
}
