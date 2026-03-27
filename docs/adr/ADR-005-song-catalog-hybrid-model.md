# ADR-005: Hybrid song catalog — curated + community submissions

## Status
Proposed (to be implemented in v2)

## Context
There is no free, legal, high-quality API for guitar chords with broad catalog coverage. Options evaluated:

- **Ultimate Guitar:** No public API, ToS prohibits scraping
- **Hooktheory API:** Paid, ~20k songs, high quality but small catalog
- **Chordify API:** Paid, auto-generated chords (lower quality)
- **Spotify API:** Song metadata only (key, BPM, genre) — no chords
- **Musixmatch API:** Lyrics only — no chords
- **Build our own community:** User-submitted content, same model as Ultimate Guitar

## Decision
Adopt a **three-tier hybrid model**:

1. **Official catalog** — songs curated and quality-controlled by us (starts as `songs.json`)
2. **Community submissions** — any authenticated user can submit a song version
3. **Metadata enrichment** — Spotify API auto-fills title, artist, key, BPM, genre, and album art when submitting

Multiple versions of the same song are allowed (same model as Ultimate Guitar). Each version is rated independently. Users see all versions sorted by rating.

Song ownership and visibility:
- Official songs: always public
- User submissions: default public (attributed to submitter)
- Original compositions (songs the user wrote): default **private**, user can opt into public

## Consequences
**Positive:**
- No licensing costs — users own their submissions (same legal model as GitHub)
- Catalog scales with the community, not with our manual work
- Spotify metadata integration reduces friction of submission dramatically
- Multiple versions per song serves different skill levels (easy vs. full arrangement)

**Negative:**
- Requires moderation system (report, flag, quality score)
- Quality variance — community tabs range from excellent to wrong
- Requires authentication before submission (dependency on ADR-003)

**Legal note:** Users submitting content assert they have the right to share it. Terms of service must reflect this. The chord progressions themselves are not copyrightable; specific lyric transcriptions carry more risk — consider chords-only mode as a safer default.
