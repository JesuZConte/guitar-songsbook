# Guitar Songbook — Resumen día 6 y plan día 7

**Fecha:** 27 de marzo, 2026
**Stack:** Kotlin + Jetpack Compose + Room + MVVM + Clean Architecture
**Principios:** Clean Code, SOLID, DRY, Frontend-First

---

## Día 6 — Completado

### Objetivos cumplidos

| # | Paso | Estado |
|---|------|--------|
| 0a | Fix bug: `LaunchedEffect(Unit)` en composables "home" y "favorites" | Listo |
| 0b | Fix bug: `isLoading` + `error` en `FavoritesUiState`, eliminar silent fail | Listo |
| 0c | Refactor: `Routes` object con constantes de navegación | Listo |
| 0d | Refactor: extraer `GuitarBottomBar` y `GuitarNavHost` de `MainActivity` | Listo |
| 1 | Modelo `Playlist` (domain) + `PlaylistEntity` + `PlaylistSongCrossRef` (Room) | Listo |
| 2 | `PlaylistDao` con queries relacionales y `@Transaction` | Listo |
| 3 | `SongDatabase` v3 + `MIGRATION_2_3` (tablas `playlists` + `playlist_songs`) | Listo |
| 4 | `PlaylistRepository` interface + `RoomPlaylistRepository` | Listo |
| 5 | `PlaylistsViewModel` (CRUD + add/remove canciones + detalle) | Listo |
| 6 | `PlaylistsScreen` (lista + FAB + dialog crear playlist) | Listo |
| 7 | `PlaylistDetailScreen` (canciones de una playlist + remover) | Listo |
| 8 | Tab Playlists en Bottom Navigation (3 tabs) | Listo |
| 9 | `PlaylistPickerDialog` en `SongReaderScreen` (botón añadir a playlist) | Listo |
| 10 | Tests: playlists (14 tests) | Listo |

### Archivos creados / modificados

```
app/src/main/java/com/guitarapp/songsbook/
├── domain/model/
│   └── Playlist.kt                        ← NUEVO: data class Playlist
├── data/
│   ├── local/
│   │   ├── PlaylistEntity.kt              ← NUEVO: PlaylistEntity + PlaylistSongCrossRef
│   │   ├── PlaylistDao.kt                 ← NUEVO: CRUD + @Transaction + isSongInPlaylist
│   │   └── SongDatabase.kt               ← MODIFICADO: version 3 + MIGRATION_2_3
│   └── repository/
│       ├── PlaylistRepository.kt          ← NUEVO: interface
│       ├── RoomPlaylistRepository.kt      ← NUEVO: implementación Room
│       └── AssetSongRepository.kt        ← MODIFICADO: import BuildConfig eliminado
├── presentation/
│   ├── Routes.kt                          ← NUEVO: HOME, FAVORITES, PLAYLISTS, READER, PLAYLIST_DETAIL
│   ├── viewmodel/
│   │   ├── FavoritesViewModel.kt          ← MODIFICADO: isLoading + error en FavoritesUiState
│   │   └── PlaylistsViewModel.kt          ← NUEVO: PlaylistsUiState + PlaylistDetailUiState
│   └── screens/
│       ├── FavoritesScreen.kt             ← MODIFICADO: manejo isLoading + error
│       ├── PlaylistsScreen.kt             ← NUEVO: lista + FAB + CreatePlaylistDialog
│       ├── PlaylistDetailScreen.kt        ← NUEVO: canciones de playlist + remove
│       └── SongReaderScreen.kt           ← MODIFICADO: PlaylistPickerDialog + botón PlaylistAdd
└── MainActivity.kt                        ← MODIFICADO: GuitarBottomBar + GuitarNavHost extraídos,
                                                         3 tabs, Routes, LaunchedEffect

app/src/test/java/com/guitarapp/songsbook/
└── data/repository/
    └── PlaylistTest.kt                    ← NUEVO: 14 tests playlists
```

### Conceptos Kotlin / Android aprendidos día 6

- `LaunchedEffect(Unit)` — side effect que se ejecuta una sola vez al entrar a la composición, no en cada recomposición
- `LaunchedEffect(key)` — se re-ejecuta cuando cambia la clave (usado para cargar detalle de playlist)
- `object Routes` — singleton Kotlin para centralizar constantes de navegación, con funciones helper (`reader(id)`, `playlistDetail(id)`)
- `@ForeignKey(onDelete = CASCADE)` — Room elimina automáticamente cross-refs al borrar una playlist o canción
- `@Index` en Room — índice en `song_id` de la tabla `playlist_songs` para acelerar consultas inversas
- `@Transaction` en Room — garantiza atomicidad en queries que unen múltiples tablas
- `@PrimaryKey(autoGenerate = true)` con `Long` — clave primaria auto-incremental para playlists
- `AlertDialog` con `LazyColumn` — dialog con lista scrollable para seleccionar playlist
- `FloatingActionButton` — botón de acción flotante Material 3
- Extracción de composables privados — SRP en Compose: cada función hace una sola cosa

### Decisiones de arquitectura día 6

- **`PlaylistRepository` separado de `SongRepository`:** ISP (Interface Segregation). Las pantallas de playlists no necesitan conocer nada de búsqueda o favoritos, y viceversa.
- **`PlaylistsViewModel` con dos `StateFlow` (`uiState` + `detailState`):** la lista de playlists y el detalle de una playlist son contextos distintos. Un solo ViewModel evita instanciar dos, pero dos estados mantienen separación de responsabilidades.
- **`ForeignKey.CASCADE` en `playlist_songs`:** eliminar una playlist limpia automáticamente sus referencias. Sin esto, quedarían orphan rows. Mismo comportamiento al eliminar una canción.
- **`isSongInPlaylist` antes de `addSong`:** previene duplicados sin depender de constraint UNIQUE (más explícito y testeable).
- **`Routes` object con funciones helper:** `Routes.reader(songId)` es más seguro que `"reader/$songId"` inline — el compilador detecta si cambias la firma.
- **`GuitarBottomBar` y `GuitarNavHost` como funciones `private @Composable`:** `MainActivity.onCreate()` pasó de ~90 líneas a ~40. Cada función tiene una responsabilidad. Añadir un tab en el futuro = una línea en `bottomNavItems` + un `composable {}` en `GuitarNavHost`.
- **`PlaylistPickerDialog` en Reader carga playlists al abrir:** `playlistsViewModel.loadPlaylists()` se llama al presionar el botón, no en init, porque el usuario puede haber creado playlists después de abrir el Reader.

### Bug corregido

- **Side effects en composables sin `LaunchedEffect`:** `refreshSongs()` y `loadFavorites()` se llamaban directamente en el cuerpo del composable, ejecutándose en **cada recomposición** (cambio de estado, rotación, etc.). Causaba refreshes innecesarios y potenciales race conditions. Solución: `LaunchedEffect(Unit)`.

### Estado actual de la app

- 11 canciones en la lista
- Búsqueda por título/artista con debounce
- Filtros por dificultad y género
- Reader con swipe tipo Kindle entre páginas
- Fullscreen con tap to toggle
- Controles de font size
- Favoritos con corazón toggle (Home, Reader, Favorites)
- **Playlists: crear, ver detalle, eliminar playlist**
- **Añadir canción a playlist desde el Reader**
- **Remover canción de playlist desde el detalle**
- Bottom navigation: Home + Favorites + Playlists
- Empty states en todas las pantallas
- Loading + error state en todas las pantallas
- 56 tests pasando en verde

### Total de tests acumulados

| Suite | Tests | Qué verifica |
|-------|-------|-------------|
| SongJsonParsingTest | 6 | Parsing JSON → domain models |
| SongEntityMappingTest | 4 | Roundtrip Domain ↔ Entity |
| PaginationTest | 7 | Lógica de paginación por secciones |
| ChordLineTest | 5 | Posicionamiento de acordes sobre texto |
| SearchFilterTest | 12 | Búsqueda por texto + filtros género/dificultad |
| FavoritesTest | 7 | Toggle, filtrar, remover favoritos |
| PlaylistTest | 14 | CRUD playlists, cross-ref, operaciones de lista |
| ExampleUnitTest | 1 | Template |
| **Total** | **56** | |

---

## Estructura completa del proyecto (actualizada)

```
app/
├── assets/
│   └── songs.json (11 canciones)
├── schemas/
│   └── com.guitarapp.songsbook.data.local.SongDatabase/
│       ├── 1.json
│       ├── 2.json
│       └── 3.json
├── src/main/java/com/guitarapp/songsbook/
│   ├── domain/
│   │   └── model/
│   │       ├── Song.kt
│   │       └── Playlist.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── Converters.kt
│   │   │   ├── SongEntity.kt
│   │   │   ├── SongDao.kt
│   │   │   ├── SongDatabase.kt
│   │   │   ├── PlaylistEntity.kt
│   │   │   └── PlaylistDao.kt
│   │   └── repository/
│   │       ├── SongRepository.kt
│   │       ├── AssetSongRepository.kt
│   │       ├── SongbookResponse.kt
│   │       ├── PlaylistRepository.kt
│   │       └── RoomPlaylistRepository.kt
│   ├── presentation/
│   │   ├── Routes.kt
│   │   ├── viewmodel/
│   │   │   ├── HomeViewModel.kt
│   │   │   ├── ReaderViewModel.kt
│   │   │   ├── FavoritesViewModel.kt
│   │   │   └── PlaylistsViewModel.kt
│   │   └── screens/
│   │       ├── HomeScreen.kt
│   │       ├── SongReaderScreen.kt
│   │       ├── FavoritesScreen.kt
│   │       ├── PlaylistsScreen.kt
│   │       └── PlaylistDetailScreen.kt
│   ├── utils/
│   │   └── ChordFormatter.kt
│   └── MainActivity.kt
├── src/test/java/com/guitarapp/songsbook/
│   ├── data/repository/
│   │   ├── SongJsonParsingTest.kt
│   │   ├── SongEntityMappingTest.kt
│   │   ├── PaginationTest.kt
│   │   └── PlaylistTest.kt
│   ├── presentation/viewmodel/
│   │   ├── SearchFilterTest.kt
│   │   └── FavoritesTest.kt
│   └── utils/
│       └── ChordLineTest.kt
└── build.gradle.kts
```

---

## Día 7 — Plan: Pulido UI + Tema Oscuro

### Objetivo

Elevar la calidad visual de la app al nivel de una app publicable. Un guitarrista usa la app en el escenario, en un ensayo con poca luz, o bajo el sol. El tema oscuro es esencial. Los detalles visuales — tipografía consistente, espaciados, animaciones, colores de sección — son lo que separa una app funcional de una app que da gusto usar.

### Pasos planificados

| # | Paso | Tiempo estimado |
|---|------|-----------------|
| 1 | Tema oscuro: paleta de colores oscura en `Color.kt` + `Theme.kt` con `dynamicColor = false` | 20 min |
| 2 | Soporte automático sistema claro/oscuro (`isSystemInDarkTheme()`) | 10 min |
| 3 | Revisar y ajustar colores de sección en `SongReaderScreen` para ambos temas | 15 min |
| 4 | Tipografía: escala de fuentes consistente usando `MaterialTheme.typography` en toda la app | 20 min |
| 5 | Animaciones de transición entre pantallas (`NavHost` con `AnimatedContentTransitionScope`) | 25 min |
| 6 | Animación de favorito (latido al hacer toggle del corazón) | 15 min |
| 7 | `SongCard` en HomeScreen: mejorar layout con chip de dificultad con color semántico (verde/amarillo/rojo) | 20 min |
| 8 | Splash screen / icono de app personalizado | 20 min |
| 9 | Revisión final: consistencia visual entre todas las pantallas | 15 min |
| 10 | Tests: snapshot o verificación de tema | 10 min |

### Conceptos nuevos que veremos

- `isSystemInDarkTheme()` — detectar preferencia del sistema en Compose
- `ColorScheme` oscuro vs claro — cómo Room y Material 3 manejan la paleta dual
- `NavHost` con transiciones — `enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition`
- `animateFloatAsState` — animación de escala para el botón de favorito
- `AnimatedContent` — transiciones de contenido dentro de una pantalla
- `SplashScreen API` — splash screen nativo de Android 12+

### Resultado esperado día 7

La app se ve profesional en modo claro y oscuro. Las transiciones entre pantallas son fluidas. El toggle de favorito tiene feedback visual. Los colores de dificultad son semánticos (verde = fácil, amarillo = intermedio, rojo = avanzado). La app tiene icono e identidad visual propia. Lista para que usuarios reales la prueben.

---

## Roadmap restante

| Día | Foco | Estado |
|-----|------|--------|
| 1 | Fundación: proyecto, modelos, JSON, HomeScreen | Completado |
| 2 | Persistencia Room, navegación, detalle | Completado |
| 3 | SongReaderScreen (experiencia core de lectura) | Completado |
| 4 | Búsqueda + filtros | Completado |
| 5 | Favoritos + bottom navigation | Completado |
| 6 | Playlists + clean code | Completado |
| 7 | Pulido UI + tema oscuro | Siguiente |
| 8 | AdMob + build release | Pendiente |
