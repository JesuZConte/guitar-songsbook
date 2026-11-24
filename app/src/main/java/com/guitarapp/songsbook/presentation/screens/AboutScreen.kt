// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.presentation.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.BuildConfig
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.ui.components.FlameGuitar
import com.guitarapp.songsbook.ui.components.LeatherHeader
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    val c = LocalLeatherColors.current
    Scaffold { paddingValues ->
        Column(Modifier.fillMaxSize()) {
            LeatherHeader(
                title = stringResource(R.string.about_title),
                subtitle = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                onBack = onBackClick,
                modifier = Modifier.statusBarsPadding()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                FlameGuitar(size = 72.dp, color = c.leatherMid, flame = c.brassLight)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = c.ink
                )

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.inkSoft,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                AboutSection(R.string.about_license_label, R.string.about_license_text)
                AboutSection(R.string.about_source_label, R.string.about_source_text)
                AboutSection(R.string.about_content_label, R.string.about_content_text)
                AboutSection(R.string.about_third_party_label, R.string.about_third_party_text)
            }
        }
    }
}

@Composable
private fun AboutSection(@StringRes label: Int, @StringRes text: Int) {
    val c = LocalLeatherColors.current
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelMedium,
        color = c.section
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodySmall,
        color = c.inkSoft,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
}
