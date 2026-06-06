package com.guitarapp.songsbook.domain.model

data class ImportConflict(
    val existing: Song,
    val incoming: Song,
    val suggestedVersionName: String
)

fun suggestVersionName(existing: Song, incoming: Song): String {
    val defaultVersion = existing.versions.firstOrNull { it.name == "Default" }
    val existingKey = defaultVersion?.key ?: existing.key
    val existingCapo = defaultVersion?.capo ?: existing.capo
    return if (incoming.key != existingKey || incoming.capo != existingCapo) {
        if (incoming.capo == 0) incoming.key else "${incoming.key} / Capo ${incoming.capo}"
    } else {
        "Versión ${existing.versions.size.coerceAtLeast(1) + 1}"
    }
}
