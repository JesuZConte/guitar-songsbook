# Third-party notices

Cancionero includes or links the components below. Each keeps its own license; none of
them is relicensed by this project.

## Distributed inside the app

| Component | License | Notes |
|---|---|---|
| AndroidX (core-ktx, appcompat, lifecycle, activity-compose, navigation-compose, core-splashscreen, Room) | Apache-2.0 | https://developer.android.com/jetpack/androidx |
| Jetpack Compose (UI, Foundation, Material 3, Material Icons Extended) | Apache-2.0 | via the Compose BOM |
| Kotlin standard library and coroutines | Apache-2.0 | https://kotlinlang.org |
| Gson | Apache-2.0 | https://github.com/google/gson |
| Reorderable (`sh.calvin.reorderable`) | Apache-2.0 | https://github.com/Calvin-LL/Reorderable |
| Google Mobile Ads SDK (`play-services-ads`) | Proprietary (Google terms) | Not GPL; see the additional permission in [NOTICE.md](NOTICE.md) |
| Google Play services Auth (`play-services-auth`) | Proprietary (Google terms) | Same |
| Firebase Analytics and Crashlytics | Proprietary (Firebase terms) | Same |
| Merriweather (regular, bold) | SIL OFL 1.1 | (c) 2024 The Merriweather Project Authors, Reserved Font Name "Merriweather". [licenses/OFL-Merriweather.txt](licenses/OFL-Merriweather.txt) |
| JetBrains Mono | SIL OFL 1.1 | (c) 2020 The JetBrains Mono Project Authors. [licenses/OFL-JetBrainsMono.txt](licenses/OFL-JetBrainsMono.txt) |

Full Apache-2.0 text: https://www.apache.org/licenses/LICENSE-2.0

## Present in the repository but not used by the code

| Component | License | Notes |
|---|---|---|
| Fraunces | SIL OFL 1.1 | (c) The Fraunces Project Authors. [licenses/OFL-Fraunces.txt](licenses/OFL-Fraunces.txt). Replaced by Merriweather in the UI; safe to delete the files under `app/src/main/res/font/Fraunces,JetBrains_Mono/` and `fraunces*.ttf`. |

## Test and build tooling (not distributed)

JUnit (EPL-1.0), Mockito (MIT), kotlinx-coroutines-test, AndroidX Test and Espresso
(Apache-2.0), Android Gradle Plugin, KSP, Google Services and Crashlytics Gradle plugins.

## Bundled example songs

"Amazing Grace" (John Newton, 1779), "Greensleeves" and "Scarborough Fair" are
traditional songs in the public domain. Do not replace them with modern arrangements or
translations without checking their copyright status.
