# ADR-026: License compliance and scope of the GPL grant

## Status
Accepted (2026-11-24). Amends [ADR-023](ADR-023-gpl-v3-license-rationale.md).
**This is an engineering compliance review, not legal advice.** Items marked *Lawyer* in the checklist should be confirmed by an IP lawyer before the commercial v2.

## Context
A review of the licensing setup found gaps between what ADR-023 says and what the repository does:

1. `LICENSE` was an abridged paraphrase of the GPL v3 (about 1,300 words vs about 5,600), not the official text. The GPL forbids modifying the license text, and sections 6 and 7 were reduced to a few lines, which made the actual grant ambiguous.
2. The license header said "version 3 or any later version" while the README and ADR-023 said "GPL v3".
3. The app bundles proprietary SDKs (AdMob, Firebase Analytics/Crashlytics, Play services Auth). ADR-023 correctly says no dependency forces GPL, but did not address that downstream redistributors of a GPL build containing those SDKs have no license to do so.
4. "Branding reserved via copyright" is inaccurate: names are protected by trademark law, not copyright.
5. Merriweather, the font the UI uses, shipped without its OFL license; no third-party notices existed.
6. Two JSON files with full commercial lyrics were committed under `docs/songs/` (now untracked and git-ignored; still present in git history).

Product purpose, for coherence ([ADR-009](ADR-009-personal-songbook-vision.md), [ADR-010](ADR-010-chords-public-lyrics-private.md)): a personal, offline repertoire manager. The app does not provide lyrics; users bring their own music; only lyric-free chord arrangements are meant to be shared. A copyleft license for the code fits this: the code is open and auditable, while the content stays with the user and is outside the license.

## Decision
1. **`LICENSE` is the verbatim official GPL v3 text** (from https://www.gnu.org/licenses/gpl-3.0.txt). The copyright line lives in the README, `NOTICE.md` and source headers.
2. **SPDX identifier: `GPL-3.0-or-later`**, matching the notice the project originally declared. Every Kotlin source file carries `SPDX-FileCopyrightText` and `SPDX-License-Identifier` headers.
3. **Additional permission under GPL section 7** (in `NOTICE.md`) to link the Google SDKs and Android libraries. The author, as sole copyright holder, is not bound by the GPL toward their own code; the permission is for downstream forks.
4. **Name, logo and launcher icon are not licensed** (GPL section 7(e)): forks must rebrand. This is trademark-style protection; registering the mark is a separate, optional step.
5. **Songs and lyrics are outside the license.** The app ships three public-domain songs. The repository must never contain third-party lyrics; `docs/songs/` is local-only and git-ignored. The About screen states that users are responsible for the music they add.
6. **Third-party notices** are kept in `THIRD_PARTY_NOTICES.md`, with OFL texts in `licenses/` and a summary in the About screen.
7. **No external code contributions for now.** If that changes, require a CLA so the section 7 permission and future relicensing remain possible.

## Compliance checklist

| Item | Status |
|---|---|
| Official GPL text in `LICENSE` | Done (2026-11-24): 674 lines / 5,644 words, sections 0-17 in official order (the abridged version also mis-numbered 11-13, omitting "Patents") |
| SPDX headers, `NOTICE.md`, `THIRD_PARTY_NOTICES.md`, OFL texts, README, About screen | Done |
| Full Apache-2.0 texts bundled for offline distribution | Open: consider Google's `oss-licenses-plugin` to generate and show them in-app |
| **Privacy policy URL** (required by Google Play for AdMob, Analytics, Crashlytics) | **Draft written** (`docs/PRIVACY_POLICY.md`, EN+ES). Open: contact email, publish at a public URL, link it in Play Console |
| Song titles were sent to Firebase Analytics and Crashlytics (`song_opened`) | Fixed 2026-11-24: only the internal song id is sent |
| **Ads consent (GDPR/UK/EEA)**: AdMob requires a consent flow (Google UMP SDK) for users in those regions | **Open: no consent code found.** Decision and design in [ADR-027](ADR-027-ads-and-analytics-consent.md) |
| Third-party lyrics in git history (`5ea0339`, `81306c0`, already on `origin/main`) | Open: rewrite history with `git filter-repo` + force-push if the repo is public or will be |
| Launcher icon authorship and Canva terms (made by a family member in Canva) | Open: get written confirmation of rights; check Canva's terms for use in a brand/logo |
| JSON song sharing between users (WhatsApp workflow) can pass full lyrics | Open: add a short notice on the backup/share action |
| v2 community features: terms of service, UGC policy, takedown procedure, in-app report, lyric-stripping enforced server-side | Open, required by Play's UGC policy before v2 |
| Trademark search/registration for "Cancionero" | Open, optional (*Lawyer*) |
| Review of the section 7 wording and the GPL + proprietary SDK position | Open (*Lawyer*) |
| Unused Fraunces font files | Optional cleanup (see `THIRD_PARTY_NOTICES.md`) |

## Consequences
**Positive:** the grant is unambiguous; forks can legally redistribute builds with Google SDKs; third-party obligations (OFL, Apache) are documented; the license stance matches the product principle "your songs, your notebook".

**Negative:** the section 7 permission and trademark carve-out are unusual enough to deserve a lawyer's review; several store-policy items (privacy policy, consent, UGC) are still open and independent of the license.
