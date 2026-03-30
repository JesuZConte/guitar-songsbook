package com.guitarapp.songsbook.utils

enum class NotationSystem {
    AMERICAN,
    LATIN
}

object ChordNotation {

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
