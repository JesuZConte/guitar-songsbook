# Día 15 — Completado

## Lo que se hizo

### 1. Icono de la aplicación actualizado
- Nuevo icono 512×512 PNG (`Cancionero_Icon.png`) creado en Canva por la esposa de Luis
- Generados todos los tamaños mipmap: mdpi (48), hdpi (72), xhdpi (96), xxhdpi (144), xxxhdpi (192)
- Eliminados los `.webp` duplicados del proyecto anterior
- Adaptive icon actualizado (`mipmap-anydpi/ic_launcher.xml` y `ic_launcher_round.xml`) para usar `@drawable/ic_launcher_full`
- Icono verificado en dispositivo físico — funciona correctamente

### 2. Version bump
- `versionCode` 1 → 2 (necesario para subir nuevo bundle a Play Store)
- `versionName` se mantiene en "1.0"

### 3. Release bundle generado y subido
- `./gradlew bundleRelease` — build limpio
- Nuevo `.aab` subido a **Prueba interna** en Play Console
- Familia añadida como testers internos con enlace de prueba

### 4. ADRs completados (del día anterior)
- ADR-016: Monetización con AdMob banner
- ADR-017: Naming — Cancionero
- ADR-018: Transposición diferida a v2

### 5. Play Store — descubrimiento importante
- Las cuentas personales creadas después del 13/11/2023 requieren **prueba cerrada obligatoria** antes de poder publicar en producción
- Requisito: mínimo **12 testers** durante **14 días continuos** en prueba cerrada
- Después: solicitar acceso a producción → Google revisa en ≤7 días → Producción

---

## Pendiente para el próximo día

### 1. Configurar Prueba cerrada en Play Console
- Crear el canal de prueba cerrada
- Generar enlace para testers
- Conseguir 12 testers (familia, amigos, comunidades de guitarra online)

### 2. Durante los 14 días de prueba
- Recoger feedback de testers
- Planificar arquitectura de v2 (Firebase Auth + Firestore)
- Planificar transposición de tonalidad (ADR-018)

---

## Estado actual
- App en Prueba interna ✅
- Icono nuevo ✅
- Prueba cerrada: pendiente de configurar
- Producción: bloqueada hasta cumplir requisitos de prueba cerrada
