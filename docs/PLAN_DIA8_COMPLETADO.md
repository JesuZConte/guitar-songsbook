# Day 8 — Completed

## What we built

### 1. Copyright analysis + product pivot
- Discussed copyright risks of shipping lyrics (prompted by Gemini conversation)
- Decision: pivot from "lyrics database" → **personal songbook tool** (UGC model, user enters their own songs)
- This is legally clean: chord progressions not copyrightable, lyrics are the user's own responsibility
- Documented in ADR-009 (vision), ADR-010 (chords public / lyrics private), ADR-011 (notation switching), ADR-012 (bracket format)

### 2. Bracket format parser (`BracketParser.kt`)
- Converts `[chord]lyrics` text into the app's internal `SongSection/SongLine/ChordPosition` model
- Handles section headers (`[Verse 1]`), inline chords, chord-only lines, plain text
- Auto-numbers repeated section types (`[Verse]` → Verse 1, Verse 2, …)
- 25 unit tests, all passing

### 3. Chord notation switching (`ChordNotation.kt`)
- American (A–G) ↔ Latin (La, Si, Do, Re, Mi, Fa, Sol) converter
- `UserPreferences.kt` — SharedPreferences wrapper (default: American)
- Applied at display layer in `SongReaderScreen` and `PreviewReaderScreen`
- 13 unit tests, all passing

### 4. Add Song flow
- `AddSongScreen` — metadata fields (title*, artist*, key, capo, genre, difficulty) + large paste area
- `AddSongViewModel` — parses bracket text, auto-detects key from first chord, saves to Room
- `PreviewReaderScreen` — lightweight reader for pre-save preview, reuses HorizontalPager
- Routes: `add_song`, `preview`
- FAB on HomeScreen triggers the flow

### 5. Public domain seed songs
- Replaced 11 copyrighted songs with 3 public domain folk songs:
  - Amazing Grace
  - Greensleeves
  - Scarborough Fair
- Only applies on fresh install (seeding logic: `count() == 0`)

### 6. Card depth polish
- All song/playlist cards: elevation 2dp → 4dp + `BorderStroke(0.5dp, outlineVariant)`
- Fixes the "flat" look reported on Day 7

### 7. Tech Lead review fixes
- `LineContent`: guard against empty text lines (whitespace pollution)
- `SongHeader`: guard against blank key field
- `PreviewReaderScreen` back button: clears `pendingPreview` + fallback `popBackStack` if null
- `AddSongScreen` capo field: numeric keyboard type

---

## Test results
- 38 unit tests — all passing
- `BracketParserTest`: 25 tests
- `ChordNotationTest`: 13 tests

---

## Known gaps going into Day 9
- No Settings screen — notation toggle (UserPreferences exists, no UI)
- No Edit/Delete for user-added songs
- AdMob integration still pending (deferred from Day 8)
- `pendingPreview` static pattern is a workaround — proper fix would use SavedStateHandle (low priority for v1)

---

# Day 9 — Plan

## Goal
Settings screen + Edit/Delete songs + AdMob integration

---

## Task list

### 1. Settings screen
- New route: `settings`
- Notation toggle: American / Latin (reads/writes `UserPreferences`)
- Gear icon in HomeScreen TopAppBar → navigate to Settings
- Simple, one-screen: no need for Preferences library

### 2. Edit song
- Long-press or swipe on a song card → edit option
- Reuse `AddSongScreen` in edit mode (pre-fill fields from existing song)
- `SongRepository.updateSong()` → `SongDao.update()`
- Only user-added songs should be editable (seed songs are read-only, or treat all as editable for simplicity)

### 3. Delete song
- Swipe-to-dismiss or delete icon on song card
- `SongRepository.deleteSong()` → `SongDao.deleteById()`
- Confirmation snackbar with Undo action (`Scaffold.snackbarHostState`)

### 4. AdMob integration
- Add AdMob dependency (`play-services-ads`)
- Banner ad at bottom of `HomeScreen` (above bottom nav)
- Test ad unit ID for dev, real ID added before release
- See `docs/admob-setup.md` for account setup steps

### 5. Tech Lead review + full test run

---

## Priority order
Settings → Delete → Edit → AdMob → Review

Settings and Delete are fast. Edit is medium (reuse AddSongScreen). AdMob is isolated.