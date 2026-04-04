package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection

/**
 * Converts the structured SongSection/SongLine/ChordPosition model back to bracket-format text
 * so existing songs can be pre-filled in AddSongScreen for editing.
 */
object BracketSerializer {

    fun serialize(sections: List<SongSection>): String {
        return sections.joinToString("\n\n") { section ->
            val header = "[${section.type.replaceFirstChar { it.uppercase() }} ${section.number}]"
            val body = section.lines.joinToString("\n") { serializeLine(it) }
            "$header\n$body"
        }
    }

    fun serializeSectionContent(section: SongSection): String {
        return section.lines.joinToString("\n") { serializeLine(it) }
    }

    private fun serializeLine(line: SongLine): String {
        if (line.chords.isEmpty()) return line.text

        val sb = StringBuilder(line.text)
        // Insert chord brackets right to left so earlier positions stay valid
        line.chords.sortedByDescending { it.position }.forEach { chordPos ->
            val pos = minOf(chordPos.position, sb.length)
            sb.insert(pos, "[${chordPos.chord}]")
        }
        return sb.toString()
    }
}