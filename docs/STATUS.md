# Cancionero — Feature Status

Single source of truth for what is built, what is pending, and what is planned.
**Check this file at the start of every session before implementing anything.**

Last verified: 2026-11-24

---

## v1 — Shipped ✓

Everything in this section is verified in the codebase.

| Feature | Key file(s) | ADR |
|---------|-------------|-----|
| Song library — search, genre/difficulty filters | `HomeScreen.kt`, `HomeViewModel.kt` | — |
| Room database with seeded content (3 public-domain songs: Amazing Grace, Greensleeves, Scarborough Fair) | `SongDatabase.kt`, `assets/songs.json` | [ADR-002](adr/ADR-002-local-first-no-backend.md) |
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
| Import song from `.txt` file (title/artist/key/capo extracted, chords-over-lyrics converted, opens Add Song pre-filled) | `HomeScreen.kt` → `importLauncher`, `TextSongImport.kt`, `TextDraftHolder.kt`, `AddSongViewModel.withImportedDraft` | [ADR-025](adr/ADR-025-txt-song-import.md) |
| Import song from JSON file | `HomeScreen.kt` → `importLauncher`, `HomeViewModel.importSongFromJson` | — |
| Smart import — duplicate title detection + version merge | `ImportConflictDialog.kt`, `HomeViewModel`, `AddSongViewModel`, `TitleNormalizer.kt` — accent-insensitive matching; dialog offers merge-as-version or save-separate | — |
| Language selector (Idiomas) | `SettingsScreen.kt` → `LanguageSelectorRow` | — |
| Firebase Analytics + Crashlytics + breadcrumbs | `AnalyticsHelper.kt`, `google-services.json` | [ADR-022](adr/ADR-022-security-hardening-and-crashlytics-breadcrumbs.md) |
| AdMob banner (Home screen, production ID) | `BannerAd.kt`, `AndroidManifest.xml` | [ADR-016](adr/ADR-016-admob-banner-monetization.md) |
| Signed AAB — Play Store ready | `build.gradle.kts` (signing config) | — |
| Setlist mode | Continuous reader across a collection; forward-only swipe; bottom nav hidden; back returns to Collection | — |

---

## Distribution

| Item | State (2026-11-24) |
|------|--------------------|
| Play Console track | **Closed testing** — recruiting 12 testers for 14 continuous days (required before production access) |
| Latest uploaded bundle | **1.4.5 (versionCode 12)** |
| Repo `app/build.gradle.kts` | 1.4.5 (12). The changes below are **not** in that bundle; next bundle = 1.4.6 (13) |

---

## v1.4.5 — Uploaded to closed testing ✓

| Change | File(s) |
|--------|---------|
| Open a `.json` song from outside the app (VIEW intent, read off the main thread) | `AndroidManifest.xml`, `MainActivity.kt` (`handleIncomingIntent`) — commits `493e83c` (its message says "add space between chords" but it does not touch chords; that fix shipped in 1.4.4) and `52ad60f` |
| Edit-version routing: reader keeps the right version after reload | `ReaderViewModel.kt` (`resolveVersionIndexOnReload`), `SongReaderScreen.kt`, `ResolveVersionIndexOnReloadTest` |
| Reader performance: per-page item composition, deferred pagination (Día 30 phases 0, 1, 3) | `SongContentComponents.kt`, `SongReaderScreen.kt` — [ADR-013](adr/ADR-013-render-then-measure-pagination.md), [PLAN_DIA30](PLAN_DIA30_COMPLETADO.md) |

---

## After 1.4.5 — Committed on `main`, not yet released

| Change | File(s) | Ref |
|--------|---------|-----|
| `.txt` song import (Home → "Importar desde archivo" accepts `.json` and `.txt`) | `TextSongImport.kt`, `TextDraftHolder.kt`, `HomeScreen.kt`, `MainActivity.kt`, `AddSongScreen.kt` | [ADR-025](adr/ADR-025-txt-song-import.md) |
| Parser fixes (also for pasted text): Spanish section labels → canonical keys, `[Intro] F Bb` split, chord padding past lyric end | `BracketParser.kt`, `OverUnderConverter.kt` | ADR-025 phase 1a |
| `BrassButton` for the Save buttons (gradient, faded when disabled) | `ui/components/Brass.kt`, `AddSongScreen.kt`, `VersionEditorScreen.kt` | — |
| Official GPL v3 `LICENSE`, `GPL-3.0-or-later` SPDX headers, `NOTICE.md`, `THIRD_PARTY_NOTICES.md`, `licenses/`, About screen license block | repo root, all `.kt` | [ADR-026](adr/ADR-026-license-compliance-and-scope.md) |
| `docs/songs/` is local-only (git-ignored) | `.gitignore` | ADR-026 |

**Uncommitted (2026-11-24):** song titles no longer sent to Firebase Analytics / Crashlytics (`AnalyticsHelper.logSongOpened(songId)`), and the privacy policy draft `docs/PRIVACY_POLICY.md`.

---

## Legal & store compliance

See the full checklist in [ADR-026](adr/ADR-026-license-compliance-and-scope.md).

| Item | State |
|------|-------|
| License text, SPDX headers, NOTICE, third-party notices, font licenses | ✓ Done |
| Privacy policy | **Draft written** (`PRIVACY_POLICY.md`); needs contact email, publication at a public URL and the URL in Play Console |
| Ad consent flow (Google UMP) for EEA/UK users | ✗ **Not implemented** — blocks serving ads there; the policy text assumes it exists |
| Play "Data safety" form vs. actual SDK behavior | To reconcile (Analytics, Crashlytics, AdMob advertising ID) |
| Third-party lyrics in git history (`5ea0339`, `81306c0`) | Open — rewrite history only if Luis decides |
| Launcher icon rights (made in Canva) | Open — written confirmation + Canva terms |
| Lyrics can travel in JSON backups shared between users | Open — add a notice on the share action |

---

## v1.4.4 — Complete ✓ (bug-fix patch)

Tester feedback fix: chord collision on tightly-spaced chord rows.

| Fix | File(s) |
|-----|---------|
| Chords rendered with no gap ("RemSolmLa7...") when Latin notation names are longer than the tab's original spacing, most visible on intro/chord-only lines | `ChordLine.kt` — now delegates to shared `layoutChordRow()` (`ChordFormatter.kt`), which pushes colliding chords right with a 1-space gap. Same fix applied to `buildChordLine`, which had it for Latin already (v1.1) but was never wired into the actual reader composable. |

---

## v1.4.1 — Complete ✓ (bug-fix patch + security hardening)

Tester feedback fixes + pre-launch security hardening. See [ADR-022](adr/ADR-022-security-hardening-and-crashlytics-breadcrumbs.md).

| Fix | File(s) |
|-----|---------|
| Nocturno toggle no-op in system/dark mode | `Color.kt` — `NocturnoColorScheme` now `DarkColors.copy(...)` |
| Keyboard covering builder UI on Samsung A50 | `AndroidManifest.xml` (`adjustResize`), `AddSongScreen.kt` (`weight(1f)` + `imePadding`) |
| Long song content clips instead of scrolling | `AddSongScreen.kt` — removed `heightIn(max)` cap on text fields |
| Font size slider only scaled titles, not lyrics/chords | `SongContentComponents.kt`, `ChordLine.kt` — threaded `fontSize` through full render chain |
| Title scaled with font size slider (undesired) | `SongContentComponents.kt` — `SongHeader` uses fixed sizes |
| Chord bar was below text input (builder UX) | `AddSongScreen.kt` — chord bar moved above text area |
| Add Section bar was at the bottom (builder UX) | `AddSongScreen.kt` — `AddSectionBar` moved to top of builder |
| Excess spacing in `SectionCard` | `AddSongScreen.kt` — offset + compact `BasicTextField` chip field |
| Delete button 24dp touch target (WCAG regression) | `AddSongScreen.kt` — restored full 48dp `IconButton` |
| Missing TalkBack labels on chord/lyrics fields | `AddSongScreen.kt` — `Modifier.semantics { contentDescription }` added |
| Genre field not auto-capitalizing | `AddSongScreen.kt` — `KeyboardCapitalization.Words` added |

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
