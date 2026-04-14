# ADR-016: AdMob Banner Monetization

## Status
Accepted — implemented in v1

## Context
Cancionero is a personal productivity tool for guitarists. A monetization strategy was needed before Play Store submission. The main options were:

1. **Paid app** — one-time purchase on Play Store
2. **Freemium / IAP** — free with in-app purchases to unlock features
3. **Subscription** — recurring fee for premium access
4. **Free + banner ads** — free download, AdMob banner shown at bottom of screen
5. **No monetization** — fully free, no ads

The app is v1 with a small initial user base and no community features yet. The v2 roadmap includes Firestore sync and community song submissions, which would justify a subscription model.

## Decision
Free download with a **single AdMob banner ad** at the bottom of the Home screen for v1.

- Ad provider: **Google AdMob** (`play-services-ads`)
- Ad format: **Banner** (320×50 dp, `AdSize.BANNER`)
- Placement: bottom of `HomeScreen` only (not in the Song Reader — reading experience is uninterrupted)
- App ID: `ca-app-pub-8804586949821046~8761207364`
- Banner Unit ID: `ca-app-pub-8804586949821046/3185447057`

## Rationale

- **Paid app** has high friction for an unknown v1 app — users won't pay without social proof or reviews
- **Subscription** requires enough value to justify recurring cost; v1 feature set is not there yet
- **IAP/Freemium** requires designing a feature gate, which adds complexity and risks feeling exploitative on a simple utility
- **Banner only** is the lowest friction option: free to download, one non-intrusive ad, easy to remove or replace in v2
- **Reader is ad-free** by design — showing an ad while a user is playing guitar would degrade the core use case

## v2 consideration
When Firebase Auth + Firestore sync ships, a **remove ads** IAP or a **Pro subscription** becomes viable. The banner can be hidden for paying users with a single flag check.

## Consequences
- AdMob SDK adds ~1 MB to APK
- `AD_ID` permission must be declared in the manifest (Android 13+)
- Data safety section must declare Device ID collection for advertising purposes
- Banner revenue at v1 scale will be negligible — goal is infrastructure, not income
