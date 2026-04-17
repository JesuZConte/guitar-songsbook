# ADR-019: Collections View — Home Screen Redesign

## Status
Proposed — deferred to v1.1

## Context
The current Home screen shows a flat list of all songs. Early testers mistook this
for a global/shared song database rather than a personal collection. A subtitle fix
("My personal songbook") was applied as a short-term patch (Day 16), but the root
UX problem remains: landing on a raw list gives no sense of personal ownership or
organisation.

## Decision
Replace the Home screen flat song list with a **Collections view** in v1.1.

### What changes
- The bottom nav tab "Playlists" is renamed to **Collections** (or **Library**).
- The Home screen landing page shows Collection cards instead of a song list.
- Two default Collections ship with the app:
  - **Traditional Songs** — the three public domain seed songs (Amazing Grace,
    Greensleeves, Scarborough Fair). These are public domain in all jurisdictions
    (oldest dates to the 16th century). Our chord arrangements were written from
    scratch, so no copyright risk applies.
  - **All Songs** — a system collection that always shows the user's full library.
- User-created Collections work as Playlists do today (no data model change needed,
  just a rename and UX restructure).

### What stays the same
- The Room data model (`Playlist` / `PlaylistEntity`) requires no migration — only
  a display rename.
- Search and filtering live inside the "All Songs" collection view.
- The Song Reader is untouched.

## Rationale
- A Collections landing page makes the personal nature of the app immediately clear.
- It maps to how guitarists think: "my rock songs", "my gig setlist", "songs I'm
  learning" — not a flat alphabetical list.
- Shipping Traditional Songs as a default collection gives new users something to
  explore immediately and demonstrates the Reader without requiring them to add a
  song first.
- Merging Playlists into Home reduces the nav from 3 tabs to 2 (Home, Favorites),
  simplifying the information architecture.

## Consequences
- Requires a dedicated implementation day (estimated: 1 day).
- The "Playlists" tab disappears from bottom nav — existing playlist data is
  preserved, just surfaced differently.
- Empty state on first launch becomes a Collections view with the Traditional Songs
  card already present, eliminating the blank-slate problem.
- Must be implemented before v1.1 release; not required for initial Play Store launch.