# Día 31 — Importación .txt, higiene legal y privacidad · Plan del día 32

**Estado: Parte A COMPLETADA (2026-11-21 → 2026-11-24) · Parte B = plan pendiente del día 32**
**Rol:** Tech Lead review
**Versión base:** 1.4.5 (versionCode 12), subida a la prueba cerrada
**Por qué existe este archivo:** el Día 30 quedó abierto desde julio. Se cierra aquí
([PLAN_DIA30_COMPLETADO](PLAN_DIA30_COMPLETADO.md)) y este día retoma el ciclo
*plan → ejecutar → medir → cerrar → STATUS*.

---

# Parte A — Resumen de lo hecho

## Contexto

La app está en **prueba cerrada** en Play Console (12 testers durante 14 días seguidos
para poder pedir acceso a producción). Un tester preguntó si la app acepta archivos
`.txt`, y eso abrió tres frentes: la importación, la licencia y la privacidad.

## 1. Play Console: más testers

Guía de referencia: Probar y lanzar → Pruebas → Prueba cerrada → *Administrar track* →
pestaña **Testers** (no "Verificadores"). Se edita la lista de correos y se comparte el
enlace de participación. Los 14 días cuentan por tester desde que acepta, de forma continua.

## 2. Importación `.txt` — [ADR-025](adr/ADR-025-txt-song-import.md)

| Fase | Qué | Resultado |
|---|---|---|
| 0 | Validar con 2 archivos reales del tester | Alineación de columnas OK, notación americana, 1 canción por archivo |
| 1a | Arreglar el procesamiento de texto | 3 bugs reales (también afectaban al pegado): etiquetas en español tomadas como acordes, `[Intro] F Bb` perdía el encabezado, acordes pasados del final de la letra se anidaban (`[[C5]G5]`) |
| 1b | Selector de archivos `.json`/`.txt` → Agregar canción pre-llenada | `TextSongImport` (UTF-8 → Windows-1252, BOM, UTF-16, límite 200 KB), metadatos (título/artista/tono/cejilla), `TextDraftHolder`, avisos de error |

Decisiones clave:
- Las etiquetas de sección se guardan como **clave canónica en inglés** (`chorus`) y la app
  las traduce al mostrarlas, así que cambian con el idioma de la app. Las desconocidas se
  conservan tal cual como sección propia.
- El texto se convierte al llegar (sin esperar "Detectar Formato") y Agregar canción abre
  en **modo texto** para revisarlo.
- Las muestras del tester contienen letras comerciales: **no se commitean**.

Verificado con TDD (rojo → verde) y en el Galaxy A50 por Luis. **Tests: 160 → 210.**

## 3. UI: `BrassButton`

El botón Guardar (Agregar canción y editor de versiones) era el `Button` plano de Material.
Ahora usa el degradado dorado de la familia `BrassSurface`; desactivado se atenúa al 40 %
en vez de volverse gris. Verificado con capturas en el teléfono.

## 4. Higiene legal — [ADR-026](adr/ADR-026-license-compliance-and-scope.md)

| Hallazgo | Acción |
|---|---|
| `LICENSE` era un resumen (≈1.300 palabras vs ≈5.600) con la numeración 11–13 mal | Reemplazado por el texto oficial (674 líneas), verificado |
| "GPL v3" vs "o posterior" sin decidir | `GPL-3.0-or-later`, encabezados SPDX en los 88 `.kt` |
| SDK propietarios (AdMob, Firebase, Play services) dentro de un APK GPL | Permiso adicional (§7) en `NOTICE.md` |
| "Marca reservada por copyright" (inexacto) | Reserva de nombre/logo/ícono como marca (§7(e)) |
| Merriweather sin licencia; sin avisos de terceros | `THIRD_PARTY_NOTICES.md`, `licenses/OFL-*.txt`, bloque en "Acerca de" |
| JSON con letras comerciales versionados en `docs/songs/` | Dejaron de versionarse; la carpeta queda ignorada (solo pruebas locales) |

## 5. Privacidad

- **Hallazgo:** `AnalyticsHelper.logSongOpened` enviaba el **título de la canción** (texto
  del usuario) a Firebase Analytics y a Crashlytics. Contradecía el principio "tus
  canciones son tuyas" y el texto de "Acerca de".
- **Corrección:** ahora solo se envía el ID interno.
- **Borrador de política de privacidad** (EN + ES) en [PRIVACY_POLICY.md](PRIVACY_POLICY.md),
  basado en lo que la app hace: sin cuentas, canciones solo locales, Analytics, Crashlytics
  y AdMob (banner solo en la biblioteca, nunca en el lector).

## 6. Documentación

`STATUS.md` actualizado (estaba en 2026-06-08 y decía "48+ canciones"; son 3),
`PLAN_DIA30` cerrado, `CLAUDE.md` con el comando de tests correcto.

## Commits en `main` (sin push)

`59e7049` ADR-024 · `f7ec92f` importación `.txt` + parser · `9757042` BrassButton ·
`b600dac` versión 1.4.5 · `112c199` y `a852bda` `docs/songs` ignorada ·
`004f275` LICENSE oficial + NOTICE + avisos · `fa8f9af` encabezados SPDX.

**Sin commitear al cerrar este documento:** fix de Analytics, `PRIVACY_POLICY.md`,
`STATUS.md`, cierre de `PLAN_DIA30`, este plan, actualización de ADR-026 y
`PLAY_STORE_LISTING.md`, `CLAUDE.md`.

## Lecciones

- `./gradlew test --tests …` **no funciona** en este proyecto Android; usar
  `:app:testDebugUnitTest --tests …`.
- El commit `493e83c` se llama "add space between chords" pero contiene otra cosa
  (apertura de `.json` y rutas de versiones). Mensajes de commit más fieles.
- Un plan sin cerrar se pierde: cerrar el día = renombrar a `_COMPLETADO` + actualizar STATUS.
- Las afirmaciones de privacidad hay que contrastarlas con el código, no con la intención.

---

# Parte B — Plan del día 32

## Objetivo

Dejar lista la **1.4.6** para la prueba cerrada **y** cumplir los requisitos de Play que
hoy bloquean la publicación: política de privacidad publicada y consentimiento de anuncios.

## Fase 0 — Arranque (≈20 min)

1. `git status`; commitear lo pendiente de la Parte A (el fix de Analytics va **solo**, en su
   propio commit, para poder revertirlo).
2. Revisar el avance de testers en Play Console (cuántos, cuántos días) y su feedback.
3. **Decisión de Luis:** ¿`git push`? ¿reescribir el historial para quitar las letras
   (`5ea0339`, `81306c0`)? Si el repo es público, mejor antes que después.

## Fase 1 — Privacidad y consentimiento (bloqueante)

| # | Tarea | Criterio de hecho |
|---|---|---|
| 1.1 | Completar `[CORREO DE CONTACTO]` y la fecha; publicar la política en una URL pública (la vista de GitHub o GitHub Pages) | URL abre en navegador, en ambos idiomas |
| 1.2 | Pegar la URL en Play Console (Contenido de la app → Política de privacidad) | Play Console la acepta |
| 1.3 | **ADR-027 + implementación del consentimiento (Google UMP)** para EEE/Reino Unido: pedirlo antes de inicializar AdMob, y una entrada "Opciones de privacidad" en Ajustes | Con la geografía de depuración en EEE no se pide anuncio sin consentimiento; lógica de decisión con tests unitarios |
| 1.4 | Reconciliar **Data safety** con la política (Analytics, Crashlytics, ID de publicidad; sin canciones) | Formulario coincide sección por sección |

Nota: la frase de consentimiento de la política (§3.3) solo es cierta cuando 1.3 está hecha.

## Fase 2 — Rendimiento pendiente del Día 30

| # | Tarea | Criterio |
|---|---|---|
| 2.1 | Diferir la carga del banner de AdMob (frame de ≈550 ms al entrar a Todas las canciones) | Medido en release con `dumpsys gfxinfo`: sin frame >100 ms en esa transición |
| 2.2 | Baseline Profiles + test Macrobenchmark de guardia (home → reader) | Solo si sobra tiempo; si no, queda como primer ítem del día 33 |

## Fase 3 — Pendientes menores (en este orden, cortar donde acabe el tiempo)

1. Aviso breve al compartir un respaldo JSON (puede llevar letras completas).
2. Botón plano del estado vacío de la biblioteca → `BrassButton`.
3. Borrar las fuentes Fraunces sin uso (ver `THIRD_PARTY_NOTICES.md`).
4. Solo si un tester lo pide: ADR-025 fase 1c (acordes en notación latina al importar) o
   fase 2 (abrir `.txt` desde WhatsApp/Archivos).

## Fase 4 — Release 1.4.6

1. Subir a **1.4.6 (versionCode 13)**.
2. Probar en el A50: importar `.txt`, guardar, pantalla "Acerca de" (con scroll), consentimiento.
3. Generar el bundle de release firmado y subirlo a la prueba cerrada con notas de versión.
4. Confirmar que los testers siguen inscritos (una versión nueva no debería reiniciar sus 14 días;
   verificarlo en la consola).

## Progreso del día 32 (2026-11-24 → 25, sesión 1) — para retomar

**Hecho y subido a `main` (`776750d`):** política publicada en Pages
(`https://jesuzconte.github.io/guitar-songsbook/privacy.html`, workflow en dos jobs, fuente
"GitHub Actions"); consentimiento UMP completo (ADR-027, 220/220 tests, verificado en el A50 con
EEE simulado: formulario, rechazar, reabrir desde Ajustes, aceptar, persistencia); `CLAUDE.md`
reescrito con las reglas de trabajo; planes del día 20 y 23 limpiados.

**En AdMob/Play (hecho por Luis):** URL de la política en AdMob y en Play Console; mensaje GDPR
publicado (EN por defecto, ES extra, Consentir / No consentir / Gestionar opciones; consentimiento
alternativo y "cerrar como no consentir" apagados). Formulario de Seguridad de los datos **en curso**:
respondido "recoge datos: Sí", "cifrado en tránsito: Sí", "no hay cuentas", "eliminación: Sí".

**Preparado, SIN commitear (esperando visto bueno de Luis):**
- `docs/DATA_DELETION.md` + `sites/delete-data.html` (pasos, qué se borra/conserva, plazos:
  eventos Analytics 2 meses, datos de usuario 14 meses, Crashlytics 90 días, respuesta en 30 días).
- `docs/PRIVACY_POLICY.md` (§8 enlaza a la página nueva) y `sites/privacy.html` regenerado.
- `tools/privacy_md_to_html.py` ampliado (tablas, enlaces, título).
- `docs/PLAY_STORE_LISTING.md`: tabla antigua "None" eliminada, hoja de respuestas del formulario,
  fila "recoge datos personales" corregida.

**Siguiente, en orden:**
1. Commit + push de lo preparado (se despliega solo) y comprobar
   `https://jesuzconte.github.io/guitar-songsbook/delete-data.html` en EN y ES.
2. Luis pega esa URL en el campo de eliminación de datos de Play y termina el formulario
   (tipos: ubicación aproximada, interacciones, fallos y diagnósticos, ID de dispositivo; todos
   recogidos y compartidos, obligatorios). Cotejar con las páginas "Play data disclosure" de AdMob
   y Firebase. Comparar el resumen final con la política.
3. Decidir la reescritura del historial (letras en `5ea0339`, `81306c0`; repo público): respaldo
   `git clone --mirror`, filter-repo, force-push solo con confirmación, pedir purga a GitHub Support.
4. Opcional: añadir "prevención de fraude y abuso en publicidad" a §4 de la política (el formulario
   lo declara como finalidad).
5. Fase 2: diferir el banner de AdMob y **medir el arranque en release** (el consentimiento puede
   afectarlo; en debug mide ~4,4 s en el A50, sin referencia previa).
6. Fase 4: release 1.4.6 (versionCode 13): importar `.txt`, Acerca de, consentimiento; bundle firmado;
   subir a la prueba cerrada.
7. Cerrar el día: renombrar a `_COMPLETADO`, actualizar `STATUS.md` ("Last verified"), crear el plan
   del día 33.

**Sin verificar todavía:** instalación nueva en el EEE sin red (falla cerrado, solo tests unitarios);
que Analytics se apague/encienda en el dispositivo; la lista de tipos del formulario contra Google.

**Idea futura (Luis):** compra dentro de la app para quitar anuncios (v2).

## Orden y tiempos estimados

Fase 0 → Fase 1 (≈ medio día; 1.3 es lo más largo) → Fase 4 si 1 termina →
Fase 2 y 3 con el tiempo restante.

## Fuera de alcance

- Todo el roadmap v2 (Firestore, Auth, comunidad, IAP, IA).
- Registrar la marca "Cancionero" y la revisión legal del texto de la §7 (decisión posterior;
  ver el checklist de ADR-026).
- Importación `.docx` (ADR-024, pausada hasta tener archivos reales).

## Definición de hecho del día 32

- `PLAN_DIA31.md` y el plan del día 32 renombrados a `_COMPLETADO` con lo realmente hecho.
- `STATUS.md` actualizado y "Last verified" con la fecha.
- Suite completa en verde; resultado citado con el número de tests.
- Nada de lo declarado en la política contradice el código.

## Forma de trabajo (para no volver a perder el hilo)

1. Al empezar: leer `STATUS.md` y el último plan.
2. Plan con fases y criterios **antes** de codificar; ADR si el cambio es estructural.
3. Lógica con TDD (rojo → verde); UI verificada en el dispositivo.
4. Al cerrar: renombrar el plan a `_COMPLETADO`, actualizar `STATUS.md`, commits pequeños.
