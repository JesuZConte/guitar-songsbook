package com.guitarapp.songsbook.presentation.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.data.local.ThemeMode
import com.guitarapp.songsbook.data.local.UserPreferences
import com.guitarapp.songsbook.utils.AnalyticsHelper
import com.guitarapp.songsbook.utils.NotationSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    var notation by remember { mutableStateOf(UserPreferences.getNotation(context)) }
    var themeMode by remember { mutableStateOf(UserPreferences.getThemeMode(context)) }
    val activity = context as Activity
    var currentLanguage by remember {
        mutableStateOf(
            UserPreferences.getLanguage(context)
                ?: context.resources.configuration.locales[0].language.takeIf { it == "es" }
                ?: "en"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_cancel))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsSectionHeader(stringResource(R.string.settings_section_appearance))
            ThemeSelectorRow(
                current = themeMode,
                onSelected = { mode ->
                    UserPreferences.setThemeMode(context, mode)
                    themeMode = mode
                    onThemeModeChanged(mode)
                }
            )
            HorizontalDivider()
            SettingsSectionHeader(stringResource(R.string.settings_section_language))
            LanguageSelectorRow(
                currentLanguage = currentLanguage,
                onSelected = { code ->
                    UserPreferences.setLanguage(context, code)
                    activity.recreate()
                }
            )
            HorizontalDivider()
            SettingsSectionHeader(stringResource(R.string.settings_section_chords))
            NotationToggleRow(
                isLatin = notation == NotationSystem.LATIN,
                onToggle = { useLatin ->
                    val newNotation = if (useLatin) NotationSystem.LATIN else NotationSystem.AMERICAN
                    UserPreferences.setNotation(context, newNotation)
                    notation = newNotation
                    AnalyticsHelper.logNotationChanged(newNotation.name)
                }
            )
            HorizontalDivider()
            SettingsSectionHeader(stringResource(R.string.settings_section_account))
            SignInRow()
            HorizontalDivider()
            SettingsSectionHeader(stringResource(R.string.settings_section_app))
            AboutRow(onAboutClick)
            HorizontalDivider()
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun AboutRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.settings_about),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SignInRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_sign_in),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = stringResource(R.string.settings_sign_in_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = stringResource(R.string.settings_coming_soon),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ThemeSelectorRow(current: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                ThemeMode.LIGHT  to stringResource(R.string.settings_theme_light),
                ThemeMode.DARK   to stringResource(R.string.settings_theme_dark)
            ).forEach { (mode, label) ->
                FilterChip(
                    selected = current == mode,
                    onClick = { onSelected(mode) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectorRow(currentLanguage: String, onSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "en" to stringResource(R.string.settings_lang_english),
                "es" to stringResource(R.string.settings_lang_spanish)
            ).forEach { (code, label) ->
                FilterChip(
                    selected = currentLanguage == code,
                    onClick = { onSelected(code) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Composable
private fun NotationToggleRow(isLatin: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isLatin) stringResource(R.string.settings_notation_latin)
                       else stringResource(R.string.settings_notation_american),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isLatin) stringResource(R.string.settings_notation_latin_notes)
                       else stringResource(R.string.settings_notation_american_notes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isLatin,
            onCheckedChange = onToggle
        )
    }
}
