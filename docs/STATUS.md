# Cancionero — Feature Status

Single source of truth for what is built, what is pending, and what is planned.
**Check this file at the start of every session before implementing anything.**

Last verified: 2026-05-03

---

## v1 — Shipped ✓

Everything in this section is verified in the codebase.

| Feature | Key file(s) | ADR |
|---------|-------------|-----|
| Song library — search, genre/difficulty filters | `HomeScreen.kt`, `HomeViewModel.kt` | — |
| Room database with seeded content (48+ songs) | `SongDatabase.kt`, `assets/songs.json` | [ADR-002](adr/ADR-002-local-first-no-backend.md) |
| Bracket format import + song builder | `AddSongScreen.kt`, `BracketParser.kt` | [ADR-012](adr/ADR-012-bracket-format-import.md) |
| Chord reader with positioned chords | `SongReaderScreen.kt`, `SongContentComponents.kt` | — |
| Render-then-measure pagination (HorizontalPager) | `SongContentComponents.kt` → `VirtualPagedSong` | [ADR-013](adr/ADR-013-render-then-measure-pagination.md) |
| Transposition (live in reader, semitone ±) | `ReaderViewModel.kt`, `ChordNotation.kt` | [ADR-018](adr/ADR-018-transposition-deferred-to-v2.md) |
| Song versions (per-song alternate arrangements) | `VersionEditorScreen.kt`, `SongVersionEntity.kt` | — |
| Playlists — CRUD, undo-delete snackbar | `PlaylistsScreen.kt`, `PlaylistsViewModel.kt` | — |
| Favorites — heart toggle, dedicated tab | `FavoritesScreen.kt`, `FavoritesViewModel.kt` | — |
| Collections landing + Traditional Songs | `CollectionsLandingScreen.kt` | [ADR-019](adr/ADR-019-collections-home-redesign.md) |
| Export / share chord chart (no lyrics) | `SongExporter.kt` | [ADR-010](adr/ADR-010-chords-public-lyrics-private.md) |
| Chord notation switching (American / Latin) | `ChordNotation.kt`, `UserPreferences.kt` | [ADR-011](adr/ADR-011-chord-notation-switching.md) |
| Theme selector (system / light / dark) | `SettingsScreen.kt`, `ThemeManager.kt` | [ADR-008](adr/ADR-008-vintage-craft-visual-identity.md) |
| Nocturno mode (black + amber, reader toggle) | `ReaderViewModel.kt`, `Color.kt` | — |
| Chord auto-detection on import ("Detectar Formato") | `OverUnderConverter.kt`, `AddSongViewModel.kt` | [ADR-012](adr/ADR-012-bracket-format-import.md) |
| Long-press delete with undo snackbar (Home) | `HomeScreen.kt`, `HomeViewModel.kt` | — |
| Firebase Analytics + Crashlytics | `AnalyticsHelper.kt`, `google-services.json` | — |
| AdMob banner (Home screen, production ID) | `BannerAd.kt`, `AndroidManifest.xml` | [ADR-016](adr/ADR-016-admob-banner-monetization.md) |
| Signed AAB — Play Store ready | `build.gradle.kts` (signing config) | — |

---

## v1 — Remaining

| Feature | Notes | ADR |
|---------|-------|-----|
| Setlist mode | Continuous reader across a collection; swipe forward between songs; no Home between songs — designed for live performance | — |

---

## Tech Debt — P1 (fix before next Play Store release)

| # | Location | Issue |
|---|----------|-------|
| 1 | `Converters.kt:20,31` | `gson.fromJson` has no try-catch — a corrupt DB row crashes the app with `JsonSyntaxException` |
| 2 | `ReaderViewModel.kt` | Font size changes update in-memory state only; `UserPreferences.setFontSize` is never called — preference lost on restart |

---

## Tech Debt — P2 (v1.1)

| # | Location | Issue |
|---|----------|-------|
| 3 | `AddSongViewModel.kt:165` | `var pendingPreview: Song?` in `companion object` — mutable static state for nav handoff; not thread-safe, no lifecycle management. Replace with `SavedStateHandle` |
| 4 | `PlaylistsViewModel.kt` | `removeSongFromPlaylist` has no try-catch; `deletePlaylist` does — inconsistent error handling |
| 5 | `RoomSongRepository.kt` | `ensureSeeded()` runs a `SELECT COUNT(*)` on every public method call — move to one-time `init` |

---

## v2 — Planned

Requires Firebase Auth + Firestore. Do not implement until v1 is stable on Play Store.

| Feature | Notes | ADR |
|---------|-------|-----|
| Google Sign-In (Firebase Auth) | Single-tap auth; gate for all v2 cloud features | [ADR-003](adr/ADR-003-authentication-deferred-to-v2.md) |
| Cross-device sync (Firestore) | Room stays as local layer; Firestore is remote source of truth | [ADR-004](adr/ADR-004-firestore-as-remote-database.md) |
| Remove Ads IAP | One-time purchase; hides banner + unlocks AI features | [ADR-016](adr/ADR-016-admob-banner-monetization.md), [ADR-020](adr/ADR-020-v2-roadmap.md) |
| Community chord sharing | Chord skeleton only (no lyrics); other users import and add their own lyrics locally | [ADR-005](adr/ADR-005-song-catalog-hybrid-model.md), [ADR-010](adr/ADR-010-chords-public-lyrics-private.md) |
| AI format conversion (Claude API) | Unlocked by Remove Ads IAP; fallback for formats local heuristic misses | [ADR-020](adr/ADR-020-v2-roadmap.md) |
| AI song completion (Claude API) | Suggests chords for empty sections based on existing patterns; unlocked by IAP | [ADR-020](adr/ADR-020-v2-roadmap.md) |
| Tap-a-word chord editor | Mobile-first chord placement; replaces bracket manual entry for new songs | [ADR-006](adr/ADR-006-chord-editor-tap-to-place.md) |
