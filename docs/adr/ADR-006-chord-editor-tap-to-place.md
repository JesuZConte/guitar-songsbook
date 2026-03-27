# ADR-006: Chord editor — tap-a-word model for mobile

## Status
Proposed (to be implemented in v2)

## Context
Adding chords to a song requires mapping each chord to a character position in a lyrics line. Exposing this as a number input would be unusable on mobile. We needed a mobile-first interaction model.

Alternatives evaluated:
- **Free text format** (e.g. `[G]Today is gonna [D]be the day`) — familiar to power users, error-prone, requires parsing
- **Desktop-style click-above-text editor** — good on web, poor on mobile (small tap targets)
- **Tap-a-word model** — user taps any word in the lyric line, chord picker appears, position calculated automatically

## Decision
Use the **tap-a-word** model as the primary chord entry interaction.

### Flow — existing song (with lyrics source)
```
Search song → Spotify fills metadata → paste lyrics
→ App splits into sections (Verse, Chorus, etc.)
→ Tap any word → chord picker → chord placed at word's start position
→ Preview in reader mode → save
```

### Flow — original composition
```
Fill metadata manually (no Spotify lookup)
→ Define sections (+ Add Section: Verse / Chorus / Bridge / Custom)
→ Write lyrics per section
→ Tap any word → chord picker → chord placed
→ Preview → save as Private (default) or Public
```

### Chord picker design
- Shows recently used chords at top (most songs use 4–6 chords)
- Root note grid (A–G)
- Modifier row (m, 7, maj7, sus2, sus4, add9, dim, aug)
- No keyboard required for standard chords

### Position calculation
```kotlin
val position = lyricLine.text.indexOf(tappedWord)
```
The internal `ChordPosition(chord, position)` model is unchanged. The editor abstracts character positions entirely from the user.

## Consequences
**Positive:**
- No character counting — the single biggest pain point of existing mobile tab editors
- Works for all skill levels — no special syntax to learn
- Internal data model unchanged — `ChordPosition` stays as-is
- Recently used chords at top reduces taps for typical songs dramatically

**Negative:**
- Tap targets on short words (articles: "a", "the", "I") are small — may need minimum tap area padding
- Words that appear multiple times in a line require disambiguation (tap the correct occurrence)
- Does not support chord placements mid-word (rare in practice)

**Competitive note:** Ultimate Guitar's mobile editor is widely considered the worst part of their product. A genuinely good mobile chord editor is a meaningful differentiator.
