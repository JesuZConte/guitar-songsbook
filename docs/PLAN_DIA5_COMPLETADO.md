# Guitar Songbook — Resumen día 5 y plan día 6

**Fecha:** 26 de marzo, 2026
**Stack:** Kotlin + Jetpack Compose + Room + MVVM + Clean Architecture
**Principios:** Clean Code, SOLID, DRY, Frontend-First

---

## Día 5 — Completado

### Objetivos cumplidos

| # | Paso | Estado |
|---|------|--------|
| 1 | isFavorite en Song + SongEntity + Room migration v1→v2 | Listo |
| 2 | SongDao con getFavorites() y toggleFavorite() | Listo |
| 3 | SongRepository extendido con favoritos | Listo |
| 4 | Botón favorito en SongCard + SongReaderScreen | Listo |
| 5 | FavoritesScreen + FavoritesViewModel | Listo |
| 6 | Bottom Navigation (Home + Favorites) | Listo |
| 7 | Tests: favoritos (7 tests) | Listo |

### Archivos creados / modificados

```
app/src/main/java/com/guitarapp/songsbook/
├── domain/model/
│   └── Song.kt                        ← MODIFICADO: isFavorite = false
├── data/
│   ├── local/
│   │   ├── SongEntity.kt             ← MODIFICADO: @ColumnInfo is_favorite + mappers
│   │   ├── SongDao.kt                ← MODIFICADO: getFavorites(), toggleFavorite()
│   │   └── SongDatabase.kt           ← MODIFICADO: version 2 + MIGRATION_1_2
│   └── repository/
│       ├── SongRepository.kt         ← MODIFICADO: getFavorites(), toggleFavorite()
│       └── AssetSongRepository.kt    ← MODIFICADO: nuevos métodos + ensureSeeded simplificado
├── presentation/
│   ├── viewmodel/
│   │   ├── HomeViewModel.kt          ← MODIFICADO: toggleFavorite(), refreshSongs()
│   │   ├── ReaderViewModel.kt        ← MODIFICADO: toggleFavorite()
│   │   └── FavoritesViewModel.kt     ← NUEVO: loadFavorites(), removeFavorite()
│   └── screens/
│       ├── HomeScreen.kt             ← MODIFICADO: corazón en SongCard
│       ├── SongReaderScreen.kt       ← MODIFICADO: corazón en TopAppBar actions
│       └── FavoritesScreen.kt        ← NUEVO: lista de favoritos con empty state
└── MainActivity.kt                    ← MODIFICADO: Bottom Navigation, Scaffold, tabs

app/src/test/java/com/guitarapp/songsbook/
└── presentation/viewmodel/
    └── FavoritesTest.kt               ← NUEVO: 7 tests favoritos
```

### Conceptos Kotlin aprendidos día 5

- `@ColumnInfo(defaultValue = "0")` — valor por defecto para columna Room (migraciones)
- `Migration(1, 2)` — migración de schema Room (ALTER TABLE)
- `NavigationBar` + `NavigationBarItem` — bottom navigation Material 3
- `currentBackStackEntryAsState()` — observar ruta actual del NavController
- `popUpTo + saveState + restoreState` — navegación entre tabs sin acumular back stack
- `launchSingleTop` — evitar instancias duplicadas al tocar tab activo
- `actions = { }` — slot de acciones a la derecha del TopAppBar
- Optimistic update — actualizar UI inmediatamente, persistir en background

### Decisiones de arquitectura día 5

- **Favoritos separados de playlists:** feature más simple y más usada merece su propio día. Playlists (many-to-many, CRUD completo) va al día 6.
- **Optimistic update:** toggle de favorito actualiza UI local inmediato + persiste en Room en background. UX instantánea sin esperar la DB.
- **ensureSeeded() simplificado:** removimos reseed agresivo en DEBUG porque sobreescribía favoritos. Solo seed si DB vacía. Cambios al JSON requieren desinstalar app manualmente.
- **Bottom nav oculta en Reader:** sin distracciones durante lectura. Mismo patrón Spotify/YouTube.
- **refreshSongs() y loadFavorites() al navegar:** cada tab re-lee de Room al hacerse visible, garantizando sincronización de favoritos entre pantallas.
- **Room migration en vez de destructive:** preserva datos del usuario. Buena práctica para producción desde el inicio.

### Bug encontrado y resuelto

- **Favoritos no sincronizaban entre pantallas:** cada ViewModel tenía su propia copia del estado. El toggle persistía en Room pero la otra pantalla no se enteraba.
- **Causa raíz:** ensureSeeded() con BuildConfig.DEBUG re-importaba JSON en cada query, sobreescribiendo favoritos con REPLACE.
- **Solución:** ensureSeeded() solo importa si count == 0. Tabs llaman refresh al hacerse visibles.

### Estado actual de la app

- 11 canciones en la lista
- Búsqueda por título/artista con debounce
- Filtros por dificultad y género
- Reader con swipe tipo Kindle entre páginas
- Fullscreen con tap to toggle
- Controles de font size
- Favoritos con corazón toggle (Home, Reader, Favorites)
- Bottom navigation: Home + Favorites
- Empty state cuando no hay favoritos
- 42 tests pasando en verde

### Total de tests acumulados

| Suite | Tests | Qué verifica |
|-------|-------|-------------|
| SongJsonParsingTest | 6 | Parsing JSON → domain models |
| SongEntityMappingTest | 4 | Roundtrip Domain ↔ Entity |
| PaginationTest | 7 | Lógica de paginación por secciones |
| ChordLineTest | 5 | Posicionamiento de acordes sobre texto |
| SearchFilterTest | 12 | Búsqueda por texto + filtros género/dificultad |
| FavoritesTest | 7 | Toggle, filtrar, remover favoritos |
| **Total** | **42** | |

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
│   │   │   ├── ReaderViewModel.kt
│   │   │   └── FavoritesViewModel.kt
│   │   └── screens/
│   │       ├── HomeScreen.kt
│   │       ├── SongReaderScreen.kt
│   │       └── FavoritesScreen.kt
│   ├── utils/
│   │   └── ChordFormatter.kt
│   └── MainActivity.kt
├── src/test/java/com/guitarapp/songsbook/
│   ├── data/repository/
│   │   ├── SongJsonParsingTest.kt
│   │   ├── SongEntityMappingTest.kt
│   │   └── PaginationTest.kt
│   ├── presentation/viewmodel/
│   │   ├── SearchFilterTest.kt
│   │   └── FavoritesTest.kt
│   └── utils/
│       └── ChordLineTest.kt
└── build.gradle.kts
```

---

## Día 6 — Plan: Playlists + Clean Code fixes

### Objetivo

Permitir al usuario crear playlists personalizadas para organizar su repertorio. Un guitarrista prepara setlists para ensayos, shows, o agrupa canciones por contexto (fogata, iglesia, fiesta, warm-up). Las playlists son el paso final antes de pulido visual.

Antes de añadir la feature, empezamos con un bloque corto de limpieza para corregir bugs reales y dejar `MainActivity` lista para crecer.

### Pasos planificados

| # | Paso | Tiempo estimado |
|---|------|-----------------|
| 0a | **Fix bug**: `LaunchedEffect(Unit)` en composables "home" y "favorites" de `MainActivity` (actualmente `refreshSongs()` y `loadFavorites()` se llaman en cada recomposición) | 5 min |
| 0b | **Fix bug**: Añadir `isLoading` + `error` a `FavoritesUiState`; manejar error en `loadFavorites()` en lugar de silenciarlo | 10 min |
| 0c | **Refactor**: Extraer `Routes` object con constantes de rutas (`HOME`, `FAVORITES`, `PLAYLISTS`, `READER`) | 5 min |
| 0d | **Refactor**: Extraer `GuitarBottomBar` y `GuitarNavHost` como composables en `MainActivity` | 15 min |
| 1 | Modelo Playlist + PlaylistSongCrossRef (many-to-many) | 20 min |
| 2 | PlaylistDao con queries de relación | 15 min |
| 3 | Actualizar SongDatabase (version 3, nuevas entities) | 15 min |
| 4 | PlaylistRepository interface + implementación | 15 min |
| 5 | PlaylistsViewModel (CRUD + agregar/quitar canciones) | 20 min |
| 6 | PlaylistsScreen (lista de playlists + crear nueva) | 25 min |
| 7 | PlaylistDetailScreen (canciones de una playlist) | 20 min |
| 8 | Agregar tab Playlists a Bottom Navigation (usar Routes object) | 10 min |
| 9 | Agregar canción a playlist desde Reader | 15 min |
| 10 | Tests: playlists | 20 min |

### Conceptos nuevos que veremos

- `LaunchedEffect` — cuándo y por qué envolver side effects en Compose
- `Routes` object — centralizar literales de navegación
- Extraer composables grandes en funciones más pequeñas (SRP en Compose)
- Room many-to-many con @Junction y cross-reference table
- @Transaction para queries con relaciones
- Room migration v2 → v3
- Dialog de Compose (crear playlist, seleccionar playlist)

### Resultado esperado día 6

Tres tabs: Home, Favorites, Playlists. El usuario puede crear playlists con nombre, agregar canciones desde el Reader (botón +), ver el contenido de cada playlist, y remover canciones de una playlist. Las playlists se persisten en Room. `MainActivity` queda limpia y preparada para escalar.

---

## Roadmap restante

| Día | Foco | Estado |
|-----|------|--------|
| 1 | Fundación: proyecto, modelos, JSON, HomeScreen | Completado |
| 2 | Persistencia Room, navegación, detalle | Completado |
| 3 | SongReaderScreen (experiencia core de lectura) | Completado |
| 4 | Búsqueda + filtros | Completado |
| 5 | Favoritos + bottom navigation | Completado |
| 6 | Playlists | Siguiente |
| 7 | Pulido UI + tema oscuro | Pendiente |
| 8 | AdMob + build release | Pendiente |
