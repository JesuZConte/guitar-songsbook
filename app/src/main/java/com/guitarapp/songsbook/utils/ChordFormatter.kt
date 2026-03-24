package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.SongLine

fun buildChordLine(line: SongLine): String {
    if (line.chords.isEmpty()) return ""

    val maxPosition = maxOf(
        line.text.length,
        line.chords.maxOf { it.position + it.chord.length }
    )
    val chordLine = CharArray(maxPosition) { ' ' }

    line.chords.forEach { chordPos ->
        chordPos.chord.forEachIndexed { i, char ->
            val index = chordPos.position + i
            if (index < chordLine.size) {
                chordLine[index] = char
            }
        }
    }

    return String(chordLine).trimEnd()
}