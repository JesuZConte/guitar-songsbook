# Roadmap v1.x — Remaining Work

What is already shipped is tracked in [STATUS.md](STATUS.md).
This file contains only what is **not yet built**, in priority order.

Last updated: 2026-05-04

---

## v1 Status

**v1 is feature-complete and tech-debt clean.** All P1 items are resolved:

- ~~TD-1: Converters crash on corrupt JSON~~ — fixed (`runCatching` in both converters)
- ~~TD-2: Font size not persisted~~ — fixed (`onFontSizePersist` callback wired in Factory)
- ~~Collections not shown on first launch~~ — fixed (race condition between seeding and ViewModel init)
- ~~Setlist mode~~ — shipped

---


## Tech Debt P2 — v1.1

**Todos resueltos.** Listo para v2.

- ~~TD-3: Static `pendingPreview`~~ — movido a campo de instancia; `Routes.PREVIEW` usa `previousBackStackEntry` para obtener el ViewModel correcto
- ~~TD-4: Error handling inconsistente en `PlaylistsViewModel`~~ — `removeSongFromPlaylist` ahora tiene `try-catch` igual que `deletePlaylist`
- ~~TD-5: `ensureSeeded()` en cada método~~ — double-checked locking con `Mutex`; el `SELECT COUNT(*)` solo ocurre una vez

---

## Antes de la release pública

| # | Qué | Detalle |
|---|-----|---------|
| 1 | Símbolos de depuración nativos | Play Console muestra warning al subir el AAB. Añadir `ndk { debugSymbolLevel = "FULL" }` en `buildTypes.release` de `build.gradle.kts`. Requiere NDK instalado. No afecta funcionamiento ni prueba cerrada — solo mejora el análisis de crashes nativos (Firebase/AdMob). |

---

## v2 — Cloud & AI

Do not start until v1 is stable on Play Store and has real user feedback.
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
