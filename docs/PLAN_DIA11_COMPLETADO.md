# Day 11 — Completed

## What we built

### 0. Test suite — all passing
- Ran `./gradlew test` — 30 unit tests pass, clean build

### 1. Firebase build fixes (carried over from Day 10)
- Renamed `-ktx` artifacts to base names (`firebase-analytics`, `firebase-crashlytics`) — the `-ktx` variants were removed in BOM 32+
- Created missing `res/xml/gma_ad_services_config.xml` — required by AdMob/Firebase manifest merge
- Added Crashlytics Gradle plugin `3.0.3` to root and app `build.gradle.kts` — Crashlytics requires it to inject a build ID at compile time; without it the app crashed on startup
- Fixed corrupt Merriweather font files (were HTML pages, not TTFs) — downloaded the variable font from Google Fonts

### 2. Crashlytics error wiring
- `FirebaseCrashlytics.getInstance().log()` breadcrumbs added before key operations:
  - `HomeViewModel`: initial data load
  - `ReaderViewModel`: song load (includes song ID)
  - `AddSongViewModel`: save/update song (includes edit mode flag)
  - `AssetSongRepository`: seed from assets
- `FirebaseCrashlytics.getInstance().recordException(e)` added to every existing catch block across all ViewModels and the repository
- Repository re-throws after logging so ViewModel error UX is unchanged

### 3. Google Sign-In stub (v2 architecture prep)
- `AuthRepository` interface created at `data/repository/AuthRepository.kt`
  - `signInWithGoogle(): Result<Unit>`
  - `signOut()`
  - `isSignedIn(): Boolean`
  - No implementation — placeholder for v2 Firebase Auth
- `play-services-auth:21.3.0` added as dependency
- Settings screen — new "Account" section with greyed-out "Sign in with Google" row
  - Subtitle: "Sync your songbook across devices"
  - "Coming soon" chip (secondary container color)
  - Not clickable — no false affordance for users

### 4. Play Store listing draft
- `docs/PLAY_STORE_LISTING.md` created with:
  - Short description (80 chars)
  - Full description (musician-focused copy, no marketing fluff)
  - Content rating Q&A → expected rating: **Everyone**
  - Data safety section (nothing collected, anonymized crash data only)
  - Pre-submission checklist (AdMob IDs, google-services.json, icon, screenshots)

---

## Test results
- 30 unit tests — all passing
- Clean debug build

---

## Known gaps going into Day 12
- App icon still uses default Android launcher icon — needed before Play Store submission
- No signed release APK verified yet
- No Play Store screenshots captured
- Real AdMob IDs not yet swapped (pending AdMob account approval)
- Real `google-services.json` not yet in place (pending Firebase Console setup)

---

# Day 12 — Plan

## Goal
v1 release readiness: app icon, signed APK, Play Store assets

---

## Task list

### 1. App icon
- Design launcher icon (guitar + notebook concept)
- Export to all mipmap densities: mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi
- Adaptive icon: `ic_launcher_foreground.xml` + `ic_launcher_background.xml`
- Verify icon renders correctly on round and square launchers

### 2. Signed release APK
- Generate upload keystore (if not already done)
- Configure `signingConfigs` in `app/build.gradle.kts`
- Build release APK: `./gradlew assembleRelease`
- Install on device and verify app launches, fonts load, ads show, navigation works

### 3. Play Store screenshots
- Capture at least 2 phone screenshots (Play Store minimum)
- Recommended screens: Home (with songs), Song Reader, Add Song, Settings
- Optional: feature graphic 1024×500 px

### 4. Swap real credentials (Luis action items)
- Replace `google-services.json` with real Firebase project file
- Replace AdMob test IDs with real App ID and Banner Unit ID
- These are blockers for store submission but do not block icon/APK work

### 5. ProGuard validation
- Confirm release build doesn't strip Room entities, Gson models, or Firebase classes
- Run release APK on device and exercise all features

---

## Priority order
App icon → Signed release APK → ProGuard validation → Screenshots → Real credentials swap
