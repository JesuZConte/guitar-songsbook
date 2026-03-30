# ADR-009: Product vision — personal songbook, not a lyrics database

## Status
Accepted

## Context
During development, a copyright analysis revealed that shipping lyrics in the app (even the 11 seed songs in `songs.json`) creates legal risk. Google Play is strict about copyrighted content and apps have been removed for displaying unlicensed lyrics.

This forced a fundamental product question: what IS this app?

## Decision
Guitar Songbook is a **personal repertoire manager for guitarists**, not a lyrics/tab database.

### Vision
The most intuitive personal songbook a guitarist can have — as natural as a paper notebook, but smarter.

### Mission
Give every guitarist a beautiful, private, offline-first tool to organize their repertoire, read chords while playing, and share arrangements with a global community — all without legal friction or intrusive ads.

### Core principles

1. **Your songs, your notebook** — the app never provides copyrighted content. You bring your music, we make it beautiful.
2. **Chords are free, lyrics are private** — chord arrangements can be shared; lyrics stay personal.
3. **Play any notation** — American or Latin, your choice. Music is universal.
4. **Stage-ready** — readability in dark venues, outdoor sun, and parties is non-negotiable.
5. **Respect the guitarist** — no auto-scroll, no intrusive ads in the Reader, no dark patterns. The app works for musicians, not against them.

### What the app IS
- A personal digital songbook (like Notion for guitarists)
- An organization tool for your repertoire
- A chord arrangement sharing platform (community, no lyrics)
- A beautiful reading experience optimized for live playing

### What the app is NOT
- A lyrics database
- A tab repository
- A music streaming or playback tool

## Consequences
**Positive:**
- Legally clean — zero copyright risk in the app itself
- Stronger product identity — clear differentiation from Ultimate Guitar, Cifra Club, etc.
- Better user story — "your personal songbook" is more compelling than "another tabs app"
- Opens the door to premium monetization (users pay for tools, not content)

**Negative:**
- The 11 seed songs in `songs.json` must be replaced with either royalty-free examples or an empty state with onboarding
- New users face a "cold start" — empty songbook on first launch. Onboarding must address this.
- Community features become essential earlier (users want content to discover)

**Supersedes:** ADR-005 (hybrid song catalog model) — the curated catalog approach is replaced by user-generated content only.
