package com.guitarapp.songsbook.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
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
import androidx.compose.foundation.layout.statusBarsPadding
import com.guitarapp.songsbook.ui.components.BrassPill
import com.guitarapp.songsbook.ui.components.LeatherHeader
import com.guitarapp.songsbook.ui.components.ReaderToolbar
import com.guitarapp.songsbook.ui.theme.PillShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import com.guitarapp.songsbook.ui.theme.DarkLeather
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import com.guitarapp.songsbook.ui.theme.NocturnoColorScheme

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SongReaderScreen(
    viewModel: ReaderViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onBackClick: () -> Unit,
    onEditClick: (versionId: Long) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    onAddVersionClick: (songId: String, sourceVersionId: Long) -> Unit = { _, _ -> },
    onEditVersionClick: (versionId: Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlistsState by playlistsViewModel.uiState.collectAsState()

    val currentVersion = uiState.song?.versions?.let { versions ->
        versions.getOrNull(uiState.selectedVersionIndex) ?: versions.firstOrNull()
    }
    val effectiveSong = uiState.song?.let { song ->
        if (currentVersion != null) {
            song.copy(
                key = currentVersion.key,
                capo = currentVersion.capo,
                chords = currentVersion.chords,
                notes = currentVersion.notes,
                content = currentVersion.content
            )
        } else song
    }

    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showShareMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val shareChordsTxt = stringResource(R.string.reader_share_chords)
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
                LeatherHeader(
                    title = uiState.song?.title ?: stringResource(R.string.reader_loading),
                    subtitle = uiState.song?.artist,
                    onBack = onBackClick,
                    leadingEmblem = false,
                    modifier = Modifier.statusBarsPadding(),
                    actionRow = if (uiState.song != null) ({
                        Box {
                            IconButton(onClick = { showShareMenu = true }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = LocalLeatherColors.current.cream)
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
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        context.startActivity(Intent.createChooser(intent, shareChordsTxt))
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { currentVersion?.let { onEditClick(it.id) } }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit version", tint = LocalLeatherColors.current.cream)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete song", tint = LocalLeatherColors.current.accent)
                        }
                        IconButton(onClick = {
                            playlistsViewModel.loadPlaylists()
                            showPlaylistPicker = true
                        }) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to playlist", tint = LocalLeatherColors.current.cream)
                        }
                        IconButton(onClick = { viewModel.toggleNocturno() }) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = "Toggle nocturno mode",
                                tint = LocalLeatherColors.current.cream,
                            )
                        }
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (uiState.song!!.isFavorite) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (uiState.song!!.isFavorite) LocalLeatherColors.current.accent
                                       else LocalLeatherColors.current.cream,
                            )
                        }
                    }) else null,
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !uiState.isFullscreen,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                ReaderToolbar(
                    fontSizeSp = uiState.fontSize,
                    transposeSemitones = uiState.transposeSteps,
                    page = uiState.currentPage + 1,
                    totalPages = uiState.totalPages.coerceAtLeast(1),
                    onSizeDelta = { delta ->
                        if (delta > 0) viewModel.increaseFontSize() else viewModel.decreaseFontSize()
                    },
                    onTransposeDelta = { delta ->
                        if (delta > 0) viewModel.transposeUp() else viewModel.transposeDown()
                    }
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
            CompositionLocalProvider(
                LocalNocturnoMode provides true,
                LocalLeatherColors provides DarkLeather,
            ) {
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
    val c = LocalLeatherColors.current
    if (selected) {
        BrassPill(modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
            Text(text = name, color = c.navyDeep, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Box(
            modifier = Modifier
                .clip(PillShape)
                .border(1.5.dp, c.rule, PillShape)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(text = name, color = c.ink, style = MaterialTheme.typography.labelLarge)
        }
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

