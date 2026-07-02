package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.SongLine

/**
 * Lays out already-converted chord labels into a single monospaced row.
 * Chords are sorted by their declared column and pushed right whenever
 * they would collide with the previous one, guaranteeing at least a
 * 1-space gap so adjacent chords never merge (e.g. "RemSolm" -> "Rem Solm").
 * Longer notations (e.g. Latin "Remmaj7" vs American "Dmmaj7") are why this
 * can't just trust the stored positions as-is.
 */
fun layoutChordRow(chords: List<Pair<String, Int>>): String {
    if (chords.isEmpty()) return ""

    val sb = StringBuilder()
    var cursor = 0
    var nextFree = 0
    for ((chord, position) in chords.sortedBy { it.second }) {
        val start = maxOf(position, nextFree)
        while (cursor < start) {
            sb.append(' ')
            cursor++
        }
        sb.append(chord)
        cursor += chord.length
        nextFree = cursor + 1
    }
    return sb.toString()
}

fun buildChordLine(line: SongLine, notation: NotationSystem = NotationSystem.AMERICAN): String {
    if (line.chords.isEmpty()) return ""

    val converted = line.chords.map { ChordNotation.convert(it.chord, notation) to it.position }
    return layoutChordRow(converted)
}