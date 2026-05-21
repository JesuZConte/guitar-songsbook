# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
./gradlew build

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.guitarapp.songsbook.presentation.viewmodel.SearchFilterTest"

# Build and install on connected device/emulator
./gradlew installDebug

# Clean build
./gradlew clean

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single instrumented test class (--tests does NOT work for connectedAndroidTest)
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.guitarapp.songsbook.ui.ReaderToolbarTest
```

## Architecture

**MVVM + Clean Architecture** with manual dependency injection (no Hilt/Dagger).

```
domain/model/        → Pure Kotlin data classes (Song, SongSection, SongLine, ChordPosition)
data/
  local/             → Room database (SongDatabase, SongDao, SongEntity, Converters)
  repository/        → SongRepository interface + AssetSongRepository implementation
presentation/
  viewmodel/         → HomeViewModel, FavoritesViewModel, ReaderViewModel
  screens/           → Compose UI (HomeScreen, FavoritesScreen, SongReaderScreen)
ui/theme/            → Material 3 theme
utils/               → ChordFormatter
```

**Data flow:** JSON seed file (`assets/songs.json`) → Room database (on first launch) → Repository → ViewModel StateFlow → Compose UI.

**Dependency wiring:** `MainActivity.onCreate()` manually creates `SongDatabase` → `AssetSongRepository` → ViewModels via custom `ViewModelProvider.Factory`. No DI framework.

**Navigation:** Single Activity with Jetpack Navigation Compose. Routes: `"home"`, `"favorites"`, `"reader/{songId}"`. Bottom navigation bar with state preservation between tabs.

**State management:** ViewModels expose `StateFlow<UiState>`. UI collects via `collectAsState()`. All repository methods are `suspend` functions.

## Key Details

- **Package:** `com.guitarapp.songsbook`
- **Min SDK:** 28, **Target/Compile SDK:** 36, **Java:** 21
- **Database:** Room with migration v1→v2 (added `is_favorite` column). `SongDatabase` is a singleton.
- **Serialization:** Gson for JSON parsing and Room type converters (`List<String>`, `List<SongSection>`).
- **No network calls** — fully offline, all data is local.
- **Song content model:** `Song` → `List<SongSection>` → `List<SongLine>` → `List<ChordPosition>` (chord + character position for alignment in reader).
- **Version catalog:** Dependencies managed in `gradle/libs.versions.toml`.

## Project Context

- **License:** GPL v3 — derivatives must also be open-sourced under GPL. Branding ("Cancionero") is reserved.
- **Commercial project** — decisions should favour production quality, not just learning exercises.
- **Architecture decisions** are documented in `docs/adr/`. Read them before proposing structural changes.
- **Roadmap:** v1 = local + AdMob. v2 = Firebase Auth (Google Sign-In) + Firestore + community song submissions. See `docs/` for day-by-day plans.