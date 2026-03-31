# Day 9 — Completed

## What we built

### 1. DAO & Repository — delete/update
- `SongDao.update()` — `@Update` annotation
- `SongDao.deleteById()` — `@Query DELETE`
- `SongRepository.updateSong()` + `deleteSong()` interface methods
- `AssetSongRepository` implementations

### 2. Settings screen
- New `SettingsScreen.kt` composable
- Notation toggle: American (C D E F G A B) ↔ Latin (Do Re Mi Fa Sol La Si)
- Reads/writes `UserPreferences` (SharedPreferences)
- Subtitle shows current notation label live as the toggle changes
- Gear icon in HomeScreen `TopAppBar` → navigates to `settings` route

### 3. Delete song from Reader
- Trash icon in `SongReaderScreen` TopAppBar
- Confirmation `AlertDialog` with song title
- `ReaderViewModel.deleteSong()` calls `songRepository.deleteSong()`
- `deleteSuccess` state flag → `LaunchedEffect` triggers `onDeleteSuccess` → `popBackStack`

### 4. BracketSerializer
- `BracketSerializer.serialize(sections)` — converts `List<SongSection>` back to bracket-format text
- Inserts chord brackets right-to-left to preserve character positions
- Used by edit flow to pre-fill the text field

### 5. Edit song from Reader
- Pencil icon in `SongReaderScreen` TopAppBar → navigates to `edit_song/{songId}`
- `AddSongViewModel` extended with `editSongId` — loads existing song via `loadSongForEdit()` in `init`
- Pre-fills all fields (title, artist, key, capo, genre, difficulty, rawText via BracketSerializer)
- `saveSong()` calls `updateSong()` instead of `insertSong()` in edit mode
- `AddSongScreen` title changes to "Edit Song" in edit mode
- New route `Routes.EDIT_SONG = "edit_song/{songId}"` + `Routes.editSong(songId)`

### 6. AdMob banner with test IDs
- Added `play-services-ads:23.6.0` dependency
- `MobileAds.initialize(this)` in `MainActivity.onCreate()`
- AdMob App ID meta-data in `AndroidManifest.xml`
- `BannerAd.kt` composable wrapping `AndroidView` + `AdView`
- Banner shown as `bottomBar` of HomeScreen Scaffold
- **Test IDs used** (safe to commit, replace before release):
  - App ID: `ca-app-pub-3940256099942544~3347511713`
  - Banner Unit ID: `ca-app-pub-3940256099942544/6300978111`

### 7. Deprecation fix
- `MenuAnchorType` → `ExposedDropdownMenuAnchorType` in `AddSongScreen.kt`

---

## Test results
- 28 unit tests — all passing
- Clean build, zero warnings

---

## Known gaps going into Day 10
- No real AdMob IDs yet (user will add them when account is ready)
- No settings for font size default or theme preference
- No empty state illustration (just text currently)
- v2 features not started: Firebase Auth, Firestore, community sharing

---

# Day 10 — Plan

## Goal
App Store readiness: icons, about screen, ProGuard/release build, Play Store assets, onboarding

---

## Task list

### 1. App icon
- Design/export a proper launcher icon (guitar + notebook concept)
- Replace default Android launcher icons in all mipmap densities
- Adaptive icon support (`ic_launcher_foreground` + `ic_launcher_background`)

### 2. About screen
- App version, license (GPL v3), author
- Link to GitHub (or placeholder)
- Accessible from Settings screen (new row below notation toggle)

### 3. Onboarding / empty state
- When song list is empty (fresh install, or user deleted everything): show a meaningful empty state
- Illustration or icon + "Add your first song" CTA button → navigates to Add Song

### 4. Release build configuration
- Enable ProGuard/R8 minification in release build type
- Add AdMob ProGuard rules
- Verify release build compiles and runs

### 5. Replace AdMob test IDs
- This depends on Luis having the AdMob account ready
- Swap test App ID and banner unit ID with real ones
- Add `ads.txt` reference in Play Store listing

### 6. Tech Lead review + full test run

---

## Priority order
Empty state → About screen → Release build → App icon → AdMob real IDs → Review

Empty state and About are fast wins. Release build is important before any Play Store work.
App icon needs design time — can be done in parallel or deferred.