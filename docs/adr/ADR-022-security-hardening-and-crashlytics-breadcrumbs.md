# ADR-022: Security hardening pre-launch and Crashlytics breadcrumbs

## Status
Accepted

## Context

Before promoting v1.4.1 to production, a security review was conducted covering the full attack surface of the app: manifest configuration, API key exposure, database access patterns, network policy, backup rules, SharedPreferences contents, and release build hardening.

The review was triggered by the impending Play Store launch (v1.4.1, day 13 of 14).

## Findings

### Firebase API key in git history
Commit `599c734` (day 11) committed the real Firebase API key inside `app/google-services.json` before the file was added to `.gitignore`. The key (`AIzaSyC…`) remained in git history permanently.

Firebase API keys for Android are client-side identifiers — the same key is embedded in every APK distributed via the Play Store — so this is not a secret in the traditional sense. However, an exposed project ID combined with permissive Firebase Security Rules could allow abuse of Firebase services.

**Resolution:** The API key was restricted in Google Cloud Console (APIs & Services → Credentials) to:
- Application restriction: Android apps
- Allowed package: `com.guitarapp.songsbook`
- Two SHA-1 certificate fingerprints: local release keystore + Play App Signing certificate (obtained from Play Console → App integrity → App signing)

With these restrictions, the key is useless outside the legitimate signed app, regardless of git history.

### Items confirmed safe
- **SQL injection:** Room uses parameterized binding for all queries, including `LIKE '%' || :query || '%'` in `SongDao.search()`. Not exploitable.
- **Cleartext HTTP:** `targetSdk = 36` (API 28+) blocks cleartext traffic by default for app code. No `network_security_config.xml` needed.
- **`android:debuggable`:** Not set in source manifest; Gradle defaults it to `false` for release builds.
- **R8 minification:** Enabled (`isMinifyEnabled = true`, `isShrinkResources = true`) for release.
- **Keystore credentials:** Read from `local.properties` which is gitignored. Not in version control.
- **Exported components:** Only `MainActivity` (LAUNCHER intent). All AdMob/Firebase components are `exported="false"`.
- **WebViews:** None in app code.
- **Debug logging:** No `Log.*` or `Timber.*` calls in production source.
- **SharedPreferences:** Stores only UI preferences (notation, theme, font size, language). No credentials or PII.
- **`android:allowBackup="true"`:** Backup rules are empty stubs — the full Room database is backed up to Google Cloud. For a personal songbook this is intentional and desirable (user's songs are the primary value). No sensitive data is stored in the database.

### OAuth consent screen warning (Google Cloud Console)
Play Services emits a warning about configuring the OAuth consent screen. This is a prerequisite for Google Sign-In (v2). Not relevant for v1 — no authentication is implemented.

## Monitoring gap identified

The review also surfaced a monitoring gap. The app ships Firebase Crashlytics and Firebase Analytics (`AnalyticsHelper.kt`), which cover crash reporting and behavioral analytics respectively. However, Crashlytics crash reports lacked contextual breadcrumbs — no information about what the user was doing immediately before a crash.

This is a meaningful gap for debugging: a stack trace alone is often insufficient to reproduce a crash without knowing which song was open, what action was taken, or what state the UI was in.

## Decision

### 1. Firebase API key restriction
Restrict the key as described above. This is a one-time configuration in Google Cloud Console, not a code change.

### 2. Crashlytics breadcrumbs on key user actions
Add `FirebaseCrashlytics.getInstance().log(...)` calls alongside the existing `AnalyticsHelper` events for the following actions:

- Song opened (song ID + title)
- Song added
- Song edited
- Song deleted
- Playlist created
- Notation changed

These breadcrumbs are only transmitted when a crash occurs, at no extra cost in normal operation. They are the mobile equivalent of structured log lines — visible in the Crashlytics dashboard under each crash event.

Implementation is co-located in `AnalyticsHelper.kt` so both analytics events and crash breadcrumbs are emitted from a single call site.

### 3. No additional monitoring tooling for v1
The reviewer (Luis) works with Cloud Logging, OpenTelemetry, Grafana/Datadog/Splunk in GCP production environments. Those tools do not apply to a client-side Android app — there is no infrastructure to instrument.

For v1 the monitoring stack is:
- **Crashlytics** — crash reporting + breadcrumbs (reactive, post-crash)
- **Firebase Analytics** — behavioral events (24h delay, not ops)
- **Android Vitals** (Play Console) — crash rate, ANR rate, startup time, battery

For v2, when Firestore and Firebase Auth are added, Cloud Logging and Cloud Monitoring apply to the Firebase backend and will integrate with existing GCP tooling.

## Consequences

- The Firebase API key is restricted and safe even though it exists in git history.
- Crashlytics reports for v1.4.1 onwards will include breadcrumbs that make crashes actionable without requiring a reproduction case.
- No new dependencies are introduced — `firebase-crashlytics` is already in the dependency graph.
- The OAuth consent screen warning remains open as a known pre-v2 task.
