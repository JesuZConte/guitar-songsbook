# ADR-020: Cancionero v2.0 — Roadmap and Architecture Vision

## Status
Proposed — to be implemented after v1 stable release

## Context
v1 ships as a fully offline, local-only personal songbook. v2 introduces cloud
infrastructure, community features, and monetization. This ADR consolidates all
prior v2 decisions into a single reference document.

Prior ADRs that feed into this vision:
- ADR-003: Firebase Auth (Google Sign-In)
- ADR-004: Firestore as remote database
- ADR-005: Hybrid song catalog (curated + community)
- ADR-010: Chords public, lyrics private sharing model
- ADR-016: Remove Ads IAP
- ADR-018: Key transposition

---

## v2.0 Feature Set

### 1. Google Sign-In (Firebase Auth)
Single-tap authentication using the user's existing Google account.
No username/password forms. Firebase Auth UIDs map directly to Firestore
security rules.

**Triggers this feature:** User feedback requesting cross-device sync, OR
community song submission going live — whichever comes first.

### 2. Cross-device Sync (Firestore)
User's personal library (songs, favorites, playlists) syncs across devices
via Firestore. Room remains the local persistence layer — Firestore is the
remote source of truth.

Firestore data model:
```
/users/{userId}/songs/{songId}         — user's personal songs
/users/{userId}/favorites              — favorited song IDs
/users/{userId}/playlists/{playlistId} — user playlists
```

Offline-first contract is preserved: the app works fully without internet.
Sync happens opportunistically when connectivity is available.

### 3. Remove Ads + AI Features (IAP)
A one-time in-app purchase that unlocks two things simultaneously:
- Removes the Home screen banner ad permanently
- Unlocks AI-powered song format conversion (see below)

Bundled into a single purchase to minimize friction. Requires Firebase Auth
so the purchase can be tied to a user account and restored on new devices.

### 4. Community Chord Sharing
Users can share the chord structure of a song publicly. Lyrics are always
stripped before sharing — only chord arrangement + metadata is transmitted.

Shared arrangement format (see ADR-010):
```
Song: Wonderwall — Artist: Oasis
Key: Em  Capo: 2  Difficulty: Intermediate

[Verse 1]
Em7  G  Dsus4  A7sus4

[Chorus]
C  D  Em
```

Other users can import the chord skeleton and add their own lyrics locally.
This model carries zero copyright risk (chord progressions are not copyrightable).

### 5. AI-Powered Song Completion (Claude API)
Many transcriptions only include chords for the first verse and chorus, leaving
the rest of the song empty. This feature detects incomplete sections and suggests
chord completions based on the patterns already present in the song.

How it works:
- User opens a song with sections missing chords
- A "✨ Complete with AI" button appears in the editor for those sections
- Claude analyzes the existing chord patterns and aligns them to the remaining lyrics
- The result is presented as an editable suggestion, never applied automatically
- UI makes clear: "AI suggestion — review and adjust as needed"

Key design constraint: the output is a starting point, not a final answer.
Most songs reuse chord progressions across verses, making this highly effective
in practice (~80% accuracy). The user corrects the rest manually — still far
less work than transcribing from scratch.

This feature is unlocked by the Remove Ads IAP (same as format conversion).

### 6. AI-Powered Song Format Conversion (Claude API)
Users often copy songs from the web in "over/under" format (chords on one
line, lyrics on the next). v1.4 handles this locally with a heuristic
algorithm for standard cases. For complex or non-standard formats, a Claude
API call converts any pasted text to the bracket format automatically.

This feature is unlocked by the Remove Ads IAP. Free users get the local
heuristic (covers ~80% of cases). Premium users get the AI fallback for
edge cases. The per-call API cost is absorbed by the IAP revenue — free
users never generate a cost.

Integration: a "✨ Convert with AI" button appears in the Text input mode
when the local auto-detect fails or produces poor results.

### 6. Key Transposition
Shift all chords in a song up or down by semitones directly in the Reader.
Saved as a per-song offset (`transposeSteps: Int`) in the database.

Implementation notes (see ADR-018):
- New `transposeSteps` column on `songs` table (DB migration required)
- Transposition logic lives in `ChordNotation` utility
- +/− stepper in the Reader toolbar
- Enharmonic spelling based on target key (flats vs. sharps)

---

## Architecture Changes Required

| Component | v1 | v2 change |
|---|---|---|
| Auth | None | Firebase Auth (Google Sign-In) |
| Remote DB | None | Firestore |
| Local DB | Room (songs, playlists) | Room + Firestore sync layer |
| Repository | `AssetSongRepository` | New `FirestoreSongRepository` implementing same interface |
| Song model | Local only | Add `remoteId`, `syncedAt`, `isPublic` fields |
| Monetization | Banner ad only | Banner ad + Remove Ads IAP |

The `SongRepository` interface is unchanged — the new Firestore implementation
slots in behind the same interface, keeping ViewModels untouched.

---

## Implementation Order

The features have hard dependencies. Recommended sequence:

```
1. Firebase Auth (Google Sign-In)
        ↓
2. Firestore sync (personal library)
        ↓
3. Remove Ads IAP
        ↓
4. Community chord sharing
```

Transposition (ADR-018) is independent of all of the above and can be
implemented at any point during v2 development.

---

## Consequences

**Positive:**
- Single Google ecosystem — one billing account, Firebase + GCP
- No custom backend API needed — Firestore SDK talks directly from the app
- Offline-first contract preserved — Firestore has built-in offline persistence
- Community scales the catalog without licensing costs

**Negative:**
- Firestore pricing scales with reads — requires careful query design and
  aggressive Room caching to avoid surprise costs
- Community moderation needed — report/flag system required before public launch
- Google Sign-In as sole auth method means no account recovery without Google
- v1 user data (songs added before v2) must be migrated to Firestore on first
  sign-in — migration logic required

---

## Open Questions (to resolve before implementation)

1. **Migration of v1 local data:** When a v1 user signs in for the first time
   in v2, do we automatically upload their local songs to Firestore? This should
   be opt-in — not all users may want their arrangements in the cloud.

2. **Firestore cost guardrails:** At what song catalog size do per-read costs
   become a concern? Establish a budget alert in Firebase console before launch.

3. **Moderation for community sharing:** Who reviews flagged content? Manual
   moderation is fine at small scale but needs a plan.