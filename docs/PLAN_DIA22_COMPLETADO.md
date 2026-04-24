# Día 22 — Completado (2026-04-23)

## Contexto

Prueba cerrada alpha en curso. Día 21 cerró transposición (v1.1).
Collections redesign (v1.1) esperando mockups — disponibles lunes 28.

---

## Lo que hicimos

### 1. Bug fix: home no refrescaba al volver de AddSong (mañana)

Al añadir o editar una canción y volver al Home, la lista no se actualizaba
hasta cerrar y reabrir la app. Causa: `LaunchedEffect(Unit)` en el composable
HOME del NavHost solo se ejecuta una vez al entrar, nunca al volver de la pila.

**Fix:** `DisposableEffect` con `LifecycleEventObserver` en `HomeScreen.kt`,
observando el lifecycle del `NavBackStackEntry`. Al evento `ON_RESUME` llama
a `viewModel.refreshSongs()`, que sí se dispara cada vez que el usuario regresa
al Home — ya sea desde AddSong, desde el Reader o desde cualquier otra pantalla.

Archivos: `HomeScreen.kt`, `MainActivity.kt` (se eliminó el `LaunchedEffect(Unit)` redundante)

---

### 2. Pinch-to-zoom en el Reader (v1.2)

Gesto de dos dedos para controlar el tamaño de fuente directamente en el
Reader, sin necesidad de usar los botones `−` / `+`.

**Implementación:**
- `UserPreferences`: añadidos `getFontSize(context)` / `setFontSize(context, size)` con clave `reader_font_size`, default 14sp
- `ReaderViewModel`: acepta `initialFontSize` en constructor + Factory; añadida `fun setFontSize(size: Int)` con clamp; `DEFAULT_FONT_SIZE = 14` en companion
- `MainActivity`: la Factory de `ReaderViewModel` recibe `UserPreferences.getFontSize(context)` → el tamaño persiste entre sesiones
- `SongReaderScreen`: `rememberTransformableState` acumula el scale del pinch; cada ±10% de escala dispara un `increase/decreaseFontSize()`; `LaunchedEffect(uiState.fontSize)` persiste el nuevo valor en `UserPreferences`

El stepper `[ − ] 14sp [ + ]` sigue funcionando igual. Ambos controles comparten
el mismo estado y la misma persistencia.

---

### 3. Bug fix: chords merging — `Dm7 G` → `DM7G` / `RemSolRemSol`

En líneas de solo acordes (`[Dm7] [G] [Dm7] [G]`), el parser asigna posiciones
0, 1, 2, 3 (un espacio entre cada par de brackets). `buildChordLine` escribía
cada acorde en el `CharArray` sin verificar si solapaba al anterior: `Dm7`
ocupa posiciones 0-2, pero `G` estaba en posición 1 → sobrescribía la `m`.
Con notación latina el resultado era `RemSolRemSol`.

**Fix en `ChordFormatter.kt`:** antes de rellenar el array, se ordenan los
acordes por posición y se recorre manteniendo `nextFree`. Cada acorde empieza
en `max(posiciónOriginal, nextFree)` y `nextFree = start + chord.length + 1`.
Esto garantiza al menos un espacio de separación. Posiciones originales intactas
en DB; el ajuste es solo visual.

Dos tests de regresión añadidos en `ChordLineTest.kt`.

---

## Archivos modificados

- `data/local/UserPreferences.kt` — getFontSize / setFontSize
- `presentation/viewmodel/ReaderViewModel.kt` — initialFontSize, setFontSize, DEFAULT_FONT_SIZE
- `presentation/screens/SongReaderScreen.kt` — pinch gesture + persistence LaunchedEffect
- `MainActivity.kt` — pass initialFontSize to ReaderViewModel.Factory
- `utils/ChordFormatter.kt` — collision-avoidance in buildChordLine
- `utils/ChordLineTest.kt` — 2 regression tests

---

## Estado al cerrar el día

- v1.1 Transposición: ✅ completa (día 21)
- v1.2 Pinch-to-zoom: ✅ completa
- v1.2 Nocturno mode Reader: pendiente
- v1.1 Collections redesign: en espera de mockups (lunes 28)
- v1.5 Language selector: no iniciado — movido a día 23
