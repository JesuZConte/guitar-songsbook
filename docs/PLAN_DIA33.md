# Día 33 — Historial de git, rendimiento medible y pendientes menores

Viene de [PLAN_DIA31_COMPLETADO](PLAN_DIA31_COMPLETADO.md) (días 31 y 32). Estado al empezar:
1.4.6 (versionCode 13) enviada a revisión en la prueba cerrada el 2026-11-26; `main` = `origin/main`;
231 tests en verde.

## Objetivo

1. Quitar las letras con derechos del historial público de git.
2. Tener una medida reproducible del rendimiento (arranque y home → lector) y bajarla con
   Baseline Profiles.

## Fase 0 — Arranque (≈15 min)

1. `git status`; leer `STATUS.md`.
2. Play Console: ¿la 1.4.6 pasó la revisión? ¿Testers inscritos y días acumulados (12 × 14)?
3. Crashlytics: sin "Window couldn't find content container view" en la 1.4.6; revisar si hay
   fallos o ANR nuevos.

## Fase 1 — Reescribir el historial (decisión y confirmación de Luis en cada paso)

Letras de terceros en `5ea0339` y `81306c0`; el repo es público.

| # | Paso | Criterio de hecho |
|---|---|---|
| 1.1 | Inventario: qué rutas con letras existen en todo el historial (`git log --all --name-only`) | Lista cerrada de rutas a borrar, revisada por Luis |
| 1.2 | Respaldo: `git clone --mirror` fuera del repo | Respaldo existe y `git log` funciona en él |
| 1.3 | `git filter-repo` sobre un clon nuevo | Ninguna de las rutas aparece en `git log --all`; el árbol de `HEAD` es idéntico al de antes |
| 1.4 | **Force-push** (solo con confirmación explícita) | `origin` reescrito; GitHub Pages sigue publicando |
| 1.5 | Pedir a GitHub Support la purga de commits huérfanos/caché | Ticket abierto |
| 1.6 | Volver a clonar en local (los hashes cambian; actualizar referencias a hashes en docs si hace falta) | Build y suite en verde en el clon nuevo |

## Fase 2 — Rendimiento medible

| # | Tarea | Criterio |
|---|---|---|
| 2.1 | Módulo Macrobenchmark: arranque en frío y home → Todas las canciones → lector | Corre en el A50 y da medianas estables (≥10 iteraciones) |
| 2.2 | Baseline Profile generado con esos recorridos e incluido en el release | Comparación antes/después con el benchmark; reportar números aunque no mejoren |
| 2.3 | Revisar el objetivo "ningún frame >100 ms" con los datos nuevos | Criterio confirmado o reescrito con evidencia |

Nota: Luis percibe el arranque de la 1.4.6 "bastante más rápido" (UMP después de `setContent`).
No está medido; 2.1 lo confirma o no.

## Fase 3 — Pendientes menores (en orden; cortar donde acabe el tiempo)

1. Aviso breve al compartir un respaldo JSON (puede llevar letras completas).
2. Botón plano del estado vacío de la biblioteca → `BrassButton`.
3. Borrar las fuentes Fraunces sin uso (y actualizar `THIRD_PARTY_NOTICES.md`).
4. Actualizar `play-services-ads` (el SDK avisa que 25.1.0 está desactualizado); revisar el changelog
   y volver a probar banner + consentimiento en el A50.
5. Opcional: `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` en release, por la advertencia de Play
   sobre símbolos nativos (solo sirve si las librerías traen `.so` sin recortar).

## Sin verificar (arrastrado)

- Instalación nueva en el EEE sin red (falla cerrado; solo tests unitarios).
- Que al volver del lector no se haga una segunda petición de anuncio (solo tests del holder).

## Fuera de alcance

- Roadmap v2 (Firestore, Auth, comunidad, IAP para quitar anuncios, IA).
- Registro de marca y revisión legal de la §7 (ADR-026).
- Importación `.docx` (ADR-024, pausada).

## Definición de hecho del día 33

- Historial sin letras en `origin`, o decisión de Luis de no hacerlo registrada aquí.
- Benchmark corriendo con números antes/después citados.
- Suite completa en verde con el número de tests.
- Plan renombrado a `_COMPLETADO` con lo realmente hecho; `STATUS.md` actualizado.
