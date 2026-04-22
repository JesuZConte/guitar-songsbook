# Día 22 — Plan (2026-04-23)

## Contexto

Prueba cerrada alpha en curso. Día 21 cerró transposición (v1.1).
Collections redesign (v1.1) espera mockups de Claude Design — disponibles
el lunes 28. Hoy aprovechamos para avanzar features independientes de diseño.

---

## Opción A — Pinch-to-zoom en el Reader (v1.2)

**Por qué ahora:** feature de alto valor para guitarristas tocando en vivo —
el teléfono está en el atril y necesitan ajustar el tamaño rápido con una mano.
Complementa perfectamente la transposición que acabamos de implementar.

**Alcance:**
- Gesto de dos dedos (pinch) aumenta/disminuye `fontSize` en el Reader
- El stepper de fuente existente (`[ - ] 14sp [ + ]`) sigue funcionando igual
- Tamaño guardado en `UserPreferences` (persiste entre sesiones)

**Esfuerzo:** bajo (medio día)

---

## Opción B — Language selector español/inglés (v1.5)

**Por qué ahora:** la mayoría de los testers son hispanohablantes y la app
está en inglés. Feedback latente que no se ha verbalizado pero se nota.

**Alcance:**
- Migrar todos los strings de UI a `strings.xml` (pantalla por pantalla)
- `strings-es.xml` con traducción al español
- Selector en Settings bajo nueva sección "Language"
- Cambio de idioma sin reiniciar con `AppCompatDelegate`

**Esfuerzo:** alto (2+ días de trabajo sistemático)

---

## Plan confirmado: A + inicio de B

1. Pinch-to-zoom (Opción A) — cerrar en la mañana
2. Arrancar migración de strings (Opción B) — avanzar lo que se pueda

Si B resulta más rápido de lo esperado, se cierra el día con ambas completas.
