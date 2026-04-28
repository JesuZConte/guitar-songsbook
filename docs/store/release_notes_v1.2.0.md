# Release notes — v1.2.0 (versionCode 5)

## English (500 chars max for Play Store "What's new")

```
• Collections — your songs are now organised in collections. Tap "All Songs" to browse your full library, or create custom collections for setlists, genres, or difficulty levels.
• Multiple versions per song — store different arrangements of the same song (different key, capo, or chord chart) and switch between them instantly while reading.
• Traditional Songs included — three public-domain songs (Amazing Grace, Greensleeves, Scarborough Fair) are pre-loaded so you can explore the reader right away.
• Help guide — tap the ? icon in "All Songs" to learn how to add songs manually or import them from a backup file.
```

## Español (500 chars max)

```
• Colecciones — tus canciones ahora se organizan en colecciones. Toca "Todas las canciones" para ver tu biblioteca completa, o crea colecciones para tus repertorios, géneros o niveles de dificultad.
• Múltiples versiones por canción — guarda arreglos distintos de la misma canción (otra tonalidad, cejilla o tablatura) y cambia entre ellos mientras lees.
• Canciones incluidas — tres canciones de dominio público (Amazing Grace, Greensleeves, Scarborough Fair) ya vienen cargadas para explorar el lector de inmediato.
• Guía de ayuda — toca el ícono ? en "Todas las canciones" para aprender a agregar canciones manualmente o importarlas desde un archivo de respaldo.
```

---

## Play Store upload checklist

### Before uploading
- [ ] Feature graphic (1024×500) — still pending (Canva template)
- [x] Screenshot of new Collections landing screen — taken
- [ ] Screenshot of help dialog (optional but nice)
- [ ] Short description (80 chars): "Your personal chord songbook. Add songs, organise in collections, play offline."
- [ ] Full description: already drafted in prior session

### Upload steps (Play Console)
1. Production → Create new release (or Internal Testing first)
2. Upload `app/build/outputs/bundle/release/app-release.aab`
3. Paste release notes (EN + ES) in "What's new in this release"
4. versionCode 5 / versionName 1.2.0 — confirm shown correctly
5. Review → Start rollout

### DB migration note
Users upgrading from any previous version will auto-migrate:
- v1→v2: added `is_favorite` column
- v2→v3: added `playlists` + `playlist_songs` tables
- v3→v4: added `song_versions` table, seeds one "Default" version per existing song

No user data is lost on upgrade.
