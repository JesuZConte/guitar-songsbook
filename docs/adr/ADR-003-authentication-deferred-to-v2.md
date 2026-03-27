# ADR-003: Authentication deferred to v2

## Status
Accepted

## Context
We considered adding Google Sign-In from day one to enable cross-device sync and user-submitted content. However, v1 is fully local and has no features that require knowing who the user is.

## Decision
No authentication in v1. When added in v2, use **Firebase Authentication with Google Sign-In** exclusively.

Rationale for Firebase Auth + Google:
- Single tap sign-in — no username/password forms
- Every Android user already has a Google account
- Seamless integration with Firestore (security rules tied to `request.auth.uid`)
- No need to store or manage passwords

## Consequences
**Positive:**
- v1 ships faster with zero auth complexity
- No premature infrastructure for a feature users haven't asked for yet
- Google Sign-In has the best UX on Android

**Negative:**
- User data (favorites, playlists) is lost if the app is uninstalled before v2
- Cannot attribute user-submitted songs to an account until v2

**What triggers the v2 auth work:** user feedback requesting cross-device sync, OR the song submission feature (ADR-006) going live — whichever comes first.
