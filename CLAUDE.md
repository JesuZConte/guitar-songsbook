# CLAUDE.md

Guidance for Claude Code (claude.ai/code) in this repository. Keep this file short and stable:
anything that changes often (architecture, routes, versions, open work) lives in
[`docs/STATUS.md`](docs/STATUS.md) and the ADRs, not here.

## Project

**Cancionero – Chord Songbook**: Android app (Kotlin, Jetpack Compose, Room, MVVM, manual DI, no Hilt)
for guitarists. Package `com.guitarapp.songsbook`. Min SDK 28, target/compile SDK 36, Java 21.
Commercial project: favour production quality. Currently in Play Console closed testing.

- **Current state, versions, open work:** `docs/STATUS.md`.
- **Architecture decisions:** `docs/adr/` (read the relevant ones before proposing structural changes).
- **Day-by-day plans:** `docs/PLAN_DIAxx.md` (open) / `PLAN_DIAxx_COMPLETADO.md` (closed).
- Dependencies are managed in `gradle/libs.versions.toml`.

## Build & test

```bash
./gradlew :app:testDebugUnitTest                      # full unit suite (use this, not plain `test`)
./gradlew :app:testDebugUnitTest --tests "com.guitarapp.songsbook.utils.BracketParserTest"
./gradlew installDebug                                # install on connected device/emulator
./gradlew connectedAndroidTest                        # instrumented (device needed; --tests does NOT work,
                                                      #  use -Pandroid.testInstrumentationRunnerArguments.class=...)
```

Plain `./gradlew test --tests ...` fails on Android. `--offline` fails (room-compiler is not cached).

## Working rules

### Day cycle
1. **Start of session:** read `docs/STATUS.md` and the open plan (the `PLAN_DIAxx.md` without
   `_COMPLETADO`). State in one line where we are before touching code.
2. No open plan → write one first, with phases and "done" criteria. ADR if the change is structural.
3. **Close of day:** rename the plan to `PLAN_DIAxx_COMPLETADO.md` recording what was *actually* done
   (not what was planned), update `docs/STATUS.md` (including its "Last verified" date), and create
   `PLAN_DIA(xx+1).md` for the next day.
4. Never leave a plan open across days without a status note.

### Code quality
- Logic is built test-first (red → green); UI is verified on the device.
- Before calling anything done, run the **full** suite and cite the test count and failures.
- New `.kt` files start with `// SPDX-FileCopyrightText: 2026 Luis Jesus` and
  `// SPDX-License-Identifier: GPL-3.0-or-later`.
- One ADR per structural decision; amend or supersede an ADR rather than silently contradicting it.

### Privacy and legal
- Never send user text (titles, lyrics, notes) to Analytics or Crashlytics; internal IDs only.
- If what data is collected changes, update `docs/PRIVACY_POLICY.md` and the Data safety table in
  `docs/PLAY_STORE_LISTING.md` in the same change.
- Do not commit copyrighted lyrics. `docs/songs/` is git-ignored and is for local tests only.
- New dependencies or bundled assets (fonts, images) go in `THIRD_PARTY_NOTICES.md`.
- License is `GPL-3.0-or-later` with the §7 additional permission and trademark carve-out described in
  `NOTICE.md` (ADR-023, ADR-026). Do not restate license terms from memory; point to those files.
- Claims shown to users (About screen, policy, store listing) must be checked against the code.

### Git
- **Commit and push only when Luis asks.** Until then, leave changes in the working tree.
- Solo project: commit straight on `main`, no PR ceremony. Keep commits small, one logical change each,
  with messages that describe what they really contain. Privacy or security fixes get their own commit
  so they can be reverted alone.
- Before a push, check that no secrets are staged (`google-services.json` is not versioned).
- Irreversible or outward-facing actions (history rewrite, force-push, publishing, Play Console changes)
  need explicit confirmation each time.

### Honesty
- Report failing tests, skipped steps and unverified claims as they are.
