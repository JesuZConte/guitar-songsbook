package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.presentation.viewmodel.AddSongViewModel
import com.guitarapp.songsbook.utils.BracketParser
import com.guitarapp.songsbook.utils.BracketSerializer
import java.util.UUID
import androidx.compose.ui.text.TextRange

// ---- Local UI models ----

private enum class InputMode { BUILDER, TEXT }

private data class BuilderSection(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val number: Int,
    val content: TextFieldValue = TextFieldValue()
)

private val SECTION_TYPES = listOf("verse", "chorus", "bridge", "intro", "outro", "pre-chorus", "solo")
private val QUICK_CHORDS = listOf("Am", "C", "G", "F", "Em", "D", "E", "A", "Dm", "Bm", "G7", "D7")

// ---- Conversion helpers ----

private fun rawTextToBuilderSections(rawText: String): List<BuilderSection> {
    if (rawText.isBlank()) return listOf(BuilderSection(type = "verse", number = 1))
    val parsed = BracketParser.parse(rawText)
    if (parsed.isEmpty()) return listOf(BuilderSection(type = "verse", number = 1))
    return parsed.map { section ->
        BuilderSection(
            type = section.type,
            number = section.number,
            content = TextFieldValue(BracketSerializer.serializeSectionContent(section))
        )
    }
}

private fun builderSectionsToRawText(sections: List<BuilderSection>): String {
    return sections.joinToString("\n\n") { section ->
        val header = "[${section.type.replaceFirstChar { it.uppercase() }} ${section.number}]"
        "$header\n${section.content.text}"
    }
}

private fun addSection(type: String, current: List<BuilderSection>): BuilderSection {
    val count = current.count { it.type == type } + 1
    return BuilderSection(type = type, number = count)
}

private fun renumber(sections: List<BuilderSection>): List<BuilderSection> {
    val counters = mutableMapOf<String, Int>()
    return sections.map { section ->
        val count = (counters[section.type] ?: 0) + 1
        counters[section.type] = count
        section.copy(number = count)
    }
}

private fun insertChord(section: BuilderSection, chord: String): BuilderSection {
    val cursor = section.content.selection.start.coerceAtLeast(0)
    val text = section.content.text
    val insertion = "[$chord]"
    val newText = text.substring(0, cursor) + insertion + text.substring(cursor)
    val newCursor = cursor + insertion.length
    return section.copy(
        content = TextFieldValue(text = newText, selection = TextRange(newCursor))
    )
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongScreen(
    viewModel: AddSongViewModel,
    onBackClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var inputMode by remember { mutableStateOf(InputMode.BUILDER) }
    var builderSections by remember { mutableStateOf(rawTextToBuilderSections(uiState.rawText)) }
    var builderInitialized by remember { mutableStateOf(!viewModel.isEditMode) }
    var showFormatHelp by remember { mutableStateOf(false) }
    val formatHelpSheetState = rememberModalBottomSheetState()

    // In edit mode the ViewModel loads the song asynchronously — reinitialize
    // builder sections once rawText arrives from the repository.
    LaunchedEffect(uiState.rawText) {
        if (!builderInitialized && uiState.rawText.isNotBlank()) {
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
                title = { Text(if (viewModel.isEditMode) "Edit Song" else "Add Song") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---- Metadata fields ----
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = uiState.artist,
                onValueChange = viewModel::onArtistChanged,
                label = { Text("Artist *") },
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
                    label = { Text("Capo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.genre,
                    onValueChange = viewModel::onGenreChanged,
                    label = { Text("Genre") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                DifficultyDropdown(
                    selected = uiState.difficulty,
                    onSelected = viewModel::onDifficultyChanged,
                    modifier = Modifier.weight(1f)
                )
            }

            // ---- Mode toggle ----
            HorizontalDivider()
            InputModeToggle(
                mode = inputMode,
                onModeChange = { newMode ->
                    if (newMode == InputMode.BUILDER && inputMode == InputMode.TEXT) {
                        builderSections = rawTextToBuilderSections(uiState.rawText)
                    }
                    inputMode = newMode
                },
                onHelpClick = { showFormatHelp = true }
            )

            // ---- Content area ----
            if (inputMode == InputMode.BUILDER) {
                BuilderContent(
                    sections = builderSections,
                    onSectionsChanged = ::onSectionsChanged
                )
            } else {
                OutlinedTextField(
                    value = uiState.rawText,
                    onValueChange = viewModel::onRawTextChanged,
                    label = { Text("Song (bracket format) *") },
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

            // ---- Actions ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPreviewClick,
                    enabled = uiState.isValid && !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) { Text("Preview") }
                Button(
                    onClick = viewModel::saveSong,
                    enabled = uiState.isValid && !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Save")
                }
            }
        }
    }

    if (showFormatHelp) {
        ModalBottomSheet(
            onDismissRequest = { showFormatHelp = false },
            sheetState = formatHelpSheetState
        ) {
            FormatHelpContent()
        }
    }
}

// ---- Mode toggle ----

@Composable
private fun InputModeToggle(
    mode: InputMode,
    onModeChange: (InputMode) -> Unit,
    onHelpClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Input mode:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilterChip(
            selected = mode == InputMode.BUILDER,
            onClick = { onModeChange(InputMode.BUILDER) },
            label = { Text("Builder") }
        )
        FilterChip(
            selected = mode == InputMode.TEXT,
            onClick = { onModeChange(InputMode.TEXT) },
            label = { Text("Text") }
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onHelpClick, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = "Format help",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---- Format help bottom sheet ----

@Composable
private fun FormatHelpContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "How to write chords",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Place the chord name in square brackets right before the syllable where it's played.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Example",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "[Am]Hello [F]darkness, my [C]old [G]friend",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Tips",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• Use Builder mode and tap a chord chip to insert it at the cursor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• Use Text mode to paste a full song and edit it directly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• Section headers like [Verse 1] or [Chorus 1] are added automatically in Builder mode.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---- Builder content ----

@Composable
private fun BuilderContent(
    sections: List<BuilderSection>,
    onSectionsChanged: (List<BuilderSection>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.forEachIndexed { index, section ->
            SectionCard(
                section = section,
                onContentChange = { updated ->
                    onSectionsChanged(sections.toMutableList().also { it[index] = updated })
                },
                onDelete = {
                    val updated = sections.toMutableList().also { it.removeAt(index) }
                    onSectionsChanged(renumber(updated))
                }
            )
        }

        AddSectionBar(
            onAdd = { type ->
                onSectionsChanged(sections + addSection(type, sections))
            }
        )
    }
}

// ---- Section card ----

@Composable
private fun SectionCard(
    section: BuilderSection,
    onContentChange: (BuilderSection) -> Unit,
    onDelete: () -> Unit
) {
    var customChord by remember { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${section.type.replaceFirstChar { it.uppercase() }} ${section.number}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete section",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Lyrics + chords text area
            OutlinedTextField(
                value = section.content,
                onValueChange = { onContentChange(section.copy(content = it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 300.dp),
                label = { Text("Lyrics & chords") },
                placeholder = {
                    Text(
                        "[Am]Hello [F]darkness\nmy [C]old [G]friend",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                },
                maxLines = Int.MAX_VALUE,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )

            // Chord quick-insert bar
            Text(
                text = "Insert chord:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QUICK_CHORDS.forEach { chord ->
                    SuggestionChip(
                        onClick = { onContentChange(insertChord(section, chord)) },
                        label = { Text(chord, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = customChord,
                    onValueChange = { customChord = it.take(6) },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    label = { Text("Other") },
                    textStyle = MaterialTheme.typography.bodySmall,
                    trailingIcon = {
                        if (customChord.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onContentChange(insertChord(section, customChord.trim()))
                                    customChord = ""
                                }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Insert", modifier = Modifier.padding(0.dp))
                            }
                        }
                    }
                )
            }
        }
    }
}

// ---- Add section bar ----

@Composable
private fun AddSectionBar(onAdd: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Add section:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SECTION_TYPES.forEach { type ->
                SuggestionChip(
                    onClick = { onAdd(type) },
                    label = { Text(type.replaceFirstChar { it.uppercase() }) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) }
                )
            }
        }
    }
}

// ---- Key dropdown ----

private val MUSICAL_KEYS = listOf(
    "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B",
    "Cm", "C#m", "Dm", "Ebm", "Em", "Fm", "F#m", "Gm", "Abm", "Am", "Bbm", "Bm"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.ifBlank { "Auto" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Key") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Auto") },
                onClick = { onSelected(""); expanded = false }
            )
            MUSICAL_KEYS.forEach { key ->
                DropdownMenuItem(
                    text = { Text(key) },
                    onClick = { onSelected(key); expanded = false }
                )
            }
        }
    }
}

// ---- Difficulty dropdown ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultyDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("beginner", "intermediate", "advanced")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Difficulty") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
