# AdMob Account Setup

Steps to create your AdMob account and get the IDs needed for Day 8 integration.

## 1. Create an AdMob account

1. Go to admob.google.com
2. Sign in with your Google account (same one you'll use for Play Console)
3. Accept the terms of service
4. Fill in your country and payment info

## 2. Register the app in AdMob

1. In the AdMob dashboard, go to **Apps > Add App**
2. Select **Android**
3. "Is the app listed on a supported app store?" → **No** (not published yet)
4. App name: **Guitar Songbook**
5. Click **Add**
6. Save the **App ID** — looks like `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`

## 3. Create ad units

Create two **Banner** ad units:

1. Inside your app, go to **Ad units > Add ad unit**
2. Select **Banner**
3. Create `home_banner` → save its Ad Unit ID
4. Repeat → create `playlists_banner` → save its Ad Unit ID

Ad Unit IDs look like: `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`

## 4. IDs needed for Day 8

| Value | Where it goes |
|-------|--------------|
| App ID (`ca-app-pub-XXX~YYY`) | `AndroidManifest.xml` |
| Home banner Ad Unit ID | `HomeScreen.kt` |
| Playlists banner Ad Unit ID | `PlaylistsScreen.kt` |

## Notes

- During development we use AdMob test IDs — never click your own real ads (policy violation)
- After publishing, AdMob takes 1–2 days to start serving real ads
- Payment threshold is typically $100
