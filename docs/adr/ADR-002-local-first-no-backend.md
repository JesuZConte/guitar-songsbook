# ADR-002: Local-first architecture, no backend for v1

## Status
Accepted

## Context
The app needs a song catalog. Options were: (a) build a backend API from day one, (b) ship with a local curated catalog and add remote data in v2.

## Decision
Launch v1 fully offline with a curated `songs.json` seeded into Room on first launch. No network calls, no backend, no authentication.

## Consequences
**Positive:**
- Zero infrastructure cost and complexity for v1
- App works without internet (critical for guitarists in venues, rehearsal rooms)
- Faster time to ship — no backend work blocks the mobile app
- Real user feedback before committing to a backend architecture

**Negative:**
- Song catalog is limited to what we manually curate
- User data (favorites, playlists) does not sync across devices
- Updating songs requires an app update

**Migration path (v2):** Firebase Firestore with offline persistence enabled. Room stays as the local cache layer — Firestore becomes the source of truth. The Repository interface means the UI layer changes zero lines of code.
