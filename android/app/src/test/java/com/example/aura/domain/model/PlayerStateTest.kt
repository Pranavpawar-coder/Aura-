package com.example.aura.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStateTest {

    @Test
    fun testDefaultPlayerState() {
        val state = PlayerState()
        assertFalse(state.isFullPlayerOpen)
        assertFalse(state.isMiniPlayerVisible)
        assertFalse(state.isQueueOpen)
        assertEquals(null, state.currentTrack)
        assertTrue(state.queue.isEmpty())
        assertEquals(PlaybackStatus.IDLE, state.playbackState)
        assertEquals(0L, state.currentTime)
        assertEquals(0L, state.duration)
        assertFalse(state.isPlaying)
        assertEquals(0f, state.progressPercent, 0.001f)
    }

    @Test
    fun testPlaybackStateAndProgress() {
        val testSong = Song(
            id = "song_1",
            title = "Midnight Horizon",
            artist = "Aura Resonance",
            album = "Ether",
            durationMs = 240000L,
            mediaUri = "content://audio/1"
        )
        val state = PlayerState(
            isFullPlayerOpen = true,
            isMiniPlayerVisible = false,
            isQueueOpen = false,
            currentTrack = testSong,
            queue = listOf(testSong),
            playbackState = PlaybackStatus.PLAYING,
            currentTime = 120000L,
            duration = 240000L
        )

        assertTrue(state.isPlaying)
        assertEquals(0.5f, state.progressPercent, 0.001f)
        assertEquals("2:00", state.formattedCurrentTime)
        assertEquals("-2:00", state.formattedRemainingTime)
    }

    @Test
    fun testMinimizeTransitionState() {
        val testSong = Song(
            id = "song_1",
            title = "Midnight Horizon",
            artist = "Aura Resonance",
            album = "Ether",
            durationMs = 240000L,
            mediaUri = "content://audio/1"
        )
        // Transition: Full Player -> Minimized to Mini Player
        val fullPlayerState = PlayerState(
            isFullPlayerOpen = true,
            isMiniPlayerVisible = false,
            isQueueOpen = false,
            currentTrack = testSong,
            queue = listOf(testSong),
            playbackState = PlaybackStatus.PLAYING,
            currentTime = 60000L,
            duration = 240000L
        )
        assertTrue(fullPlayerState.isFullPlayerOpen)
        assertFalse(fullPlayerState.isMiniPlayerVisible)

        val minimizedState = fullPlayerState.copy(
            isFullPlayerOpen = false,
            isMiniPlayerVisible = true,
            isQueueOpen = false
        )
        assertFalse(minimizedState.isFullPlayerOpen)
        assertTrue(minimizedState.isMiniPlayerVisible)
        // Audio state and position must remain completely unchanged
        assertEquals(fullPlayerState.currentTrack, minimizedState.currentTrack)
        assertEquals(fullPlayerState.currentTime, minimizedState.currentTime)
        assertEquals(fullPlayerState.playbackState, minimizedState.playbackState)
    }

    @Test
    fun testQueueOpenTransitionState() {
        val testSong = Song(
            id = "song_1",
            title = "Midnight Horizon",
            artist = "Aura Resonance",
            album = "Ether",
            durationMs = 240000L,
            mediaUri = "content://audio/1"
        )
        // Full Player -> Queue Opened
        val fullPlayerState = PlayerState(
            isFullPlayerOpen = true,
            isMiniPlayerVisible = false,
            isQueueOpen = false,
            currentTrack = testSong,
            queue = listOf(testSong),
            playbackState = PlaybackStatus.PLAYING
        )

        val queueOpenState = fullPlayerState.copy(isQueueOpen = true)
        assertTrue(queueOpenState.isFullPlayerOpen)
        assertTrue(queueOpenState.isQueueOpen)
        assertEquals(fullPlayerState.playbackState, queueOpenState.playbackState)
    }
}
