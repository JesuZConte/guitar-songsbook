package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.ChordPosition
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.utils.ChordNotation

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

    /** Spanish label (accent-folded, lowercase) → canonical section key. */
    private val SECTION_ALIASES = mapOf(
        "estrofa" to "verse",
        "verso" to "verse",
        "estribillo" to "chorus",
        "coro" to "chorus",
        "puente" to "bridge",
        "introduccion" to "intro",
        "final" to "outro",
        "pre-coro" to "pre-chorus",
        "precoro" to "pre-chorus",
        "pre-estribillo" to "pre-chorus",
        "preestribillo" to "pre-chorus"
    )

    /** "[Primera Parte]" style labels, which carry their own verse number. */
    private val ORDINAL_PARTS = mapOf(
        "primera parte" to 1,
        "segunda parte" to 2,
        "tercera parte" to 3,
        "cuarta parte" to 4
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
            val chord = ChordNotation.toAmericanNotation(match.groupValues[1])
            chords.add(ChordPosition(chord = chord, position = position))
            lastEnd = match.range.last + 1
        }

        textBuilder.append(line.substring(lastEnd))

        return SongLine(
            text = textBuilder.toString(),
            chords = chords
        )
    }

    /**
     * Parses a `[Label]` line into (section type, explicit number — 0 when absent).
     *
     * The returned type is a language-neutral key (`verse`, `chorus`…) that the reader localizes
     * at display time, so Spanish labels are normalized to the English key here (ADR-025).
     * Labels we don't recognize are kept verbatim as their own section type, never as chords.
     */
    internal fun parseSectionHeader(line: String): Pair<String, Int>? {
        if (!line.startsWith("[") || !line.endsWith("]")) return null

        val inner = line.substring(1, line.length - 1).trim()
        if (inner.isEmpty() || '[' in inner || ']' in inner) return null

        val folded = normalizeTitle(inner)
        ORDINAL_PARTS[folded]?.let { return Pair("verse", it) }

        val parts = folded.split(Regex("""\s+"""), limit = 2)
        val name = parts[0]
        val rest = parts.getOrNull(1)

        // English names: the first word decides, whatever follows is tolerated ("[Chorus x2]").
        if (name in SECTION_NAMES) return Pair(name, rest?.toIntOrNull() ?: 0)

        // Spanish aliases: stricter — only an optional number may follow, so that
        // "[Puente instrumental]" stays a custom label instead of collapsing into "bridge".
        val aliasKey = SECTION_ALIASES[name]
        if (aliasKey != null && (rest == null || rest.first().isDigit())) {
            return Pair(aliasKey, rest?.toIntOrNull() ?: 0)
        }

        return if (isCustomLabel(inner)) Pair(inner, 0) else null
    }

    /** A label we can't map: multi-word, or a single plain word that isn't a chord name. */
    private fun isCustomLabel(inner: String): Boolean {
        if (inner.any { it.isWhitespace() }) return true
        if (!inner.all { it.isLetter() || it == '-' }) return false
        val american = ChordNotation.toAmericanNotation(inner)
        return !OverUnderConverter.isValidChord(inner) && !OverUnderConverter.isValidChord(american)
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
