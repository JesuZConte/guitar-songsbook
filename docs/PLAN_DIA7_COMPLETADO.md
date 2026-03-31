# Guitar Songbook — Resumen día 7 y plan día 8

**Fecha:** 28 de marzo, 2026
**Stack:** Kotlin + Jetpack Compose + Room + MVVM + Clean Architecture
**Principios:** Clean Code, SOLID, DRY, Frontend-First
**Identidad visual:** Vintage Craft — "brand in the frame, clarity in the content"

---

## Día 7 — Completado

### Objetivos cumplidos

| # | Paso | Estado |
|---|------|--------|
| 1 | Paleta de colores vintage guitar (light + dark) en `Color.kt` | Listo |
| 2 | Fuente serif Merriweather + escala tipográfica completa en `Type.kt` | Listo |
| 3 | Theme.kt con ambos esquemas, `dynamicColor` eliminado, sin APIs deprecated | Listo |
| 4 | XML themes + colors.xml con colores de marca warm | Listo |
| 5 | Colores de acordes dedicados en Reader (amber light / golden dark) | Listo |
| 6 | Título de canción en Reader con Merriweather serif | Listo |
| 7 | Indicador de dificultad semántico: `●●○ Intermediate` con colores por tema | Listo |
| 8 | Animaciones de navegación: slide horizontal 300ms | Listo |
| 9 | Animación de favorito: bounce con `Animatable` (1.0 → 1.3 → 1.0) | Listo |
| 10 | Splash screen con `core-splashscreen` + fondo cream de marca | Listo |
| 11 | Surface container slots (5 niveles) para eliminar tonos grises/fríos | Listo |
| 12 | Tech Lead review + fixes: dead code, import order, contrast WCAG | Listo |

### ADR creado

- **ADR-008: Vintage craft visual identity** — documenta la identidad visual, paleta de colores, tipografía, reglas de legibilidad, y qué NO hacer.

### Archivos creados / modificados

```
app/src/main/java/com/guitarapp/songsbook/
├── ui/theme/
│   ├── Color.kt              ← REESCRITO: paleta vintage completa (light+dark),
│   │                            surface containers, difficulty colors por tema, chord colors
│   ├── Type.kt               ← REESCRITO: Merriweather serif headings + sans-serif body
│   └── Theme.kt              ← REESCRITO: ambos ColorScheme con todos los slots,
│                                dynamicColor eliminado, sin APIs deprecated
├── presentation/screens/
│   ├── HomeScreen.kt         ← MODIFICADO: DifficultyIndicator (●○○), bounce animation,
│   │                            theme-aware difficulty colors
│   └── SongReaderScreen.kt   ← MODIFICADO: chord colors dedicados, Merriweather en header,
│                                fix dead code (if false), fix import order, unused import removed
└── MainActivity.kt            ← MODIFICADO: slide transitions NavHost, installSplashScreen()

app/src/main/res/
├── font/
│   ├── merriweather_regular.ttf  ← NUEVO: ~303KB
│   └── merriweather_bold.ttf     ← NUEVO: ~303KB
├── values/
│   ├── colors.xml                ← REESCRITO: brand colors (cream, dark brown, amber)
│   └── themes.xml                ← REESCRITO: splash theme + warm window background
└── AndroidManifest.xml           ← MODIFICADO: activity theme → Theme.GuitarSongsbook.Splash

gradle/libs.versions.toml         ← MODIFICADO: +coreSplashscreen 1.0.1
app/build.gradle.kts              ← MODIFICADO: +core-splashscreen dependency
docs/adr/ADR-008-vintage-craft-visual-identity.md  ← NUEVO
```

### Conceptos Kotlin / Android aprendidos día 7

- `lightColorScheme()` / `darkColorScheme()` — Material 3 tiene ~30 color slots; los no definidos usan defaults fríos que rompen paletas cálidas
- `surfaceContainer`, `surfaceContainerHigh`, etc. — nuevos slots M3 que Card, NavigationBar, BottomAppBar usan internamente
- `FontFamily(Font(R.font.xxx))` — bundlear fuentes TTF y referenciarlas en Typography
- `Typography()` con escala completa — display/headline/title (serif) vs body/label (sans-serif)
- `Animatable` + `animateTo()` secuencial — animaciones de dos fases (scale up → spring back)
- `spring(dampingRatio, stiffness)` — física de resorte para animaciones naturales
- `AnimatedContentTransitionScope.SlideDirection` — transiciones de navegación direccionales
- `installSplashScreen()` — debe llamarse ANTES de `super.onCreate()`
- `Theme.SplashScreen` parent — el tema XML de splash necesita `postSplashScreenTheme` para transicionar
- `isSystemInDarkTheme()` — lectura del tema del sistema, usada directamente en composables para colores fuera del scheme

### Decisiones de arquitectura día 7

- **`dynamicColor` eliminado:** Material You adapta colores al wallpaper del usuario. Nosotros queremos identidad de marca consistente (ADR-008). Sin dynamic color.
- **Merriweather solo para headings:** serif da personalidad vintage pero dificulta legibilidad en cuerpo de texto. Sans-serif para body/labels mantiene lectura cómoda.
- **Monospace en Reader NO se toca:** los acordes necesitan alineación por carácter. Ni serif ni sans-serif sirven para esto. Es una regla no negociable.
- **Surface containers explícitos:** Card, NavigationBar, BottomAppBar, AlertDialog usan distintos niveles de surface container internamente. Sin definirlos, M3 genera tonos fríos que rompen la paleta cálida.
- **Difficulty colors por tema (light/dark):** un verde `#388E3C` legible sobre cream `#FFF8F0` no es legible sobre dark `#1A1614`. Se necesitan variantes claras para dark theme.
- **Chord colors separados del scheme:** `ChordColorLight`/`ChordColorDark` son constantes semánticas propias, no dependen de `MaterialTheme.colorScheme.tertiary` que cambiaría si ajustamos la paleta general.
- **`Animatable` en vez de `animateFloatAsState`:** `animateFloatAsState` anima hacia un target y se queda. Para un bounce (escalar arriba y volver) necesitamos dos `animateTo` secuenciales, lo cual requiere `Animatable` con coroutines.
- **Splash screen con compat library:** `core-splashscreen` da splash consistente en API 28–36. Sin ella, pre-Android 12 muestra pantalla blanca al arrancar.

### Bugs corregidos

- **`if (false)` en ReaderBottomBar:** el icono de fullscreen exit estaba detrás de una condición imposible (`if (false)`). Código muerto eliminado junto con el import `FullscreenExit` no usado.
- **Import desordenado en SongReaderScreen:** `isSystemInDarkTheme` estaba antes de `AnimatedVisibility` rompiendo orden alfabético.
- **Difficulty colors sin variante dark:** `DifficultyBeginner` (verde único) tenía bajo contraste sobre fondo oscuro. Separado en `Light`/`Dark` variants con ratios WCAG AA.

### Estado actual de la app

- 11 canciones en la lista
- Búsqueda por título/artista
- Filtros por dificultad y género
- Reader con swipe entre páginas, fullscreen toggle, font size controls
- Favoritos con corazón animado (bounce)
- Playlists: crear, ver detalle, eliminar, añadir/remover canciones
- Bottom navigation: Home + Favorites + Playlists
- **Identidad visual vintage craft: paleta cálida amber/cream/brown**
- **Tema oscuro automático (sigue preferencia del sistema)**
- **Tipografía dual: Merriweather serif (headings) + sans-serif (body)**
- **Indicador de dificultad visual: ●○○ / ●●○ / ●●●**
- **Transiciones slide horizontal entre pantallas**
- **Splash screen con color de marca**
- Empty/loading/error states en todas las pantallas
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

## Día 8 — Plan: AdMob + Build Release + Depth Polish

### Objetivo

Monetización básica con AdMob (banner ads no intrusivos — nunca en el Reader), build de release firmado, y ajustes de profundidad visual para eliminar la sensación "flat" de las tarjetas.

### Pasos planificados

| # | Paso | Descripción |
|---|------|-------------|
| 1 | AdMob SDK integration | Añadir dependencia, configurar `APPLICATION_ID` en manifest |
| 2 | Banner ad en HomeScreen | Banner al final de la lista, no intrusivo. NUNCA en el Reader (el guitarrista está tocando) |
| 3 | Banner ad en PlaylistsScreen | Mismo patrón: banner al final |
| 4 | Ad-free Reader policy | Documentar en ADR: el Reader es zona sagrada, sin publicidad |
| 5 | Card depth polish | Bordes sutiles con `outlineVariant`, elevación ajustada, sombras warm-tinted |
| 6 | NavigationBar polish | Indicador de tab seleccionado con color primary, posible icono badge |
| 7 | Build release firmado | Keystore, `signingConfigs`, `isMinifyEnabled = true`, ProGuard rules |
| 8 | ProGuard rules | Room, Gson, Compose — evitar que se obfusquen clases serializadas |
| 9 | App icon personalizado | Reemplazar el icono default por uno vintage/guitar themed |
| 10 | Revisión final + test en dispositivo | Verificar ads, theme, transiciones, release build funcional |

### Conceptos nuevos que veremos

- AdMob SDK + `AdView` en Compose (wrapped via `AndroidView`)
- `BannerAd` composable reusable
- `signingConfigs` en `build.gradle.kts`
- ProGuard/R8 rules para Room entities y Gson
- `isMinifyEnabled = true` + `isShrinkResources = true`
- Adaptive icons (foreground + background layers)

### Decisiones pendientes

- **Ad placement strategy:** banners only, or interstitials between certain actions? Banners are less intrusive but lower revenue. Recommend starting with banners only — the app is for guitarists mid-performance, interruptions are unacceptable.
- **Ad frequency:** one banner per screen max, always at the bottom of scrollable content, never fixed/sticky.
- **Future premium tier?** If v2 adds Firebase Auth, a "Pro" tier (ad-free + cloud sync) becomes possible. Worth documenting in ADR now even if not implemented.

### Notas sobre profundidad visual

El usuario reportó que la app se ve "un poco flat". Esto es porque las tarjetas usan los surface container colors correctos pero sin diferenciación visual suficiente. Opciones:
- `CardDefaults.cardElevation(defaultElevation = 4.dp)` (actualmente 2.dp)
- Añadir `border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)` a las cards
- Sombra ligeramente más pronunciada en cards sobre el fondo cream

### Resultado esperado día 8

La app genera ingresos básicos con ads no intrusivos. Existe un APK release firmado listo para testing externo o subida a Play Console. Las tarjetas tienen más profundidad visual. El icono de la app refleja la identidad vintage craft.

---

## Roadmap actualizado

| Día | Foco | Estado |
|-----|------|--------|
| 1 | Fundación: proyecto, modelos, JSON, HomeScreen | Completado |
| 2 | Persistencia Room, navegación, detalle | Completado |
| 3 | SongReaderScreen (experiencia core de lectura) | Completado |
| 4 | Búsqueda + filtros | Completado |
| 5 | Favoritos + bottom navigation | Completado |
| 6 | Playlists + clean code | Completado |
| 7 | Pulido UI + identidad vintage craft + tema oscuro | Completado |
| 8 | AdMob + build release + depth polish | Siguiente |
| 9+ | Firebase Auth + Firestore + community features (v2) | Futuro |
