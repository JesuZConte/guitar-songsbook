# Día 24 — Múltiples versiones por canción

**Estado: COMPLETADO**
**Fecha:** 2026-04-25

---

## Objetivos cumplidos

### 1. Modelo de datos y migración DB (v3 → v4)
- Nueva tabla `song_versions` con FK a `songs` (ON DELETE CASCADE)
- `SongVersionEntity` / `SongVersionDao` (CRUD + `insertAll` para seed)
- `SongVersion` domain class en `Song.kt`; `Song.versions: List<SongVersion>` populada en cada fetch
- Migración `MIGRATION_3_4`: crea la tabla y siembra una versión "Default" por cada canción existente copiando sus campos directamente desde `songs` (funciona porque chords/content ya son JSON strings en Room)
- `SongRepository` e `AssetSongRepository` extendidos con `getVersionsForSong`, `getVersionById`, `insertVersion`, `updateVersion`, `deleteVersion`

### 2. Reader: selección de versión
- `ReaderUiState.selectedVersionIndex` + `selectVersion(index)` en `ReaderViewModel`
- Patrón `effectiveSong`: copia de `Song` con los campos de la versión activa (key, capo, chords, notes, content) sustituidos antes de pasarla a `VirtualPagedSong` — sin cambiar la API del componente de renderizado
- `VersionSelectorRow` visible cuando `song.versions.isNotEmpty()` y no está en fullscreen

### 3. VersionSelectorRow con gestión completa
- Chips de versión usando `VersionChip` personalizado (`Box` + `combinedClickable`)
  — **lección clave**: `FilterChip` con `combinedClickable` externo NO funciona porque su `toggleable` interno consume los eventos tap (dispatch bottom-up en Compose); hay que usar un composable propio
- Long-press en un chip abre `DropdownMenu` con opciones Editar / Eliminar
- Eliminar deshabilitado cuando solo hay una versión
- Confirmación de borrado con `AlertDialog`
- Botón "+" al final del row para agregar versión nueva
- `deleteVersion()` en `ReaderViewModel` actualiza el estado localmente sin round-trip a la DB

### 4. VersionEditorScreen y VersionEditorViewModel
- Ruta `ADD_VERSION = "add_version/{songId}/{sourceVersionId}"` — clona el contenido de la versión activa
- Ruta `EDIT_VERSION = "edit_version/{versionId}"` — carga la versión por ID
- `VersionEditorViewModel` con modo add/edit, dos llamadas DB en paralelo (`async/await`) en modo add
- `VersionEditorScreen` reutiliza `KeyDropdown`, `InputModeToggle`, `BuilderContent` (hechos `internal` en `AddSongScreen.kt`)
- Campos: nombre de versión, tonalidad, cejilla, contenido (Builder / Texto)
- Navegación completa cableada en `MainActivity`

### 5. Strings
- 5 nuevas cadenas en EN (`edit_version_title`, `version_name_label`, `version_delete_title`, `version_delete_body`, `version_menu_edit`, `version_menu_delete`)
- Ídem en ES

---

## Archivos clave modificados / creados

| Archivo | Cambio |
|---|---|
| `domain/model/Song.kt` | + `SongVersion` data class, `Song.versions` |
| `data/local/SongVersionEntity.kt` | nuevo |
| `data/local/SongVersionDao.kt` | nuevo (+ `insertAll`) |
| `data/local/SongDatabase.kt` | v4, `MIGRATION_3_4`, `songVersionDao()` |
| `data/repository/SongRepository.kt` | + métodos de versión |
| `data/repository/AssetSongRepository.kt` | implementación completa de versiones |
| `presentation/viewmodel/ReaderViewModel.kt` | `selectVersion`, `deleteVersion` (local) |
| `presentation/viewmodel/VersionEditorViewModel.kt` | nuevo |
| `presentation/screens/VersionEditorScreen.kt` | nuevo |
| `presentation/screens/SongReaderScreen.kt` | `VersionSelectorRow` + `VersionChip` |
| `presentation/screens/AddSongScreen.kt` | varios symbols `private` → `internal` |
| `presentation/Routes.kt` | `ADD_VERSION`, `EDIT_VERSION` |
| `MainActivity.kt` | navegación para las dos nuevas rutas |
| `res/values/strings.xml` | 6 nuevas cadenas EN |
| `res/values-es/strings.xml` | 6 nuevas cadenas ES |

---

## Lección técnica del día

**FilterChip + combinedClickable no funciona.**

En Compose los eventos de puntero se despachan bottom-up (el nodo más profundo primero). El `toggleable` interno de `FilterChip` consume el tap antes de que el `combinedClickable` externo lo vea. Solución: `VersionChip` propio con `Box` + `combinedClickable` como único gesture handler — visualmente igual, sin conflictos.

---

## Próximo: Día 25

### Opción A — Collections / Library redesign (v1.1, ADR-019)
El cambio de UX más impactante pendiente. El Home actual es una lista plana; la visión es mostrar "Collections" (grupos de canciones) al estilo de una biblioteca. Implica:
- Renombrar "Playlists" → "Collections" en UI y navegación
- Home muestra Collections en lugar de lista directa
- Colección por defecto "All Songs" que agrupa toda la biblioteca
- FAB o acción "+" en el detalle de colección para agregar canciones directamente
- Requiere pensar el flujo de creación de colección y cómo convive con la búsqueda actual

### Opción B — Búsqueda por acorde
Filtro adicional en Home: buscar canciones que usan un acorde específico (ej. "Bm"). Útil para practicar un acorde concreto. Trabajo acotado: nueva query en `SongDao` + chip de acorde en la barra de filtros.

### Opción C — APK firmado + preparación de release v1.1
El app tiene suficientes features para una actualización sólida al Play Store. Día dedicado a:
- Generar keystore y APK/AAB firmado
- Verificar `versionCode` y `versionName`
- Prueba de migración DB en dispositivo real (v3 → v4)
- Actualizar descripción en Play Store con las nuevas features

**Recomendación:** Opción A si quieres avanzar en producto, Opción C si quieres publicar pronto.
