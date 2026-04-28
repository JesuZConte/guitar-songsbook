package com.guitarapp.songsbook.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import com.guitarapp.songsbook.data.local.UserPreferences
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongVersion
import com.guitarapp.songsbook.utils.SongExporter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import com.guitarapp.songsbook.ui.theme.NocturnoColorScheme

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SongReaderScreen(
    viewModel: ReaderViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    onAddVersionClick: (songId: String, sourceVersionId: Long) -> Unit = { _, _ -> },
    onEditVersionClick: (versionId: Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlistsState by playlistsViewModel.uiState.collectAsState()

    val effectiveSong = uiState.song?.let { song ->
        val version = song.versions.getOrNull(uiState.selectedVersionIndex)
            ?: song.versions.firstOrNull()
        if (version != null) {
            song.copy(
                key = version.key,
                capo = version.capo,
                chords = version.chords,
                notes = version.notes,
                content = version.content
            )
        } else song
    }

    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showShareMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var songToBackup by remember { mutableStateOf<Song?>(null) }

    var accumulatedScale by remember { mutableStateOf(1f) }
    val pinchState = rememberTransformableState { zoomChange, _, _ ->
        accumulatedScale *= zoomChange
        val steps = (accumulatedScale - 1f) / 0.1f
        if (steps >= 1f) {
            viewModel.increaseFontSize()
            accumulatedScale = 1f
        } else if (steps <= -1f) {
            viewModel.decreaseFontSize()
            accumulatedScale = 1f
        }
    }

    LaunchedEffect(uiState.fontSize) {
        UserPreferences.setFontSize(context, uiState.fontSize)
    }
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val song = songToBackup ?: return@rememberLauncherForActivityResult
        val json = Gson().toJson(song)
        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) onDeleteSuccess()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val screenContent: @Composable () -> Unit = {
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !uiState.isFullscreen,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.song?.title ?: stringResource(R.string.reader_loading),
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (uiState.song != null) {
                                Text(
                                    text = uiState.song!!.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (uiState.song != null) {
                            Box {
                                IconButton(onClick = { showShareMenu = true }) {
                                    Icon(Icons.Filled.Share, contentDescription = "Share")
                                }
                                DropdownMenu(
                                    expanded = showShareMenu,
                                    onDismissRequest = { showShareMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.reader_backup)) },
                                        leadingIcon = { Icon(Icons.Filled.SaveAlt, contentDescription = null) },
                                        onClick = {
                                            showShareMenu = false
                                            songToBackup = uiState.song
                                            backupLauncher.launch("${uiState.song!!.title}.json")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.reader_share_chords)) },
                                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                                        onClick = {
                                            showShareMenu = false
                                            val text = SongExporter.buildChordShareText(uiState.song!!)
                                            val shareChords = context.getString(R.string.reader_share_chords)
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, text)
                                            }
                                            context.startActivity(Intent.createChooser(intent, shareChords))
                                        }
                                    )
                                }
                            }
                            IconButton(onClick = onEditClick) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit song")
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete song")
                            }
                            IconButton(onClick = {
                                playlistsViewModel.loadPlaylists()
                                showPlaylistPicker = true
                            }) {
                                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to playlist")
                            }
                            IconButton(onClick = { viewModel.toggleNocturno() }) {
                                Icon(
                                    imageVector = Icons.Filled.Bedtime,
                                    contentDescription = "Toggle nocturno mode",
                                    tint = if (uiState.isNocturno) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (uiState.song!!.isFavorite) Icons.Filled.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Toggle favorite",
                                    tint = if (uiState.song!!.isFavorite) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !uiState.isFullscreen,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                ReaderBottomBar(
                    currentPage = uiState.currentPage,
                    totalPages = uiState.totalPages,
                    fontSize = uiState.fontSize,
                    transposeSteps = uiState.transposeSteps,
                    onIncreaseFontSize = viewModel::increaseFontSize,
                    onDecreaseFontSize = viewModel::decreaseFontSize,
                    onTransposeUp = viewModel::transposeUp,
                    onTransposeDown = viewModel::transposeDown,
                    onResetTranspose = viewModel::resetTranspose,
                    onToggleFullscreen = viewModel::toggleFullscreen
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val song = uiState.song
            if (song != null && song.versions.isNotEmpty() && !uiState.isFullscreen) {
                VersionSelectorRow(
                    versions = song.versions,
                    selectedIndex = uiState.selectedVersionIndex,
                    onSelected = viewModel::selectVersion,
                    onAddVersion = {
                        val currentVersion = song.versions.getOrNull(uiState.selectedVersionIndex)
                            ?: song.versions.first()
                        onAddVersionClick(song.id, currentVersion.id)
                    },
                    onEditVersion = onEditVersionClick,
                    onDeleteVersion = viewModel::deleteVersion
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .transformable(state = pinchState)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null -> {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    effectiveSong != null -> {
                        VirtualPagedSong(
                            song = effectiveSong,
                            fontSize = uiState.fontSize,
                            transposeSteps = uiState.transposeSteps,
                            currentPage = uiState.currentPage,
                            onPageChanged = viewModel::onPageChanged,
                            onPageCountMeasured = viewModel::onMeasuredPageCount,
                            onTap = viewModel::toggleFullscreen,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (showDeleteConfirm && uiState.song != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.reader_delete_title)) },
                text = { Text(stringResource(R.string.reader_delete_body, uiState.song!!.title)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteSong()
                    }) {
                        Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
                }
            )
        }

        if (showPlaylistPicker && uiState.song != null) {
            PlaylistPickerDialog(
                playlists = playlistsState.playlists,
                onPlaylistSelected = { playlistId ->
                    playlistsViewModel.addSongToPlaylist(playlistId, uiState.song!!.id)
                    showPlaylistPicker = false
                },
                onDismiss = { showPlaylistPicker = false }
            )
        }
    }
    } // end screenContent lambda

    if (uiState.isNocturno) {
        MaterialTheme(colorScheme = NocturnoColorScheme) {
            CompositionLocalProvider(LocalNocturnoMode provides true) {
                screenContent()
            }
        }
    } else {
        screenContent()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun VersionSelectorRow(
    versions: List<SongVersion>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    onAddVersion: () -> Unit,
    onEditVersion: (Long) -> Unit,
    onDeleteVersion: (Long) -> Unit
) {
    var menuVersionIndex by remember { mutableStateOf<Int?>(null) }
    var deleteTarget by remember { mutableStateOf<SongVersion?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        versions.forEachIndexed { index, version ->
            Box {
                VersionChip(
                    name = version.name,
                    selected = index == selectedIndex,
                    onClick = { onSelected(index) },
                    onLongClick = { menuVersionIndex = index }
                )
                DropdownMenu(
                    expanded = menuVersionIndex == index,
                    onDismissRequest = { menuVersionIndex = null }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.version_menu_edit)) },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            menuVersionIndex = null
                            onEditVersion(version.id)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.version_menu_delete)) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        enabled = versions.size > 1,
                        onClick = {
                            menuVersionIndex = null
                            deleteTarget = version
                        }
                    )
                }
            }
        }
        IconButton(onClick = onAddVersion) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_version))
        }
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.version_delete_title)) },
            text = { Text(stringResource(R.string.version_delete_body, deleteTarget!!.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteVersion(deleteTarget!!.id)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun VersionChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
                         else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                       else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
                      else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = CircleShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = name, color = contentColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PlaylistPickerDialog(
    playlists: List<Playlist>,
    onPlaylistSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reader_add_to_playlist_title)) },
        text = {
            if (playlists.isEmpty()) {
                Text(
                    text = stringResource(R.string.reader_no_playlists),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(playlists, key = { it.id }) { playlist ->
                        Text(
                            text = "${playlist.name} (${playlist.songCount})",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaylistSelected(playlist.id) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    totalPages: Int,
    fontSize: Int,
    transposeSteps: Int,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onTransposeUp: () -> Unit,
    onTransposeDown: () -> Unit,
    onResetTranspose: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font size
            IconButton(onClick = onDecreaseFontSize) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease font")
            }
            Text(text = "${fontSize}sp", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onIncreaseFontSize) {
                Icon(Icons.Filled.Add, contentDescription = "Increase font")
            }

            // Transpose
            IconButton(onClick = onTransposeDown) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Transpose down")
            }
            val transposeLabel = when {
                transposeSteps > 0 -> "+$transposeSteps"
                else -> "$transposeSteps"
            }
            Text(
                text = "T:$transposeLabel",
                style = MaterialTheme.typography.bodyMedium,
                modifier = androidx.compose.ui.Modifier.clickable(onClick = onResetTranspose)
            )
            IconButton(onClick = onTransposeUp) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Transpose up")
            }

            // Page + fullscreen
            Text(
                text = "${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onToggleFullscreen) {
                Icon(Icons.Filled.Fullscreen, contentDescription = "Toggle fullscreen")
            }
        }
    }
}
