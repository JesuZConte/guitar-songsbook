// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.presentation.screens

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.guitarapp.songsbook.ads.AdUnits
import com.guitarapp.songsbook.ads.BannerAdHolder
import com.guitarapp.songsbook.ads.BannerHandle
import kotlinx.coroutines.flow.first

/** AdMob `AdView` adapted to [BannerHandle] so [BannerAdHolder] can manage it. */
class AdViewBanner(context: Context) : BannerHandle {
    val view: AdView = AdView(context).apply {
        setAdSize(AdSize.BANNER)
        adUnitId = AdUnits.banner()
    }

    override fun load() = view.loadAd(AdRequest.Builder().build())
    override fun pause() = view.pause()
    override fun resume() = view.resume()
    override fun destroy() = view.destroy()
}

/** Height of [AdSize.BANNER]; reserved up front so the list does not jump when the ad arrives. */
private val BANNER_HEIGHT = 50.dp

/**
 * Shows the Activity's single banner.
 *
 * The first time, the `AdView` (and its WebView) is created only once this navigation entry is
 * RESUMED, which Navigation Compose reaches after the 300 ms enter transition. Creating it
 * earlier made it compete with the slide animation (measured on the A50: p99 up to 300 ms).
 * Later visits reuse the existing view, so they attach it straight away.
 */
@Composable
fun BannerAd(holder: BannerAdHolder<AdViewBanner>, modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var ready by remember { mutableStateOf(holder.isCreated) }
    LaunchedEffect(lifecycle) {
        if (!ready) {
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            ready = true
        }
    }
    Box(
        modifier = modifier.fillMaxWidth().height(BANNER_HEIGHT),
        contentAlignment = Alignment.Center
    ) {
        if (ready) {
            AndroidView(
                factory = {
                    holder.obtain().view.also { (it.parent as? ViewGroup)?.removeView(it) }
                }
            )
            DisposableEffect(holder) {
                holder.onShown()
                onDispose { holder.onHidden() }
            }
        }
    }
}
