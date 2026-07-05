# PLAN DÍA 30 — Rendimiento: transiciones lentas

**Rol:** Tech Lead review · **Fecha:** 2026-07-03 · **Versión base:** 1.4.4 (versionCode 11)

## 1. Diagnóstico

### Qué se revisó
- Commit `81306c0` (fix colisión de acordes, ChordFormatter/ChordLine)
- Trabajo sin commitear (edit-version routing, import por VIEW intent, `resolveVersionIndexOnReload`)
- Camino de renderizado del reader (`VirtualPagedSong`, ADR-013)
- Capa de datos (`AssetSongRepository`, `SongDao`, Converters)
- Navegación (`GuitarNavHost`, transiciones tween 300ms)

### Conclusión principal
**Ningún cambio reciente introduce una regresión algorítmica.** El nuevo
`layoutChordRow()` tiene el mismo costo que el código que reemplazó (O(n) con un
sort trivial por línea). Los costos que hacen lentas las transiciones son
**preexistentes y escalan con los datos**: la librería crece con el workflow de
smart import y las canciones importadas son cada vez más largas (ej. "Loca" —
Chico Trujillo, añadida en el último commit). La correlación temporal con "el
último cambio" es casi seguro crecimiento de datos, no el código del commit.

### Hallazgos (por impacto estimado)

| # | Hallazgo | Ubicación | Cuándo duele |
|---|----------|-----------|--------------|
| 1 | `VirtualPagedSong` sub-compone y mide **cada línea de la canción en cada measure pass** del `SubcomposeLayout`; la primera composición ocurre durante el slide de 300ms | `SongContentComponents.kt:113-147` | Al abrir el reader, peor con canciones largas |
| 2 | `pagerState.currentPageOffsetFraction` leído en composición del scope grande → recompone el subtree completo del pager en cada frame de swipe | `SongContentComponents.kt:198` | Al pasar página |
| 3 | Cada página del `HorizontalPager` compone el `FullSongColumn` completo (riesgo ya anotado como "Negative" en ADR-013, mitigación `movableContentOf` nunca aplicada) | `SongContentComponents.kt` (PageSlice) | Canciones largas, swipe |
| 4 | **N+1 queries:** `getSongs()` = `getAll()` + una query de versiones **por canción**; igual en `searchSongs()` y `findSongsByTitle()` | `AssetSongRepository.kt:22-29,41-45` | Listas, búsqueda; crece linealmente con la librería |
| 5 | Las listas cargan el **contenido completo** de cada canción (Gson parsea `List<SongSection>` por fila en Converters) solo para mostrar título/artista | `SongDao.kt:12-13`, `Converters.kt:29-31` | Home, All Songs, favoritos, colecciones |
| 6 | `LaunchedEffect(Unit) { loadPlaylists() }` se relanza en **cada regreso** al home; `loadFavorites`/`loadPlaylistDetail` igual — el trabajo compite con la animación de pop | `MainActivity.kt:355,382,395` | Al volver atrás |
| 7 | *(Sin commitear)* `handleIncomingIntent` hace I/O bloqueante de `contentResolver` en el main thread (`lifecycleScope.launch` = Main) | `MainActivity.kt` (diff actual) | Solo apertura vía VIEW intent |
| 8 | No hay Baseline Profiles ni Macrobenchmark; si las pruebas de percepción se hacen en build **debug**, Compose es varias veces más lento | `app/build.gradle.kts` | Siempre |

### Limitación del diagnóstico
No había dispositivo/emulador conectado: este análisis es estático. **Ninguna
fase de implementación arranca sin confirmar con medición (Fase 0).**

## 1b. Resultados de la Fase 0 (ejecutada 2026-07-04, Galaxy A50 físico)

Medición con `dumpsys gfxinfo` por flujo, mismas canciones en debug y release
(La Voz De Los 80 = 10 páginas; Scarborough Fair = 2 páginas). La app instalada
en el teléfono resultó ser **build debug** (instalada 2026-07-03 21:12).

| Flujo | Debug | Release |
|---|---|---|
| Abrir reader (canción larga) | 42% janky / **p95 400ms** | 33% janky / **p95 400ms** |
| Pop reader→lista | 100% janky / 350ms ×2 | 59% janky / p95 150ms |
| 1er swipe a página nueva (larga) | 62% / p90 150ms | 62% / p90 133ms |
| 3 swipes seguidos (larga) | 56% / p99 117ms | 31% / p99 73ms |
| Swipe (canción corta) | 31% / p90 42ms | 29% / p90 32ms |
| Cambio de versión (síntoma reportado) | 1 frame de 69–93ms | — (sin versiones en seed) |
| SIZE+ (mismo camino: re-layout completo) | — | 1 frame de **200ms** |

**Conclusiones confirmadas:**
1. **El congelamiento (~400ms) al abrir una canción larga es real en release**
   — es la medición inicial completa de `VirtualPagedSong` (hallazgo #1).
   El usuario lo describió como "se pegó" y se reprodujo en vivo.
2. **El primer swipe a cada página nueva es igual de malo en release** (62%
   janky): cada página compone el `FullSongColumn` completo (hallazgo #3).
   Los swipes posteriores mejoran por el prefetch del pager.
3. **El tirón al cambiar de versión es real**: un frame de 69–200ms por el
   re-layout completo de la canción. Mismo camino que el cambio de fontSize.
4. El build debug **amplifica** el pop (350ms → 150ms en release) y los swipes
   sostenidos (56% → 31%), pero no es la causa raíz de los síntomas del reader.
5. **Hallazgo nuevo:** el banner de AdMob (solo release) añade un frame de
   ~550ms en la primera entrada a All Songs (init de WebView).
6. La capa de datos (N+1) es **irrelevante hoy**: la librería real tiene 6
   canciones / 7 versiones (~21 KB). La Fase 2 baja de prioridad — es deuda
   de escalabilidad, no causa actual.

**Re-priorización tras Fase 0:** el orden pasa a ser
Fase 1 (quick wins) → **Fase 3 (reader — ahora el fix principal)** →
Fase 4 (Baseline Profiles + benchmark de guardia) → Fase 2 (datos, diferible).
Añadir a Fase 4: diferir la carga del banner AdMob hasta después de la primera
composición de la lista (o precargarlo), por el frame de ~550ms medido.

## 2. Plan de mejora

### Fase 0 — Medir y confirmar (gate obligatorio, ~½ día)
1. Confirmar en qué build se percibió la lentitud. Instalar release local
   (`./gradlew installRelease`) y re-evaluar — si en release no se nota, el
   resto del plan baja de prioridad.
2. Acotar el síntoma: ¿abrir reader? ¿volver al home? ¿swipe de páginas? ¿todas?
3. Perfetto system trace + Layout Inspector (recomposition counts) sobre tres
   flujos: home→reader con canción de 150+ líneas, reader→home (pop),
   home→All Songs con la librería real.
4. **Criterio de éxito global:** 0 frames >16ms durante los slides de 300ms con
   una canción de 200 líneas y librería de 100+ canciones, en release.

### Fase 1 — Quick wins (~½ día, bajo riesgo) ✅ ejecutada 2026-07-04
1. ✅ Mover la lectura del intent a IO: `withContext(Dispatchers.IO)` en
   `handleIncomingIntent` (fix de corrección; no medible en estos flujos).
2. ✅ Aislar las lecturas per-frame del pager: `PageSeamOverlay` y
   `PageIndicator` extraídos como composables hoja que leen
   `currentPageOffsetFraction` en su propio scope.
3. ✅ `effectiveSong` envuelto en `remember(uiState.song, currentVersion)`.

**Resultado medido (debug, mismos flujos que Fase 0):** primer swipe 62–65% /
p90 150ms (igual a baseline), cambio de versión ~100ms (igual a baseline).
**Los quick wins no mueven la aguja**: el costo dominante en primer swipe,
cambio de versión y apertura es la composición/medición completa del
`FullSongColumn`, confirmando que la ganancia real está en la Fase 3.
Los tres cambios se conservan como corrección (#1) e higiene de recomposición
(#2, #3). Tests unitarios en verde.

### Fase 2 — Capa de datos (~1 día)
1. **Proyección para listas:** query `SELECT id, title, artist, genre,
   difficulty, is_favorite FROM songs` → data class `SongListItem`. Las listas
   dejan de parsear el contenido completo. El detalle (`getSongById`) sigue
   igual.
2. **Eliminar N+1:** `@Query("SELECT * FROM song_versions WHERE song_id IN
   (:songIds)")` + agrupado en memoria (o `@Transaction` + `@Relation`).
   Aplica a `getSongs`, `searchSongs`, `findSongsByTitle`.
3. Mover `searchSongs` a SQL (`LIKE` sobre la proyección) en lugar de
   `getAll()` + filtro en memoria.
4. Tests: `SearchFilterTest` debe seguir verde; añadir test de repositorio para
   la proyección.

### Fase 3 — Reader / VirtualPagedSong (~2 días, respeta ADR-013) ✅ ejecutada 2026-07-04
1. ✅ **Páginas por items** (`SongPageItems`): cada página compone solo las
   líneas de su rango de píxeles (O(líneas/página), antes O(líneas/canción)
   por página), colocadas en las posiciones que midió el Pass 1. `PageSlice`
   eliminado. Breaks y renderizado comparten fuente de verdad.
2. ❌ **Cachear el Pass 1 — intentado y revertido:** saltarse `subcompose()`
   en pasadas cacheadas hace que `SubcomposeLayout` descarte ~200 slots en un
   frame (+150–200ms medidos en cambio de versión). El Pass 1 solo corre en
   measure passes reales (abrir, cambiar versión/fuente, viewport) — costo
   aceptable medido.
3. ❌ **`movableContentOf` — descartado:** durante el swipe hay dos páginas
   visibles a la vez; el contenido movible solo puede existir en un lugar.
4. ✅ Addendum añadido a ADR-013 con el detalle y las mediciones.

**Resultado medido (debug, La Voz De Los 80, 10 páginas):**
| Flujo | Baseline | Fase 3 |
|---|---|---|
| 1er swipe a página nueva | 62%, p90 150ms, peor ~200ms | 30-32%, p90 ~50-60ms, peor ~150ms |
| 3 swipes sostenidos | 56%, p99 117ms | 38-45%, p99 77-89ms |
| Cambio de versión | frame 97-105ms | frame 89-113ms (igual — acotado por Pass 1) |
| Abrir reader | ~400-650ms | ~400-550ms (igual — acotado por Pass 1) |

**Fase 3b ✅ (mismo día, tras feedback del usuario: "tac tac" al abrir):**
paginación diferida. `SongFirstPageView` compone solo el tope de la canción
(cap proporcional al viewport, ~18-25 líneas) durante los 400ms del slide de
entrada; `VirtualPagedSong` lo reemplaza cuando la pantalla está estática —
el frame pesado del Pass 1 deja de competir con la animación. El swap es
visualmente invisible (mismos paddings y composables que la página 1).
Medido en debug: ventana de animación de p95 400ms → p90 69ms / p99 200ms
(warm; el primer open tras arrancar el proceso sigue sucio por JIT de debug —
validar en release, donde no aplica). El cambio de versión en página 1
también difiere su re-medición.

### Fase 4 — Calidad de producción (continuo)
1. **Baseline Profiles** (`androidx.profileinstaller` + módulo
   `baselineprofile`): mejora real de jank en instalaciones desde Play,
   especialmente primeras transiciones. Prioridad alta siendo app comercial.
2. Módulo **Macrobenchmark** con un test de transición home→reader como guardia
   de regresión (correr antes de cada release).
3. Registrar presupuesto de rendimiento en `STATUS.md` (frames janky permitidos,
   tiempo de apertura del reader).

## 3. Fuera de alcance (decisión explícita)
- No migrar a Paging 3 (librería local de cientos de canciones no lo amerita).
- No reescribir la paginación: ADR-013 sigue siendo la decisión correcta.
- No tocar nada del roadmap v2 (Firestore/Auth).

## 4. Orden recomendado de ejecución (actualizado tras Fase 0)
~~Fase 0~~ ✅ → Fase 1 (quick wins) → **Fase 3 (reader: caché de page breaks +
movableContentOf + aislar offsetFraction)** → re-medir con los mismos flujos de
la tabla → Fase 4 (Baseline Profiles, benchmark, AdMob diferido) → Fase 2
(datos) cuando la librería crezca o junto al roadmap v2.