# Guitar Songbook — Resumen día 3 y plan día 4

**Fecha:** 24 de marzo, 2026
**Stack:** Kotlin + Jetpack Compose + Room + MVVM + Clean Architecture
**Principios:** Clean Code, SOLID, DRY, Frontend-First

---

## Día 3 — Completado

### Objetivos cumplidos

| # | Paso | Estado |
|---|------|--------|
| 1 | ReaderViewModel con paginación | Listo |
| 2 | SongReaderScreen con HorizontalPager (swipe tipo Kindle) | Listo |
| 3 | Fullscreen + tap to toggle (patrón Kindle) | Listo |
| 4 | Navegación actualizada + reseed en debug | Listo |
| 5 | Renderizado mejorado: header en página 1 + colores por sección | Listo |
| 6 | Refactor buildChordLine a utils/ (DRY) | Listo |
| 7 | Tests: paginación (7 tests) + buildChordLine (5 tests) | Listo |

### Archivos creados / modificados

```
app/src/main/java/com/guitarapp/songsbook/
├── data/repository/
│   └── AssetSongRepository.kt        ← MODIFICADO: reseed en debug con BuildConfig
├── presentation/
│   ├── viewmodel/
│   │   ├── ReaderViewModel.kt         ← NUEVO: paginación, font size, fullscreen
│   │   └── SongDetailViewModel.kt     ← Sin cambios (se mantiene para detalle)
│   └── screens/
│       ├── SongReaderScreen.kt        ← NUEVO: HorizontalPager, header, colores sección
│       ├── SongDetailScreen.kt        ← MODIFICADO: buildChordLine movido a utils
│       └── HomeScreen.kt              ← Sin cambios
├── utils/
│   └── ChordFormatter.kt             ← NUEVO: buildChordLine (extraído de screens, DRY)
└── MainActivity.kt                    ← MODIFICADO: ruta reader/, ReaderViewModel

app/assets/
└── songs.json                         ← MODIFICADO: 11 canciones, Zombie completa para test

app/build.gradle.kts                   ← MODIFICADO: buildConfig = true, compose foundation

app/src/test/java/com/guitarapp/songsbook/
├── data/repository/
│   └── PaginationTest.kt             ← NUEVO: 7 tests paginación
└── utils/
    └── ChordLineTest.kt              ← NUEVO: 5 tests formateo acordes
```

### Dependencias agregadas

- `androidx.compose.foundation:foundation` — HorizontalPager para swipe entre páginas
- `buildConfig = true` en buildFeatures — acceso a BuildConfig.DEBUG

### Conceptos Kotlin aprendidos día 3

- `HorizontalPager` + `rememberPagerState` — ViewPager declarativo en Compose
- `LaunchedEffect` + `snapshotFlow` — observar cambios de estado de Compose en coroutines
- `AnimatedVisibility` — animaciones de entrada/salida declarativas
- `_uiState.update { }` — actualización thread-safe de StateFlow (como AtomicReference)
- `indication = null` — clickable sin efecto visual ripple
- `MutableInteractionSource` — fuente de interacción para gestos personalizados
- `internal` — visibilidad de módulo (como package-private en Java, para testing)
- `chunked()` — dividir lista en sublistas de tamaño fijo
- `BuildConfig.DEBUG` — constante de compile time para distinguir debug/release
- `FontStyle.Italic` — estilo tipográfico en Compose

### Decisiones de arquitectura día 3

- **Paginación por secciones en vez de auto-scroll:** decisión del usuario/guitarrista. Experiencia tipo Kindle/cancionero físico. Swipe horizontal para pasar página.
- **SECTIONS_PER_PAGE = 4:** valor fijo por ahora. Futuro: cálculo dinámico según font size y pantalla.
- **buildChordLine extraído a utils/:** estaba duplicado en SongDetailScreen y SongReaderScreen. Aplicamos DRY moviendo a ChordFormatter.kt.
- **Reseed en debug:** BuildConfig.DEBUG fuerza re-importación del JSON cada vez que se ejecuta en desarrollo. En release solo importa si DB está vacía.
- **Tap to toggle fullscreen:** patrón estándar de apps de lectura (Kindle, PDF readers). Sin distracciones durante la lectura.

### Feedback del usuario (guitarrista)

- Auto-scroll descartado: no es la experiencia que busca un guitarrista de vieja escuela
- Modelo mental: cancionero/revista física, pasar páginas con el dedo
- Colores de sección: funcionales pero suaves, evaluar contraste más adelante
- Pruebas en Samsung real: gestos funcionan correctamente, emulador no es bueno para swipe

### Estado actual de la app

- 11 canciones en la lista (Zombie con letra completa)
- Toque en canción → reader con swipe horizontal entre páginas
- Header en primera página (título, artista, key, capo, notas)
- Colores por tipo de sección (verse, chorus, intro, outro, bridge)
- Controles de font size (+/-)
- Fullscreen con tap to toggle
- Indicador de página ("2 / 3")
- 23 tests pasando en verde

### Total de tests acumulados

| Suite | Tests | Qué verifica |
|-------|-------|-------------|
| SongJsonParsingTest | 6 | Parsing JSON → domain models |
| SongEntityMappingTest | 4 | Roundtrip Domain ↔ Entity |
| PaginationTest | 7 | Lógica de paginación por secciones |
| ChordLineTest | 5 | Posicionamiento de acordes sobre texto |
| **Total** | **23** | |

---

## Estructura completa del proyecto (actualizada)

```
app/
├── assets/
│   └── songs.json (11 canciones)
├── schemas/
├── src/main/java/com/guitarapp/songsbook/
│   ├── domain/
│   │   └── model/
│   │       └── Song.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── Converters.kt
│   │   │   ├── SongEntity.kt
│   │   │   ├── SongDao.kt
│   │   │   └── SongDatabase.kt
│   │   └── repository/
│   │       ├── SongRepository.kt
│   │       ├── AssetSongRepository.kt
│   │       └── SongbookResponse.kt
│   ├── presentation/
│   │   ├── viewmodel/
│   │   │   ├── HomeViewModel.kt
│   │   │   ├── SongDetailViewModel.kt
│   │   │   └── ReaderViewModel.kt
│   │   └── screens/
│   │       ├── HomeScreen.kt
│   │       ├── SongDetailScreen.kt
│   │       └── SongReaderScreen.kt
│   ├── utils/
│   │   └── ChordFormatter.kt
│   └── MainActivity.kt
├── src/test/java/com/guitarapp/songsbook/
│   ├── data/repository/
│   │   ├── SongJsonParsingTest.kt
│   │   ├── SongEntityMappingTest.kt
│   │   └── PaginationTest.kt
│   └── utils/
│       └── ChordLineTest.kt
└── build.gradle.kts
```

---

## Día 4 — Plan: Búsqueda y filtros

### Objetivo

Permitir al usuario encontrar canciones rápidamente. Un guitarrista con 50+ canciones necesita buscar por título, artista, o filtrar por dificultad/género. La búsqueda debe ser instantánea (local, sin red).

### Pasos planificados

| # | Paso | Tiempo estimado |
|---|------|-----------------|
| 1 | Agregar query de búsqueda al SongDao | 10 min |
| 2 | Extender SongRepository con método de búsqueda | 10 min |
| 3 | SearchViewModel con debounce | 20 min |
| 4 | SearchBar en HomeScreen | 20 min |
| 5 | Filtros por dificultad y género | 25 min |
| 6 | Chips de filtro activos en la UI | 20 min |
| 7 | Limpiar SongDetailScreen (evaluar si se mantiene o se elimina) | 10 min |
| 8 | Tests: búsqueda + filtros | 20 min |

### Conceptos nuevos que veremos

- Queries LIKE en Room (búsqueda por texto)
- Debounce en coroutines (no buscar en cada keystroke)
- SearchBar de Material 3
- FilterChip de Material 3
- Combine de StateFlows (query + filtros → resultados)

### Resultado esperado día 4

Desde la HomeScreen el usuario puede escribir texto para buscar por título o artista, y usar chips para filtrar por dificultad (beginner, intermediate, advanced) o género. Los resultados se actualizan en tiempo real. La búsqueda es local e instantánea.

---

## Roadmap restante

| Día | Foco | Estado |
|-----|------|--------|
| 1 | Fundación: proyecto, modelos, JSON, HomeScreen | Completado |
| 2 | Persistencia Room, navegación, detalle | Completado |
| 3 | SongReaderScreen (experiencia core de lectura) | Completado |
| 4 | Búsqueda + filtros | Siguiente |
| 5 | Playlists + favoritos | Pendiente |
| 6 | Pulido UI + tema oscuro | Pendiente |
| 7 | AdMob + build release | Pendiente |
