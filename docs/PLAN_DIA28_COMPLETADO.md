# Día 28 — Compose UI smoke tests + Architecture review

**Estado: COMPLETADO**
**Fecha:** 2026-05-02

---

## Objetivos cumplidos

### 1. Nuevos tests — Compose UI (instrumentados)

**`ReaderToolbarTest`** (11 tests, requiere dispositivo/emulador)
- Rendering: font size value (`14sp`), transpose zero (`±0`), positive transpose (`+3`), negative transpose (`-2`), page indicator (`2 / 5`), all 4 testTagged buttons present
- Interactions: size `+` fires `onSizeDelta(+1)`, size `−` fires `onSizeDelta(-1)`, transpose `+` fires `onTransposeDelta(+1)`, transpose `−` fires `onTransposeDelta(-1)`
- Independence: size and transpose callbacks are fully isolated

**`SongContentTest`** (8 tests, requiere dispositivo/emulador)
- Title and artist render correctly
- Key label renders locale-aware (`context.getString(R.string.reader_key_label, "Am")`)
- Capo label renders when capo > 0; absent when capo = 0
- Key label absent when key is blank
- Notes render when present; absent node when blank
- `@Before`/`@After` resets `UserPreferences` notation to AMERICAN for consistent assertions across devices/locales

### 2. Infraestructura de tests

| Archivo | Cambio |
|---|---|
| `ReaderToolbar.kt` | Added `testTag` to all 4 `IconButton`s (`toolbar_size_minus`, `toolbar_size_plus`, `toolbar_transpose_minus`, `toolbar_transpose_plus`) |
| `CLAUDE.md` | Documented correct syntax for running a single instrumented test class (`-Pandroid.testInstrumentationRunnerArguments.class=`) |

### 3. Fixes en tests de migración (Day 27 pendiente)

`MigrationTest` — 3 tests `migrate3To4_*` estaban fallando porque la semilla manual de v3 no incluía el índice `index_playlist_songs_song_id` que sí crea `MIGRATION_2_3`. Añadido `V3_CREATE_INDEX_PLAYLIST_SONGS` a los 3 seeds afectados.

### 4. README actualizado

Reescrito para reflejar el estado real de v1.x:
- Feature list completo
- Tech stack table
- Architecture diagram
- Test coverage section (5 clases de test documentadas)
- Migrations table v1→v4
- Roadmap actualizado con AdMob, chord auto-detection, setlist mode

### 5. Architecture review (Software Architect validation)

Revisión completa de todas las capas. Ver sección de deuda técnica abajo.

---

## Total de tests

- Unit tests (JVM): **132 passing**
- Instrumented tests: **27 passing** (8 MigrationTest + 11 ReaderToolbarTest + 8 SongContentTest)

---

## Deuda técnica identificada

### P1 — Antes de Play Store

| # | Archivo | Hallazgo | Esfuerzo |
|---|---------|----------|----------|
| 1 | `Converters.kt:20,31` | `gson.fromJson` sin try-catch — una fila corrompida en la BD crashea la app con `JsonSyntaxException` | 15 min |
| 2 | `ReaderViewModel.kt` | `increaseFontSize`/`decreaseFontSize` actualizan estado en memoria pero nunca llaman a `UserPreferences.setFontSize` — el tamaño de fuente se pierde al reiniciar | 20 min |

### P2 — v1.1

| # | Archivo | Hallazgo | Esfuerzo |
|---|---------|----------|----------|
| 3 | `AddSongViewModel.kt:165` | `var pendingPreview: Song? = null` en `companion object` — estado mutable estático para handoff de navegación; no es thread-safe, sin gestión de lifecycle. Mejor: `SavedStateHandle` o nav args | 30 min |
| 4 | `PlaylistsViewModel.kt` | `removeSongFromPlaylist` no tiene try-catch, a diferencia de `deletePlaylist` que sí lo tiene — fallo de DAO silencioso | 5 min |
| 5 | `RoomSongRepository.kt` | `ensureSeeded()` al inicio de cada método público — ejecuta `SELECT COUNT(*)` en cada llamada; mover a `init` o `Application.onCreate` | 20 min |

---

## Próximo día (Day 29)

- AdMob banner integration
