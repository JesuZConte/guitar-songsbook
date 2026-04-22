# Día 21 — Plan (2026-04-21)

## Contexto

Prueba cerrada alpha en curso. Día 20 cerró v1.3 y v1.4 antes de lo previsto
(backup/import + over/under detection). Hoy arrancamos v1.1: Transposición.

---

## Feature principal — Transposición de tonalidad (v1.1)

**Por qué ahora:** es una necesidad diaria para cualquier guitarrista. Cambiar
de tonalidad para ajustarse a la voz del cantante, o para evitar cejilla, es
uno de los usos más frecuentes de un cancionero digital.

**Principio clave:** la transposición es **en tiempo real y no destructiva**.
Los acordes originales quedan guardados en la DB — solo cambia la visualización
en el Reader. El offset se guarda como preferencia de la sesión (no persiste
entre aperturas).

---

## Alcance mínimo viable

### 1. Lógica de transposición en ChordNotation

Añadir `transpose(chord: String, semitones: Int): String` a `ChordNotation`:

- Soporta sostenidos (#) y bemoles (b) como enarmónicos
- Transpone la nota raíz + la nota del bajo si es slash chord (ej. `D/F#`)
- Preserva la calidad del acorde (m, maj7, sus4, etc.)
- Sube o baja libremente: `transposeSteps` puede ser cualquier entero (−11 a +11)

### 2. Estado en ReaderViewModel

```kotlin
data class ReaderUiState(
    ...
    val transposeSteps: Int = 0   // offset actual, no persiste
)

fun transposeUp()   // +1 semitono
fun transposeDown() // −1 semitono
fun resetTranspose() // vuelve a 0
```

### 3. UI en el Reader BottomBar

Añadir al `ReaderBottomBar` un control de transposición:

```
[ ↓ ]  T: 0  [ ↑ ]
```

- Diseño compacto junto a los controles de fuente existentes
- El número muestra el offset actual (`+2`, `−1`, `0`)
- Tap en el número para resetear a 0

### 4. Aplicar transposición en la renderización

En `SongReaderScreen`, al renderizar cada `ChordPosition`, aplicar
`ChordNotation.transpose(chord, transposeSteps)` antes de mostrarlo.

---

## Orden de implementación

1. `ChordNotation.transpose()` + tests unitarios
2. `ReaderViewModel`: transposeSteps en state + métodos
3. `ReaderBottomBar`: controles +/−
4. `SongReaderScreen`: aplicar offset al renderizar acordes

**Esfuerzo estimado:** medio (1 día)

---

## Notas

- La nota objetivo al transponer C# puede ser Db o C# según convención.
  Regla: si el acorde original usa `b`, mantener bemoles; si usa `#`, usar
  sostenidos. Por defecto usar sostenidos para transposiciones hacia arriba
  y bemoles para hacia abajo.
- No tocar el modelo de datos (`Song`, `SongEntity`) — la transposición es
  solo presentación.
- No persistir el offset entre sesiones (v1.1 scope). Persistencia queda
  para v1.2 si hay demanda.
