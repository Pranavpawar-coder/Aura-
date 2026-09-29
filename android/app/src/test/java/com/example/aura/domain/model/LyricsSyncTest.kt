package com.example.aura.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LyricsSyncTest {

    private val sampleLines = listOf(
        LyricLine(1000L, "Line 1: Stargazing in the dark"),
        LyricLine(5500L, "Line 2: Lost in the melody"),
        LyricLine(10000L, "Line 3: Echoes in the night"),
        LyricLine(15800L, "Line 4: AURA audio fidelity")
    )

    @Test
    fun testActiveLineLookup() {
        val lyrics = LyricsState.Success(
            lines = sampleLines,
            isSynchronized = true,
            offsetMs = 0L
        )

        // Before first line
        assertEquals(-1, lyrics.getActiveLineIndex(500L))

        // Exactly on line 1
        assertEquals(0, lyrics.getActiveLineIndex(1000L))

        // Between line 1 and line 2
        assertEquals(0, lyrics.getActiveLineIndex(3000L))

        // Exactly on line 2
        assertEquals(1, lyrics.getActiveLineIndex(5500L))

        // Line 3
        assertEquals(2, lyrics.getActiveLineIndex(12000L))

        // Line 4 and beyond
        assertEquals(3, lyrics.getActiveLineIndex(16000L))
        assertEquals(3, lyrics.getActiveLineIndex(99999L))
    }

    @Test
    fun testLyricsOffsetAdjustment() {
        // With +500ms offset, song position 5000ms effectively becomes 5500ms
        // which triggers line 2 (5500ms) 500ms earlier!
        val lyricsWithPositiveOffset = LyricsState.Success(
            lines = sampleLines,
            isSynchronized = true,
            offsetMs = 500L
        )

        // Without offset, 5000ms would be Line 1 (index 0).
        // With +500ms offset, 5000ms + 500ms = 5500ms -> Line 2 (index 1)!
        assertEquals(1, lyricsWithPositiveOffset.getActiveLineIndex(5000L))

        // With -500ms offset, song position 5500ms effectively becomes 5000ms
        // which delays transition to Line 2
        val lyricsWithNegativeOffset = LyricsState.Success(
            lines = sampleLines,
            isSynchronized = true,
            offsetMs = -500L
        )
        assertEquals(0, lyricsWithNegativeOffset.getActiveLineIndex(5500L))
        assertEquals(1, lyricsWithNegativeOffset.getActiveLineIndex(6000L))
    }

    @Test
    fun testUnsyncedLyricsReturnsNoActiveLine() {
        val unsynced = LyricsState.Success(
            lines = listOf(
                LyricLine(0L, "Verse 1 line"),
                LyricLine(0L, "Verse 2 line")
            ),
            isSynchronized = false
        )

        assertEquals(-1, unsynced.getActiveLineIndex(5000L))
    }
}
