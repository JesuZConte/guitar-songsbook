# Día 23 — Plan (2026-04-24)

## Contexto

Prueba cerrada alpha en curso. Día 22 cerró pinch-to-zoom (v1.2) y dos bug
fixes (home refresh + chord collision). Collections redesign espera mockups
el lunes 28. Hoy continuamos con features independientes de diseño.

---

## Opción A — Language selector español/inglés (v1.5)

**Por qué ahora:** la mayoría de los testers son hispanohablantes y la app
está en inglés. Pendiente desde el día 22 donde no alcanzó el tiempo.

**Alcance:**
- Migrar todos los strings de UI a `strings.xml` (pantalla por pantalla)
- `strings-es.xml` con traducción al español
- Selector en Settings bajo nueva sección "Language"
- Cambio de idioma sin reiniciar con `AppCompatDelegate`
- Orden recomendado: Settings → HomeScreen → Reader → AddSong → Playlists

**Esfuerzo:** alto (2+ días si se hace completo); medio si se priorizan las
pantallas más visibles primero

---

## Opción B — Nocturno mode en el Reader (v1.2)

**Por qué ahora:** complementa directamente pinch-to-zoom (también v1.2).
Guitarristas en escena o ensayo con poca luz necesitan un modo que no lastime
la visión, distinto al dark mode genérico.

**Alcance:**
- Botón rápido en el Reader (topBar o bottomBar) para activar nocturno
- Fondo negro profundo + texto ámbar (no el dark mode del sistema)
- No sale de la canción — se activa y desactiva en pantalla
- No requiere persistencia (basta con estado en sesión)

**Esfuerzo:** bajo (medio día)

---

## Plan recomendado: B + inicio de A

1. Nocturno mode (Opción B) — cerrar en la mañana (bajo esfuerzo, alto impacto en testers)
2. Arrancar migración de strings (Opción A) — empezar por Settings y HomeScreen

Si B resulta más rápido de lo esperado, se puede completar más pantallas de A.
