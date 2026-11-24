// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songCount: Int = 0
)
