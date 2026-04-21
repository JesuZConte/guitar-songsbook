package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.Song

object SongExporter {

    /**
     * Builds a plain-text chord sheet for sharing: title, artist, key/capo, chord list,
     * and per-section chord names. No lyrics — safe to share publicly.
     */
    fun buildChordShareText(song: Song): String {
        val sb = StringBuilder()
        sb.append("${song.title} — ${song.artist}")
        if (song.key.isNotBlank()) {
            sb.append("\nKey: ${song.key}")
            if (song.capo > 0) sb.append(" | Capo: ${song.capo}")
        }
        if (song.chords.isNotEmpty()) {
            sb.append("\nChords: ${song.chords.joinToString(", ")}")
        }

        song.content.forEach { section ->
            sb.append("\n\n[${section.type.replaceFirstChar { it.uppercase() }} ${section.number}]")
            section.lines.forEach { line ->
                if (line.chords.isNotEmpty()) {
                    sb.append("\n${line.chords.joinToString("  ") { it.chord }}")
                }
            }
        }

        return sb.toString()
    }
}
