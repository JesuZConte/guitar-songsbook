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
                    // Chord-only line — render bracketed chords
                    val chords = TOKEN_REGEX.findAll(line)
                        .filter { isValidChord(it.value) }
                        .joinToString("   ") { "[${it.value}]" }
                    result.appendLine(chords)
                    i++
                }
            } else {
                result.appendLine(line)
                i++
            }
        }

        return if (converted) result.toString().trimEnd() else null
    }

    private fun mergeChordAndLyricLines(chordLine: String, lyricLine: String): String {
        // Sort descending so right-to-left insertions don't shift earlier positions
        val chordPositions = TOKEN_REGEX.findAll(chordLine)
            .filter { isValidChord(it.value) }
            .map { Pair(it.range.first, it.value) }
            .sortedByDescending { it.first }

        val sb = StringBuilder(lyricLine)
        for ((col, chord) in chordPositions) {
            val pos = minOf(col, sb.length)
            sb.insert(pos, "[$chord]")
        }
        return sb.toString()
    }
}
