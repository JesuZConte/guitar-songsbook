package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.presentation.viewmodel.VersionEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionEditorScreen(
    viewModel: VersionEditorViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var inputMode by remember { mutableStateOf(InputMode.BUILDER) }
    var builderSections by remember { mutableStateOf(rawTextToBuilderSections("")) }
    var builderInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.rawText) {
        if (!builderInitialized && !uiState.isLoading && uiState.rawText.isNotBlank()) {
            builderSections = rawTextToBuilderSections(uiState.rawText)
            builderInitialized = true
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaveSuccess()
    }

    fun onSectionsChanged(updated: List<BuilderSection>) {
        builderSections = updated
        viewModel.onRawTextChanged(builderSectionsToRawText(updated))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isEditMode) stringResource(R.string.edit_version_title)
                        else stringResource(R.string.add_version)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text(stringResource(R.string.version_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KeyDropdown(
                    selected = uiState.key,
                    onSelected = viewModel::onKeyChanged,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.capo,
                    onValueChange = viewModel::onCapoChanged,
                    label = { Text(stringResource(R.string.add_song_field_capo)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            HorizontalDivider()
            InputModeToggle(
                mode = inputMode,
                onModeChange = { newMode ->
                    if (newMode == InputMode.BUILDER && inputMode == InputMode.TEXT) {
                        builderSections = rawTextToBuilderSections(uiState.rawText)
                    }
                    inputMode = newMode
                },
                onHelpClick = {}
            )

            if (inputMode == InputMode.BUILDER) {
                BuilderContent(
                    sections = builderSections,
                    onSectionsChanged = ::onSectionsChanged
                )
            } else {
                OutlinedTextField(
                    value = uiState.rawText,
                    onValueChange = viewModel::onRawTextChanged,
                    label = { Text(stringResource(R.string.add_song_song_field)) },
                    placeholder = {
                        Text(
                            "[Verse 1]\n[Am]Hello [F]darkness\nmy [C]old [G]friend",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 250.dp, max = 400.dp),
                    maxLines = Int.MAX_VALUE,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = viewModel::saveVersion,
                enabled = uiState.isValid && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(stringResource(R.string.common_save))
            }
        }
    }
}
