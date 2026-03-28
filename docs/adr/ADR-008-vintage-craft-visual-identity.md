# ADR-008: Vintage craft visual identity — brand in the frame, clarity in the content

## Status
Accepted

## Context
The app needed a visual identity before Day 7 (UI polish). The default Android Studio purple template has no personality. The product is inspired by a physical paper songbook — a guitarist's well-worn book of chords. We needed to decide how far to take that metaphor.

The app is used in challenging reading conditions:
- Dark venues (bars, campfires, stages) — screen is the only light source
- Outdoor daylight (parks, barbecues) — high glare, low contrast
- Social settings (parties) — phone at arm's length, multiple readers, quick glances
- Solo practice — close distance, comfortable conditions

## Decision
Adopt a **"vintage craft"** aesthetic: warm, earthy, acoustic-instrument-inspired — but with strict readability rules.

### Core Principle: brand in the frame, clarity in the content

The vintage personality lives in the **UI chrome** — navigation bars, cards, headers, splash screen, branding. The **Reader content area** (lyrics + chords) prioritizes pure readability with maximum contrast and clean monospace type.

Think: leather-bound songbook with a beautiful cover. Inside, the pages are clean black text on plain paper.

### Color Palette

**Light theme — aged paper under warm light:**
- Background: warm cream/ivory (not stark white)
- Cards: slightly warmer than background
- Primary: deep amber/burnt orange — guitar wood finish
- Secondary: warm brown — leather strap, fretboard
- Accents: muted gold — brass tuning pegs, capo hardware
- Reader text: near-black on cream — high contrast, WCAG AA compliant (4.5:1 minimum)

**Dark theme — stage lighting / late night rehearsal:**
- Background: very dark warm brown (not pure black)
- Cards: slightly lighter, like a dimly lit page
- Primary: warm amber/golden — like a spotlight
- Secondary: soft cream
- Reader text: light cream on dark — high contrast, no bright white (eye strain on OLED)

**Rule: no cool grays, no pure black, no pure white. Everything has warmth.**

### Typography

Two font families working together:
- **Headings** (song titles, screen titles): Serif font (Merriweather, Lora, or similar) — like the title page of a printed songbook
- **Body** (artist names, metadata, UI labels): Clean sans-serif — modern and readable
- **Chords and lyrics** (Reader only): Monospace — non-negotiable for chord alignment. No decorative fonts for content people read while playing

### What NOT to do
- No paper textures as background images — warmth comes from color, not JPEGs
- No fake shadows or page curl effects
- No handwriting fonts for body text
- No sepia filters
- No decorative elements in the Reader content area

### Difficulty indicator
Visual dots instead of colored text: `●○○` (beginner), `●●○` (intermediate), `●●●` (advanced) — like a rating in a printed catalog.

## Consequences
**Positive:**
- Strong brand identity that differentiates from generic music apps
- Readability-first approach ensures usability in all lighting conditions
- Warm tones reduce eye strain during long practice sessions
- Consistent metaphor (physical songbook) guides all future design decisions

**Negative:**
- Custom font files increase APK size slightly (~100-200KB per font weight)
- Warm palette limits future color choices — cool-toned features would clash
- `dynamicColor` (Material You) disabled — the app won't adapt to the user's wallpaper colors, but this is intentional for brand consistency

**Design test:** Every UI element should pass the question: "Would this feel at home in a premium leather-bound songbook?" If yes, keep it. If it feels like a generic Android app, redesign it.
