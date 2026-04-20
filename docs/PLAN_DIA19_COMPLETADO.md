# Día 19 — Completado (2026-04-19)

## Contexto

Día de quick wins mientras corre la prueba cerrada alpha (14 testers, 14 días).
Objetivo: mejorar UX basándose en feedback temprano de testers y en observaciones
del propio equipo. Sin cambios de arquitectura ni nuevas pantallas.

---

## Lo que se hizo

### 1. Theme selector manual (Settings)

- Nueva sección "Appearance" en `SettingsScreen` con tres `FilterChip`:
  **System / Light / Dark**
- Persiste en `SharedPreferences` via `UserPreferences.getThemeMode` /
  `setThemeMode`
- El tema cambia instantáneamente sin reiniciar la app (`mutableStateOf` en
  `MainActivity` fuerza recomposición del `GuitarSongsbookTheme`)
- Archivos: `UserPreferences.kt`, `SettingsScreen.kt`, `MainActivity.kt`

### 2. Key notation fix en AddSong

- El dropdown de tonalidades mostraba siempre notación americana aunque el
  usuario tuviera notación latina activa
- Fix: `KeyDropdown` lee `UserPreferences.getNotation(context)` y aplica
  `ChordNotation.convert()` a las etiquetas de display; almacena siempre en
  americano internamente
- Archivo: `AddSongScreen.kt`

### 3. Eliminar / Editar canción desde el Home (long-press)

- Long-press en cualquier `SongCard` abre un `DropdownMenu` con tres opciones:
  - **Edit** → navega al editor de la canción
  - **Add to playlist** → abre diálogo con lista de playlists (o hint si no hay)
  - **Delete** (en rojo, separado por divider) → elimina con snackbar de undo
- El AlertDialog de confirmación fue reemplazado por el patrón snackbar + undo:
  - La canción desaparece inmediatamente de la UI (optimistic remove)
  - Snackbar `"[Título] deleted"` con botón **Undo** (duración corta)
  - Undo: `refreshSongs()` restaura desde DB (canción nunca fue borrada)
  - Sin undo: `deleteSong()` borra de DB
- `HomeViewModel` nuevos métodos: `removeSongFromUi`, `undoDelete`,
  `confirmDelete`
- Archivos: `HomeScreen.kt`, `HomeViewModel.kt`, `MainActivity.kt`

### 4. Add to playlist desde el Home

- Tercer item en el long-press dropdown de `SongCard`
- `AddToPlaylistDialog`: lista de playlists existentes como `TextButton` con
  ícono `QueueMusic`; si no hay playlists, muestra hint hacia la pestaña
  Playlists
- Usa `PlaylistsViewModel.addSongToPlaylist` que ya existía
- `HomeScreen` recibe `playlists: List<Playlist>` y callback `onAddToPlaylist`
  desde `MainActivity` via `playlistsViewModel.uiState`
- Archivos: `HomeScreen.kt`, `MainActivity.kt`

---

## Commits del día

```
feat: theme selector, notation fix in add-song, delete/edit from home
feat: add-to-playlist and undo-delete snackbar from home long-press
```

---

## Estado al cerrar

- Closed testing alpha activa (12 testers, período de 14 días)
- No hay deuda técnica nueva introducida hoy
- Constitution en v1.1.0 (ratificada en día 16/17/18)
- Roadmap v1.x documentado en `docs/ROADMAP_V1X.md`
