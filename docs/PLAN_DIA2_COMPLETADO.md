# Guitar Songbook — Resumen día 2 y plan día 3

**Fecha:** 22 de marzo, 2026
**Stack:** Kotlin + Jetpack Compose + Room + MVVM + Clean Architecture
**Principios:** Clean Code, SOLID, DRY, Frontend-First

---

## Día 2 — Completado

### Objetivos cumplidos

| # | Paso | Estado |
|---|------|--------|
| 1 | Room Entity + TypeConverters | Listo |
| 2 | SongDao (Data Access Object) | Listo |
| 3 | SongDatabase (Room singleton) | Listo |
| 4 | Actualizar Repository para usar Room | Listo |
| 5 | Actualizar MainActivity + arreglar tests | Listo |
| 6 | SongDetailScreen (vista completa con acordes) | Listo |
| 7 | Navegación (NavHost entre Home y Detail) | Listo |
| 8 | Click en SongCard navega al detalle | Listo |
| 9 | Tests: JSON parsing + Entity mapping | Listo |

### Archivos creados / modificados

```
app/src/main/java/com/guitarapp/songsbook/
├── data/
│   ├── local/                          ← NUEVO paquete
│   │   ├── Converters.kt              ← TypeConverters (List → JSON string)
│   │   ├── SongEntity.kt              ← Room Entity + mappers toDomain/fromDomain
│   │   ├── SongDao.kt                 ← Data Access Object (getAll, getById, insertAll, count)
│   │   └── SongDatabase.kt            ← Room Database singleton
│   └── repository/
│       └── AssetSongRepository.kt     ← MODIFICADO: ahora usa Room + seed desde assets
├── presentation/
│   ├── viewmodel/
│   │   └── SongDetailViewModel.kt     ← NUEVO: ViewModel para detalle
│   └── screens/
│       ├── HomeScreen.kt              ← MODIFICADO: agregado onSongClick + clickable cards
│       └── SongDetailScreen.kt        ← NUEVO: detalle con acordes posicionados
└── MainActivity.kt                    ← MODIFICADO: NavHost con rutas home y detail

app/src/test/java/com/guitarapp/songsbook/
└── data/repository/
    ├── SongJsonParsingTest.kt         ← MODIFICADO: test puro sin mocks de Android
    └── SongEntityMappingTest.kt       ← NUEVO: test roundtrip Domain ↔ Entity
```

### Dependencias agregadas

- `androidx.navigation:navigation-compose:2.7.7` — navegación entre pantallas
- `androidx.compose.material:material-icons-extended` — iconos (flecha atrás)
- `room.schemaLocation` configurado en KSP para exportar schemas

### Conceptos Kotlin aprendidos día 2

- `by lazy { }` — inicialización perezosa thread-safe
- `.map { it.toDomain() }` — transformar listas (como stream().map().collect())
- `?.toDomain()` — safe call operator (como Optional.map())
- `companion object` — métodos estáticos (factory methods)
- `return@composable` — early return de una lambda con label
- `@Volatile` + `synchronized` — singleton thread-safe (igual que Java)
- `copy()` — crear variantes de data classes para tests
- `FlowRow` — flexbox con wrap en Compose

### Conceptos de arquitectura aplicados

- **Room como cache local:** JSON se lee una sola vez, se persiste en Room, lecturas siguientes son de DB
- **Entity separada de Domain:** SongEntity (capa data) vs Song (capa domain) — Clean Architecture
- **TypeConverters:** serialización de tipos complejos para columnas SQLite
- **Navegación por rutas:** similar a REST endpoints, con parámetros en el path
- **Callbacks para navegación:** las pantallas no conocen NavController, solo exponen lambdas (SRP)

### Estado actual de la app

- Lista de 10 canciones cargadas desde Room DB
- Toque en una canción navega al detalle
- Detalle muestra: artista, tonalidad, capo, dificultad, chips de acordes, notas
- Contenido de la canción con acordes posicionados sobre el texto (fuente monospace)
- Botón atrás funciona
- 10 tests pasando en verde

---

## Estructura completa del proyecto (actualizada)

```
app/
├── assets/
│   └── songs.json
├── schemas/                                ← Room schema export (auto-generado)
├── src/main/java/com/guitarapp/songsbook/
│   ├── domain/
│   │   └── model/
│   │       └── Song.kt                    ← Song, SongSection, SongLine, ChordPosition
│   ├── data/
│   │   ├── local/
│   │   │   ├── Converters.kt
│   │   │   ├── SongEntity.kt
│   │   │   ├── SongDao.kt
│   │   │   └── SongDatabase.kt
│   │   └── repository/
│   │       ├── SongRepository.kt          ← Interface
│   │       ├── AssetSongRepository.kt     ← Implementación (assets + Room)
│   │       └── SongbookResponse.kt        ← DTO para JSON
│   ├── presentation/
│   │   ├── viewmodel/
│   │   │   ├── HomeViewModel.kt
│   │   │   └── SongDetailViewModel.kt
│   │   └── screens/
│   │       ├── HomeScreen.kt
│   │       └── SongDetailScreen.kt
│   ├── utils/                              ← Vacío (disponible para futuro)
│   └── MainActivity.kt
├── src/test/java/com/guitarapp/songsbook/
│   └── data/repository/
│       ├── SongJsonParsingTest.kt
│       └── SongEntityMappingTest.kt
└── build.gradle.kts
```

---

## Día 3 — Plan: SongReaderScreen

### Objetivo

Crear la pantalla de lectura real de canciones — la experiencia core de la app. El usuario ve la canción como la vería en un cancionero físico, con acordes sobre el texto, navegación fluida y formato optimizado para leer mientras toca guitarra.

### Pasos planificados

| # | Paso | Tiempo estimado |
|---|------|-----------------|
| 1 | Refactorizar SongDetailScreen → SongReaderScreen | 20 min |
| 2 | Renderizado mejorado de acordes con colores | 20 min |
| 3 | Auto-scroll (scroll lento automático para tocar) | 25 min |
| 4 | Controles de tamaño de fuente (+/-) | 15 min |
| 5 | Barra de controles inferior (font size, auto-scroll, sección) | 20 min |
| 6 | Swipe horizontal para siguiente/anterior canción | 25 min |
| 7 | Modo pantalla completa (ocultar top bar) | 15 min |
| 8 | Tests del día 3 | 20 min |

### Resultado esperado día 3

Pantalla de lectura profesional: acordes en color sobre el texto, auto-scroll ajustable para tocar sin manos, pinch/botones para cambiar tamaño de fuente, swipe para cambiar de canción, modo fullscreen para maximizar espacio de lectura.

---

## Roadmap restante

| Día | Foco | Estado |
|-----|------|--------|
| 1 | Fundación: proyecto, modelos, JSON, HomeScreen | Completado |
| 2 | Persistencia Room, navegación, detalle | Completado |
| 3 | SongReaderScreen (experiencia core de lectura) | Siguiente |
| 4 | Búsqueda + filtros | Pendiente |
| 5 | Playlists + favoritos | Pendiente |
| 6 | Pulido UI + tema oscuro | Pendiente |
| 7 | AdMob + build release | Pendiente |
