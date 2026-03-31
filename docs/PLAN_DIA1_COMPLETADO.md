# Guitar Songbook — Plan de desarrollo actualizado

**Fecha:** 22 de marzo, 2026
**Stack:** Kotlin + Jetpack Compose + MVVM + Clean Architecture
**Principios:** Clean Code, SOLID, DRY, Frontend-First

---

## Día 1 — Fundación (completado)

| # | Paso | Estado |
|---|------|--------|
| 1 | Crear proyecto Android Studio (Empty Activity, Kotlin, API 28) | Listo |
| 2 | Configurar dependencias (Room, Gson, ViewModel, KSP) | Listo |
| 3 | Estructura de paquetes Clean Architecture | Listo |
| 4 | Modelo de dominio Song.kt (data classes) | Listo |
| 5 | JSON mockeado con 10 canciones en assets/ | Listo |
| 6 | Repository interface + AssetSongRepository | Listo |
| 7 | HomeViewModel + StateFlow + UiState | Listo |
| 8 | HomeScreen con lista de canciones + MainActivity | Listo |
| 9 | Test unitario del Repository | Movido a día 2 |

### Archivos creados

```
app/
├── assets/
│   └── songs.json (10 canciones)
├── src/main/java/com/guitarapp/songsbook/
│   ├── domain/model/
│   │   └── Song.kt (Song, SongSection, SongLine, ChordPosition)
│   ├── data/repository/
│   │   ├── SongRepository.kt (interface)
│   │   ├── AssetSongRepository.kt (implementación)
│   │   └── SongbookResponse.kt (DTO)
│   ├── presentation/
│   │   ├── viewmodel/
│   │   │   └── HomeViewModel.kt (ViewModel + HomeUiState + Factory)
│   │   └── screens/
│   │       └── HomeScreen.kt (UI Compose)
│   └── MainActivity.kt (actualizado)
```

### Conceptos Kotlin aprendidos (mapeados desde Java)

- data class = POJO con equals/hashCode/toString/copy auto-generados
- val = final (inmutable)
- suspend = async sin CompletableFuture
- Song? = nullable (como Optional)
- ?: = operador Elvis (ternario para nulls)
- .also {} = side-effect que devuelve el objeto
- by viewModels = delegated property para ViewModel
- StateFlow = observable reactivo (como LiveData/BehaviorSubject)
- @Composable = función que describe UI (no clase)
- when {} = switch mejorado
- Modifier = estilos encadenables type-safe
- LazyColumn = RecyclerView declarativo

### Decisiones técnicas día 1

- Sin Hilt (inyección manual por constructor, se agrega después)
- Sin Retrofit (no hay llamadas HTTP aún)
- Sin Room aún (se lee directo del JSON, Room viene en día 2)
- KSP 2.3.0 (independiente de versión Kotlin)
- builtInKotlin deshabilitado (warning de deprecación, no bloquea)

---

## Día 2 — Persistencia + Testing + Navegación

### Objetivos

Integrar Room para persistencia local, escribir los primeros tests, y agregar navegación para poder abrir una canción desde la lista.

### Pasos planificados

| # | Paso | Tiempo estimado |
|---|------|-----------------|
| 1 | Room Entity (SongEntity) + TypeConverters | 20 min |
| 2 | SongDao (Data Access Object) | 15 min |
| 3 | SongDatabase (Room database) | 10 min |
| 4 | Actualizar Repository para usar Room | 20 min |
| 5 | Test unitario: Repository + JSON parsing | 20 min |
| 6 | Test unitario: ViewModel states | 15 min |
| 7 | SongDetailScreen (vista de una canción) | 30 min |
| 8 | Navegación entre HomeScreen y SongDetailScreen | 20 min |
| 9 | Click en SongCard navega al detalle | 10 min |

### Conceptos nuevos que veremos

- Room annotations (@Entity, @Dao, @Database) — similar a JPA
- TypeConverters — cómo Room guarda List<String> en SQLite
- Coroutines en tests (runTest)
- Jetpack Compose Navigation
- Navegación con argumentos (pasar song ID)

### Resultado esperado día 2

La app abre, muestra la lista de canciones (desde Room DB), al tocar una canción navega al detalle donde ves el título, artista, acordes y el contenido con las secciones. Tests unitarios pasan en verde.

---

## Días 3-7 — Roadmap restante

| Día | Foco | Entregable |
|-----|------|------------|
| 3 | SongReaderScreen (renderizado) | Pantalla de lectura con acordes sobre el texto |
| 4 | Gestos (swipe entre secciones + pinch zoom) | Navegación por gestos en el lector |
| 5 | Búsqueda + filtros | Buscar por título, artista, género, dificultad |
| 6 | Playlists + favoritos | Crear y gestionar playlists |
| 7 | Pulido UI + tema oscuro + AdMob | MVP listo para testing |

---

## Post-MVP

| Semana | Foco |
|--------|------|
| 2 | Feedback, bug fixes, performance |
| 3 | Backend (Firebase o Spring Boot), sincronización |
| 4 | Build release APK, Google Play Store |
