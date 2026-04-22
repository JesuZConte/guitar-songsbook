package com.guitarapp.songsbook.utils

enum class NotationSystem {
    AMERICAN,
    LATIN
}

object ChordNotation {

    // ── Transposition ─────────────────────────────────────────────────────────

    private val NOTE_TO_SEMITONE = mapOf(
        "C" to 0, "B#" to 0,
        "C#" to 1, "Db" to 1,
        "D" to 2,
        "D#" to 3, "Eb" to 3,
        "E" to 4, "Fb" to 4,
        "F" to 5, "E#" to 5,
        "F#" to 6, "Gb" to 6,
        "G" to 7,
        "G#" to 8, "Ab" to 8,
        "A" to 9,
        "A#" to 10, "Bb" to 10,
        "B" to 11, "Cb" to 11
    )

    private val SHARP_NOTES = arrayOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")
    private val FLAT_NOTES  = arrayOf("C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B")

    private val ROOT_REGEX = Regex("""^[A-G][#b]?""")

    /**
     * Transposes [chord] by [semitones] semitones (positive = up, negative = down).
     * Preserves chord quality (m, maj7, sus4…) and slash bass notes.
     * Uses sharps when transposing up, flats when transposing down.
     */
    fun transpose(chord: String, semitones: Int): String {
        if (semitones == 0) return chord
        val slashIdx = chord.lastIndexOf('/')
        return if (slashIdx > 0) {
            val main = chord.substring(0, slashIdx)
            val bass = chord.substring(slashIdx + 1)
            "${transposeNote(main, semitones)}/${transposeNote(bass, semitones)}"
        } else {
            transposeNote(chord, semitones)
        }
    }

    private fun transposeNote(chord: String, semitones: Int): String {
        val match = ROOT_REGEX.find(chord) ?: return chord
        val root = match.value
        val quality = chord.removePrefix(root)
        val semitone = NOTE_TO_SEMITONE[root] ?: return chord
        val newSemitone = ((semitone + semitones) % 12 + 12) % 12
        val newRoot = if (semitones > 0) SHARP_NOTES[newSemitone] else FLAT_NOTES[newSemitone]
        return newRoot + quality
    }

    // ── Notation conversion ───────────────────────────────────────────────────

    private val AMERICAN_TO_LATIN = listOf(
        "Ab" to "Lab",
        "A#" to "La#",
        "Bb" to "Sib",
        "Cb" to "Dob",
        "C#" to "Do#",
        "Db" to "Reb",
        "D#" to "Re#",
        "Eb" to "Mib",
        "E#" to "Mi#",
        "Fb" to "Fab",
        "F#" to "Fa#",
        "Gb" to "Solb",
        "G#" to "Sol#",
        "A" to "La",
        "B" to "Si",
        "C" to "Do",
        "D" to "Re",
        "E" to "Mi",
        "F" to "Fa",
        "G" to "Sol"
    )

    private val LATIN_TO_AMERICAN = AMERICAN_TO_LATIN.map { (am, lat) -> lat to am }
        .sortedByDescending { it.first.length }

    fun toLatinNotation(chord: String): String {
        for ((american, latin) in AMERICAN_TO_LATIN) {
            if (chord.startsWith(american)) {
                return latin + chord.removePrefix(american)
            }
        }
        return chord
    }

    fun toAmericanNotation(chord: String): String {
        for ((latin, american) in LATIN_TO_AMERICAN) {
            if (chord.startsWith(latin)) {
                return american + chord.removePrefix(latin)
            }
        }
        return chord
    }

    fun convert(chord: String, target: NotationSystem): String {
        return when (target) {
            NotationSystem.AMERICAN -> toAmericanNotation(chord)
            NotationSystem.LATIN -> toLatinNotation(chord)
        }
    }
}
