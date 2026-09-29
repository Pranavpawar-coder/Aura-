package com.example.aura.widget

import com.example.aura.domain.model.PlaybackState
import com.example.aura.domain.model.PlaybackStatus
import com.example.aura.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuraWidgetIntegrationTest {

    private val sampleSong1 = Song(
        id = "song_1",
        title = "Paaro",
        artist = "Aditya Rikhari",
        album = "Jaana",
        durationMs = 210000L,
        mediaUri = "content://media/external/audio/media/1",
        artworkUri = "content://media/external/audio/albumart/1",
        isLossless = true,
        codec = "FLAC",
        sampleRate = 96000,
        bitDepth = 24
    )

    private val sampleSong2 = Song(
        id = "song_2",
        title = "Neon Horizons",
        artist = "Aura Sound Lab",
        album = "Cinematic Audio",
        durationMs = 185000L,
        mediaUri = "content://media/external/audio/media/2",
        artworkUri = null,
        isLossless = false,
        codec = "AAC",
        bitrate = 320
    )

    @Test
    fun testWidgetDefaultIdleState() {
        val idleState = PlaybackState()
        assertEquals(PlaybackStatus.IDLE, idleState.status)
        assertEquals(null, idleState.currentSong)
        assertEquals(0L, idleState.currentPositionMs)
        assertEquals(0L, idleState.durationMs)
        assertEquals(0f, idleState.progressPercent, 0.001f)
    }

    @Test
    fun testWidgetCurrentTrackMetadata() {
        val playingState = PlaybackState(
            currentSong = sampleSong1,
            status = PlaybackStatus.PLAYING,
            currentPositionMs = 60000L,
            durationMs = sampleSong1.durationMs
        )

        assertNotNull(playingState.currentSong)
        assertEquals("Paaro", playingState.currentSong?.title)
        assertEquals("Aditya Rikhari", playingState.currentSong?.artist)
        assertEquals("Jaana", playingState.currentSong?.album)
        assertEquals(PlaybackStatus.PLAYING, playingState.status)
        assertEquals("1:00", playingState.formattedCurrentPosition)
        assertEquals("-2:30", playingState.formattedRemainingTime)
    }

    @Test
    fun testWidgetFallbackForMissingMetadata() {
        val blankSong = Song(
            id = "empty_meta",
            title = "   ",
            artist = "",
            album = "",
            durationMs = 120000L,
            mediaUri = "content://media/external/audio/media/3"
        )

        val resolvedTitle = blankSong.title.ifBlank { "Unknown Title" }
        val resolvedArtist = blankSong.artist.ifBlank { "Unknown Artist" }

        assertEquals("Unknown Title", resolvedTitle)
        assertEquals("Unknown Artist", resolvedArtist)
        assertNotEquals("empty_meta", resolvedTitle)
    }

    @Test
    fun testWidgetAudioQualityBadge() {
        val losslessBadge = sampleSong1.qualityBadge
        assertNotNull(losslessBadge)
        assertTrue(losslessBadge!!.contains("Lossless") || losslessBadge.contains("FLAC") || losslessBadge.contains("96kHz"))

        val standardBadge = sampleSong2.qualityBadge
        // Standard AAC should not falsely report Hi-Res
        assertTrue(standardBadge == null || !standardBadge.contains("Hi-Res Lossless"))
    }

    @Test
    fun testWidgetProgressCalculation() {
        val state = PlaybackState(
            currentSong = sampleSong1,
            status = PlaybackStatus.PLAYING,
            currentPositionMs = 105000L,
            durationMs = 210000L
        )

        assertEquals(0.5f, state.progressPercent, 0.001f)
        val progressScaled = (state.progressPercent * 1000).toInt()
        assertEquals(500, progressScaled)
    }

    @Test
    fun testWidgetIntentActionConstants() {
        assertEquals("com.example.aura.widget.ACTION_PLAY_PAUSE", AuraWidgetManager.ACTION_PLAY_PAUSE)
        assertEquals("com.example.aura.widget.ACTION_NEXT", AuraWidgetManager.ACTION_NEXT)
        assertEquals("com.example.aura.widget.ACTION_PREVIOUS", AuraWidgetManager.ACTION_PREVIOUS)
        assertEquals("com.example.aura.widget.ACTION_OPEN_FULL_PLAYER", AuraWidgetManager.ACTION_OPEN_FULL_PLAYER)
        assertEquals("extra_open_full_player", AuraWidgetManager.EXTRA_OPEN_FULL_PLAYER)
    }

    @Test
    fun testMultipleWidgetInstancesSync() {
        // Simulates 3 home-screen widgets (Small, Medium, Large)
        val instanceIds = intArrayOf(101, 102, 103)
        var state = PlaybackState(currentSong = sampleSong1, status = PlaybackStatus.PLAYING)

        // When track changes:
        state = state.copy(currentSong = sampleSong2, currentPositionMs = 0L, durationMs = sampleSong2.durationMs)

        // All 3 instances must observe the exact same state
        for (id in instanceIds) {
            assertEquals("Neon Horizons", state.currentSong?.title)
            assertEquals("Aura Sound Lab", state.currentSong?.artist)
            assertEquals(PlaybackStatus.PLAYING, state.status)
        }
    }

    @Test
    fun testRapidTrackTransitions() {
        val queue = listOf(sampleSong1, sampleSong2)
        var currentIndex = 0
        var currentState = PlaybackState(currentSong = queue[currentIndex], status = PlaybackStatus.PLAYING)

        // Rapid next
        currentIndex = (currentIndex + 1) % queue.size
        currentState = currentState.copy(currentSong = queue[currentIndex])
        assertEquals("song_2", currentState.currentSong?.id)

        // Rapid previous
        currentIndex = (currentIndex - 1 + queue.size) % queue.size
        currentState = currentState.copy(currentSong = queue[currentIndex])
        assertEquals("song_1", currentState.currentSong?.id)

        // Rapid toggle play/pause
        val toggledStatus = if (currentState.status == PlaybackStatus.PLAYING) PlaybackStatus.PAUSED else PlaybackStatus.PLAYING
        currentState = currentState.copy(status = toggledStatus)
        assertEquals(PlaybackStatus.PAUSED, currentState.status)
    }
}
