# ADR-011: Chord notation switching — American and Latin

## Status
Accepted

## Context
Two major chord notation systems exist worldwide:

| American | A | B | C | D | E | F | G |
|----------|---|---|---|---|---|---|---|
| Latin    | La | Si | Do | Re | Mi | Fa | Sol |

Latin notation is standard in Spain, Latin America, France, Italy, Portugal, and much of Europe. A guitarist who learned with "Do Re Mi" may not immediately recognize "C D E" — and vice versa.

Most guitar apps default to American notation with no option to switch. This is a barrier for millions of guitarists.

## Decision
Support both notation systems with a user preference toggle.

### Implementation
- **Internal storage:** Always American notation (A, B, C, D, E, F, G). This is the universal standard in digital music tools, APIs, and databases.
- **Display layer:** Convert on the fly based on user setting. `Am7` displays as `Lam7` in Latin mode.
- **User setting:** A preference stored locally (SharedPreferences or DataStore). Default: American. Changeable from Settings.
- **Scope:** Affects the Reader, the chord editor, the community arrangements view, and any chord display in the app.

### Conversion rules
The conversion is a simple string prefix replacement:
```
A → La    (including A#, Ab, Am, Amaj7, etc.)
B → Si
C → Do
D → Re
E → Mi
F → Fa
G → Sol
```
Modifiers (m, 7, maj7, sus2, dim, aug, #, b) remain unchanged. Examples:
- `F#m7` → `Fa#m7`
- `Bb` → `Sib`
- `Gsus4` → `Solsus4`

### Where NOT to convert
- Internal data model (`ChordPosition.chord` always stores American)
- Database storage
- JSON export/import
- Community sharing format

Conversion happens exclusively at the UI display layer.

## Consequences
**Positive:**
- Massive accessibility gain for Spanish, Portuguese, French, Italian, German-speaking guitarists
- Trivial to implement — pure string transformation, no data model changes
- Differentiator — most competitors don't offer this
- Future-extensible to other notation systems (German: B = H, Nashville numbers, etc.)

**Negative:**
- Latin chord names are longer (`Sol#m7` vs `G#m7`) — may affect layout in tight spaces like chord lines in the Reader
- Users sharing arrangements cross-notation need to understand that the internal format is American
- Testing doubles — every chord display needs verification in both notations
