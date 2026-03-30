# ADR-010: Sharing model — chords public, lyrics private

## Status
Accepted

## Context
Chord progressions are not copyrightable — a sequence of G - D - Em - C is not owned by anyone. This has been tested in court multiple times. A user's chord *arrangement* (which chords go where, voicings, capo position) is their creative work.

Lyrics, however, are copyrighted by the songwriter or publisher. Displaying them without a license is infringement.

We needed a sharing model that lets users contribute to a community while staying legally clean.

## Decision
Two-tier content model:

### Private (local only)
- Complete songs: lyrics + chords + metadata
- Stored locally (Room database) or in user's cloud account (v2)
- Never transmitted to other users
- Never indexed or searchable by others

### Public (community)
- Chord arrangements: section structure + chord positions + metadata (key, capo, tempo, difficulty)
- No lyrics — the lyric text lines are stripped before sharing
- Searchable by song title and artist
- Ratable by other users
- Downloadable — other users can import the chord skeleton into their songbook and add their own lyrics

### How a shared arrangement looks
```
Song: Wonderwall
Artist: Oasis
Key: Em  Capo: 2  Difficulty: Intermediate

[Verse 1]
Em7  G  Dsus4  A7sus4
Em7  G  Dsus4  A7sus4

[Chorus]
C  D  Em
C  D  Em
```

No lyrics, just the chord structure. The receiving user adds their own lyrics (or plays from memory).

## Consequences
**Positive:**
- Zero copyright risk on shared content — chord progressions are not copyrightable
- Users contribute their arrangements (creative work they own)
- Community grows organically around arrangement quality, not lyric availability
- Legal clarity — DMCA takedown process only needed for edge cases

**Negative:**
- Shared arrangements are less useful without lyrics (many users want the full package)
- Users must manually add lyrics after importing a community arrangement
- Moderation still needed for metadata (song titles, artist names — these aren't copyrightable but could be used for spam)
