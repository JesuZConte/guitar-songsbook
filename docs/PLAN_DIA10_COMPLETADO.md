# Day 10 — Completed

## What we built

### 1. Firebase integration
- Added Firebase BOM `33.7.0`, `firebase-analytics-ktx`, `firebase-crashlytics-ktx` dependencies
- Applied `google-services` plugin (`4.4.2`) in `app/build.gradle.kts`
- Placeholder `google-services.json` committed (replace with real one from Firebase Console before release)

### 2. AnalyticsHelper
- New `utils/AnalyticsHelper.kt` singleton
- Events tracked: `screen_view`, `song_opened`, `song_added`, `song_edited`, `song_deleted`, `notation_changed`, `playlist_created`
- All event names defined as constants to prevent typos
- Wired `logNotationChanged()` into `SettingsScreen` notation toggle

### 3. AboutScreen
- New `presentation/screens/AboutScreen.kt` composable
- Displays app name, `BuildConfig.VERSION_NAME`, description, GPL v3 license notice
- Accessible from Settings via a new "About" row with chevron (`KeyboardArrowRight`)
- New route `Routes.ABOUT = "about"` added to nav graph

### 4. Settings → About navigation
- Added `AboutRow` composable inside `SettingsScreen`
- New "App" section header above the row
- `onAboutClick` lambda wired through `MainActivity` nav graph

### 5. Empty library state (HomeScreen UX)
- Split old `EmptyContent()` into two cases:
  - **Active filter + no results** → `NoResultsContent()` (unchanged copy)
  - **No songs at all** → `EmptyLibraryContent()` with `LibraryMusic` icon, descriptive text, and "Add Song" CTA button
- CTA navigates directly to Add Song flow via `onAddSongClick` lambda

### 6. Release build hardening
- Enabled `isMinifyEnabled = true` + `isShrinkResources = true` in release build type
- Updated `proguard-rules.pro`: added rules for Firebase, Crashlytics, Gson, Room, Compose Navigation, and AdMob

### 7. AndroidManifest conflict fix
- Added `tools:replace="android:resource"` on `AD_SERVICES_CONFIG` property to resolve merge conflict between `play-services-ads` and `firebase-measurement`

---

## Test results
- Tests not run this session — scheduled for Day 11

---

## Known gaps going into Day 11
- `google-services.json` is a placeholder — real file needed before release
- No real AdMob IDs yet
- Crashlytics not wired into error paths yet (only SDK initialized)
- App icon still uses default Android launcher icon
- v2 features not started: Firebase Auth, Firestore, community sharing

---

# Day 11 — Plan

## Goal
Test validation, Crashlytics error wiring, Play Store prep

---

## Task list

### 0. Pending from Day 10 (priority)
- Run full test suite (`./gradlew test`) — all 28+ unit tests must pass
- Smoke-test on device/emulator: Firebase not crashing, About screen navigation, empty state CTA, analytics events in DebugView

### 1. Crashlytics error wiring
- Log non-fatal errors in repository catch blocks (`FirebaseCrashlytics.getInstance().recordException(e)`)
- Add breadcrumbs (`log()`) to key flows: song load, DB write, bracket parse failure

### 2. v2 prep: Google Sign-In stub
- Add `play-services-auth` dependency
- Create `AuthRepository` interface (no implementation yet)
- Add "Sign in with Google" row in Settings — visually present but disabled with "Coming soon" label
- Keeps architecture ready without blocking v1 release

### 3. Play Store listing draft
- Short description (80 chars max)
- Full description (4000 chars max)
- Content rating questionnaire answers
- Verify signed release APK compiles and launches cleanly

### 4. App icon (if time permits)
- Design/export launcher icon (guitar + notebook concept)
- Replace default mipmap assets across all densities
- Adaptive icon support (`ic_launcher_foreground` + `ic_launcher_background`)

---

## Priority order
Tests → Crashlytics wiring → Play Store draft → Google Sign-In stub → App icon