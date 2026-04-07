# ADR-015: Song Difficulty Field — Usefulness Review

## Status
Deferred — field exists in the data model, no UI decision made yet

## Context
The `Song` data model includes a `difficulty` field. The intended use was to let users classify their songs (e.g. Beginner / Intermediate / Advanced) and potentially filter by difficulty on the Home screen.

A question was raised: **if the user is the one adding the song, how meaningful is it for them to classify their own difficulty level?**

This is a valid concern. Difficulty is highly personal:
- A beginner might add "Wonderwall" and call it Hard. An intermediate player calls it Easy.
- The same song can be hard to play and easy to sing, or vice versa.
- There is no objective source of difficulty for a personal repertoire.

In contrast, in a community/shared song database (planned for v2), difficulty ratings submitted by many users could be averaged into a meaningful score.

## Options

1. **Keep as-is (personal label)** — treat difficulty as a personal tag, not an objective score. "Hard for me right now" is a valid use case. A user can filter their own repertoire by difficulty to decide what to practice.

2. **Rename to "Practice level" or "My level"** — makes the personal nature explicit in the UI, reducing the expectation of objectivity.

3. **Remove for v1, reintroduce in v2** — remove from the Add Song form for now. Keep the database column so no migration is needed. Re-expose it when community ratings are available and the field has more meaning.

4. **Replace with a "Mastery" tag** — instead of difficulty, track how well the user knows the song: Not started / Learning / Comfortable / Mastered. More actionable for a personal repertoire manager.

## Recommendation
Option 4 (Mastery) is the most aligned with the product vision of a personal repertoire manager. Option 3 is the safest short-term choice. Option 1 is fine for v1 if no change is made.

## Consequences
- No breaking change required for any option (the DB column exists and can be repurposed)
- Decision can safely be deferred to v2 planning
