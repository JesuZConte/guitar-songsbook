# Día 20 — Plan (2026-04-20)

## Contexto

Prueba cerrada alpha en curso. Seguimos con mejoras locales de v1.x que aporten
valor visible a los testers antes de que termine el período de 14 días.

---

## Opciones para el día

### Opción A — Export / Import de canciones (v1.3)

**Por qué ahora:** una tester ya preguntó cómo "descargar" o hacer backup de sus
canciones. Es la función más solicitada del feedback hasta ahora.

**Alcance mínimo viable:**
- Exportar canción a **JSON** (backup completo) via Android ShareSheet
- Importar canción desde **JSON** (restore desde backup)
- Botón de export accesible desde el menú del Reader (ya tiene menú de 3 puntos)
- Importar desde el botón `+` del Home (nuevo item "Import from file")

**Esfuerzo estimado:** medio (1 día completo)

---

### Opción B — Auto-detección de formato over/under (v1.4 parcial)

**Por qué ahora:** una tester pegó una canción completa en formato over/under y
los acordes no se parsearon. Es un caso de uso real y frecuente.

**Alcance mínimo viable:**
- Botón "Detect format" en el modo Text del builder
- Algoritmo heurístico: detecta líneas donde todos los tokens son acordes válidos
  (Am, F#m7, Cmaj7…) seguidas de línea de letra, y convierte al formato bracket
- Cubre ~80% de los casos sin internet

**Esfuerzo estimado:** medio (1 día)

---

### Opción C — Language selector (v1.5 parcial)

**Por qué ahora:** identificado en día 19 como mejora deseable. La app está en
inglés pero la mayoría de los testers son hispanohablantes.

**Alcance mínimo viable:**
- Strings en inglés migrados a `strings.xml` (pantalla por pantalla)
- `strings-es.xml` con traducción al español
- Selector en Settings bajo nueva sección "Language"
- Cambio de idioma sin reiniciar usando `AppCompatDelegate`

**Esfuerzo estimado:** alto (2+ días, trabajo sistemático)

---

### Opción D — Más quick wins pequeños

Si se prefiere iterar rápido antes de comprometerse con una feature de un día:

| Quick win | Esfuerzo | Valor |
|-----------|----------|-------|
| Confirmation snackbar al guardar/editar canción | Bajo | Medio |
| Contador de canciones en Home ("5 songs") | Bajo | Bajo |
| Sort songs A-Z / by date added | Medio | Medio |
| Empty state en Favorites con call to action | Bajo | Medio |

---

## Plan confirmado: A + B

### Orden sugerido

1. **Export a JSON** — el más solicitado, más simple de implementar
2. **Import desde JSON** — cierra el ciclo de backup
3. **Auto-detección de formato** — si queda tiempo; si no, pasa a día 21

Si el tiempo ajusta, Export + Import + Detect format es un día cargado pero
factible. Export solo o Export + Import ya sería un día exitoso.

### Notas de implementación

**Export:**
- Serializar `Song` a JSON con Gson (ya es dependencia)
- `Intent(Intent.ACTION_SEND)` con `type = "application/json"`
- Accesible desde el menú del Reader (⋮) y desde long-press en Home

**Import:**
- `ActivityResultContracts.GetContent("application/json")`
- Deserializar con Gson → `songRepository.insertSong()`
- Nuevo ID generado si colisiona con uno existente

**Auto-detección:**
- Botón "Detect format" en el modo Text del AddSong builder
- Heurística: línea donde ≥80% de los tokens son acordes válidos → línea de
  acordes; línea siguiente es la letra → convertir a formato bracket `[Am]word`
- Lista de patrones de acordes válidos: `[A-G][#b]?(m|maj|min|aug|dim|sus|add)?[0-9]?`
