# ADR-025: Song import from .txt files

## Status
Proposed — **prioritized** (2026-11-23), ahead of [ADR-024](ADR-024-docx-song-import.md). Phase 0 completed the same day with two real tester files: viable, but the existing parser has bugs that must be fixed first (Phase 1a).

## Context
A closed-testing tester asked whether the app accepts `.txt` files, since that is how they keep their songs.

Today the only file format the app imports is **JSON**:
- In-app picker: `HomeScreen.kt` → `importLauncher` (`GetContent`, launched with `"application/json"`) → `HomeViewModel.importSongFromJson`.
- External open: `AndroidManifest.xml` `VIEW` intent-filter for `application/json` only → `MainActivity.handleIncomingIntent` → same `importSongFromJson`.

Plain text, however, is **already supported by paste**: `AddSongScreen` accepts raw text, and "Detectar Formato" (`AddSongViewModel.detectAndConvertFormat`) runs `OverUnderConverter` (chords-above-lyrics → bracket format) before `BracketParser` ([ADR-012](ADR-012-bracket-format-import.md)). Smart import conflict detection (duplicate titles → merge as version or save separately) is already wired into the save path.

So a `.txt` import is mostly a **new entry point into an existing pipeline**, not a new parser. That makes it far cheaper and lower-risk than `.docx` (ADR-024), which needs ZIP/XML extraction *and* carries the proportional-font alignment risk.

**Why prioritize it:** real tester demand during closed testing (12 testers × 14 days), small scope, and it de-risks ADR-024 — if `.docx` ever proceeds, its extracted text would flow into the same entry point built here.

## Known gaps found while writing this ADR

1. **Latin chord notation is not detected on import.** `OverUnderConverter.CHORD_REGEX` is `^[A-G][#b]?(m|maj|min|aug|dim|sus|add)?[0-9]{0,2}(/[A-G][#b]?)?$` — American notation only. A Spanish-speaking tester writing `Do  Rem  Sol7` above lyrics will get "format not detected". The app *displays* Latin notation ([ADR-011](ADR-011-chord-notation-switching.md)) but cannot *ingest* it. Likely relevant for this tester; confirm in Phase 0.
2. **Some common chord shapes fail the regex**, e.g. `E7sus4`, `Cm7b5`, `C#m7(b5)`, `A7/9`. Lines with too many misses drop below the 80% chord-token threshold and are treated as lyrics.
3. **Import failures are silent.** Both JSON import paths swallow exceptions (`catch (_: Exception) { }` / `?: return`). A `.txt` path must give visible feedback (snackbar) on unreadable file or undetected format.

## Phase 0 results (2026-11-23)

Samples (kept local only, git-ignored because one contains a commercial song's lyrics): `docs/songs/Camino de Luz.txt`, `docs/songs/Fading like a flower.txt`. Both were run through a faithful Python port of `OverUnderConverter.convert` + `BracketParser.parse` as of `6e09cd4`. It is a port, not the Kotlin code itself, so the findings must be reproduced as unit tests before fixing.

**Good news:**
- **Column alignment holds.** Chords sit at the right character over the lyric. The core risk that blocks ADR-024 does not apply here.
- **American notation** in both files. Latin appears only in a header comment (`C (Do mayor)`), so gap 1 is **not blocking** for this tester.
- **Encoding:** one file is UTF-8 without BOM, the other plain ASCII. No Windows-1252 seen.
- **One song per file.**
- "Detectar Formato" recognises both files.

**Problems found.** These are worse than an outright failure, because the import "succeeds" with corrupted content. A–C also affect the **paste flow that ships today**.

- **A. Spanish section headers become chords.** `SECTION_NAMES` is English-only. `[Estrofa 1]`, `[Coro]`, `[Estribillo]`, `[Primera Parte]`, `[Segunda Parte]` and `[Final]` are not recognised as headers, so `BracketParser` turns them into chords named "Estrofa 1", "Coro", and so on. The whole song collapses into a single `verse 1`.
- **B. A header followed by chords on the same line loses the header.** On `[Intro] F  Bb  Dm  C`, 4 of 5 tokens are chords, so the line counts as a chord line and the `[Intro]` label is dropped. On `[Solo] Dm  F  C` only 3 of 4 are chords, which is under the 80% threshold, so `Solo` becomes a chord and `Dm  F  C` becomes lyric text.
- **C. Chords past the end of the lyric corrupt the brackets (real bug).** In `mergeChordAndLyricLines`, `min(col, sb.length)` uses the length *after* earlier insertions. When several chords fall past the end of a short lyric (`D5  C5  F5  G5` over a 4-word lyric line), one gets inserted *inside* another's bracket: `…word[[C5]G5]…` → chords `D5`, `[C5`, `F5` and stray `G5]` left in the visible text. Fix: pad the lyric with spaces up to the chord column before inserting.
- **D. Metadata lines end up as lyrics.** Two header styles were seen: labelled (`Título: …` / `Tono: C (Do mayor) | Tempo: 80 BPM`) and bare (`Fading like a flower` / `Roxette` / `Tono:  F`). Both are rendered as the song's first lyric lines.
- **E. Tab blocks** (`E|-7--7-9b10r9--…|`) come through as plain lyric lines. That is tolerable: the reader uses a monospace font, but long lines may wrap. Deferred.

## Proposed decisions

1. **Entry point: file → pre-filled AddSong textarea, not direct save.** The `.txt` content opens `AddSongScreen` with the raw text pre-filled (title/artist guessed where possible), then the user runs/sees "Detectar Formato" and the preview as today. Rationale: free-form text needs a human checkpoint; reuses UI and conflict detection; same decision as ADR-024 §3.
2. **Picker accepts both types.** The Home import action accepts `application/json` and `text/plain` (`OpenDocument`/`GetContent` with multiple MIME types). JSON keeps its current direct-import behaviour; text routes to AddSong.
3. **Encoding:** decode as UTF-8; if the result contains replacement characters (`�`), retry as Windows-1252 (common for `.txt` files created on Windows, breaks `ñ`/accents otherwise). Strip a UTF-8 BOM.
4. **Title/artist heuristic (minimal):** if the first non-blank line is not a chord line or section header, propose it as title; if it has the form `Title - Artist`, split it. Otherwise fall back to the file name without extension. User can edit before saving.
5. **One song per file.** Multi-song `.txt` files are out of scope for v1 of this feature (same boundary-detection problem as ADR-024 §5).
6. **Size guard:** reject files above a sane limit (e.g. 200 KB) with a message, so a mis-picked file can't freeze the UI.

7. **Section labels are normalized to canonical English keys at import (decided 2026-11-23).** `SongSection.type` is already a language-neutral key; the reader localizes it via `sectionTypeLabel()` (`SongContentComponents.kt:561`, `strings.xml` / `values-es`). So a song imported with `[Estribillo]` stores `chorus` and displays as "Coro" in Spanish or "Chorus" in English, following the app language with no stored translation.
   - Alias table (ES → key): estrofa/verso → `verse`; estribillo/coro → `chorus`; puente → `bridge`; intro/introducción → `intro`; final/outro → `outro`; pre-coro/pre-estribillo → `pre-chorus`; primera parte / segunda parte → `verse` 1 / 2. (`instrumental`, `interlude`, `solo`… keep their existing English keys; `instrumental` was not remapped to avoid changing existing English behavior.)
   - English labels keep the old lenient rule (first word decides, `[Chorus x2]` → chorus). Spanish aliases are strict (only an optional number may follow) so `[Puente instrumental]` stays a custom label.
   - A single-word unknown label is a section only if it is letters-only and not a chord in American or Latin notation (`[Sol]`, `[Lam]` stay chords).
   - Side effect: `[Estrofa 1]` displays as "Verso 1" (one Spanish name per type).
   - **Unknown labels (e.g. `[Puente instrumental]`) are kept as a section whose type is the full label verbatim** — never a chord, never dropped. Not translatable; shown as written (first letter capitalized by the existing fallback). Requires `parseSectionHeader` to stop splitting multi-word labels into name + number.
   - Lyrics themselves are never translated; only section labels follow the app language.

## Phases

- **Phase 0 — Validate (gate).** ✅ Done 2026-11-23, see "Phase 0 results".
- **Phase 1a — Parser fixes ✅ done 2026-11-23 (also fixes paste today).** A, B and C were reproduced as failing tests first (14 red), then fixed; full unit suite 179/179 green. New `OverUnderConverterTest` (10 tests) and 9 new `BracketParserTest` cases use short original lyrics, not the tester's songs. Re-running both real sample files through the Kotlin pipeline: no chord names with brackets, sections come out as intro / verse / chorus / solo / outro. Still open from the samples: D (metadata lines become lyrics; with an explicit `[Estrofa 1]` after them the preamble and the verse both end up numbered 1 — resolved by Phase 1b) and E (tabs). Original plan:
  - C: pad the lyric before inserting chords. Smallest change, real bug.
  - A: add Spanish section names (`estrofa`, `coro`, `estribillo`, `puente`, `final`, `primera/segunda parte`…). Decide how an unknown `[Label]` that is not a valid chord is treated: as a generic section header, never as a chord.
  - B: split `[Header] chords…` into a header plus a chord-only line before the chord-line check.
- **Phase 1b — In-app `.txt` picker ✅ done 2026-11-23** (unit-tested; **not yet exercised on a device** — the phone showed `unauthorized` in adb). Implemented as:
  - `TextSongImport` (`utils/`): strict UTF-8 → Windows-1252 fallback, UTF-8/UTF-16 BOMs, NUL bytes → "not text", 200 KB cap. Metadata (D): labeled `Título/Title/Canción`, `Artista/Autor/Intérprete/By`, `Tono/Key/Tonalidad` (Latin and "La menor" → `Am`), `Capo/Cejilla`; `Tempo/BPM/Compás/Afinación` are consumed and ignored. Unlabeled lines become title/artist only when a section header or chord row follows (so lyrics-only files are never mistaken), at most 2, each ≤ 60 chars; a lone `Title - Artist` line is split. Title falls back to the file name. Consumed lines are removed from the body.
  - Home's "Importar desde archivo" now uses `OpenDocument` with `application/json` + `text/plain`. A file is treated as a JSON backup if it ends in `.json` or starts with `{`; otherwise as text. Text opens Add Song through `TextDraftHolder` (delivered once; resolves the navigation-argument open question).
  - **Deviation from decision 1:** the body is converted to bracket format on arrival (`OverUnderConverter.convert`, else kept as is) rather than waiting for the user to press "Detectar Formato", and Add Song opens in **Text mode** so the result can be reviewed. The user still has to confirm with the normal preview/save path (including duplicate-title detection).
  - Visible snackbars for empty / too large / not text / unreadable files (new `import_file_*` strings, EN + ES); help text mentions `.txt`. The existing JSON path still swallows errors silently apart from the new unreadable-file message (gap 3 only fixed for the new path).
  - Real samples: both tester files produce the expected title/artist/key and clean sections.
  Original plan:
- **Phase 1c — Latin notation in import** (gap 1) and broader `CHORD_REGEX` (gap 2). **Not blocking** for this tester per Phase 0; keep it on the list. Watch for false positives (`La`, `Si`, `Do` are also Spanish words).
- **Phase 2 — Open from outside the app.** Add `text/plain` to the `VIEW` intent-filter (and optionally `SEND` for "Share → Cancionero" from WhatsApp/Files) routing to the same pre-filled AddSong. Trade-off: Cancionero will appear in the "Open with" chooser for *every* `.txt` file on the device — acceptable, but deliberate.

## Open questions
- ~~Navigation: `Routes.ADD_SONG` has no argument for initial text.~~ Resolved in Phase 1b with `TextDraftHolder`.
- Should a JSON-looking `.txt` (someone renamed a Cancionero export) be detected and sent to `importSongFromJson`? Cheap to add; decide in Phase 1.

## Consequences
**Positive:** answers concrete tester demand quickly; reuses parser, preview, and smart-import conflict handling; builds the entry point ADR-024 would need; fixing Latin-notation ingestion also improves plain paste.

**Negative / risk:** Phase 2 widens the app's intent surface (every `.txt`); regex broadening for Latin notation can introduce false chord-line detections on Spanish lyrics; if the tester's format turns out to be unusual, Phase 1 alone won't help them and Phase 0 findings may reshape the plan.
