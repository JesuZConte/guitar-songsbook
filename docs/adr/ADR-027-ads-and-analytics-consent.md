# ADR-027: Consent for ads and analytics (Google UMP)

## Status
Accepted and implemented (2026-11-24). Follows from [ADR-026](ADR-026-license-compliance-and-scope.md) (checklist item "Ads consent") and the privacy policy draft (`docs/PRIVACY_POLICY.md`, section 3.3).
**Engineering analysis, not legal advice.**

## Context
Today `MainActivity.onCreate` calls `MobileAds.initialize(this)` unconditionally and `BannerAd` (shown only in `HomeScreen`'s bottom bar) calls `loadAd` right away. Firebase Analytics collects with its defaults. There is no consent code.

Google requires publishers serving ads to users in the EEA, the UK and Switzerland to use a certified consent management platform (CMP); Google's own is the **User Messaging Platform (UMP)**, bundled with `play-services-ads` (25.1.0 here), so no new dependency is needed. Without it, Google limits or stops serving ads there, and the privacy policy sentence "we ask for your consent" would be false.

Firebase Analytics also falls under the same rules in those regions: it uses device identifiers for measurement, so it needs consent too. Crashlytics (crash reports with random installation ids, no user text) is treated as legitimate interest in the policy and is not gated.

Constraints: no accounts; the reader never shows ads; the app must keep working fully offline and when the consent form cannot load.

## Decision
1. **Use UMP.** On every cold start, request a consent-information update (`ConsentInformation.requestConsentInfoUpdate`) and, if a form is required, show it (`UserMessagingPlatform.loadAndShowConsentFormIfRequired`). Outside regulated regions UMP reports "not required" and nothing is shown.
2. **Gate on `canRequestAds()`.** `MobileAds.initialize` is called, and `BannerAd` renders/loads, only after UMP says ads can be requested. Until then no ad SDK call is made. If the update call fails, use the last known state UMP cached (`canRequestAds()` is persisted across launches); on a first launch with no state we do not request ads (fail closed).
3. **Gate Firebase Analytics on the same decision.** Analytics collection is disabled by default in the manifest (`firebase_analytics_collection_enabled=false`) and enabled with `setAnalyticsCollectionEnabled(true)` only when UMP allows it (user consented, or consent is not required in their region). Crashlytics stays on.
4. **Privacy options entry.** UMP reports whether a "privacy options" entry point is required (`PrivacyOptionsRequirementStatus`). When it is, Settings shows a "Privacy options" row that calls `showPrivacyOptionsForm`, so the user can change or withdraw consent at any time.
5. **Testable core.** The decisions are pure functions over a small snapshot (`ConsentSnapshot(canRequestAds, privacyOptionsRequired, updateFailed)`) in a class with no Android types, covered by unit tests (TDD). A thin `UmpConsentManager` adapts the real UMP SDK to that snapshot and is verified on the device with UMP's debug geography set to EEA.
6. **Banner layout.** While ads are not allowed, the bottom bar is empty rather than reserving blank space.

## Out of the app (Luis, in consoles)
- AdMob → Privacy & messaging: create and publish a **GDPR message** (and, optionally, US states) for the app. Without it UMP has nothing to show.
- Play Console → Data safety: align with the policy (Analytics, Crashlytics, advertising id) once this ships.

## Alternatives considered
| Option | Why not |
|---|---|
| Disable ads in the EEA/UK | Needs geolocation or locale guessing, loses revenue there, still leaves Analytics unaddressed. |
| Remove AdMob / Analytics | Contradicts the v1 monetization plan; Analytics is how we learn what testers use. |
| Build our own consent dialog | Google only serves personalized ads through a certified CMP; a custom dialog is not accepted. |
| Gate only ads, leave Analytics on | Leaves a compliance gap in the regions where consent is required. |

## Consequences
**Positive:** policy text becomes true; ads and analytics are off until allowed; consent can be withdrawn; core logic is unit tested.
**Negative:** in regulated regions the first launch shows a consent form; analytics from users who decline is lost; a first launch with no network in the EEA shows no ad (acceptable, fails closed). Adds an app start-up network call (async, does not block the UI).

## Verification
- Unit tests for the decision logic: ads allowed / blocked, failed update with and without cached consent, privacy-options visibility.
- On device (Galaxy A50): debug geography EEA with a test device id → form appears, "Decline" → no ad request and no Analytics; "Accept" → banner loads; Settings row reopens the form. Geography "not in EEA" → no form, banner as today.
- `dumpsys gfxinfo` on entering "All songs": no regression vs. the deferred-banner baseline (plan day 32, 2.1).

## Verified on device (Galaxy A50, 2026-11-24)
GDPR message published in AdMob (English default, Spanish extra; Consent / Do not consent / Manage options).
- Normal region: no form, banner loads, no "Privacy options" row in Settings.
- Simulated EEA (debug extra `consent_test_device`): the form appears; **Do not consent** -> no ad request at all; the Settings row **Privacy options** reopens the form; **Consent** -> consent stored (`CONSENT_SIGNAL_SUFFICIENT`), ads initialized and requested (first request returned NO_FILL, a later launch loaded the banner); the choice persists across restarts and the form is not shown again.
- Unit tests: 10 (`ConsentControllerTest`); full suite 220/220.

Not verified on device: a fresh install in the EEA with no cached consent and no network (fail-closed path, covered only by unit tests); that Analytics collection really switches off/on (a single `setAnalyticsCollectionEnabled` call, covered by unit tests of the decision); cold-start time impact (measure in release, plan day 32 task 2.1).
