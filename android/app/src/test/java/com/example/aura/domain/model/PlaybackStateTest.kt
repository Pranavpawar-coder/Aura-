package com.example.aura.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun testProgressPercent() {
        val stateZero = PlaybackState(durationMs = 0L, currentPositionMs = 0L)
        assertEquals(0f, stateZero.progressPercent, 0.001f)

        val stateHalf = PlaybackState(durationMs = 200000L, currentPositionMs = 100000L)
        assertEquals(0.5f, stateHalf.progressPercent, 0.001f)

        val stateFull = PlaybackState(durationMs = 200000L, currentPositionMs = 200000L)
        assertEquals(1.0f, stateFull.progressPercent, 0.001f)

        val stateOverflow = PlaybackState(durationMs = 200000L, currentPositionMs = 250000L)
        assertEquals(1.0f, stateOverflow.progressPercent, 0.001f)
    }

    @Test
    fun testTimeFormatting() {
        val state = PlaybackState(durationMs = 185000L, currentPositionMs = 65000L)
        assertEquals("1:05", state.formattedCurrentPosition)
        assertEquals("-2:00", state.formattedRemainingTime)
    }

    @Test
    fun testSongFormattedDuration() {
        val song = Song(
            id = "1",
            title = "Test",
            artist = "Artist",
            album = "Album",
            durationMs = 237000L,
            mediaUri = "uri"
        )
        assertEquals("3:57", song.formattedDuration)
    }
}
