# Día 27 — Testing layer: unit tests + DB migration tests

**Estado: COMPLETADO**
**Fecha:** 2026-05-02

---

## Objetivos cumplidos

### 1. Inventario de cobertura existente
Pre-existing tests (all passing, kept unchanged):
- `ChordNotationTest` — convert + roundtrip
- `TransposeTest` — full transpose logic
- `BracketParserTest` — parseLine, parseSectionHeader, full parse
- `ChordLineTest` — buildChordLine overlap/collision
- `SearchFilterTest` — search + genre + difficulty filters
- `FavoritesTest` — toggle, filter, update
- `PlaylistTest` — entity mapping, CRUD
- `SongJsonParsingTest` — Gson deserialization
- `SongEntityMappingTest` — fromDomain / toDomain roundtrip

### 2. Nuevos tests — Capa unitaria

**`SongExporterTest`** (10 tests)
- Output starts with title — artist
- Key line included / omitted when blank
- Capo appended to key line when > 0; omitted when 0
- Chord list in header; omitted when empty
- Section headers present (Verse, Chorus, etc.)
- Chord names appear per line
- **Lyrics text never appears in output** (copyright safety)
- Lines with no chords produce no output

**`ReaderViewModelStateTest`** (20 tests)
- fontSize: increaseFontSize +2, caps at MAX(24), decreaseFontSize -2, floors at MIN(10)
- setFontSize: clamps low, clamps high, accepts valid value
- transpose: transposeUp +1, transposeDown -1, accumulation, resetTranspose
- version: selectVersion updates index, resets currentPage to 0
- page tracking: onMeasuredPageCount, onPageChanged
- toggles: toggleFullscreen (flip+flip), toggleNocturno (flip+flip)

Implementation note: uses `StandardTestDispatcher` — the `viewModelScope.launch {}` coroutine
in `init` is queued but never executed, so Firebase is never touched in unit tests.

### 3. Nuevos tests — Capa de migración de DB (instrumentados)

**`MigrationTest`** (8 tests, requiere dispositivo/emulador)
- `migrate1To2_schemaIsValid` — schema validation
- `migrate1To2_existingRowHasIsFavoriteDefaultZero` — data integrity
- `migrate2To3_schemaIsValid`
- `migrate2To3_playlistsTableExists`
- `migrate2To3_playlistSongsTableExists`
- `migrate3To4_schemaIsValid`
- `migrate3To4_seedsDefaultVersionForExistingSong` — INSERT seed check
- `migrate3To4_versionCountMatchesSongCount` — cardinality check
- `migrateFullChain_1To4_schemaIsValid` — full migration chain

### 4. Cambios de infraestructura

| Archivo | Cambio |
|---|---|
| `SongDatabase.kt` | migrations `private` → `internal`, added `ALL_MIGRATIONS` array |
| `gradle/libs.versions.toml` | added `androidx-room-testing` |
| `app/build.gradle.kts` | added `room-testing` dep, androidTest schema assets source set, `testOptions.unitTests.isReturnDefaultValues = true` |

---

## Total: 132 unit tests passing

## Pendiente

- Migration tests (instrumented) — run with `./gradlew connectedAndroidTest` on device/emulator
- Day 28: Compose smoke tests (reader renders, toolbar responds)
