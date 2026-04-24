package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.SongLine

fun buildChordLine(line: SongLine, notation: NotationSystem = NotationSystem.AMERICAN): String {
    if (line.chords.isEmpty()) return ""

    val converted = line.chords
        .map { it.copy(chord = ChordNotation.convert(it.chord, notation)) }
        .sortedBy { it.position }

    // Push each chord right if it would overlap the previous one,
    // ensuring at least one space gap between adjacent chords.
    var nextFree = 0
    val displayChords = converted.map { cp ->
        val start = maxOf(cp.position, nextFree)
        nextFree = start + cp.chord.length + 1
        cp.copy(position = start)
    }

    val maxPosition = maxOf(
        line.text.length,
        displayChords.maxOf { it.position + it.chord.length }
    )
    val chordLine = CharArray(maxPosition) { ' ' }

    displayChords.forEach { chordPos ->
        chordPos.chord.forEachIndexed { i, char ->
            val index = chordPos.position + i
            if (index < chordLine.size) {
                chordLine[index] = char
            }
        }
    }

    return String(chordLine).trimEnd()
}