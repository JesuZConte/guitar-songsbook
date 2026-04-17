# Día 16 — Completado

## Lo que se hizo

### 1. Project constitution ratified (Speckit)
- `.specify/memory/constitution.md` populated with 10 principles for Cancionero v1.0.0
- All placeholder tokens replaced; governance section added
- Confirmed `.specify/` directory is safe to push (no secrets)

### 2. Tester feedback triaged and fixed (6 items)

| # | Fix | File |
|---|---|---|
| Genre chips not refreshing | `refreshSongs()` now reloads genres + difficulties from DB | `HomeViewModel.kt` |
| Text box too tall when pasting | `heightIn(max = 400dp)` added to Text mode field | `AddSongScreen.kt` |
| Builder sections also too tall | `heightIn(max = 300dp)` added to each section card field | `AddSongScreen.kt` |
| Key field accepts any value | Replaced with dropdown of 24 valid musical keys (12 major + 12 minor) | `AddSongScreen.kt` |
| Nav bar + keyboard cover buttons | `contentWindowInsets = WindowInsets(0)` + `imePadding()` on Column | `AddSongScreen.kt` |
| Home looks like a global list | TopAppBar subtitle "My personal songbook" + offline copy in empty state | `HomeScreen.kt` |
| Bracket format not explained | `ⓘ` info button opens ModalBottomSheet with format guide | `AddSongScreen.kt` |

### 3. ADR-019 written
- Collections/Library home redesign deferred to v1.1
- Traditional Songs sample collection confirmed as public domain (Amazing Grace,
  Greensleeves, Scarborough Fair)

### 4. Items deferred
- Pinch-to-zoom font size → v1.1
- Collections home redesign → v1.1 (ADR-019)

---

## Pendiente para mañana (Día 17)

### 1. Commit today's fixes
### 2. Play Console — set up Closed Testing track
### 3. Recruit 12 testers and send opt-in link