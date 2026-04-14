# ADR-018: Key Transposition Deferred to v2

## Status
Deferred — not implemented in v1

## Context
Key transposition is the ability to shift all chords in a song up or down by a number of semitones. It is one of the most requested features in chord app reviews on the Play Store.

Common use cases:
- **Capo usage**: user plays with a capo at fret 2, wants to see the chords as if open position (transpose down 2)
- **Voice range**: singer needs the song in a different key to match their range
- **Instrument tuning**: dropped tuning or alternate tuning changes the effective key

v1 ships with **chord notation switching** (American: C, D, E vs Latin: Do, Re, Mi), which is a display-only change — no pitch change. Transposition is a different, more complex feature.

## Why deferred

1. **Song data model complexity**: transposing requires understanding that `C#` and `Db` are enharmonic equivalents and choosing which spelling to output based on the target key signature. This is non-trivial logic.

2. **Persistence question**: should the transposed key be saved per song, per session, or be ephemeral? The answer changes the data model. Saving it per song makes sense (the user always plays this song capoed at 2), but it requires a new DB column and migration.

3. **UI complexity**: a semitone stepper (+/- buttons) or a key picker dropdown needs to be integrated into the Reader toolbar without cluttering it.

4. **v1 scope**: v1 goal is a working personal songbook. Transposition is a quality-of-life feature, not a blocker for first release.

## v2 plan

Implement transposition as a per-song saved offset:
- Add `transposeSteps: Int` column to `songs` table (default 0, DB migration required)
- Transpose all chord tokens at render time in `ChordNotation` utility
- Add +/− stepper in `ReaderBottomBar`
- Handle enharmonic spelling based on target key (prefer flats for flat keys, sharps for sharp keys)

## Consequences
- v1 users who need transposition must manually re-write chords in the correct key when adding a song
- No breaking change required — the DB column does not exist yet, so adding it in v2 is a straightforward migration
- `ChordNotation` utility is already isolated, making it the right place to add transposition logic in v2
