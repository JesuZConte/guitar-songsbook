# Day 12 — Completed

## What we built

### 1. App icon (Day 12 original plan)
- Guitar pick shape (cream `#FFF8E1`) with eighth note (brown `#4E342E`)
- Adaptive icon: `ic_launcher_foreground.xml` + `ic_launcher_background.xml`
- Solid warm brown background

### 2. Signed release APK
- Keystore generated and configured in `app/build.gradle.kts`
- `signingConfigs` reading from `local.properties`
- `isMinifyEnabled = true`, `isShrinkResources = true`
- ProGuard rules fixed for Gson reflection (`keep` instead of `keepclassmembers`)

### 3. Play Store listing draft
- `docs/PLAY_STORE_LISTING.md` — short description, full description, content rating, data safety

### 4. Security: google-services.json
- Added to `.gitignore`
- Created `app/google-services.json.example` placeholder for contributors
- Removed real file from git tracking (kept on disk)

---

# Day 13 — Completed

## What we built

### 1. Reader refresh after edit
- Added `refresh()` method to `ReaderViewModel` — reloads song from DB without showing loading spinner
- Added `DisposableEffect` + `LifecycleEventObserver` in `SongReaderScreen` — calls `viewModel.refresh()` on `ON_RESUME`
- Fix: editing a song and pressing back now shows updated content immediately

### 2. Dynamic line-based pagination (major rewrite)
- **Removed**: static `SECTIONS_PER_PAGE = 2` chunking
- **New**: `paginateContent()` estimates line heights based on font size and packs lines into pages until available screen height is filled
- Large sections are split across pages (section header repeated on continuation)
- `BoxWithConstraints` in `SongReaderScreen` measures actual screen height and passes it to ViewModel via `setAvailableHeight()`
- `repaginate()` called on font size change AND available height change
- Font size up → more pages, font size down → fewer pages
- `TEXT_HEIGHT_FACTOR = 1.8` — multiplicative estimate for Compose Text height (font metrics 1.17× + includeFontPadding)

### 3. Page indicator layout fix
- Moved page indicator from floating `Box` overlay (`Alignment.BottomCenter`) to flow-based layout (sibling below content in a `Column`)
- Content Column uses `weight(1f)` + `verticalScroll` as safety net
- Page indicator shown when `totalPages > 1` (not just fullscreen)
- `PAGE_INDICATOR_HEIGHT = 32dp` reserved in pagination math

### 4. Text input improvements
- Builder mode section cards: `height(120.dp)` → `heightIn(min = 120.dp)` — grows with pasted content
- Text mode field: `height(250.dp)` → `heightIn(min = 250.dp)` — grows with large song pastes
- Title and Artist fields: added `KeyboardCapitalization.Words` — auto-capitalizes first letter of each word

### 5. Bug fixes carried from Day 12 testing
- Empty genre chip: `WHERE genre != ''` in DAO queries
- Notation label: `if (isLatin) "Latin notation" else "American notation"`
- Key not converting in HomeScreen and SongReaderScreen: `ChordNotation.convert(song.key, notation)`
- Preview → double popBackStack: `remember { AddSongViewModel.pendingPreview }`
- FAB not visible: `LargeFloatingActionButton` with `inverseSurface` color
- Edit mode empty builder: `LaunchedEffect(uiState.rawText)` + `builderInitialized` flag

### 6. Tests updated
- `PaginationTest.kt` rewritten for new `paginateContent()` API
- Tests cover: empty sections, section splitting, font-size-aware pagination, first-page reduction, line preservation
- All 30 unit tests passing

---

## Test results
- 30 unit tests — all passing
- Debug build compiles cleanly (1 deprecation warning: `LocalLifecycleOwner` import path)

---

## Known issues going into Day 14

### Pagination fine-tuning (priority)
- At **16sp+**, the page indicator area and content boundary still have edge cases:
  - Some font sizes show last line partially clipped at the content/indicator boundary
  - `verticalScroll` safety net works but user needs to scroll 1-2 lines at some sizes
  - Root cause: `TEXT_HEIGHT_FACTOR = 1.8` is close but not exact — Compose Text height varies by font (Monospace, Merriweather), weight (Bold), device density, and system font scaling
- **Possible approaches for Day 14**:
  - Use `onGloballyPositioned` or `SubcomposeLayout` to measure actual content height instead of estimating
  - Or: accept estimate-based pagination and ensure the scroll safety net provides smooth UX
  - Or: hybrid — measure after first render and re-paginate if needed

### Remaining v1 tasks
- Play Store screenshots (minimum 2, recommended: Home, Reader, Add Song, Settings)
- Real AdMob IDs (pending account approval)
- Real `google-services.json` (pending Firebase Console)
- Optional: tap left/right screen edges to navigate pages in Reader

---

# Day 14 — Plan

## Goal
Polish pagination UX, capture Play Store screenshots, prepare final release

## Task list

### 1. Pagination fine-tuning (carry over)
- Fix remaining edge cases at 16sp+ where content overflows page boundary
- Test all font sizes 10-24 in both normal and fullscreen modes
- Ensure page indicator never overlaps content at any size

### 2. Play Store screenshots
- Capture at minimum 2 phone screenshots (Play Store requirement)
- Recommended: Home (with songs), Song Reader (showing chords), Add Song (builder mode), Settings
- Optional: feature graphic 1024×500px

### 3. Real credentials swap (Luis action items)
- Replace AdMob test IDs with real App ID + Banner Unit ID (when approved)
- Verify ads display correctly in release build

### 4. Final release build
- Build signed release APK with all fixes
- Install on device and exercise all features
- Verify ProGuard doesn't break anything

### 5. Optional: Reader page navigation via tap zones
- Tap left third of screen → previous page
- Tap right third → next page
- Tap center → toggle fullscreen (current behavior)

## Priority order
Pagination fix → Screenshots → Release build → Credentials → Tap navigation
