# Day 14 — Completed

## What we built

### 1. Kindle-style render-then-measure pagination (major rewrite)
- **Removed**: static `TEXT_HEIGHT_FACTOR` estimation approach from `ReaderViewModel`
- **New**: `SubcomposeLayout` two-pass render-then-measure in `SongContentComponents.kt`
  - Pass 1: measures each composable individually (`SongHeader`, `SectionHeaderText`, each `LineContent`) using string keys ("h", "sh0", "l0_0", etc.)
  - Pass 2: builds `pageStarts` list by snapping pixel targets to nearest valid line boundary — no line ever straddles a page boundary
- `PageSlice(contentOffsetPx, contentHeightPx)`: clips exactly to distance between adjacent page starts
- Empty last page fix: removes last `pageStart` if remaining content ≤ topPaddingPx
- `PAGE_INDICATOR_DP = 48f` (increased from 28f for readability)
- `ReaderViewModel` simplified: removed `paginateContent()`, `estimateSongHeaderHeight()`, `setAvailableHeight()`, `repaginate()`, and the companion object
- `loadSong()` now just sets `song` and `isLoading = false` — no pagination math

### 2. Tap zones for page navigation
- Left third of screen → previous page
- Right third of screen → next page
- Center third → toggle fullscreen (existing behavior preserved)
- Implemented via `pointerInput(pageCount)` + `detectTapGestures`
- `onSizeChanged` tracks container width for zone calculation
- `rememberCoroutineScope` + `animateScrollToPage` for animated transitions

### 3. App renamed to Cancionero
- `res/values/strings.xml`: `GuitarSongsbook` → `Cancionero`
- `HomeScreen.kt` TopAppBar title: `"Guitar Songbook"` → `"Cancionero"`
- `AboutScreen.kt` headlineSmall: `"Guitar Songbook"` → `"Cancionero"`

### 4. Real AdMob credentials
- `AndroidManifest.xml`: App ID updated to `ca-app-pub-8804586949821046~8761207364`
- `BannerAd.kt`: Banner Unit ID updated to `ca-app-pub-8804586949821046/3185447057`
- Test ID comments removed

### 5. ADRs created
- `docs/adr/ADR-014-app-icon-design.md` — icon similarity to Guitar Tuna documented, deferred to v2
- `docs/adr/ADR-015-song-difficulty-field.md` — difficulty field usefulness reviewed, recommends "Mastery" concept for v2

### 6. Bug fixes
- Fatal crash on launch: `maxWidth must be >= than minWidth` — fixed by adding `minWidth = 0, minHeight = 0` to `constraints.copy()` call
- Page indicator overlapping last line — fixed by `PageSlice` clipping to variable `contentHeightPx` per page
- Top content cut on page 2+ — same fix as above
- Empty last page — removed last `pageStart` when remaining content ≤ padding

---

## Test results
- `PaginationTest.kt` deleted (tested `paginateContent()` which was removed)
- No new unit tests — pagination is pure UI/SubcomposeLayout, not unit-testable
- Manual testing confirmed: all font sizes 10–24sp, normal and fullscreen, no cut lines, no empty pages

---

## Play Store submission progress

### Completed in Play Console
- Data safety: 10 declarations submitted
  - Crash logs, Diagnostics, Other performance → collected + shared, not temporary, required, analytics + bug prevention
  - App interactions → collected + shared, not temporary, required, analytics only
  - Device ID → collected + shared, not temporary, required, advertising/marketing
  - Search history → NOT declared (no analytics event fires on search)
  - Advertising ID declared: Sí, Publicidad o marketing
- App type: Application → **Música y audio**
- Tags (5/5): **Guitarra, Instrumentos musicales, Karaoke, Letra, Bloc de notas**
- Marketing externo: enabled (free)
- App icon: 512×512 PNG created in Canva (new design, distinct from Guitar Tuna)

### Decisions made
- Chord notation labeled **"Americano / Latino"** (C/D/E vs Do/Re/Mi) in UI and store listing
- Transposition not in v1 — add to v2 roadmap
- Dark theme follows system setting — no in-app toggle
- Final app name: **Cancionero – Chord Songbook**

---

## Known gaps going into Day 15

- Feature Graphic (1024×500 px) not yet created — use Canva "Google Play Feature Graphic" template
- Store listing not yet filled in (name, descriptions, screenshots)
- APK not yet uploaded to Play Console
- AdMob banner may take 24–72h to show real ads on new account (normal)

---

# Day 15 — Plan

## Goal
Complete Play Store listing and upload APK to Internal Testing

## Task list

### 1. Feature Graphic (Luis + wife action)
- Open Canva → search "Google Play Feature Graphic"
- 1024×500 px, PNG or JPG
- Include app name "Cancionero", brand colors (warm brown + cream)
- Keep important content away from edges (cropped on some devices)

### 2. Fill in store listing
- **Nombre**: `Cancionero – Chord Songbook`
- **Descripción breve** (80 chars): `Your personal guitar songbook with chords, tabs and playlists.`
- **Descripción completa**: see draft below
- Upload screenshots (Home + Song Reader minimum)
- Upload icon (512×512) and Feature Graphic (1024×500)

### 3. Upload APK
- Build signed release: `./gradlew assembleRelease`
- Upload to **Internal Testing** track first
- Verify app launches, fonts load, ads load (may take 24–72h), navigation works

### 4. Submit for review
- Complete any remaining Play Console checklist items
- Submit to Internal Testing → then promote to Production

---

## Store listing description draft

**Descripción breve:**
```
Your personal guitar songbook with chords, tabs and playlists.
```

**Descripción completa:**
```
Cancionero is your personal chord songbook for guitar players.

Add the songs you play and keep your chords always at hand — whether you're rehearsing at home, performing live, or teaching a student.

FEATURES
• Add your own songs with chords and lyrics
• Chord notation: American (C, D, E) or Latin (Do, Re, Mi)
• Organize songs in playlists
• Mark your favourites for quick access
• Full-screen reader with adjustable font size
• Tap left/right to navigate pages, tap center for full screen
• Search and filter by genre or difficulty
• Works 100% offline — no internet required
• Dark theme (follows system setting)

Built for guitarists who want a clean, distraction-free notebook — not a streaming app or a lyrics database.

Open source · GPL v3
```

## Priority order
Feature Graphic → Store listing → Upload APK → Internal Testing → Production
