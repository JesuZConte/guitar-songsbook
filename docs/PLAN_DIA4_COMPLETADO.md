# Guitar Songbook — Resumen día 4 y plan día 5

**Fecha:** 25 de marzo, 2026
**Stack:** Kotlin + Jetpack Compose + Room + MVVM + Clean Architecture
**Principios:** Clean Code, SOLID, DRY, Frontend-First

---

## Día 4 — Completado

### Objetivos cumplidos

| # | Paso | Estado |
|---|------|--------|
| 1 | Queries de búsqueda en SongDao (LIKE, DISTINCT) | Listo |
| 2 | Extender SongRepository con búsqueda y filtros | Listo |
| 3 | HomeViewModel con debounce, query + filtros combinados | Listo |
| 4 | SearchBar + FilterChips en HomeScreen | Listo |
| 5 | Eliminar SongDetailScreen y SongDetailViewModel (código muerto) | Listo |
| 6 | Tests: búsqueda + filtros (12 tests) | Listo |

### Archivos creados / modificados

```
app/src/main/java/com/guitarapp/songsbook/
├── data/
│   ├── local/
│   │   └── SongDao.kt                ← MODIFICADO: search(), getAllGenres(), getAllDifficulties()
│   └── repository/
│       ├── SongRepository.kt         ← MODIFICADO: searchSongs(), getGenres(), getDifficulties()
│       └── AssetSongRepository.kt    ← MODIFICADO: nuevos métodos + ensureSeeded() (DRY)
├── presentation/
│   ├── viewmodel/
│   │   ├── HomeViewModel.kt          ← MODIFICADO: búsqueda con debounce, filtros, clearFilters
│   │   └── SongDetailViewModel.kt    ← ELIMINADO (código muerto)
│   └── screens/
│       ├── HomeScreen.kt             ← MODIFICADO: SearchBar, FilterChips, EmptyContent
│       └── SongDetailScreen.kt       ← ELIMINADO (código muerto)
└── MainActivity.kt                    ← Sin cambios

app/src/test/java/com/guitarapp/songsbook/
└── presentation/viewmodel/
    └── SearchFilterTest.kt            ← NUEVO: 12 tests búsqueda + filtros
```

### Conceptos Kotlin aprendidos día 4

- `debounce(300)` — esperar antes de ejecutar búsqueda (evita búsqueda por cada tecla)
- `collectLatest` — cancela operación anterior si llega una nueva (como switchMap en RxJava)
- `FlowPreview` — opt-in para API experimental de Flow (debounce)
- `LIKE '%' || :query || '%'` — búsqueda con wildcards en Room/SQLite
- `DISTINCT` — valores únicos en queries SQL
- `OutlinedTextField` — campo de texto Material 3 con bordes
- `FilterChip` — chip seleccionable de Material 3
- `contains(query, ignoreCase = true)` — búsqueda case-insensitive en Kotlin

### Decisiones de arquitectura día 4

- **Búsqueda por texto en Room, filtros en memoria:** SQL LIKE para búsqueda textual (eficiente con índices), filtros de género/dificultad en memoria (pocas canciones, instantáneo). Evita queries SQL dinámicas complejas — YAGNI.
- **Debounce 300ms:** balance entre responsividad y no sobrecargar queries. Estándar de la industria.
- **Filtros dinámicos:** géneros y dificultades se leen de la DB, no están hardcodeados. Si agregas canciones de un género nuevo, el chip aparece automáticamente.
- **Toggle behavior en chips:** tocar chip seleccionado lo deselecciona. UX intuitiva sin botón extra de "limpiar".
- **Eliminación de SongDetailScreen:** código muerto. SongReaderScreen cubre la misma funcionalidad y más. DRY aplicado.
- **ensureSeeded() extraído:** lógica de verificación de seed centralizada. Todos los métodos del repository la llaman — imposible olvidarla al agregar métodos nuevos.

### Estado actual de la app

- 11 canciones en la lista
- Búsqueda por título o artista con debounce
- Filtros por dificultad (beginner, intermediate, advanced)
- Filtros por género (Rock, Britpop, Latin Rock, etc.)
- Combinación de búsqueda + filtros
- "No songs found" cuando no hay resultados
- Botón limpiar (X) en barra de búsqueda
- Reader con swipe tipo Kindle
- Fullscreen con tap to toggle
- Controles de font size
- 35 tests pasando en verde

### Total de tests acumulados

| Suite | Tests | Qué verifica |
|-------|-------|-------------|
| SongJsonParsingTest | 6 | Parsing JSON → domain models |
| SongEntityMappingTest | 4 | Roundtrip Domain ↔ Entity |
| PaginationTest | 7 | Lógica de paginación por secciones |
| ChordLineTest | 5 | Posicionamiento de acordes sobre texto |
| SearchFilterTest | 12 | Búsqueda por texto + filtros género/dificultad |
| **Total** | **35** | |

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
│   │   │   └── ReaderViewModel.kt
│   │   └── screens/
│   │       ├── HomeScreen.kt
│   │       └── SongReaderScreen.kt
│   ├── utils/
│   │   └── ChordFormatter.kt
│   └── MainActivity.kt
├── src/test/java/com/guitarapp/songsbook/
│   ├── data/repository/
│   │   ├── SongJsonParsingTest.kt
│   │   ├── SongEntityMappingTest.kt
│   │   └── PaginationTest.kt
│   ├── presentation/viewmodel/
│   │   └── SearchFilterTest.kt
│   └── utils/
│       └── ChordLineTest.kt
└── build.gradle.kts
```

---

## Día 5 — Plan: Playlists y favoritos

### Objetivo

Permitir al usuario organizar sus canciones en playlists personalizadas y marcar favoritos. Un guitarrista prepara setlists para ensayos, repertorios para shows, o agrupa canciones por contexto (fogata, iglesia, fiesta). Los favoritos son un acceso rápido a las canciones más tocadas.

### Pasos planificados

| # | Paso | Tiempo estimado |
|---|------|-----------------|
| 1 | Modelo de dominio Playlist + actualizar Song con isFavorite | 15 min |
| 2 | PlaylistEntity + PlaylistSongCrossRef (Room many-to-many) | 20 min |
| 3 | PlaylistDao + actualizar SongDao con favoritos | 15 min |
| 4 | Actualizar SongDatabase (nueva entity + migration) | 10 min |
| 5 | PlaylistRepository interface + implementación | 15 min |
| 6 | Botón favorito en SongCard + SongReaderScreen | 20 min |
| 7 | PlaylistsScreen (crear, ver, editar playlists) | 30 min |
| 8 | Bottom navigation (Home, Favorites, Playlists) | 20 min |
| 9 | Tests: favoritos + playlists | 20 min |

### Conceptos nuevos que veremos

- Room many-to-many relationships (cross-reference table)
- Database migration (version 1 → 2)
- Bottom Navigation Bar en Compose
- Multi-screen navigation con tabs
- Room @Transaction para queries complejas

### Resultado esperado día 5

La app tiene navegación inferior con tres tabs: Home (lista completa con búsqueda), Favorites (canciones marcadas), Playlists (listas personalizadas). El usuario puede marcar favoritos con un toque y crear playlists para organizar su repertorio.

---

## Roadmap restante

| Día | Foco | Estado |
|-----|------|--------|
| 1 | Fundación: proyecto, modelos, JSON, HomeScreen | Completado |
| 2 | Persistencia Room, navegación, detalle | Completado |
| 3 | SongReaderScreen (experiencia core de lectura) | Completado |
| 4 | Búsqueda + filtros | Completado |
| 5 | Playlists + favoritos | Siguiente |
| 6 | Pulido UI + tema oscuro | Pendiente |
| 7 | AdMob + build release | Pendiente |
