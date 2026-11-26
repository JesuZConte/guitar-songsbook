<!--
DRAFT — do NOT link this from Google Play until every item below is done.
(HTML comments are not shown when GitHub renders the page.)

 1. DONE (2026-11-24): contact email and effective date filled in (both languages).
 2. The consent sentence in section 3.3 (EN and ES) is only true once an ad-consent flow
    (Google UMP) is implemented for EEA/UK users. It is NOT implemented yet.
 3. Verify the Firebase retention settings mentioned in section 7 in the Firebase console.
 4. Make the Play Console "Data safety" form match sections 3-4 exactly.
 5. Re-read against the code: song titles are no longer sent to Analytics/Crashlytics
    (AnalyticsHelper.logSongOpened sends only the internal song id). Keep it that way, or
    update this text.
 6. Have a lawyer review it before the commercial launch.
-->

# Privacy Policy — Cancionero

*Effective date: 2026-11-24 · Español abajo / Spanish version below*

Cancionero ("the app", "we") is a personal songbook for guitarists, published by **Luis Jesus**. This policy explains what information the app handles and why. Contact: **jesuzconte@gmail.com**.

## 1. In short

- **Your songs stay on your device.** We do not upload your songs, lyrics, playlists or notes, and we have no servers that store them.
- There is **no account and no sign-in** in this version.
- The app uses three Google services that collect limited technical data automatically: **Firebase Analytics** (usage statistics), **Firebase Crashlytics** (crash reports) and **Google AdMob** (ads).
- We do not sell your personal information.

## 2. What stays on your device

Songs you add or import, versions, playlists, favorites and your settings (theme, language, chord notation, font size) are stored locally in the app's private storage. We never receive them.

- **Importing** a file (`.json` or `.txt`) is processed on your device.
- **Exporting or sharing** a song (backup file or chord chart) happens only when you choose to, through Android's share menu. It goes to the app you pick, not to us. A backup file contains the song's full content, including lyrics; share it only if you have the right to.
- **Android backup:** if you have device backup turned on, Android may include the app's data in your Google account backup. That is controlled by your device and Google, not by us.
- Uninstalling the app, or clearing its storage, deletes this local data.

## 3. Information collected automatically

We do not ask you for your name, email, phone number or location, and the app does not request any dangerous permission. The Google libraries it contains add the permissions needed to work online (internet access, network state) and to read the advertising ID.

### 3.1 Firebase Analytics (Google)
Counts how the app is used so we can improve it:
- screens viewed, and actions such as: a song was opened (identified only by an internal ID, **never by its title**), added, edited or deleted; a playlist was created; the chord notation was changed;
- technical data: app version, device model, Android version, language, country/region (approximate), and a random app-instance identifier.

### 3.2 Firebase Crashlytics (Google)
When the app crashes, it sends a report with the error details, the app version, device model, Android version, device state (memory, storage, orientation) and a short log of the last actions (for example "song opened", "song saved"), **without song titles or lyrics**. It also uses a random installation identifier.

### 3.3 Google AdMob (Google)
The song library screen shows a banner ad. (The song reader never shows ads.) To serve and measure ads, Google collects your **advertising ID**, IP address, device and app information and ad interactions, and may use them to personalize ads depending on your Google ad settings. In the European Economic Area and the United Kingdom, we ask for your consent before ads that use personal data are shown. You can change your choices in the app and in your Android settings (**Settings → Google → Ads**), where you can also reset or delete your advertising ID.

These Google services process data under Google's own terms; see their policies:
- Google Privacy Policy: https://policies.google.com/privacy
- How Google uses data from apps that use its services: https://policies.google.com/technologies/partner-sites
- Firebase privacy and security: https://firebase.google.com/support/privacy

## 4. Why we use this information

- To keep the app working and fix crashes (Crashlytics).
- To understand which features are used and decide what to improve (Analytics).
- To fund the app through advertising (AdMob).
- To protect against fraud and abuse, including invalid ad traffic (AdMob, Crashlytics).

Where the law requires a legal basis (GDPR/UK GDPR): crash reports are based on our legitimate interest in keeping the app stable; analytics and personalized advertising are based on your consent where required.

## 5. Who receives the information

Only Google (the providers above). We do not sell your personal information and we do not share it with anyone else. Google may process data in countries outside your own, including the United States, under the safeguards described in its policies.

## 6. Children

The app is not directed to children under 13 (or under 16 where local law sets a higher age), and we do not knowingly collect their information.

## 7. How long we keep it

Local data stays until you delete it or uninstall the app. Analytics and crash data are kept by Google for the periods configured in our Firebase project (we use the default settings), and ad data according to Google's policies.

## 8. Your rights and choices

Depending on where you live, you may have the right to access, correct, delete, restrict or object to the processing of your data, to withdraw consent, and to complain to your data protection authority. You can:
- delete all local data by uninstalling the app or clearing its storage (this also resets the app's random identifiers);
- reset or delete your advertising ID and opt out of ad personalization in Android settings;
- contact us at **jesuzconte@gmail.com** (steps, data deleted and retention periods: [Delete your data](delete-data.html)). Because the data is tied to random identifiers and not to your identity, we may ask you for the app version and device details to locate it.

California residents: we do not sell personal information. Ad-related sharing can be limited through your Android ad settings.

## 9. Open source and transparency

Cancionero is open source (GPL v3 or later), so you can inspect exactly what it does: https://github.com/JesuZConte/guitar-songsbook

## 10. Changes

If we add features that change how data is handled (for example accounts or cloud sync), we will update this policy and the app before they take effect. The effective date above shows the latest version.

---

# Política de privacidad — Cancionero

*Fecha de vigencia: 2026-11-24*

Cancionero ("la app", "nosotros") es un cancionero personal para guitarristas, publicado por **Luis Jesus**. Esta política explica qué información maneja la app y para qué. Contacto: **jesuzconte@gmail.com**.

## 1. En resumen

- **Tus canciones se quedan en tu dispositivo.** No subimos tus canciones, letras, listas ni notas, y no tenemos servidores que las guarden.
- En esta versión **no hay cuentas ni inicio de sesión**.
- La app usa tres servicios de Google que recogen automáticamente datos técnicos limitados: **Firebase Analytics** (estadísticas de uso), **Firebase Crashlytics** (informes de errores) y **Google AdMob** (anuncios).
- No vendemos tu información personal.

## 2. Qué se queda en tu dispositivo

Las canciones que agregas o importas, las versiones, las listas, los favoritos y tus ajustes (tema, idioma, notación de acordes, tamaño de letra) se guardan localmente en el almacenamiento privado de la app. Nunca los recibimos.

- **Importar** un archivo (`.json` o `.txt`) se procesa en tu dispositivo.
- **Exportar o compartir** una canción (archivo de respaldo o cifrado de acordes) ocurre solo cuando tú lo decides, mediante el menú de compartir de Android. Va a la app que elijas, no a nosotros. Un archivo de respaldo contiene la canción completa, letra incluida; compártelo solo si tienes derecho a hacerlo.
- **Copia de seguridad de Android:** si tienes activada la copia de seguridad del dispositivo, Android puede incluir los datos de la app en la copia de tu cuenta de Google. Eso lo controlan tu dispositivo y Google, no nosotros.
- Desinstalar la app, o borrar su almacenamiento, elimina estos datos locales.

## 3. Información recogida automáticamente

No te pedimos nombre, correo, teléfono ni ubicación, y la app no solicita ningún permiso peligroso. Las bibliotecas de Google que incluye agregan los permisos necesarios para funcionar en línea (acceso a internet, estado de la red) y para leer el ID de publicidad.

### 3.1 Firebase Analytics (Google)
Cuenta cómo se usa la app para poder mejorarla:
- pantallas vistas y acciones como: se abrió una canción (identificada solo por un ID interno, **nunca por su título**), se agregó, editó o eliminó; se creó una lista; se cambió la notación de acordes;
- datos técnicos: versión de la app, modelo del dispositivo, versión de Android, idioma, país o región (aproximado) y un identificador aleatorio de la instalación.

### 3.2 Firebase Crashlytics (Google)
Cuando la app falla, envía un informe con los detalles del error, la versión de la app, el modelo del dispositivo, la versión de Android, el estado del dispositivo (memoria, almacenamiento, orientación) y un registro breve de las últimas acciones (por ejemplo "canción abierta", "canción guardada"), **sin títulos ni letras de canciones**. También usa un identificador aleatorio de instalación.

### 3.3 Google AdMob (Google)
La pantalla de la biblioteca de canciones muestra un anuncio de banner. (El lector de canciones nunca muestra anuncios.) Para mostrar y medir anuncios, Google recoge tu **ID de publicidad**, dirección IP, información del dispositivo y de la app e interacciones con los anuncios, y puede usarlos para personalizar anuncios según tus ajustes de anuncios de Google. En el Espacio Económico Europeo y el Reino Unido, te pedimos consentimiento antes de mostrar anuncios que usen datos personales. Puedes cambiar tus elecciones en la app y en los ajustes de Android (**Ajustes → Google → Anuncios**), donde también puedes restablecer o eliminar tu ID de publicidad.

Estos servicios de Google tratan los datos bajo los términos de Google; consulta sus políticas:
- Política de privacidad de Google: https://policies.google.com/privacy
- Cómo usa Google los datos de apps que usan sus servicios: https://policies.google.com/technologies/partner-sites
- Privacidad y seguridad de Firebase: https://firebase.google.com/support/privacy

## 4. Para qué usamos la información

- Para que la app funcione y corregir errores (Crashlytics).
- Para entender qué funciones se usan y decidir qué mejorar (Analytics).
- Para financiar la app con publicidad (AdMob).
- Para protegernos del fraude y el abuso, incluido el tráfico publicitario no válido (AdMob, Crashlytics).

Cuando la ley exige una base legal (RGPD/RGPD del Reino Unido): los informes de errores se basan en nuestro interés legítimo en mantener la app estable; el análisis y la publicidad personalizada se basan en tu consentimiento cuando se requiere.

## 5. Quién recibe la información

Solo Google (los proveedores indicados). No vendemos tu información personal ni la compartimos con nadie más. Google puede tratar datos en países distintos al tuyo, incluido Estados Unidos, con las garantías que describe en sus políticas.

## 6. Menores

La app no está dirigida a menores de 13 años (ni de 16 donde la ley local fije una edad mayor), y no recogemos conscientemente su información.

## 7. Cuánto tiempo la conservamos

Los datos locales permanecen hasta que los borres o desinstales la app. Google conserva los datos de análisis y errores durante los períodos configurados en nuestro proyecto de Firebase (usamos los valores predeterminados), y los datos de anuncios según sus políticas.

## 8. Tus derechos y opciones

Según dónde vivas, puedes tener derecho a acceder, rectificar, suprimir, limitar u oponerte al tratamiento de tus datos, a retirar tu consentimiento y a presentar una reclamación ante tu autoridad de protección de datos. Puedes:
- borrar todos los datos locales desinstalando la app o borrando su almacenamiento (esto también restablece los identificadores aleatorios de la app);
- restablecer o eliminar tu ID de publicidad y desactivar la personalización de anuncios en los ajustes de Android;
- escribirnos a **jesuzconte@gmail.com** (pasos, datos eliminados y plazos de conservación: [Eliminar tus datos](delete-data.html)). Como los datos están asociados a identificadores aleatorios y no a tu identidad, podemos pedirte la versión de la app y datos del dispositivo para localizarlos.

Residentes de California: no vendemos información personal. Puedes limitar el uso compartido relacionado con anuncios en los ajustes de anuncios de Android.

## 9. Código abierto y transparencia

Cancionero es de código abierto (GPL v3 o posterior), así que puedes revisar exactamente qué hace: https://github.com/JesuZConte/guitar-songsbook

## 10. Cambios

Si agregamos funciones que cambien el tratamiento de los datos (por ejemplo cuentas o sincronización en la nube), actualizaremos esta política y la app antes de que entren en vigor. La fecha de vigencia indica la versión más reciente.
