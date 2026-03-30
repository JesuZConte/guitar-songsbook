package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.SongLine

fun buildChordLine(line: SongLine, notation: NotationSystem = NotationSystem.AMERICAN): String {
    if (line.chords.isEmpty()) return ""

    val displayChords = line.chords.map { it.copy(chord = ChordNotation.convert(it.chord, notation)) }

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