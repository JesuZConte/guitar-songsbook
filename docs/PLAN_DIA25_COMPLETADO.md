# Día 25 — Collections / Library Redesign (ADR-019)

**Estado: COMPLETADO**
**Fecha:** 2026-04-25

---

## Objetivos cumplidos

### Slice 1 — CollectionsLandingScreen + ruta ALL_SONGS
- Nueva pantalla `CollectionsLandingScreen` como destino del tab Home
- Tarjeta "All Songs" virtual (no es un `PlaylistEntity`) — navega a `Routes.ALL_SONGS`
- Tarjetas de colecciones reales (playlists del usuario) con botón de borrado
- FAB "+" abre `CreateCollectionDialog` (inline; misma lógica que `CreatePlaylistDialog`)
- Icono de ajustes en el TopAppBar de la landing; eliminado de la vista "All Songs"
- `Routes.ALL_SONGS = "all_songs"` añadido
- `HomeScreen` recibe `showSettings: Boolean = true` — se pasa `false` en la ruta ALL_SONGS

### Slice 2 — Bottom nav: 3 tabs → 2 tabs
- Eliminada la entrada "Playlists" de `bottomNavItems`
- Solo quedan Home y Favorites
- La ruta `Routes.PLAYLISTS` sigue registrada en el NavHost (accesible desde colecciones) pero no aparece en la barra

### Slice 3 — "Traditional Songs" sembrada al arrancar
- `PlaylistDao.getByName(name)` nuevo método
- `PlaylistRepository.ensureDefaultCollections()` nuevo método en interfaz e implementación
- `RoomPlaylistRepository.ensureDefaultCollections()`: comprueba si ya existe "Traditional Songs"; si no, la crea y añade pd-001 / pd-002 / pd-003
- Llamada desde `MainActivity.onCreate()` vía `lifecycleScope.launch` — primero llama a `songRepository.getSongs()` para garantizar que las canciones están sembradas antes de insertar los cross-refs

### Slice 4 — Renombre de strings "playlist" → "collection"
- Valores actualizados (no las claves) en EN y ES:
  - `playlists_title`, `playlists_empty_title/body`, `playlists_new_dialog_title`, `playlists_name_label`
  - `home_menu_add_to_playlist`, `home_add_to_playlist_title`, `home_no_playlists_hint`
  - `reader_add_to_playlist_title`, `reader_no_playlists`
- 3 cadenas nuevas: `collections_all_songs`, `collections_all_songs_subtitle`, `collections_my_collections` (EN + ES)

---

## Archivos clave modificados / creados

| Archivo | Cambio |
|---|---|
| `presentation/Routes.kt` | + `ALL_SONGS` |
| `presentation/screens/CollectionsLandingScreen.kt` | nuevo |
| `presentation/screens/HomeScreen.kt` | + parámetro `showSettings: Boolean` |
| `MainActivity.kt` | HOME → CollectionsLandingScreen; + ALL_SONGS; 2 tabs; lifecycleScope seed |
| `data/local/PlaylistDao.kt` | + `getByName` |
| `data/repository/PlaylistRepository.kt` | + `ensureDefaultCollections()` |
| `data/repository/RoomPlaylistRepository.kt` | implementación + companion con IDs |
| `res/values/strings.xml` | 3 strings nuevas + 9 valores actualizados |
| `res/values-es/strings.xml` | ídem en español |

---

## Decisiones de diseño

- **"All Songs" es UI-only**: no se almacena en la tabla `playlists`. Evita migración DB y lógica especial para que no sea borrable.
- **"Traditional Songs" se siembra una sola vez** en `ensureDefaultCollections()` usando un `getByName` check. Si el usuario la borra, no vuelve a aparecer (comportamiento intencional).
- **Strings keys sin cambio**: se actualizaron solo los valores, no las claves (`playlists_*`). Evita refactor masivo de código Kotlin. El renombre semántico completo queda para una iteración futura si se necesita.
- **`Routes.PLAYLISTS` sigue registrado** aunque no esté en el nav bar — garantiza que `PlaylistDetailScreen` siga siendo navegable sin cambios.

---

## Próximo: Día 26

### Opción A — APK firmado + release v1.1
El app tiene suficientes features para publicar en Play Store. El día se dedicaría a:
- Generar keystore y AAB firmado
- Verificar `versionCode` (incrementar a 5+) y `versionName` → `1.1.0`
- Prueba de migración DB en dispositivo real
- Actualizar descripción y capturas en Play Store

### Opción B — Mejoras de polish post-colecciones
- Animaciones de entrada en `CollectionsLandingScreen`
- "Traditional Songs" con nombre localizado (guardando el ID como constante en lugar del nombre)
- Contador de canciones en la tarjeta "All Songs"
- Doble acción en "+" del detalle de colección (agregar existente / crear nueva)

**Recomendación:** Opción A — el producto está listo para v1.1.
