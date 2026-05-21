# Cancionero – Chord Songbook

Android app for guitarists to manage their personal repertoire — browse songs, read chord charts with lyrics, transpose on the fly, and organise into playlists. Fully offline.

## Features

- **Song library** — searchable and filterable by genre, difficulty, and key
- **Chord reader** — chords positioned over lyrics, paginated with horizontal swipe (HorizontalPager)
- **Transposition** — semitone up/down with reset, applied live in the reader
- **Song versions** — store alternate arrangements (capo, key change, simplified chords) per song
- **Collections** — Traditional Songs library + personal Playlists with add/remove and undo-delete
- **Favorites** — heart toggle with dedicated tab
- **Export/Share** — chord chart as plain text (no lyrics, copyright-safe)
- **Nocturno mode** — low-brightness reading in dark environments
- **Theme selector** — multiple color themes with live preview
- **Offline-first** — all data stored locally with Room; no network required

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Database | Room 2.8.4 (SQLite) with versioned migrations |
| Navigation | Jetpack Navigation Compose |
| Serialization | Gson |
| Async | Kotlin Coroutines + StateFlow |
| Analytics / Crash | Firebase Analytics + Crashlytics |
| Build | KSP, Gradle version catalog |
| Testing | JUnit 4, Mockito, Coroutines Test, Room instrumented tests |

## Architecture

```
domain/model/        → Pure Kotlin data classes (Song, SongSection, SongLine, ChordPosition, SongVersion)
data/
  local/             → Room database, entities, DAOs, type converters, migrations
  repository/        → SongRepository interface + RoomSongRepository implementation
presentation/
  viewmodel/         → HomeViewModel, FavoritesViewModel, ReaderViewModel, PlaylistsViewModel
  screens/           → Compose screens + shared components
ui/theme/            → Material 3 theme + ThemeManager
utils/               → ChordFormatter, ChordNotation, SongExporter
```

Data flow: `assets/songs.json` seeds the Room database on first launch. All subsequent reads/writes go through the Repository → ViewModel StateFlow → Compose UI.

Dependency wiring is manual (no Hilt/Dagger). `MainActivity.onCreate()` constructs `SongDatabase` → `RoomSongRepository` → ViewModels via `ViewModelProvider.Factory`.

## Building

1. Open in Android Studio (Meerkat or later)
2. Sync Gradle
3. Run on emulator or device (API 28+)

```bash
./gradlew build
./gradlew installDebug
```

## Running Tests

```bash
# Unit tests (JVM)
./gradlew test

# Single test class
./gradlew test --tests "com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModelStateTest"

# Instrumented tests — requires connected device or emulator
./gradlew connectedAndroidTest
```

**Test coverage:**
- `ReaderViewModelStateTest` — font size bounds, transposition, version selection, page tracking, fullscreen/nocturno toggles
- `SongExporterTest` — chord share text format, key/capo lines, section headers, no-lyrics guarantee
- `MigrationTest` — full migration chain v1→v4, per-step assertions, data integrity
- `ReaderToolbarTest` — toolbar renders font size / transpose / page values; size and transpose buttons fire correct callbacks independently
- `SongContentTest` — song header renders title, artist, key/capo labels (locale-aware), notes; absent fields do not appear

## Database Migrations

| Version | Change |
|---|---|
| v1 → v2 | Added `is_favorite` column to `songs` |
| v2 → v3 | Created `playlists` and `playlist_songs` tables |
| v3 → v4 | Created `song_versions` table; seeded a "Default" version for every existing song |

## Status

See [docs/STATUS.md](docs/STATUS.md) for the full feature registry — what is shipped, what is remaining, tech debt, and v2 plans with ADR links. Check it at the start of every session.

## Contributing

This project is not currently accepting external contributions. The codebase is public to comply with the GPL v3 license. If you find a bug or have a suggestion, feel free to open an issue.

## License

GPL v3 — see [LICENSE](LICENSE) for details. Derivatives must also be open-sourced under GPL. The "Cancionero" branding is reserved.
