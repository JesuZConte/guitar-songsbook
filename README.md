# Guitar Songbook

Android songbook app for guitarists. Browse songs, view chords positioned over lyrics, and build playlists — all offline.

## Features

- Song library with searchable list
- Chord positioning over lyrics (monospace aligned)
- Song detail view with sections (verse, chorus, intro)
- Offline-first with local Room database
- Material 3 design

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean Architecture
- **Database:** Room (SQLite)
- **Navigation:** Jetpack Navigation Compose
- **Serialization:** Gson
- **Testing:** JUnit 4 + Mockito

## Architecture

```
domain/model/        → Pure Kotlin data classes (Song, SongSection, etc.)
data/local/          → Room database, entities, DAOs, type converters
data/repository/     → Repository interface + implementation
presentation/        → ViewModels, Compose screens, UI state
```

The app follows **Frontend-First** development with JSON mock data in `assets/`. Data flows through the Repository pattern, making it easy to swap data sources (local JSON → Room → remote API) without touching the UI layer.

## Building

1. Open in Android Studio (latest stable)
2. Sync Gradle
3. Run on emulator or device (API 28+)

## Running Tests

```bash
./gradlew test
```

## Roadmap

- [x] Song list with cards
- [x] Room database persistence
- [x] Song detail with positioned chords
- [x] Navigation between screens
- [ ] Song reader with auto-scroll
- [ ] Search and filters
- [ ] Playlists and favorites
- [ ] Dark theme
- [ ] AdMob integration
- [ ] Google Play Store release

## License

MIT — see [LICENSE](LICENSE) for details.
