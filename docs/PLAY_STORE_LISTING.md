# Play Store Listing — Cancionero – Chord Songbook

---

## App title
Cancionero – Chord Songbook

## Short description (80 chars max)
Your personal chord notebook. Add songs, organize playlists, play with confidence.

---

## Full description (4000 chars max)

Cancionero is the personal notebook every guitarist needs.

Add the songs you actually play — your favorite covers, originals, or session material — with chords aligned exactly where they fall in the lyrics. No subscriptions, no internet required, no bloat. Just your songs, always at hand.

**Add your own songs**
Write songs using a simple bracket notation: place chords in [brackets] right above the word they land on. The app aligns them automatically so you always know where to play what.

**Read comfortably while you play**
The reader view is designed for real practice sessions. Adjust the font size on the fly, navigate between song sections with a swipe, and go fullscreen to remove all distractions.

**Organize your repertoire**
Group songs into playlists — set lists for a gig, songs you're learning, or favorites by style. Add a song to as many playlists as you need.

**American or Latin notation**
Switch between C D E F G A B and Do Re Mi Fa Sol La Si with one toggle. The preference is saved globally so every song shows chords the way you think.

**Completely offline**
Your songbook lives on your device. No account, no sync, no data sent anywhere. Open the app and your songs are there — whether you're backstage, at a campfire, or on a plane.

**Built for guitarists, by a guitarist**
No unnecessary features. No clutter. Just a clean, fast tool that gets out of the way so you can focus on playing.

---

## Content rating questionnaire answers

| Question | Answer |
|---|---|
| Does the app contain violence? | No |
| Does the app contain sexual content? | No |
| Does the app contain profanity? | No |
| Does the app contain controlled substance references? | No |
| Does the app target children under 13? | No |
| Does the app contain ads? | Yes — banner ads (AdMob) |
| Does the app collect personal data? | Limited technical data and the advertising ID through Google SDKs (Analytics, Crashlytics, AdMob); no name, email or location; songs stay on the device. See Data safety below |
| Does the app use location? | No |
| Does the app use the camera or microphone? | No |

**Expected rating: Everyone (E)**

---

## Category
Music & Audio

## Tags (Play Store keywords)
guitar, chords, songbook, chord chart, tabs, setlist, playlist, musician, band, acoustic

---

## Data safety section (Play Store form)

> Rewritten 2026-11-24 to match the code. The old table said "Data collected: None", which is
> not accurate: Analytics, Crashlytics and AdMob collect data. **Compare with what is actually
> declared in Play Console and make them match `docs/PRIVACY_POLICY.md`.**

| Data type | Collected | Shared | Purpose | Source |
|---|---|---|---|---|
| App interactions (screens, song opened by internal id, add/edit/delete) | Yes | Yes (Google) | Analytics | Firebase Analytics |
| Crash logs and diagnostics | Yes | Yes (Google) | App functionality / analytics | Firebase Crashlytics |
| Device or other IDs (app-instance id, installation id, advertising ID) | Yes | Yes (Google) | Analytics, advertising | Firebase, AdMob |
| Approximate location (from IP) | Yes | Yes (Google) | Analytics, advertising | Firebase, AdMob |
| Songs, lyrics, playlists, notes | **No** (never leave the device) | No | — | — |
| Name, email, phone, precise location, contacts | No | No | — | — |

Encrypted in transit: yes (SDK traffic over HTTPS). Account creation: none. Data deletion
request: contact email in the privacy policy; local data is removed by uninstalling.


### Answers for the Play Console form (prepared 2026-11-24)

Based on the code: manifest permissions come only from the Google SDKs (internet, network state,
advertising ID); no accounts; songs stay on the device; Analytics is off until consent
(ADR-027); Crashlytics is always on.

1. **Collects or shares required user data types?** Yes.
2. **All data encrypted in transit?** Yes.
3. **Way to request data deletion?** Yes: contact email and the policy page. Local data is removed by uninstalling.
4. **Data types to tick** (each: *collected* and *shared*; AdMob is a third-party ad network, so treat as shared):
   - Location > **Approximate location**: Analytics, advertising.
   - App activity > **App interactions**: Analytics.
   - App info and performance > **Crash logs** and **Diagnostics**: app functionality, analytics.
   - Device or other IDs > **Device or other IDs** (app-instance id, installation id, advertising ID): analytics, advertising or marketing, fraud prevention.
5. **Is collection required or optional?** Required (users outside the EEA/UK/Switzerland cannot opt out; inside them the consent form applies).
6. **Not collected, leave unticked:** personal info (name, email, phone, address), financial info, health, messages, photos and videos, audio, **files and docs** (imported files are read on the device and never sent), calendar, contacts, web browsing, precise location.
7. **Cross-check:** verify the AdMob and Firebase lists against Google's own pages ("Google Mobile Ads SDK: Play data disclosure" and "Firebase: Play data disclosure"), since SDK versions can change what they collect. Re-check after each SDK upgrade.
8. **Privacy policy URL:** https://jesuzconte.github.io/guitar-songsbook/privacy.html

---

## Notes before submission

- [ ] Replace AdMob test IDs with real unit IDs before release
- [ ] Replace placeholder `google-services.json` with real Firebase project file
- [ ] Generate signed release APK and verify it launches cleanly
- [ ] Upload at least 2 screenshots (phone) — required by Play Store
- [ ] Upload a 512×512 px app icon (high-res)
- [ ] Upload a 1024×500 px feature graphic (optional but recommended)
