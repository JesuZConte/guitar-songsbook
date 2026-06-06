package com.guitarapp.songsbook.utils

import java.text.Normalizer

fun normalizeTitle(title: String): String =
    Normalizer.normalize(title.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
