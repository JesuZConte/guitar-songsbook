package com.guitarapp.songsbook.utils

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/** What an imported .txt turns into before it reaches the Add Song screen. */
data class TextSongDraft(
    val title: String,
    val artist: String,
    val key: String,
    val capo: String,
    val body: String
)

sealed interface TextImportResult {
    data class Success(val draft: TextSongDraft) : TextImportResult
    data object Empty : TextImportResult
    data object TooLarge : TextImportResult
    data object NotText : TextImportResult
}

/**
 * Reads a plain-text song file and extracts the title/artist/key/capo header lines so that
 * only the song body is left for the existing "Detectar Formato" pipeline (ADR-025).
 */
object TextSongImport {

    /** A song file is a few KB; anything above this was picked by mistake. */
    const val MAX_BYTES = 200 * 1024

    /** Bare (unlabeled) title/artist lines are short; a long first line is lyrics. */
    private const val MAX_BARE_LINE = 60

    private val WINDOWS_1252: Charset = Charset.forName("windows-1252")

    private val LABEL_REGEX = Regex("""^(\p{L}+)\s*:\s*(.*)$""")
    private val CAPO_REGEX = Regex("""^(?:capo|cejilla|cejuela|capotraste)\s*:?\s*(?:traste\s*)?(\d{1,2})\b""", RegexOption.IGNORE_CASE)

    private val TITLE_LABELS = setOf("titulo", "title", "cancion", "song")
    private val ARTIST_LABELS = setOf("artista", "artist", "autor", "interprete", "by", "por")
    private val KEY_LABELS = setOf("tono", "key", "tonalidad")
    private val IGNORED_LABELS = setOf("tempo", "bpm", "compas", "afinacion", "tuning")
    private val MINOR_WORDS = setOf("menor", "minor", "m")

    // ── Bytes → text ──────────────────────────────────────────────────────────

    /** Decodes [bytes] as text, or returns null when they are not text (NUL bytes). */
    fun decode(bytes: ByteArray): String? {
        val text = when {
            bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() ->
                String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
            bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() ->
                String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
            bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() ->
                decodeUtf8OrWindows1252(bytes, offset = 3)
            else -> decodeUtf8OrWindows1252(bytes, offset = 0)
        }
        return if ('\u0000' in text) null else text
    }

    /** Strict UTF-8 first; files saved by Windows tools are usually Windows-1252 instead. */
    private fun decodeUtf8OrWindows1252(bytes: ByteArray, offset: Int): String {
        val buffer = ByteBuffer.wrap(bytes, offset, bytes.size - offset)
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(buffer)
                .toString()
        } catch (_: CharacterCodingException) {
            String(bytes, offset, bytes.size - offset, WINDOWS_1252)
        }
    }

    fun parse(fileName: String, bytes: ByteArray): TextImportResult {
        if (bytes.size > MAX_BYTES) return TextImportResult.TooLarge
        val text = decode(bytes) ?: return TextImportResult.NotText
        if (text.isBlank()) return TextImportResult.Empty
        return TextImportResult.Success(extractDraft(fileName, text))
    }

    /** A Cancionero JSON export picked through the same file chooser. */
    fun looksLikeJson(fileName: String, bytes: ByteArray): Boolean {
        if (fileName.endsWith(".json", ignoreCase = true)) return true
        val head = decode(bytes.copyOf(minOf(bytes.size, 256))) ?: return false
        return head.trimStart().startsWith("{")
    }

    // ── Text → draft ──────────────────────────────────────────────────────────

    fun extractDraft(fileName: String, rawText: String): TextSongDraft {
        val lines = rawText.replace("\r\n", "\n").replace('\r', '\n').split('\n')

        // Header lines only count before the first real song line. If the file never has a
        // section header or chord row it is lyrics only, so bare lines are never taken as title.
        val bodyStart = lines.indexOfFirst { isBodyStart(it) }
        val limit = if (bodyStart >= 0) bodyStart else lines.size

        var title = ""
        var artist = ""
        var key = ""
        var capo = ""
        val bare = mutableListOf<String>()
        val consumed = BooleanArray(lines.size)

        for (i in 0 until limit) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            val capoMatch = CAPO_REGEX.find(line)
            if (capoMatch != null) {
                capo = capoMatch.groupValues[1]
                consumed[i] = true
                continue
            }

            val label = LABEL_REGEX.find(line)
            val labelName = label?.let { normalizeTitle(it.groupValues[1]) }
            val value = label?.groupValues?.get(2)?.trim().orEmpty()
            when {
                labelName in TITLE_LABELS -> { title = value; consumed[i] = true }
                labelName in ARTIST_LABELS -> { artist = value; consumed[i] = true }
                labelName in KEY_LABELS -> { key = parseKey(value); consumed[i] = true }
                labelName in IGNORED_LABELS -> consumed[i] = true
                // Unlabeled: only a short title/artist pair directly before the song proper.
                bodyStart < 0 || bare.size >= 2 || line.length > MAX_BARE_LINE -> break
                else -> { bare.add(line); consumed[i] = true }
            }
        }

        if (bare.size == 1 && title.isEmpty() && artist.isEmpty() && " - " in bare[0]) {
            val parts = bare[0].split(" - ")
            if (parts.size == 2) {
                title = parts[0].trim()
                artist = parts[1].trim()
                bare.clear()
            }
        }
        for (line in bare) {
            if (title.isEmpty()) title = line else if (artist.isEmpty()) artist = line
        }

        if (title.isEmpty()) title = titleFromFileName(fileName)

        val body = lines.filterIndexed { i, _ -> !consumed[i] }
            .dropWhile { it.isBlank() }
            .joinToString("\n")
            .trimEnd()

        return TextSongDraft(title = title, artist = artist, key = key, capo = capo, body = body)
    }

    private fun isBodyStart(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return false
        return BracketParser.parseSectionHeader(trimmed) != null ||
            OverUnderConverter.isChordLine(line) ||
            OverUnderConverter.matchInlineHeader(line) != null
    }

    /** "C (Do mayor) | Tempo: 80 BPM" → "C"; "La menor" → "Am"; unreadable → "". */
    private fun parseKey(value: String): String {
        val tokens = value.split(Regex("""[\s|(),]+""")).filter { it.isNotEmpty() }
        val root = ChordNotation.toAmericanNotation(tokens.firstOrNull() ?: return "")
        val minor = tokens.getOrNull(1)?.let { normalizeTitle(it) in MINOR_WORDS } == true
        val chord = if (minor && !root.contains('m')) root + "m" else root
        return if (OverUnderConverter.isValidChord(chord)) chord else ""
    }

    private fun titleFromFileName(fileName: String): String {
        val name = fileName.substringAfterLast('/').trim()
        val dot = name.lastIndexOf('.')
        return if (dot > 0 && name.length - dot <= 5) name.substring(0, dot) else name
    }
}
