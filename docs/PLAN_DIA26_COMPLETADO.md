# Día 26 — APK firmado + preparación release v1.1.0

**Estado: COMPLETADO**
**Fecha:** 2026-04-25

---

## Objetivos cumplidos

### 1. versionCode / versionName
- `versionCode`: 4 → **5**
- `versionName`: "1.1" → **"1.1.0"**

### 2. AAB firmado
- Comando: `./gradlew bundleRelease`
- Firmado con keystore existente: `/Users/luis_jesus/keystores/guitar-songbook.jks`
- Propietario: CN=Luis Zuniga, OU=zuniga-conte, válido hasta 2053
- Archivo: `app/build/outputs/bundle/release/app-release.aab` (17 MB)

### 3. Verificación de migración DB
- Cadena completa: v1→v2→v3→v4 registrada en `SongDatabase`
- Todos los `addMigrations()` en su lugar
- Sin riesgo de pérdida de datos en actualización

### 4. Release notes escritas
- `docs/store/release_notes_v1.1.0.md` — EN + ES listas para pegar en Play Console
- Checklist de pasos de subida incluido

---

## Pendiente (fuera del alcance de Claude)

| Tarea | Responsable |
|---|---|
| Feature graphic 1024×500 | Luis (Canva) |
| Screenshot nueva del Home (Collections landing) | Luis |
| Subir AAB a Play Console | Luis |
| Pegar release notes EN + ES | Luis |
| Revisar y publicar (Internal Testing → Production) | Luis |

---

## Archivos clave

| Archivo | Cambio |
|---|---|
| `app/build.gradle.kts` | versionCode 5, versionName 1.1.0 |
| `docs/store/release_notes_v1.1.0.md` | nuevo — release notes + checklist |
| `app/build/outputs/bundle/release/app-release.aab` | artefacto generado (no en git) |
