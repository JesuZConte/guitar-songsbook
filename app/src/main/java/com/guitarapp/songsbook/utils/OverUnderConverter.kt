// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.utils

/**
 * Converts "over/under" chord notation to bracket format.
 *
 * Over/under:          Bracket output:
 *   Am    F  C  G        [Am]Hello [F]darkness my [C]old [G]friend
 *   Hello darkness...
 *
 * A line is treated as a chord line when ≥80% of its whitespace-separated
 * tokens match the chord regex. Section headers ([Verse 1], [Chorus 1]…)
 * are never treated as chord lines.
 */
object OverUnderConverter {

    private val CHORD_REGEX = Regex(
        """^[A-G][#b]?(m|maj|min|aug|dim|sus|add)?[0-9]{0,2}(/[A-G][#b]?)?$"""
    )
    private val TOKEN_REGEX = Regex("""\S+""")
    private val INLINE_HEADER_REGEX = Regex("""^\s*(\[[^\[\]]+])\s+(\S.*)$""")

    fun isValidChord(token: String): Boolean = CHORD_REGEX.matches(token)

    fun isChordLine(line: String): Boolean {
        if (line.isBlank()) return false
        val trimmed = line.trim()
        if (trimmed.startsWith("[") && trimmed.endsWith("]") &&
            BracketParser.parseSectionHeader(trimmed) != null) return false

        val tokens = TOKEN_REGEX.findAll(line).map { it.value }.toList()
        if (tokens.isEmpty()) return false
        val chordCount = tokens.count { isValidChord(it) }
        return chordCount.toFloat() / tokens.size >= 0.8f
    }

    /**
     * Converts over/under text to bracket format.
     * Returns null when no over/under pattern is found (input unchanged).
     */
    fun convert(input: String): String? {
        val lines = input.lines()
        var converted = false
        val result = StringBuilder()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            // "[Intro] F  Bb  Dm  C": split into the header and a chord-only line. Without this
            // the whole line is either read as a chord row (label dropped) or, when fewer than
            // 80% of tokens are chords, as lyrics.
            val inline = matchInlineHeader(line)
            if (inline != null) {
                converted = true
                result.appendLine(inline.first)
                result.appendLine(bracketChordRow(inline.second))
                i++
                continue
            }

            if (isChordLine(line)) {
                converted = true
                val next = lines.getOrNull(i + 1)
                val nextIsLyric = next != null &&
                        next.isNotBlank() &&
                        !isChordLine(next) &&
                        BracketParser.parseSectionHeader(next.trim()) == null

                if (nextIsLyric) {
                    result.appendLine(mergeChordAndLyricLines(line, next!!))
                    i += 2
                } else {
                    result.appendLine(bracketChordRow(line))
                    i++
                }
            } else {
                result.appendLine(line)
                i++
            }
        }

        return if (converted) result.toString().trimEnd() else null
    }

    /**
     * Matches "[Intro] F  Bb  Dm  C": a section label followed by a chord row on the same line.
     * Returns (label, chordRow), or null when the line is anything else.
     */
    internal fun matchInlineHeader(line: String): Pair<String, String>? {
        val m = INLINE_HEADER_REGEX.find(line) ?: return null
        val header = m.groupValues[1]
        val rest = m.groupValues[2]
        return if (BracketParser.parseSectionHeader(header) != null && isChordLine(rest)) {
            Pair(header, rest)
        } else null
    }

    /** Chord-only line — renders the chords as `[C]   [D]   [Em]`. */
    private fun bracketChordRow(chordLine: String): String =
        TOKEN_REGEX.findAll(chordLine)
            .filter { isValidChord(it.value) }
            .joinToString("   ") { "[${it.value}]" }

    private fun mergeChordAndLyricLines(chordLine: String, lyricLine: String): String {
        // Sort descending so right-to-left insertions don't shift earlier positions
        val chordPositions = TOKEN_REGEX.findAll(chordLine)
            .filter { isValidChord(it.value) }
            .map { Pair(it.range.first, it.value) }
            .sortedByDescending { it.first }

        val sb = StringBuilder(lyricLine)
        // Chords can sit past the end of a short lyric. Pad up to the rightmost chord column so
        // every chord keeps its own column; clamping to the growing string length instead nests
        // later insertions inside earlier "[...]" brackets.
        val lastCol = chordPositions.firstOrNull()?.first ?: 0
        while (sb.length < lastCol) sb.append(' ')
        for ((col, chord) in chordPositions) {
            sb.insert(col, "[$chord]")
        }
        return sb.toString()
    }
}
