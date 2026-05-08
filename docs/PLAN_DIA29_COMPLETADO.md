# Día 29 — v1.4.0 Leather Journal UI — Final Polish & Release Prep

**Estado: COMPLETADO**
**Fecha:** 2026-05-07

---

## Objetivos cumplidos

### 1. Seam animation (section 8b) aplicada al SetlistScreen

`VirtualSetlist` (`SetlistScreen.kt`) ahora tiene el mismo efecto de costura de cuero que `VirtualPagedSong`:
- `HorizontalPager` envuelto en `Box` con `onSizeChanged`
- `offsetFraction` → `seamX` calculado con el mismo midpoint-flip
- Sombra degradada de 24dp + franja de costura de 3dp usando `leatherDeep` / `leatherMid`
- Activo únicamente cuando `abs(offsetFraction) > 0.005f`

### 2. Corrección: versiones "Default" duplicadas

- **Root cause:** `MIGRATION_3_4` y `seedFromAssets()` ambos insertaban una versión "Default" por canción en distintas etapas del ciclo de vida.
- **Migración 5→6:** `DELETE FROM song_versions WHERE id NOT IN (SELECT MIN(id) FROM song_versions GROUP BY song_id, name)` — deduplica en caliente.
- **`seedFromAssets()`:** ahora filtra canciones que ya tienen versiones antes de insertar, evitando duplicados en instalaciones limpias o re-seeds futuros.

### 3. Mejora del gradiente de los botones brass

- `brassBrush()` cambiado de `radialGradient` a `linearGradient` diagonal (top-left → bottom-right).
- `brassLight` subido de `0xFFE6C26B` a `0xFFF2CF58` en Light y Dark para un brillo más nítido.
- `BrassButton` (dead code) eliminado de `Brass.kt`.

### 4. Notación latina en el builder

- Los chips de acordes rápidos ahora muestran y escriben en notación latina cuando el usuario la tiene activada.
- `BracketParser` normaliza a notación americana en el límite de parseo (`ChordNotation.toAmericanNotation()`), transparente para el resto del sistema.
- El transpositor y el reader no se ven afectados.

### 5. Audit de tema — todas las vistas al día

| Vista | Cambio |
|-------|--------|
| `SetlistScreen` | `TopAppBar` → `LeatherHeader`; `Button/OutlinedButton` → `BrassSurface` + outline leather; `FlameGuitar` en End Page |
| `VersionEditorScreen` | `TopAppBar` → `LeatherHeader` |
| `AboutScreen` | `TopAppBar` → `LeatherHeader`; `LibraryMusic` → `FlameGuitar` |
| `PreviewReaderScreen` | `TopAppBar` → `LeatherHeader` |
| `PlaylistDetailScreen` | `ExtendedFloatingActionButton` → `BrassSurface` themed |

### 6. Código muerto eliminado

- `PlaylistsScreen.kt` — archivo eliminado
- `VersionChips.kt` — archivo eliminado
- `Routes.PLAYLISTS` — constante eliminada
- Navegación y imports de `PlaylistsScreen` eliminados de `MainActivity.kt`

### 7. Menú FAB (Add Song / Import from File) temático

`DropdownMenu` + `DropdownMenuItem` ahora usan colores del design system:
- Fondo: `leatherMid`
- Borde: `rule` 1dp, `RoundedCornerShape(12.dp)`
- Texto: `cream` (igual que el título del `LeatherHeader`)
- Iconos: `brass`
- Divisor: `rule` 0.5dp entre ítems

### 8. Version bump

- `versionCode`: 6 → 7
- `versionName`: `"1.3.0"` → `"1.4.0"`

---

## Próximo día (Day 30)

- Subir v1.4.0 AAB a Play Console (prueba cerrada)
- Tomar screenshots actualizados para la ficha de la tienda
- Revisar feedback de la prueba cerrada si hay respuestas

---

## Tests

Sin cambios en tests — todos los tests previos siguen en verde.
- Unit tests (JVM): **132 passing**
- Instrumented tests: **27 passing**
