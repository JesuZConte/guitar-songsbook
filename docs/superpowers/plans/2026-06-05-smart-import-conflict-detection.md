# Smart Import: Conflict Detection & Version Merge — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When a user imports a JSON song or manually adds a song whose title matches an existing one, show a dialog offering to merge it as a new version or save it as a separate song.

**Architecture:** Conflict detection lives in each ViewModel (`HomeViewModel` for JSON import, `AddSongViewModel` for manual add). Both call a new `SongRepository.findSongsByTitle()` method that normalizes titles (case-insensitive, trim, accent-stripped) in Kotlin. A shared `ImportConflict` domain model carries the state to the UI, where a single reusable `ImportConflictDialog` composable handles both entry points.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Coroutines, `java.text.Normalizer` (standard JDK — no new dependency).

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Create | `utils/TitleNormalizer.kt` | `normalizeTitle()` pure function |
| Create | `domain/model/ImportConflict.kt` | `ImportConflict` data class + `suggestVersionName()` |
| Modify | `data/repository/SongRepository.kt` | Add `findSongsByTitle()` to interface |
| Modify | `data/repository/AssetSongRepository.kt` | Implement `findSongsByTitle()` |
| Modify | `presentation/viewmodel/HomeViewModel.kt` | `HomeUiState` new field, updated import flow, resolve functions |
| Modify | `presentation/viewmodel/AddSongViewModel.kt` | `AddSongUiState` new field, updated save flow, resolve functions |
| Create | `presentation/screens/ImportConflictDialog.kt` | Reusable conflict dialog composable |
| Modify | `presentation/screens/HomeScreen.kt` | Observe conflict state, show dialog |
| Modify | `presentation/screens/AddSongScreen.kt` | Observe conflict state, show dialog |
| Create | `test/.../utils/TitleNormalizerTest.kt` | Unit tests for `normalizeTitle()` |
| Create | `test/.../domain/ImportConflictTest.kt` | Unit tests for `suggestVersionName()` |

**Note:** `SongDao` already has `getAll(): List<SongEntity>` — no new DAO query is needed.

---

## Task 1: TitleNormalizer utility

**Files:**
- Create: `app/src/main/java/com/guitarapp/songsbook/utils/TitleNormalizer.kt`
- Create: `app/src/test/java/com/guitarapp/songsbook/utils/TitleNormalizerTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// app/src/test/java/com/guitarapp/songsbook/utils/TitleNormalizerTest.kt
package com.guitarapp.songsbook.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TitleNormalizerTest {

    @Test fun `lowercase is normalized`() {
        assertEquals(normalizeTitle("CANCION"), normalizeTitle("cancion"))
    }

    @Test fun `leading and trailing spaces are stripped`() {
        assertEquals(normalizeTitle("  Hola  "), normalizeTitle("hola"))
    }

    @Test fun `acute accents are stripped`() {
        assertEquals(normalizeTitle("Corazón"), normalizeTitle("Corazon"))
        assertEquals(normalizeTitle("Séptimo"), normalizeTitle("Septimo"))
        assertEquals(normalizeTitle("Última"), normalizeTitle("Ultima"))
        assertEquals(normalizeTitle("Búscame"), normalizeTitle("Buscame"))
    }

    @Test fun `combined trim accent and case`() {
        assertEquals(normalizeTitle("  La Canción  "), normalizeTitle("la cancion"))
    }

    @Test fun `different titles do not match`() {
        assertNotEquals(normalizeTitle("La Bamba"), normalizeTitle("La Rumba"))
    }

    @Test fun `empty string returns empty`() {
        assertEquals("", normalizeTitle(""))
    }

    @Test fun `whitespace-only string returns empty`() {
        assertEquals("", normalizeTitle("   "))
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew test --tests "com.guitarapp.songsbook.utils.TitleNormalizerTest"
```

Expected: FAIL — `normalizeTitle` not yet defined.

- [ ] **Step 3: Implement TitleNormalizer**

```kotlin
// app/src/main/java/com/guitarapp/songsbook/utils/TitleNormalizer.kt
package com.guitarapp.songsbook.utils

import java.text.Normalizer

fun normalizeTitle(title: String): String =
    Normalizer.normalize(title.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
./gradlew test --tests "com.guitarapp.songsbook.utils.TitleNormalizerTest"
```

Expected: All 7 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/utils/TitleNormalizer.kt \
        app/src/test/java/com/guitarapp/songsbook/utils/TitleNormalizerTest.kt
git commit -m "feat: add normalizeTitle utility for accent-insensitive title matching"
```

---

## Task 2: ImportConflict domain model and suggestVersionName

**Files:**
- Create: `app/src/main/java/com/guitarapp/songsbook/domain/model/ImportConflict.kt`
- Create: `app/src/test/java/com/guitarapp/songsbook/domain/ImportConflictTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// app/src/test/java/com/guitarapp/songsbook/domain/ImportConflictTest.kt
package com.guitarapp.songsbook.domain

import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongVersion
import com.guitarapp.songsbook.domain.model.suggestVersionName
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportConflictTest {

    private fun song(key: String, capo: Int, versions: List<SongVersion> = emptyList()) = Song(
        id = "existing-id", title = "Test Song", artist = "Artist",
        genre = "Rock", difficulty = "beginner",
        key = key, capo = capo,
        chords = emptyList(), tags = emptyList(), notes = "", content = emptyList(),
        versions = versions
    )

    private fun incoming(key: String, capo: Int) = Song(
        id = "new-id", title = "Test Song", artist = "Artist",
        genre = "Rock", difficulty = "beginner",
        key = key, capo = capo,
        chords = emptyList(), tags = emptyList(), notes = "", content = emptyList()
    )

    private fun version(name: String, key: String, capo: Int) =
        SongVersion(id = 1L, songId = "existing-id", name = name, key = key, capo = capo)

    @Test fun `different key with no capo uses key name`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("A", suggestVersionName(existing, incoming("A", 0)))
    }

    @Test fun `different capo uses key slash capo name`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("G / Capo 2", suggestVersionName(existing, incoming("G", 2)))
    }

    @Test fun `different key and capo uses key slash capo name`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("A / Capo 1", suggestVersionName(existing, incoming("A", 1)))
    }

    @Test fun `same key and capo with one version generates Version 2`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("Versión 2", suggestVersionName(existing, incoming("G", 0)))
    }

    @Test fun `same key and capo with two versions generates Version 3`() {
        val existing = song("G", 0, listOf(
            version("Default", "G", 0),
            version("Versión 2", "G", 0)
        ))
        assertEquals("Versión 3", suggestVersionName(existing, incoming("G", 0)))
    }

    @Test fun `no versions falls back to Version 2`() {
        val existing = song("G", 0, emptyList())
        assertEquals("Versión 2", suggestVersionName(existing, incoming("G", 0)))
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew test --tests "com.guitarapp.songsbook.domain.ImportConflictTest"
```

Expected: FAIL — `ImportConflict` and `suggestVersionName` not yet defined.

- [ ] **Step 3: Implement ImportConflict and suggestVersionName**

```kotlin
// app/src/main/java/com/guitarapp/songsbook/domain/model/ImportConflict.kt
package com.guitarapp.songsbook.domain.model

data class ImportConflict(
    val existing: Song,
    val incoming: Song,
    val suggestedVersionName: String
)

fun suggestVersionName(existing: Song, incoming: Song): String {
    val defaultVersion = existing.versions.firstOrNull { it.name == "Default" }
    val existingKey = defaultVersion?.key ?: existing.key
    val existingCapo = defaultVersion?.capo ?: existing.capo
    return if (incoming.key != existingKey || incoming.capo != existingCapo) {
        if (incoming.capo == 0) incoming.key else "${incoming.key} / Capo ${incoming.capo}"
    } else {
        "Versión ${existing.versions.size + 1}"
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
./gradlew test --tests "com.guitarapp.songsbook.domain.ImportConflictTest"
```

Expected: All 6 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/domain/model/ImportConflict.kt \
        app/src/test/java/com/guitarapp/songsbook/domain/ImportConflictTest.kt
git commit -m "feat: add ImportConflict model and suggestVersionName logic"
```

---

## Task 3: Repository — findSongsByTitle

**Files:**
- Modify: `app/src/main/java/com/guitarapp/songsbook/data/repository/SongRepository.kt`
- Modify: `app/src/main/java/com/guitarapp/songsbook/data/repository/AssetSongRepository.kt`

- [ ] **Step 1: Add method to SongRepository interface**

Open `app/src/main/java/com/guitarapp/songsbook/data/repository/SongRepository.kt` and add after the existing `getSongById` line:

```kotlin
suspend fun findSongsByTitle(title: String): List<Song>
```

- [ ] **Step 2: Implement in AssetSongRepository**

Open `app/src/main/java/com/guitarapp/songsbook/data/repository/AssetSongRepository.kt`.

Add the import at the top of the file:
```kotlin
import com.guitarapp.songsbook.utils.normalizeTitle
```

Add the following method. Place it after the `getSongById` implementation:

```kotlin
override suspend fun findSongsByTitle(title: String): List<Song> {
    val normalized = normalizeTitle(title)
    return songDao.getAll()
        .filter { normalizeTitle(it.title) == normalized }
        .map { entity ->
            val versions = songVersionDao.getVersionsForSong(entity.id).map { it.toDomain() }
            entity.toDomain().copy(versions = versions)
        }
}
```

- [ ] **Step 3: Build to confirm no compilation errors**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/data/repository/SongRepository.kt \
        app/src/main/java/com/guitarapp/songsbook/data/repository/AssetSongRepository.kt
git commit -m "feat: add findSongsByTitle to repository with accent-insensitive matching"
```

---

## Task 4: HomeViewModel — conflict detection in import flow

**Files:**
- Modify: `app/src/main/java/com/guitarapp/songsbook/presentation/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: Add imports to HomeViewModel**

Add these imports to `HomeViewModel.kt`:

```kotlin
import com.guitarapp.songsbook.domain.model.ImportConflict
import com.guitarapp.songsbook.domain.model.SongVersion
import com.guitarapp.songsbook.domain.model.suggestVersionName
```

- [ ] **Step 2: Add pendingImportConflict to HomeUiState**

Replace the existing `HomeUiState` data class:

```kotlin
data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val genres: List<String> = emptyList(),
    val difficulties: List<String> = emptyList(),
    val query: String = "",
    val selectedGenre: String? = null,
    val selectedDifficulty: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val importedSongTitle: String? = null,
    val pendingImportConflict: ImportConflict? = null
)
```

- [ ] **Step 3: Replace importSongFromJson with conflict-aware version**

Replace the entire `importSongFromJson` function:

```kotlin
fun importSongFromJson(json: String) {
    viewModelScope.launch {
        try {
            val song = Gson().fromJson(json, Song::class.java)
            val incoming = song.copy(
                id = UUID.randomUUID().toString(),
                isFavorite = false,
                versions = song.versions ?: emptyList()
            )
            val matches = try { songRepository.findSongsByTitle(incoming.title) } catch (_: Exception) { emptyList() }
            if (matches.isNotEmpty()) {
                val existing = matches.first()
                _uiState.update {
                    it.copy(
                        pendingImportConflict = ImportConflict(
                            existing = existing,
                            incoming = incoming,
                            suggestedVersionName = suggestVersionName(existing, incoming)
                        )
                    )
                }
            } else {
                songRepository.insertSong(incoming)
                refreshSongs()
                _uiState.update { it.copy(importedSongTitle = incoming.title) }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uiState.update { it.copy(error = "Could not import song") }
        }
    }
}
```

- [ ] **Step 4: Add three resolve functions**

Add these functions after `importSongFromJson`:

```kotlin
fun resolveImportAsVersion(conflict: ImportConflict, versionName: String) {
    viewModelScope.launch {
        try {
            songRepository.insertVersion(
                SongVersion(
                    id = 0,
                    songId = conflict.existing.id,
                    name = versionName,
                    key = conflict.incoming.key,
                    capo = conflict.incoming.capo,
                    chords = conflict.incoming.chords,
                    notes = conflict.incoming.notes,
                    content = conflict.incoming.content
                )
            )
            refreshSongs()
            _uiState.update { it.copy(pendingImportConflict = null, importedSongTitle = conflict.existing.title) }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uiState.update { it.copy(pendingImportConflict = null, error = "Could not add version") }
        }
    }
}

fun resolveImportAsSeparate(conflict: ImportConflict) {
    viewModelScope.launch {
        try {
            songRepository.insertSong(conflict.incoming)
            refreshSongs()
            _uiState.update { it.copy(pendingImportConflict = null, importedSongTitle = conflict.incoming.title) }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uiState.update { it.copy(pendingImportConflict = null, error = "Could not import song") }
        }
    }
}

fun cancelImport() {
    _uiState.update { it.copy(pendingImportConflict = null) }
}
```

- [ ] **Step 5: Build to confirm no errors**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/presentation/viewmodel/HomeViewModel.kt
git commit -m "feat: conflict detection in HomeViewModel import flow"
```

---

## Task 5: AddSongViewModel — conflict detection in save flow

**Files:**
- Modify: `app/src/main/java/com/guitarapp/songsbook/presentation/viewmodel/AddSongViewModel.kt`

- [ ] **Step 1: Add imports to AddSongViewModel**

Add these imports:

```kotlin
import com.guitarapp.songsbook.domain.model.ImportConflict
import com.guitarapp.songsbook.domain.model.SongVersion
import com.guitarapp.songsbook.domain.model.suggestVersionName
```

- [ ] **Step 2: Add pendingConflict to AddSongUiState**

Replace the existing `AddSongUiState` data class:

```kotlin
data class AddSongUiState(
    val title: String = "",
    val artist: String = "",
    val key: String = "",
    val capo: String = "0",
    val genre: String = "",
    val difficulty: String = "beginner",
    val rawText: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val formatNotDetected: Boolean = false,
    val pendingConflict: ImportConflict? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && artist.isNotBlank() && rawText.isNotBlank()
}
```

- [ ] **Step 3: Replace saveSong with conflict-aware version**

Replace the entire `saveSong` function:

```kotlin
fun saveSong() {
    val song = buildPreviewSong() ?: return
    val songWithId = if (editSongId != null) {
        song.copy(id = editSongId)
    } else {
        song.copy(id = UUID.randomUUID().toString())
    }

    _uiState.value = _uiState.value.copy(isSaving = true, error = null)

    viewModelScope.launch {
        try {
            FirebaseCrashlytics.getInstance().log("AddSongViewModel: saving song, editMode=$isEditMode")
            if (editSongId != null) {
                songRepository.updateSong(songWithId)
                AnalyticsHelper.logSongEdited()
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } else {
                val matches = try { songRepository.findSongsByTitle(songWithId.title) } catch (_: Exception) { emptyList() }
                if (matches.isNotEmpty()) {
                    val existing = matches.first()
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        pendingConflict = ImportConflict(
                            existing = existing,
                            incoming = songWithId,
                            suggestedVersionName = suggestVersionName(existing, songWithId)
                        )
                    )
                } else {
                    songRepository.insertSong(songWithId)
                    AnalyticsHelper.logSongAdded()
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                error = e.message ?: "Failed to save song"
            )
        }
    }
}
```

- [ ] **Step 4: Add three resolve functions**

Add these functions after `saveSong`:

```kotlin
fun resolveConflictAsVersion(conflict: ImportConflict, versionName: String) {
    viewModelScope.launch {
        try {
            songRepository.insertVersion(
                SongVersion(
                    id = 0,
                    songId = conflict.existing.id,
                    name = versionName,
                    key = conflict.incoming.key,
                    capo = conflict.incoming.capo,
                    chords = conflict.incoming.chords,
                    notes = conflict.incoming.notes,
                    content = conflict.incoming.content
                )
            )
            _uiState.value = _uiState.value.copy(pendingConflict = null, saveSuccess = true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uiState.value = _uiState.value.copy(
                pendingConflict = null,
                error = e.message ?: "Failed to save version"
            )
        }
    }
}

fun resolveConflictAsSeparate(conflict: ImportConflict) {
    viewModelScope.launch {
        try {
            songRepository.insertSong(conflict.incoming)
            AnalyticsHelper.logSongAdded()
            _uiState.value = _uiState.value.copy(pendingConflict = null, saveSuccess = true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uiState.value = _uiState.value.copy(
                pendingConflict = null,
                error = e.message ?: "Failed to save song"
            )
        }
    }
}

fun cancelConflict() {
    _uiState.value = _uiState.value.copy(pendingConflict = null, isSaving = false)
}
```

- [ ] **Step 5: Build to confirm no errors**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/presentation/viewmodel/AddSongViewModel.kt
git commit -m "feat: conflict detection in AddSongViewModel save flow"
```

---

## Task 6: ImportConflictDialog composable

**Files:**
- Create: `app/src/main/java/com/guitarapp/songsbook/presentation/screens/ImportConflictDialog.kt`

- [ ] **Step 1: Create the composable**

```kotlin
// app/src/main/java/com/guitarapp/songsbook/presentation/screens/ImportConflictDialog.kt
package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.domain.model.ImportConflict

@Composable
fun ImportConflictDialog(
    conflict: ImportConflict,
    onAddAsVersion: (versionName: String) -> Unit,
    onSaveAsSeparate: () -> Unit,
    onCancel: () -> Unit
) {
    var versionName by remember(conflict) { mutableStateOf(conflict.suggestedVersionName) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Ya tienes \"${conflict.existing.title}\"")
        },
        text = {
            Column {
                Text(text = "¿Qué quieres hacer?")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nombre de la versión:",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = versionName,
                    onValueChange = { versionName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAddAsVersion(versionName.trim()) },
                enabled = versionName.isNotBlank()
            ) {
                Text("Agregar como versión nueva")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onSaveAsSeparate) {
                    Text("Guardar como canción separada")
                }
                TextButton(onClick = onCancel) {
                    Text("Cancelar")
                }
            }
        }
    )
}
```

- [ ] **Step 2: Build to confirm no compilation errors**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/presentation/screens/ImportConflictDialog.kt
git commit -m "feat: ImportConflictDialog composable"
```

---

## Task 7: Wire dialog into HomeScreen

**Files:**
- Modify: `app/src/main/java/com/guitarapp/songsbook/presentation/screens/HomeScreen.kt`

The `HomeScreen` composable function currently ends with `if (showHelp) { AddSongsHelpDialog(...) }`. The conflict dialog is placed immediately before it.

- [ ] **Step 1: Add the conflict dialog block**

In `HomeScreen.kt`, add these lines right before the existing `if (showHelp)` block (which is near the end of the composable function, around line 270):

```kotlin
val importConflict = uiState.pendingImportConflict
if (importConflict != null) {
    ImportConflictDialog(
        conflict = importConflict,
        onAddAsVersion = { name -> viewModel.resolveImportAsVersion(importConflict, name) },
        onSaveAsSeparate = { viewModel.resolveImportAsSeparate(importConflict) },
        onCancel = { viewModel.cancelImport() }
    )
}
```

- [ ] **Step 2: Build to confirm no errors**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/presentation/screens/HomeScreen.kt
git commit -m "feat: show conflict dialog on JSON import in HomeScreen"
```

---

## Task 8: Wire dialog into AddSongScreen

**Files:**
- Modify: `app/src/main/java/com/guitarapp/songsbook/presentation/screens/AddSongScreen.kt`

- [ ] **Step 1: Add the conflict dialog block**

In `AddSongScreen.kt`, find the end of the main composable function (look for the `LaunchedEffect(uiState.saveSuccess)` block around line 185). Add the conflict dialog **after** all `LaunchedEffect` blocks and **before** the `Scaffold` or main content:

```kotlin
val songConflict = uiState.pendingConflict
if (songConflict != null) {
    ImportConflictDialog(
        conflict = songConflict,
        onAddAsVersion = { name -> viewModel.resolveConflictAsVersion(songConflict, name) },
        onSaveAsSeparate = { viewModel.resolveConflictAsSeparate(songConflict) },
        onCancel = { viewModel.cancelConflict() }
    )
}
```

- [ ] **Step 2: Build and install on device**

```bash
./gradlew installDebug
```

Expected: BUILD SUCCESSFUL and app installs.

- [ ] **Step 3: Manual smoke test — import conflict**
  1. Import a JSON song (use `docs/songs/la-voz-de-los-80.json`)
  2. Import the same JSON file again
  3. Confirm the conflict dialog appears with "Ya tienes "La Voz De Los 80""
  4. Confirm the version name field is pre-filled (e.g. "Versión 2")
  5. Edit the version name to "Mi versión" and tap "Agregar como versión nueva"
  6. Open the song — confirm two versions exist (Default + Mi versión)

- [ ] **Step 4: Manual smoke test — manual add conflict**
  1. Add a new song manually via the builder with the same title as an existing one
  2. Tap Save
  3. Confirm the conflict dialog appears
  4. Tap "Guardar como canción separada"
  5. Confirm two separate songs appear in the list

- [ ] **Step 5: Manual smoke test — cancel**
  1. Trigger either conflict dialog
  2. Tap Cancelar
  3. Confirm no song or version is added and the dialog closes

- [ ] **Step 6: Run all unit tests**

```bash
./gradlew test
```

Expected: All tests PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/guitarapp/songsbook/presentation/screens/AddSongScreen.kt
git commit -m "feat: show conflict dialog on manual save in AddSongScreen"
```
