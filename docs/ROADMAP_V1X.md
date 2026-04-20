# Roadmap v1.x — Local Improvements

Estas funcionalidades no requieren backend ni Firebase. Se publican como
actualizaciones incrementales al Play Store mientras se construye v2.0.

Prioridad definida el 2026-04-18, basada en valor para el usuario y feedback
de testers de la prueba cerrada alpha.

---

## v1.1 — Transposición + Collections

### Transposición de tonalidad (ADR-018)
- `transposeSteps: Int` en el modelo de Song + migración DB
- Lógica de transposición en `ChordNotation` utility
- Stepper +/− en el Reader toolbar
- Manejo de enarmónicos (sostenidos vs. bemoles según tonalidad destino)

### Collections / Library (ADR-019)
- Home muestra Collections en lugar de lista plana de canciones
- Colección por defecto: "All Songs" (toda la biblioteca)
- Colección de muestra: "Traditional Songs" (Amazing Grace, Greensleeves,
  Scarborough Fair — dominio público)
- Renombrar "Playlists" → "Collections" en UI y navegación
- Fondo tab de bottom nav: Home + Favorites (Playlists se integra en Home)

---

## v1.2 — Reader UX + Múltiples versiones

### Pinch-to-zoom en el Reader
- Gesture de dos dedos para aumentar/disminuir tamaño de fuente
- Tamaño guardado como preferencia del usuario (persistido en UserPreferences)

### Control manual de tema + modo nocturno del Reader
- Settings: selector "Seguir sistema / Siempre claro / Siempre oscuro"
- Reader: botón rápido para activar modo nocturno optimizado sin salir de la canción
- Modo nocturno del Reader: fondo negro profundo + texto ámbar — distinto al dark
  mode genérico, optimizado para no dañar la visión en escenarios oscuros

### Múltiples versiones por canción (reemplaza ADR-015 Difficulty/Mastery)
- Una canción puede tener N versiones: "Versión fácil", "Con cejilla", "Original", etc.
- El campo `difficulty` se elimina de la UI — el nombre de la versión lo comunica
- Home muestra la canción una sola vez — se entra y se elige la versión
- Reader: selector discreto para cambiar de versión sin salir
- Casos de uso: versión simplificada vs. completa, distintas tonalidades, solo vs. banda
- Requiere migración de DB: nueva tabla `song_versions`, relación 1:N con `songs`

---

## v1.3 — Gestión de canciones

### Eliminar canción desde el Home
- Swipe o long-press en la SongCard para revelar opción de eliminar
- Diálogo de confirmación antes de borrar
- Actualmente solo es posible desde dentro del Reader — no es descubrible

### Exportar / Importar canciones
- **Exportar a TXT** — contenido en formato bracket, legible e imprimible
- **Exportar a JSON** — backup completo con todos los campos de la canción
- **Importar desde JSON** — restore de un backup previo (cierra el ciclo)
- Compartir usando el sistema nativo de Android (ShareSheet)

---

## v1.4 — Detección automática de formato

### Auto-detección de over/under format (local, sin internet)
- Botón "Detect format" en el modo Text del builder
- Algoritmo heurístico que detecta líneas de acordes por:
  - Tokens cortos que coinciden con patrones de acordes válidos (Am, F#m7, Cmaj7…)
  - Ausencia de palabras largas en la línea
  - Línea de acordes seguida de línea de letra
- Convierte automáticamente al formato bracket antes de parsear
- Cubre el 80% de los casos (formato over/under estándar de la web)
- No requiere internet — cumple Principio II (Offline First)

### AI format conversion (Claude API — desbloqueado con Remove Ads IAP)
- Para formatos complejos, texto sucio o estructuras no estándar
- El usuario pega el texto, Claude lo convierte al formato bracket
- Requiere conexión a internet
- Costo por llamada absorbido por el IAP — no genera costo a usuarios gratuitos
- Ver ADR-020 para integración con el modelo de monetización

### AI song completion (Claude API — desbloqueado con Remove Ads IAP)
- Muchas transcripciones solo tienen acordes en el primer verso y coro
- Botón "✨ Completar con IA" aparece en secciones sin acordes dentro del editor
- Claude detecta los patrones existentes y sugiere acordes alineados a la letra
- El resultado se presenta como sugerencia editable, nunca se aplica automáticamente
- Copy en la UI: "Sugerencia de IA — revisa y ajusta según necesites"
- ~80% de precisión en canciones con progresiones repetidas entre estrofas
- El usuario corrige el resto manualmente — siempre menos trabajo que empezar de cero

---

## v1.5 — Internacionalización (i18n)

### Selector de idioma de la app
- Soporte inicial: Inglés (por defecto) + Español
- Implementación con el sistema nativo de Android (`strings.xml` por idioma)
- Selector en Settings bajo una nueva sección "Language"
- Todos los textos de la UI migrados a recursos de strings (trabajo sistemático
  pero sin complejidad técnica — es revisión pantalla por pantalla)
- El idioma se puede cambiar sin reiniciar la app usando `AppCompatDelegate`
- Abrir a más idiomas en el futuro según la base de usuarios que se forme

---

## v1.6 — Setlist mode

### Setlist mode (Reader continuo)
- Una Collection puede marcarse como Setlist
- En Setlist mode, al terminar una canción se avanza a la siguiente con un swipe
- No hay vuelta al Home entre canciones — las manos no dejan la guitarra
- Diseñado para ensayos y actuaciones en vivo
- Diferencia clave con Playlists/Collections: el orden es estricto y la navegación
  es siempre hacia adelante

---

## v1.6 — Onboarding + Builder

### Builder wizard paso a paso
- Flujo guiado: primero añade letra por sección, luego añade acordes
- Diseño a definir antes de implementar (requiere sesión de diseño)

---

## Backlog v2+ (requiere Firebase o más investigación)

### Tiempo de práctica (v2/v3)
- Registro automático de cuánto tiempo pasa el usuario en cada canción en el Reader
- Historial de práctica: canciones más trabajadas, abandonadas, dominadas
- Posible integración con Mastery inferida por comportamiento en lugar de etiqueta manual
- A evaluar con usuarios reales — puede ser más útil para músicos profesionales

---

## Notas

- El feedback de la prueba cerrada alpha puede reordenar estas prioridades.
  Revisar después de los 14 días de testing.
- v2.0 empieza en paralelo a v1.2 o v1.3 según disponibilidad.
- Cada versión se documenta en su propio PLAN_DIA*.md al implementarse.