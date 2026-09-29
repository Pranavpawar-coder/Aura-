package com.example.aura.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsTest {

    @Test
    fun testParseSynchronizedLrc() {
        val lrcContent = """
            [ti:Test Title]
            [ar:Test Artist]
            [00:04.50]First line of song
            [00:12.30]Second line after chorus
            [01:05.00]Final dramatic outro
        """.trimIndent()

        val state = LyricsParser.parse(lrcContent)
        assertTrue(state is LyricsState.Success)

        val sync = state as LyricsState.Success
        assertTrue(sync.isSynchronized)
        assertEquals(3, sync.lines.size)

        assertEquals(4500L, sync.lines[0].timestampMs)
        assertEquals("First line of song", sync.lines[0].text)

        assertEquals(12300L, sync.lines[1].timestampMs)
        assertEquals("Second line after chorus", sync.lines[1].text)

        assertEquals(65000L, sync.lines[2].timestampMs)
        assertEquals("Final dramatic outro", sync.lines[2].text)
    }

    @Test
    fun testParseMultipleTimestampsOnSingleLine() {
        val lrcContent = """
            [00:10.00][00:30.00]Repeating hook
        """.trimIndent()

        val state = LyricsParser.parse(lrcContent)
        assertTrue(state is LyricsState.Success)

        val sync = state as LyricsState.Success
        assertTrue(sync.isSynchronized)
        assertEquals(2, sync.lines.size)
        assertEquals(10000L, sync.lines[0].timestampMs)
        assertEquals(30000L, sync.lines[1].timestampMs)
        assertEquals("Repeating hook", sync.lines[0].text)
        assertEquals("Repeating hook", sync.lines[1].text)
    }

    @Test
    fun testParsePlainTextLyrics() {
        val plainLyrics = """
            I was walking down the street
            Thinking about the rhythm and the beat
            No timestamps here
        """.trimIndent()

        val state = LyricsParser.parse(plainLyrics)
        assertTrue(state is LyricsState.Success)
        val plain = state as LyricsState.Success
        assertFalse(plain.isSynchronized)
        assertEquals(3, plain.lines.size)
        assertEquals("I was walking down the street", plain.lines[0].text)
    }

    @Test
    fun testEmptyOrBlankLyrics() {
        assertEquals(LyricsState.Unavailable, LyricsParser.parse(null))
        assertEquals(LyricsState.Unavailable, LyricsParser.parse(""))
        assertEquals(LyricsState.Unavailable, LyricsParser.parse("   \n\t  "))
    }
}
