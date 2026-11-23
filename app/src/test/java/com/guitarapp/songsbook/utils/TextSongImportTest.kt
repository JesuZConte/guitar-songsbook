package com.guitarapp.songsbook.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.Charset

/** ADR-025 phase 1b: reading a .txt file and turning it into an AddSong draft. Lyrics are placeholders. */
class TextSongImportTest {

    private val win1252: Charset = Charset.forName("windows-1252")

    private fun extract(text: String, fileName: String = "song.txt") =
        TextSongImport.extractDraft(fileName, text)

    // ── decode ──

    @Test
    fun `decode - plain UTF-8 keeps accents and enye`() {
        assertEquals("canción ñandú", TextSongImport.decode("canción ñandú".toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun `decode - UTF-8 BOM is stripped`() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "hola".toByteArray(Charsets.UTF_8)
        assertEquals("hola", TextSongImport.decode(bytes))
    }

    @Test
    fun `decode - Windows-1252 bytes fall back instead of showing replacement characters`() {
        val decoded = TextSongImport.decode("canción ñandú".toByteArray(win1252))
        assertEquals("canción ñandú", decoded)
    }

    @Test
    fun `decode - UTF-16 with BOM is supported`() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + "canción".toByteArray(Charsets.UTF_16LE)
        assertEquals("canción", TextSongImport.decode(bytes))
    }

    @Test
    fun `decode - binary content with NUL bytes is not text`() {
        assertNull(TextSongImport.decode(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x00, 0x01, 0x02)))
    }

    // ── parse (size / empty guards) ──

    @Test
    fun `parse - empty and blank files are reported as Empty`() {
        assertEquals(TextImportResult.Empty, TextSongImport.parse("a.txt", ByteArray(0)))
        assertEquals(TextImportResult.Empty, TextSongImport.parse("a.txt", "  \n \n".toByteArray()))
    }

    @Test
    fun `parse - files above the size limit are rejected`() {
        val big = ByteArray(TextSongImport.MAX_BYTES + 1) { 'a'.code.toByte() }
        assertEquals(TextImportResult.TooLarge, TextSongImport.parse("a.txt", big))
    }

    @Test
    fun `parse - binary files are reported as NotText`() {
        assertEquals(TextImportResult.NotText, TextSongImport.parse("a.txt", byteArrayOf(1, 0, 2, 0)))
    }

    @Test
    fun `parse - valid text returns a draft`() {
        val result = TextSongImport.parse("Mi canción.txt", "Am\nhola".toByteArray())
        assertTrue(result is TextImportResult.Success)
        assertEquals("Mi canción", (result as TextImportResult.Success).draft.title)
    }

    // ── looksLikeJson ──

    @Test
    fun `looksLikeJson - by extension or by leading brace`() {
        assertTrue(TextSongImport.looksLikeJson("song.json", "anything".toByteArray()))
        assertTrue(TextSongImport.looksLikeJson("song.txt", "  {\"title\":\"x\"}".toByteArray()))
        assertFalse(TextSongImport.looksLikeJson("song.txt", "[Intro] Am  G".toByteArray()))
        assertFalse(TextSongImport.looksLikeJson("song.txt", "Am\nhola".toByteArray()))
    }

    // ── extractDraft: labeled metadata ──

    @Test
    fun `extract - labeled title and key are consumed from the body`() {
        val draft = extract(
            "Título: Mi canción\nTono: C (Do mayor) | Tempo: 80 BPM\n\n[Estrofa 1]\nC   Am\nhola mundo"
        )
        assertEquals("Mi canción", draft.title)
        assertEquals("", draft.artist)
        assertEquals("C", draft.key)
        assertEquals("[Estrofa 1]\nC   Am\nhola mundo", draft.body)
    }

    @Test
    fun `extract - labeled artist`() {
        val draft = extract("Título: Mi canción\nArtista: Un artista\n\n[Coro]\nC\nla la")
        assertEquals("Un artista", draft.artist)
        assertTrue(draft.body.startsWith("[Coro]"))
    }

    @Test
    fun `extract - labels are matched ignoring case and accents`() {
        val draft = extract("TITULO: Mi canción\nautor: Alguien\nTONALIDAD: G\n\n[Verso]\nG\nla")
        assertEquals("Mi canción", draft.title)
        assertEquals("Alguien", draft.artist)
        assertEquals("G", draft.key)
    }

    @Test
    fun `extract - key in Latin notation is converted`() {
        assertEquals("G", extract("Tono: Sol\n\n[Verso]\nG\nla").key)
        assertEquals("Am", extract("Tono: La menor\n\n[Verso]\nAm\nla").key)
        assertEquals("F#m", extract("Key: F#m\n\n[Verso]\nAm\nla").key)
    }

    @Test
    fun `extract - unreadable key is dropped but the line is still consumed`() {
        val draft = extract("Tono: no se\n\n[Verso]\nAm\nla")
        assertEquals("", draft.key)
        assertEquals("[Verso]\nAm\nla", draft.body)
    }

    @Test
    fun `extract - capo with and without colon`() {
        assertEquals("2", extract("Capo: 2\n\n[Verso]\nAm\nla").capo)
        assertEquals("3", extract("Cejilla 3\n\n[Verso]\nAm\nla").capo)
    }

    // ── extractDraft: bare title / artist ──

    @Test
    fun `extract - bare title and artist lines before the first section`() {
        val draft = extract("Mi canción\nUn artista\n\nTono:  F\n\n[Intro] F  Bb  Dm  C\n        F  Bb  Dm  C")
        assertEquals("Mi canción", draft.title)
        assertEquals("Un artista", draft.artist)
        assertEquals("F", draft.key)
        assertEquals("[Intro] F  Bb  Dm  C\n        F  Bb  Dm  C", draft.body)
    }

    @Test
    fun `extract - single bare line in Title dash Artist form is split`() {
        val draft = extract("Mi canción - Un artista\n\n[Verso]\nAm\nla")
        assertEquals("Mi canción", draft.title)
        assertEquals("Un artista", draft.artist)
    }

    @Test
    fun `extract - a labeled title leaves a following bare line as the artist`() {
        val draft = extract("Título: Mi canción\nUn artista\n\n[Verso]\nAm\nla")
        assertEquals("Mi canción", draft.title)
        assertEquals("Un artista", draft.artist)
    }

    @Test
    fun `extract - only two bare lines are consumed, a third stays in the body`() {
        val draft = extract("Uno\nDos\nTres\n[Verso]\nAm\nla")
        assertEquals("Uno", draft.title)
        assertEquals("Dos", draft.artist)
        assertEquals("Tres\n[Verso]\nAm\nla", draft.body)
    }

    @Test
    fun `extract - a long first line is lyrics, not a title`() {
        val longLine = "x".repeat(70)
        val draft = extract("$longLine\n[Verso]\nAm\nla", fileName = "Mi canción.txt")
        assertEquals("Mi canción", draft.title)
        assertTrue(draft.body.startsWith(longLine))
    }

    // ── extractDraft: no metadata ──

    @Test
    fun `extract - lyrics only file keeps every line and uses the file name as title`() {
        val text = "primera linea\nsegunda linea\ntercera linea"
        val draft = extract(text, fileName = "Mi canción.txt")
        assertEquals("Mi canción", draft.title)
        assertEquals("", draft.artist)
        assertEquals(text, draft.body)
    }

    @Test
    fun `extract - file starting with a chord line has no preamble`() {
        val text = "Am   G\nhola mundo\nF    C\nadios mundo"
        val draft = extract(text, fileName = "Mi_canción.txt")
        assertEquals(text, draft.body)
        assertEquals("Mi_canción", draft.title)
    }

    @Test
    fun `extract - file name without extension still works`() {
        assertEquals("Mi canción", extract("Am\nla", fileName = "Mi canción").title)
    }

    @Test
    fun `extract - CRLF line endings are normalized`() {
        val draft = extract("Título: Mi canción\r\n\r\n[Verso]\r\nAm\r\nla")
        assertEquals("Mi canción", draft.title)
        assertEquals("[Verso]\nAm\nla", draft.body)
    }
}
