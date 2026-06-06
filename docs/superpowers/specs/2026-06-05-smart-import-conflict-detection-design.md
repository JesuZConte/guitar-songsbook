# Smart Import: Conflict Detection & Version Merge

**Date:** 2026-06-05  
**Status:** Approved

## Problem

When a user imports a JSON song or manually creates a song via the builder, and a song with the same title already exists in the database, the app currently creates a duplicate entry. The user has no way to merge the incoming content as a new version of the existing song without manual cleanup.

## Scope

Two entry points, same behavior:

1. **Import JSON** — `HomeViewModel.importSongFromJson()`
2. **Manual add** — `AddSongViewModel.saveSong()` (new song only, not edit mode)

## Title Matching Rules

A conflict is detected when an existing song matches the incoming song's title using a normalized comparison:

- Strip leading/trailing whitespace (`trim()`)
- Lowercase
- **Strip diacritics** — NFD decomposition followed by removal of combining characters, so `"Corazón"` == `"Corazon"`, `"Sé"` == `"Se"`, etc.
- No fuzzy matching — if titles differ after normalization, no conflict is reported

Normalization is done in Kotlin (SQLite does not support accent-insensitive comparison natively).

```kotlin
fun normalizeTitle(title: String): String =
    Normalizer.normalize(title.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
```

## Architecture

### 1. Repository — new query

Add to `SongDao`:

```kotlin
@Query("SELECT * FROM songs")
suspend fun getAllSongsOnce(): List<SongEntity>
```

Add to `SongRepository` interface:

```kotlin
suspend fun findSongsByTitle(title: String): List<Song>
```

Implemented in `AssetSongRepository`: loads all songs via `getAllSongsOnce()`, then filters in Kotlin using `normalizeTitle()` on both sides.

```kotlin
override suspend fun findSongsByTitle(title: String): List<Song> {
    val normalized = normalizeTitle(title)
    return songDao.getAllSongsOnce()
        .map { it.toDomain() }
        .filter { normalizeTitle(it.title) == normalized }
}
```

`normalizeTitle` lives in a shared utility (e.g., `utils/TitleNormalizer.kt`) so both the repository and any future callers use the same logic.

### 2. Shared conflict model

New data class in the domain layer (e.g., `domain/model/ImportConflict.kt`):

```kotlin
data class ImportConflict(val existing: Song, val incoming: Song)
```

### 3. HomeUiState — new field

```kotlin
data class HomeUiState(
    ...
    val pendingImportConflict: ImportConflict? = null
)
```

### 4. AddSongUiState — new field

```kotlin
data class AddSongUiState(
    ...
    val pendingConflict: ImportConflict? = null
)
```

### 5. HomeViewModel — updated import flow

```kotlin
fun importSongFromJson(json: String) {
    viewModelScope.launch {
        try {
            val song = Gson().fromJson(json, Song::class.java)
            val incoming = song.copy(id = UUID.randomUUID().toString(), isFavorite = false, versions = song.versions ?: emptyList())
            val matches = songRepository.findSongsByTitle(incoming.title)
            if (matches.isNotEmpty()) {
                _uiState.update { it.copy(pendingImportConflict = ImportConflict(existing = matches.first(), incoming = incoming)) }
            } else {
                songRepository.insertSong(incoming)
                refreshSongs()
                _uiState.update { it.copy(importedSongTitle = incoming.title) }
            }
        } catch (e: Exception) {
            ...
        }
    }
}
```

New functions to resolve the conflict:

```kotlin
fun resolveImportAsVersion(conflict: ImportConflict)   // inserts version, clears state
fun resolveImportAsSeparate(conflict: ImportConflict)  // inserts song, clears state
fun cancelImport()                                      // clears state only
```

### 6. AddSongViewModel — updated save flow

In `saveSong()`, before `insertSong()`:

```kotlin
val matches = songRepository.findSongsByTitle(songWithId.title)
if (matches.isNotEmpty()) {
    _uiState.value = _uiState.value.copy(pendingConflict = ImportConflict(existing = matches.first(), incoming = songWithId))
    return@launch  // pause, wait for user decision
}
// no conflict — proceed normally
songRepository.insertSong(songWithId)
```

New functions:

```kotlin
fun resolveConflictAsVersion(conflict: ImportConflict)  // inserts version, sets saveSuccess = true
fun resolveConflictAsSeparate(conflict: ImportConflict) // inserts song, sets saveSuccess = true
fun cancelConflict()                                     // clears pendingConflict
```

## Version Name Generation

When adding the incoming song as a version of the existing one:

1. Load the existing song's current Default version (key + capo).
2. Compare with incoming song's key + capo:
   - If different → name = `"${incoming.key}"` if capo == 0, else `"${incoming.key} / Capo ${incoming.capo}"`
   - If same → count existing versions → name = `"Versión ${count + 1}"`
3. Insert a `SongVersionEntity` linked to `existing.id` with the incoming song's key, capo, chords, notes, content.

The incoming song itself is **not** inserted as a standalone song.

## UI — Conflict Dialog

A single reusable composable `ImportConflictDialog` shown in both `HomeScreen` and `AddSongScreen`:

```
┌─────────────────────────────────────────┐
│  Ya tienes "[título]"                   │
│                                         │
│  ¿Qué quieres hacer?                    │
│                                         │
│  [Agregar como versión nueva]           │
│  [Guardar como canción separada]        │
│  [Cancelar]                             │
└─────────────────────────────────────────┘
```

- Triggered when `pendingImportConflict != null` (HomeScreen) or `pendingConflict != null` (AddSongScreen)
- Dismissing the dialog (back gesture or Cancelar) calls the cancel function
- Uses existing `LeatherDialog` / `AlertDialog` pattern from the codebase

## Error Handling

- If `findSongsByTitle()` throws, treat as no conflict and proceed with normal insert (fail-safe).
- If `insertVersion()` throws during conflict resolution, show the standard error snackbar and clear the conflict state.

## Out of Scope

- Fuzzy title matching
- Merging song metadata (genre, tags, difficulty) from the incoming song into the existing one — only the version data (key, capo, chords, notes, content) is merged
- Handling multiple matches (if somehow two songs share a title — uses `matches.first()`)
- Edit mode in AddSongScreen (conflict detection only applies to new songs)