# ADR-004: Firestore as the remote database (v2)

## Status
Proposed (to be implemented in v2)

## Context
When v1 goes remote, we need a database. Options evaluated: Firebase Firestore, MongoDB Atlas, MySQL (Cloud SQL on GCP).

| | Firestore | MongoDB Atlas | Cloud SQL (MySQL) |
|---|---|---|---|
| GCP native | Yes | No | Yes |
| Android SDK | First-class | Needs backend layer | Needs backend layer |
| Offline sync | Built-in | Manual | Manual |
| Auth integration | Firebase Auth, seamless | Manual | Manual |
| Free tier | Generous | OK | Always-on cost |
| Schema flexibility | High (NoSQL) | High (NoSQL) | Low (relational) |

## Decision
Use **Firebase Firestore** as the remote database.

Proposed data model:
```
/songs/{songId}           — curated catalog + community submissions
/users/{userId}/favorites — user's favorited song IDs
/users/{userId}/playlists/{playlistId} — user playlists
/playlists/{playlistId}/songs — songs in a playlist
```

Room stays as the local persistence layer. Firestore replaces `AssetSongRepository` as the remote source. The `SongRepository` interface is unchanged.

## Consequences
**Positive:**
- No backend API server needed — Firestore SDK talks directly from the app
- Offline persistence built-in — critical for musicians in low-connectivity venues
- Firebase Auth UIDs map directly to Firestore security rules
- Single Google ecosystem — one billing account, one console

**Negative:**
- NoSQL requires denormalization — song data may be duplicated across documents
- Firestore pricing scales with reads — a viral app could get expensive without careful query design
- Less flexible for complex relational queries than SQL

**Cost mitigation:** paginate song list queries, cache aggressively in Room, avoid listening to real-time updates where snapshots are sufficient.
