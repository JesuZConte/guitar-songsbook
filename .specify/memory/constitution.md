<!--
SYNC IMPACT REPORT
==================
Version change: 1.0.0 → 1.1.0
Rationale: New principle added (XI). MINOR bump per versioning policy.

Principles added:
  XI. Premium by Gratitude, Never by Frustration

Principles modified: none.
Sections modified: Version Boundaries (AI format conversion added to v2 list).

Templates reviewed:
  ✅ .specify/templates/plan-template.md — no update needed.
  ✅ .specify/templates/spec-template.md — no update needed.
  ✅ .specify/templates/tasks-template.md — no update needed.

Deferred TODOs: none.
-->

# Cancionero Constitution

## Core Principles

### I. The Reader is Sacred

The Song Reader is the heart of the app. It MUST be ad-free, distraction-free,
and pixel-perfect at every font size and under every lighting condition.

Every feature decision MUST pass the live-musician test:
*Does this help or hinder a guitarist playing live? Does it make adding a song
more effortless?*

The app's primary purpose is to save and organize a personal song collection.
No feature may compromise the reading experience in order to serve any other goal.

### II. Offline First, Always

The app MUST work fully offline. Every core capability — browsing, reading,
adding songs, and navigation — MUST function without a network connection.

Cloud sync is a v2 feature. Local functionality MUST always be shipped and
validated before any cloud-dependent convenience is considered.

### III. Users Bring Their Music, We Provide the Tools

The app MUST NOT ship copyrighted content. Chord progressions are not
copyrightable; lyrics are. The app provides the tools; users supply their
own arrangements.

Cancionero is a personal notebook, not a content platform. We MUST never
act as a distribution channel for third-party material.

### IV. Chords Public, Lyrics Private

Any sharing feature MUST enforce the following split:
- Chord structure MAY be shared publicly.
- Lyrics MUST remain private and MUST NOT be transmitted without explicit
  user action and clear informed consent.

This is the legal foundation that makes community features viable without
copyright risk. No implementation may violate this boundary.

### V. Simplicity Now, Extensibility Later

Ship the smallest viable feature that satisfies the user need. Over-engineering
for hypothetical future requirements is not permitted.

Sequencing MUST respect clear v1/v2 boundaries:
- Manual DI before Hilt/Dagger.
- Local storage before cloud.
- Notation switching before transposition.
- Banner ads before subscription billing.

Complexity MUST be justified against a concrete current need, documented in
an ADR, and approved before implementation.

### VI. Warm Vintage Identity — Brand in Chrome, Clarity in Content

The vintage aesthetic MUST be applied to navigation chrome, cards, and headers.
The Reader MUST prioritize pure readability above decorative design.

Monospace font for chord notation is non-negotiable. The design system MAY
evolve over time; readability constraints on the Reader MUST NOT.

### VII. Respect Musician Workflows

The bracket format `[Am]Hello [F]world` is the primary import standard. The app
MUST guide users unfamiliar with this format rather than requiring prior knowledge.

The following constraints are non-negotiable:
- Tap zones MUST enable reliable one-handed page navigation.
- American (C, D, E…) and Latin (Do, Re, Mi…) notation MUST be supported equally.
- Auto-scroll MUST NOT be implemented; musicians need manual control during
  live performance.

### VIII. Privacy by Default

Original compositions MUST be private until the user explicitly and deliberately
chooses to share them.

Data collection is minimal by design:
- Collected: crash logs, diagnostics, device ID for ad targeting.
- MUST NOT collect: search history, user profiling data, behavioral tracking.

Only chord structure MAY be shared in any community feature. Lyrics MUST
never leave the device without explicit user consent.

### IX. Production Quality from Day One

This is a commercial project. Every significant architectural or product decision
MUST be documented in an ADR under `docs/adr/` before implementation begins.

The architecture MUST be testable and layered (domain → data → presentation).
Technical debt shortcuts that undermine testability or future maintainability
are not acceptable.

### X. Free to Use, Unobtrusive to Monetize

Monetization constraints:
- v1: A single banner ad on the Home screen only. Ads MUST NOT appear in the
  Song Reader or any other content-consumption screen.
- v2: A "Remove Ads" in-app purchase option, gated on Firebase Auth user
  accounts being available.

Revenue MUST never compromise the reading experience. Any ad placement that
affects Reader usability is a constitution violation.

### XI. Premium by Gratitude, Never by Frustration

The free version of Cancionero MUST be genuinely complete. A user who never
pays MUST be able to use every core feature without feeling blocked, crippled,
or nudged into paying.

Premium features MUST earn their price independently — they exist because they
deliver real extra value, not because core functionality was withheld to create
pressure.

The test for any premium feature:
*Would a free user feel frustrated that this is locked, or would they feel that
paying is a fair exchange for something genuinely useful?*

If the answer is frustration, the feature MUST NOT be put behind a paywall.

**Specific constraints:**
- Core songbook features (add, edit, delete, read, search, export) MUST always
  be free.
- Premium MAY include: removing ads, AI-powered tools, cloud sync, community
  features — things that cost money to operate or deliver exceptional convenience.
- Showing locked features prominently to pressure free users into paying is
  PROHIBITED. Premium features are discoverable but MUST NOT dominate the UI.

## Version Boundaries

This section governs which features belong to which release. Shipping a v2
feature in v1 requires an ADR justifying the exception.

**v1 (current)**
- Fully offline, local Room database.
- Manual dependency injection (no Hilt/Dagger).
- AdMob banner on Home screen.
- American and Latin chord notation display.
- Song Reader with Kindle-style pagination.
- Favorites and search.

**v2 (planned)**
- Firebase Auth (Google Sign-In).
- Firestore sync for cross-device access.
- Community chord sharing (chord structure only; lyrics private).
- "Remove Ads" in-app purchase.
- Transposition (deferred from v1 per ADR-018).
- AI-powered song format conversion (Claude API, unlocked by Remove Ads IAP).

## Governance

This constitution supersedes all other development practices, informal agreements,
and prior conventions. Where conflict exists, this document wins.

**Amendment procedure**:
1. Open an ADR describing the proposed change and its rationale.
2. Update this constitution and bump the version per semantic versioning:
   - MAJOR: removing or redefining a principle in a backward-incompatible way.
   - MINOR: adding a new principle or materially expanding guidance.
   - PATCH: clarifications, wording fixes, or non-semantic refinements.
3. Update `LAST_AMENDED_DATE` to the date of the amendment.
4. Propagate changes to dependent templates and guidance files as needed.

**Compliance**:
- Every plan (`/speckit.plan`) MUST include a Constitution Check gate.
- Any implementation that would violate a principle requires an ADR exception
  approved before work begins.
- Architecture decisions are tracked in `docs/adr/`.

**Version**: 1.1.0 | **Ratified**: 2026-04-13 | **Last Amended**: 2026-04-18
