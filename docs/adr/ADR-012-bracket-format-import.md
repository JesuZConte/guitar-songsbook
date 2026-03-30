# ADR-012: Song import via bracket format parsing

## Status
Accepted (v1 priority)

## Context
The app is a personal songbook — users bring their own songs. The critical UX question is: how does a guitarist get their songs into the app quickly and without friction?

Three import paths were evaluated:
1. **Tap-a-word editor** (ADR-006) — rich but slow, best for creating from scratch
2. **Community import** — download chord arrangement, add lyrics (requires v2 backend)
3. **Bracket format paste** — paste text with `[chord]` markers, app parses automatically

Most guitarists already have songs in bracket format. Tab sites, WhatsApp groups, text files, printed sheets — the `[Am]Hello [F]darkness` format is the de facto standard for sharing guitar tabs as plain text. Getting 50 songs into the app should take minutes, not hours.

## Decision
**Bracket format import is the primary song entry method for v1.** The tap-a-word editor (ADR-006) becomes a v2 feature for creating/editing from scratch.

### Import flow
```
Add Song → fill metadata (title, artist, key, capo, genre, difficulty)
→ paste raw text with [chord] markers
→ app parses in real time
→ preview in Reader
→ save to songbook
```

### Parser rules

**Chord markers:** `[X]` where X is a valid chord name.
```
[Am]Hello [F]darkness my [C]old [G]friend
→ lyric: "Hello darkness my old friend"
→ chords: Am@0, F@6, C@20, G@24
```

**Section headers:** Line containing only `[Section Name]` with no other text.
```
[Verse 1]        → section type: verse, number: 1
[Chorus]          → section type: chorus, number: 1
[Bridge]          → section type: bridge, number: 1
[Intro]           → section type: intro, number: 1
```
Distinction from chord markers: section headers are alone on the line, chord markers precede lyrics text.

**Chord-only lines:** Lines with only chord markers and whitespace (common for intros/outros).
```
[Am] [F] [C] [G]
→ lyric: "" (empty)
→ chords: Am@0, F@4, C@8, G@12
```

**Plain text lines:** Lines with no brackets — treated as lyrics with no chords.
```
I've come to talk with you again
→ lyric: "I've come to talk with you again"
→ chords: [] (empty)
```

### Edge cases
- Nested brackets `[[Am]]` — treat as `[Am]` (strip extra brackets)
- Unknown chord names `[Xyz]` — treat as chord anyway (user may use custom voicing names)
- Empty lines — section separator, preserved for readability
- Tabs/spaces between chords on chord-only lines — preserve spacing for alignment

### Metadata fields
The paste area handles the song body. Metadata is entered separately:
- Title (required)
- Artist (required)
- Key (optional, auto-detected from first chord if empty)
- Capo (optional, default 0)
- Genre (optional, free text)
- Difficulty (optional: beginner / intermediate / advanced)

## Consequences
**Positive:**
- Fastest way to populate a songbook — paste and done
- Leverages existing content format that guitarists already use
- No learning curve — the bracket format is already the standard
- Parser is technically simple — regex for `\[([^\]]+)\]` patterns
- Enables bulk import — user can paste multiple songs quickly

**Negative:**
- Parsing edge cases (unusual formatting, non-standard section names) may produce imperfect results
- Preview step is essential — user must verify the parse before saving
- Does not support tab notation (numeric fret positions) — only chord names
- Original section structure from the source may not match our model perfectly (e.g., "Pre-Chorus" needs mapping)

**Future:** The tap-a-word editor (ADR-006) complements this as a visual editing tool. Users import via bracket paste, then fine-tune chord positions via tap-a-word if needed.
