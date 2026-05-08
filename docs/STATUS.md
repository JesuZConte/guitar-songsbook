# Cancionero — Feature Status

Single source of truth for what is built, what is pending, and what is planned.
**Check this file at the start of every session before implementing anything.**

Last verified: 2026-05-07 (v1.4.0 complete — uploading to Play Console closed test tomorrow)

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
| Nocturno mode (black + amber, reader toggle) | `ReaderViewModel.kt`, `SongReaderScreen.kt`, `Color.kt` | — |
| Tipografía Merriweather | `Type.kt` — reemplaza Fraunces en toda la UI; JetBrains Mono se mantiene en el reader | — |
| Chord auto-detection on import ("Detectar Formato") | `OverUnderConverter.kt`, `AddSongViewModel.kt` | [ADR-012](adr/ADR-012-bracket-format-import.md) |
| Long-press delete with undo snackbar (Home) | `HomeScreen.kt`, `HomeViewModel.kt` | — |
| Pinch-to-zoom font size in Reader | `SongReaderScreen.kt` → `rememberTransformableState` | — |
| Import song from JSON file | `HomeScreen.kt` → `importLauncher`, `HomeViewModel.importSongFromJson` | — |
| Language selector (Idiomas) | `SettingsScreen.kt` → `LanguageSelectorRow` | — |
| Firebase Analytics + Crashlytics | `AnalyticsHelper.kt`, `google-services.json` | — |
| AdMob banner (Home screen, production ID) | `BannerAd.kt`, `AndroidManifest.xml` | [ADR-016](adr/ADR-016-admob-banner-monetization.md) |
| Signed AAB — Play Store ready | `build.gradle.kts` (signing config) | — |
| Setlist mode | Continuous reader across a collection; forward-only swipe; bottom nav hidden; back returns to Collection | — |

---

## v1.4 — Complete ✓

Full Leather Journal UI refinement pass. See [ROADMAP_V1X.md](ROADMAP_V1X.md) for item-by-item status.
Rationale in [ADR-021](adr/ADR-021-leather-journal-ui-refinement.md).

Highlights: leather seam animation on all pagers, all screens use `LeatherHeader`, brass gradient improved, Latin notation in builder, dead code removed, FAB menu themed.

---

## Tech Debt — P2 (v1.1)

Resuelto. Sin deuda técnica pendiente.

---

## v2 — Planned

Requires Firebase Auth + Firestore. Do not implement until v1.4 is complete and v1 is stable on Play Store.

| Feature | Notes | ADR |
|---------|-------|-----|
| Google Sign-In (Firebase Auth) | Single-tap auth; gate for all v2 cloud features | [ADR-003](adr/ADR-003-authentication-deferred-to-v2.md) |
| Cross-device sync (Firestore) | Room stays as local layer; Firestore is remote source of truth | [ADR-004](adr/ADR-004-firestore-as-remote-database.md) |
| Remove Ads IAP | One-time purchase; hides banner + unlocks AI features | [ADR-016](adr/ADR-016-admob-banner-monetization.md), [ADR-020](adr/ADR-020-v2-roadmap.md) |
| Community chord sharing | Chord skeleton only (no lyrics); other users import and add their own lyrics locally | [ADR-005](adr/ADR-005-song-catalog-hybrid-model.md), [ADR-010](adr/ADR-010-chords-public-lyrics-private.md) |
| AI format conversion (Claude API) | Unlocked by Remove Ads IAP; fallback for formats local heuristic misses | [ADR-020](adr/ADR-020-v2-roadmap.md) |
| AI song completion (Claude API) | Suggests chords for empty sections based on existing patterns; unlocked by IAP | [ADR-020](adr/ADR-020-v2-roadmap.md) |
| Tap-a-word chord editor | Mobile-first chord placement; replaces bracket manual entry for new songs | [ADR-006](adr/ADR-006-chord-editor-tap-to-place.md) |
