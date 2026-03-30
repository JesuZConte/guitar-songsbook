package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.ChordPosition
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection

/**
 * Parses bracket-format song text into structured SongSection/SongLine/ChordPosition.
 *
 * Bracket format examples:
 *   [Verse 1]
 *   [Am]Hello [F]darkness my [C]old [G]friend
 *
 *   [Chorus]
 *   [C]   [D]   [Em]
 */
object BracketParser {

    private val BRACKET_REGEX = Regex("""\[([^\]]+)]""")

    private val SECTION_NAMES = setOf(
        "verse", "chorus", "bridge", "intro", "outro",
        "pre-chorus", "prechorus", "interlude", "solo",
        "instrumental", "hook", "refrain", "coda", "tag"
    )

    fun parse(text: String): List<SongSection> {
        val lines = text.lines()
        val sections = mutableListOf<SongSection>()
        var currentType = "verse"
        var currentNumber = 1
        var currentLines = mutableListOf<SongLine>()
        val sectionCounters = mutableMapOf<String, Int>()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                if (currentLines.isNotEmpty()) {
                    currentLines.add(SongLine(text = "", chords = emptyList()))
                }
                continue
            }

            val sectionHeader = parseSectionHeader(trimmed)
            if (sectionHeader != null) {
                if (currentLines.isNotEmpty()) {
                    sections.add(SongSection(currentType, currentNumber, dropTrailingBlanks(currentLines)))
                    currentLines = mutableListOf()
                }
                currentType = sectionHeader.first
                currentNumber = if (sectionHeader.second > 0) {
                    sectionHeader.second
                } else {
                    val count = sectionCounters.getOrDefault(currentType, 0) + 1
                    sectionCounters[currentType] = count
                    count
                }
                continue
            }

            currentLines.add(parseLine(trimmed))
        }

        if (currentLines.isNotEmpty()) {
            sections.add(SongSection(currentType, currentNumber, dropTrailingBlanks(currentLines)))
        }

        if (sections.isEmpty() && text.isNotBlank()) {
            sections.add(SongSection("verse", 1, listOf(SongLine(text.trim(), emptyList()))))
        }

        return sections
    }

    fun parseLine(line: String): SongLine {
        val matches = BRACKET_REGEX.findAll(line).toList()

        if (matches.isEmpty()) {
            return SongLine(text = line, chords = emptyList())
        }

        val chords = mutableListOf<ChordPosition>()
        val textBuilder = StringBuilder()
        var lastEnd = 0

        for (match in matches) {
            textBuilder.append(line.substring(lastEnd, match.range.first))
            val position = textBuilder.length
            val chord = match.groupValues[1]
            chords.add(ChordPosition(chord = chord, position = position))
            lastEnd = match.range.last + 1
        }

        textBuilder.append(line.substring(lastEnd))

        return SongLine(
            text = textBuilder.toString(),
            chords = chords
        )
    }

    internal fun parseSectionHeader(line: String): Pair<String, Int>? {
        if (!line.startsWith("[") || !line.endsWith("]")) return null

        val inner = line.substring(1, line.length - 1).trim()
        if (inner.isEmpty()) return null

        val parts = inner.lowercase().split(Regex("""\s+"""), limit = 2)
        val name = parts[0]

        if (name !in SECTION_NAMES) return null

        val number = if (parts.size > 1) {
            parts[1].toIntOrNull() ?: 0
        } else {
            0
        }

        return Pair(name, number)
    }

    fun extractChordNames(sections: List<SongSection>): List<String> {
        return sections
            .flatMap { it.lines }
            .flatMap { it.chords }
            .map { it.chord }
            .distinct()
    }

    private fun dropTrailingBlanks(lines: MutableList<SongLine>): List<SongLine> {
        while (lines.isNotEmpty() && lines.last().text.isEmpty() && lines.last().chords.isEmpty()) {
            lines.removeAt(lines.size - 1)
        }
        return lines.toList()
    }
}
