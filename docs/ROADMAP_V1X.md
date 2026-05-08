# Roadmap v1.x — Remaining Work

What is already shipped is tracked in [STATUS.md](STATUS.md).
This file contains only what is **not yet built**, in priority order.

Last updated: 2026-05-07

---

## v1 Status

**Features complete. v1.4.0 UI refinement complete. Uploading to Play Console closed test on Day 30.**

All functional P1 items resolved as of v1.3.0:

- ~~TD-1: Converters crash on corrupt JSON~~ — fixed
- ~~TD-2: Font size not persisted~~ — fixed
- ~~Collections not shown on first launch~~ — fixed (race condition)
- ~~Setlist mode~~ — shipped
- ~~TD-3/TD-4/TD-5: Tech debt P2~~ — all resolved

---

## v1.4 — Leather Journal UI Refinement ✓ COMPLETE

Full rationale in [ADR-021](adr/ADR-021-leather-journal-ui-refinement.md).

| # | What | File(s) | Status |
|---|------|---------|--------|
| 1 | FAB shape → rounded square 52dp | `ui/components/Brass.kt` | ✓ done |
| 2 | "Mis colecciones" header → label + rule + count | `CollectionsLandingScreen.kt` | ✓ done |
| 3 | AllSongsCard icon → cream tile container | `CollectionsLandingScreen.kt` | ✓ done |
| 4 | `LeatherChip` composable + wire HomeScreen filters | `ui/components/LeatherChip.kt`, `HomeScreen.kt` | ✓ done |
| 5 | `LeatherChip` wired in SettingsScreen | `SettingsScreen.kt` | ✓ done |
| 6 | "Coming soon" badge → `BrassSurface` | `SettingsScreen.kt` | ✓ done |
| 7 | Bottom nav → gradient bg + brass active pill | `MainActivity.kt` | ✓ done |
| 8 | Help dialog styling | `HomeScreen.kt` | ✓ done |
| 9 | `BrassToggle` replacing settings `Switch` | `SettingsScreen.kt`, `ui/components/` | ✓ done |
| 10 | Leather seam animation on all pagers | `SongContentComponents.kt`, `SetlistScreen.kt` | ✓ done |
| 11 | All screens → `LeatherHeader` (no more TopAppBar) | `SetlistScreen`, `VersionEditorScreen`, `AboutScreen`, `PreviewReaderScreen` | ✓ done |
| 12 | Brass gradient → diagonal linear, brighter brassLight | `Brass.kt`, `Color.kt` | ✓ done |
| 13 | Latin notation in builder quick chips | `AddSongScreen.kt`, `BracketParser.kt` | ✓ done |
| 14 | Duplicate "Default" versions fix | `SongDatabase.kt` (MIGRATION_5_6), `AssetSongRepository.kt` | ✓ done |
| 15 | Start Setlist FAB themed | `PlaylistDetailScreen.kt` | ✓ done |
| 16 | Add Song / Import menu themed | `HomeScreen.kt` | ✓ done |
| 17 | Dead code removed (PlaylistsScreen, VersionChips, BrassButton) | multiple | ✓ done |

**Known deferred (not in v1.4):**
- `font-variant: small-caps` — requires Fraunces or a small-caps font variant; planned for a future type pass
- Double-frame inset on help dialog — not achievable with native `AlertDialog`; skip for now

---

## Day 30 — Próximas acciones

| # | Qué | Detalle |
|---|-----|---------|
| 1 | Subir v1.4.0 a Play Console | Build AAB firmado → subir a prueba cerrada (internal/closed test track) |
| 2 | Screenshots actualizados | Capturar nuevas pantallas con el tema Leather Journal completo para la ficha de la tienda |
| 3 | Revisar feedback de prueba cerrada | Si hay testers activos, revisar reportes antes de promover a producción |

## Antes de la release pública

| # | Qué | Detalle |
|---|-----|---------|
| 1 | Símbolos de depuración nativos | Play Console muestra warning al subir el AAB. Añadir `ndk { debugSymbolLevel = "FULL" }` en `buildTypes.release` de `build.gradle.kts`. Requiere NDK instalado. No afecta funcionamiento ni prueba cerrada — solo mejora el análisis de crashes nativos (Firebase/AdMob). |

---

## v2 — Cloud & AI

Do not start until v1.4 UI refinement is complete and v1 is stable on Play Store with real user feedback.
Full context in [ADR-020](adr/ADR-020-v2-roadmap.md).

| Priority | Feature | Gate |
|----------|---------|------|
| 1 | Google Sign-In (Firebase Auth) | Prerequisite for everything below |
| 2 | Cross-device sync (Firestore) | Requires Auth |
| 3 | Remove Ads IAP | Requires Auth (purchase tied to account) |
| 4 | Community chord sharing | Requires Firestore + moderation plan |
| 5 | AI format conversion (Claude API) | Requires Remove Ads IAP |
| 6 | AI song completion (Claude API) | Requires Remove Ads IAP |
| 7 | Tap-a-word chord editor | Independent, but v2 scope |
