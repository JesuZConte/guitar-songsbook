// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.utils

import java.text.Normalizer

fun normalizeTitle(title: String): String =
    Normalizer.normalize(title.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
