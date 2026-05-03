# Roadmap v1.x — Remaining Work

What is already shipped is tracked in [STATUS.md](STATUS.md).
This file contains only what is **not yet built**, in priority order.

Last updated: 2026-05-03

---

## Immediate — Tech Debt P1

Fix these before the next Play Store release. Small scope, high impact.

### TD-1: Converters crash on corrupt JSON
**File:** `Converters.kt:20,31`  
`gson.fromJson` has no error handling. A single corrupt row in the database
crashes the app with an unhandled `JsonSyntaxException`. Wrap each call in
`runCatching` and return an empty list on failure.  
**Effort:** ~15 min

### TD-2: Font size not persisted
**File:** `ReaderViewModel.kt`  
`increaseFontSize` / `decreaseFontSize` update in-memory state but never call
`UserPreferences.setFontSize`. The user's preferred font size is lost every time
the app restarts. Read persisted value on ViewModel init; write on every change.  
**Effort:** ~20 min

---

## Next — v1.x Features

### Setlist mode
A Collection can be entered as a Setlist: the reader advances from song to song
with a forward swipe, no return to Home between songs. Designed for rehearsals
and live performance — hands never leave the guitar.

- Order is strict (follows Collection song order)
- Navigation is always forward; back returns to the Collection, not the previous song
- No UI chrome between songs (full reader experience end to end)
- Bottom nav hidden while in Setlist mode

**Depends on:** Collections (done)  
**Effort:** ~1 day

---


## Tech Debt P2 — v1.1

Lower priority, but clean up before v2 work begins.

### TD-3: Static `pendingPreview` in AddSongViewModel
`companion object` holds a mutable `Song?` used as a navigation handoff.
Not thread-safe, no lifecycle awareness. Replace with `SavedStateHandle`.  
**File:** `AddSongViewModel.kt:165`

### TD-4: Missing error handling in PlaylistsViewModel
`removeSongFromPlaylist` silently swallows DAO failures. `deletePlaylist` has
a try-catch — make them consistent.  
**File:** `PlaylistsViewModel.kt`

### TD-5: ensureSeeded() called on every repository method
Runs a `SELECT COUNT(*)` on every public call to `RoomSongRepository`.
Move the seeded check to the repository `init` block.  
**File:** `RoomSongRepository.kt`

---

## v2 — Cloud & AI

Do not start until v1 is stable on Play Store and has real user feedback.
Full context in [ADR-020](adr/ADR-020-v2-roadmap.md).

| Priority | Feature | Gate |
|----------|---------|------|
| 1 | Google Sign-In (Firebase Auth) | Prerequisite for everything below |
| 2 | Cross-device sync (Firestore) | Requires Auth |
| 3 | Remove Ads IAP | Requires Auth (purchase tied to account) |
| 4 | Community chord sharing | Requires Firestore + moderation plan |
| 5 | AI format conversion (Claude API) | Requires Remove Ads IAP |
| 6 | AI song completion (Claude API) | Requires Remove Ads IAP |
| 7 | Tap-a-word chord editor | Independent, but v2 scope |
