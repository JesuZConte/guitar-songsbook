# ADR-024: Song import from .docx files

## Status
Exploring — blocked on feasibility validation (see Open Questions)

## Context
A tester gave feedback that they keep their songs in `.docx` and `.pdf` files rather than plain text. PDF was already ruled out as impractical (arbitrary text positioning, no reliable way to recover character-level alignment between chords and lyrics). `.docx` looked more promising to revisit because it's a ZIP of XML rather than a rendering format, so plain-text extraction is much more tractable than PDF.

The app already supports song import via bracket-format paste (`[Am]Hello [F]darkness`, see [ADR-012](ADR-012-bracket-format-import.md)). The tester's files use a different convention: **chords on their own line, aligned above the lyric line by whitespace** — the classic "Text" format used by tab sites (Ultimate Guitar, etc.) and the same format Luis already converts manually via Claude into song JSON (see `project_smart_import` memory / WhatsApp workflow).

## Decisions made so far (in this exploration)

1. **Entry point:** open the `.docx` file directly from the app (file picker), not just paste text. More friction-free for the tester than "open in Word, copy, paste."
2. **Extraction approach:** lightweight custom parser — unzip the `.docx` and read `word/document.xml` with Android's built-in `XmlPullParser`, pulling text out of `<w:t>` runs per paragraph. Rejected Apache POI (`poi-ooxml`): known to be a poor fit for Android (java.awt / StAX dependencies, significant APK size increase, historically fragile on Android runtimes).
3. **UI reuse:** extracted text pre-fills the *same* paste textarea used for bracket-format import today, rather than a new screen. Gives the user an escape hatch to manually fix text before parsing if extraction garbles spacing (e.g. tabs).
4. **Parser selection:** auto-detect between the existing bracket-format parser and a new "chords above lyrics" parser based on the content (presence of inline `[Chord]word` patterns vs. separate chord/lyric line pairs). No manual toggle.
5. **Multi-song files:** tester's `.docx` files can contain multiple songs in one document (not one song per file like the current JSON/WhatsApp workflow). This adds a song-boundary-detection problem on top of the per-song parser. Candidate signal: explicit page breaks between songs, since that's how people naturally structure a multi-song document — more reliable than guessing from blank lines or bold text. Not yet designed in detail.

## The core open risk

The "chords above lyrics" parser works by reading each chord's **character column** in the chord line and placing it at the same character column in the lyric line below. That mapping is only meaningful if the source text was authored with literal space characters at a fixed width (e.g., pasted from a tab site in a monospace font) — not by eyeballing alignment in Word's default proportional font.

If the tester typed chords freehand in Word using a proportional font, the spaces that make the text *look* aligned on screen will not correspond to real character-column alignment once extracted. We'd extract technically-correct text with useless chord positions — the same wall that ruled out PDF, just less obvious up front, since docx text extraction itself is easy.

**This blocks writing a full design/spec.** Nothing else in this ADR should be treated as committed until this is checked against real files.

## Next steps (resume here)

1. Get 1-2 real `.docx` files from the tester — ideally one typed freehand and one copy-pasted from a tab site, if both exist.
2. Unzip and inspect `word/document.xml` (or just extract plain text and eyeball it) to check whether chord-line columns actually line up with lyric-line columns character-for-character.
3. If alignment holds: proceed to full brainstorming/spec via `superpowers:brainstorming`, phased as recommended — **Phase 1: single-song extraction + chords-above-lyrics parser**, proven against the real sample files, before designing multi-song splitting or auto-detect refinements on top.
4. If alignment does not hold: this import path is not viable as designed; would need a fundamentally different approach (e.g., manual chord placement via the tap-to-place editor from [ADR-006](ADR-006-chord-editor-tap-to-place.md) after importing plain lyrics only) or drop the idea.

## Consequences
**Positive (if viable):** meaningfully lowers the barrier for testers/users who keep songs in Word docs; no new heavy dependencies if the lightweight XML approach works; reuses existing paste-preview UX.

**Negative / risk:** alignment assumption is unverified; multi-song splitting adds real complexity; scope crept toward the more ambitious option at every branch (file-open vs. paste, auto-detect vs. toggle, multi-song vs. single-song) during exploration — worth re-confirming with the user once the core risk is resolved, rather than building all of it on spec.