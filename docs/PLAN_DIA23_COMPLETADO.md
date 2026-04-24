# Día 23 — Nocturno Mode + Language Selector (EN/ES i18n)

**Estado: COMPLETADO**
**Commit:** aeca94a
**Fecha:** 2026-04-23

---

## Objetivos cumplidos

### 1. Nocturno Mode en Reader
- Tema visual "nocturno": fondo negro puro + texto ámbar para uso en escenario
- Activado con el ícono de luna (`Bedtime`) en la TopAppBar del Reader
- Implementado mediante `MaterialTheme` override con `NocturnoColorScheme` (darkColorScheme)
- `CompositionLocalProvider(LocalNocturnoMode provides true)` pasa el estado a `LineContent`
- Color de acordes respeta el modo: `NocturnoChord` (ámbar) cuando está activo
- El estado persiste en `ReaderUiState.isNocturno` durante la sesión

### 2. Selector de idioma en Ajustes
- Sección "Language" / "Idioma" en Settings entre Apariencia y Acordes
- `FilterChip` para EN / ES sin restart gracias a `AppCompatDelegate.setApplicationLocales()`
- `MainActivity` migrado de `ComponentActivity` a `AppCompatActivity`
- Dependencia AppCompat 1.7.0 añadida a `libs.versions.toml` y `build.gradle.kts`
- El idioma seleccionado persiste automáticamente sin `SharedPreferences` adicional

### 3. Internacionalización completa (EN + ES)
- Todos los strings hardcodeados migrados a `values/strings.xml` y `values-es/strings.xml`
- Pantallas migradas: Home, Favorites, Playlists, PlaylistDetail, Reader, Settings, About, AddSong
- `BottomNavItem` usa `@StringRes Int` en lugar de `String`
- `context.getString()` para lambdas non-composable (snackbars)
- `sectionTypeLabel()` helper `@Composable` en `SongContentComponents.kt` para etiquetas de sección localizadas (Verso/Estrofa, Coro, Puente…)
- Dificultad traducida en `HomeScreen` (DifficultyIndicator) y `AddSongScreen` (DifficultyDropdown)
- Plurales correctos en ES: "canción" / "canciones"

---

## Archivos clave modificados
- `MainActivity.kt` — AppCompatActivity, @StringRes nav labels
- `ui/theme/Color.kt` — NocturnoBackground, NocturnoChord, etc.
- `ui/theme/Theme.kt` — NocturnoColorScheme
- `presentation/screens/SongReaderScreen.kt` — nocturno toggle, screenContent lambda
- `presentation/screens/SongContentComponents.kt` — LocalNocturnoMode, sectionTypeLabel()
- `presentation/screens/SettingsScreen.kt` — LanguageSelectorRow con AppCompatDelegate
- `presentation/screens/HomeScreen.kt` — i18n completo
- `presentation/screens/AddSongScreen.kt` — i18n completo
- `presentation/screens/FavoritesScreen.kt` — i18n
- `presentation/screens/PlaylistsScreen.kt` — i18n + plurales
- `presentation/screens/PlaylistDetailScreen.kt` — i18n
- `presentation/screens/AboutScreen.kt` — i18n
- `res/values/strings.xml` — 143 strings EN
- `res/values-es/strings.xml` — 143 strings ES (nuevo)

---

## Próximo: Día 24
Ideas para continuar:
- Búsqueda por acorde (filtrar canciones que usan un acorde específico)
- Modo de presentación / setlist con orden de canciones para un ensayo
- Exportar lista de acordes de toda una playlist
- Preparar APK signed para el cierre testing v1.1
