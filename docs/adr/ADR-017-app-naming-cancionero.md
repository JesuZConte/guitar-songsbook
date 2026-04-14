# ADR-017: App Naming — Cancionero

## Status
Accepted — applied in v1

## Context
The app was originally developed under the working title "Guitar Songbook" / package name `com.guitarapp.songsbook`. Before Play Store submission, a final public name was needed.

Requirements:
- Unique on the Play Store (no direct competitors with the same name)
- Clearly communicates the app's purpose to guitar players
- Memorable and brandable
- Works for a Spanish-speaking audience (primary target) and English-speaking audience

## Alternatives considered

| Name | Verdict |
|------|---------|
| Guitar Songbook | Generic — many similar names on Play Store, no differentiation |
| ChordBook | Clean but "Chord" is extremely crowded on Play Store (Chord!, ChordU, Ultimate Chord Finder, etc.) |
| Songbook Pro | "Songbook Pro" already exists on Play Store |
| Cancionero | Unique on Play Store — only results were niche regional apps ("Cancionero Boliviano", etc.) |
| Cancionero – Chord Songbook | Final choice — Spanish name + English subtitle for discoverability |

## Decision
**Cancionero – Chord Songbook**

- Primary name: `Cancionero` — Spanish for "songbook" or "song collection". Culturally resonant, distinctive, easy to pronounce.
- Subtitle: `– Chord Songbook` — English keywords for Play Store search discoverability.
- Package name (`com.guitarapp.songsbook`) unchanged — renaming the package after Play Store submission would be a breaking change.

## Applied changes
- `res/values/strings.xml`: app name → `Cancionero`
- `HomeScreen.kt` TopAppBar title → `"Cancionero"`
- `AboutScreen.kt` headline → `"Cancionero"`
- Play Store listing title → `Cancionero – Chord Songbook`

## Consequences
- The package name retains the old `guitarapp.songsbook` namespace — acceptable for v1, no user-facing impact
- The brand "Cancionero" should be used consistently in all future marketing, store listings, and UI
- "Guitar Songbook" branding is reserved per the GPL v3 license header — the rename does not affect this reservation
