package com.example.aura

import com.example.aura.domain.model.LyricLine
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.audio.SleepTimerSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase4FeaturesTest {

    @Test
    fun testSongRatingProperty() {
        val unratedSong = Song(
            id = "1",
            title = "Test Song",
            artist = "Artist",
            album = "Album",
            durationMs = 210000,
            mediaUri = "content://media/external/audio/media/1",
            filePath = "/storage/emulated/0/Music/test.mp3"
        )
        assertEquals(0, unratedSong.rating)

        val ratedSong = unratedSong.copy(rating = 5)
        assertEquals(5, ratedSong.rating)
    }

    @Test
    fun testSmartPlaylistFiltering() {
        val song1 = Song(id = "1", title = "Rock Star", artist = "Band", album = "A", durationMs = 350000, mediaUri = "content://1", rating = 5)
        val song2 = Song(id = "2", title = "Pop Star", artist = "Singer", album = "B", durationMs = 120000, mediaUri = "content://2", rating = 4, mimeType = "audio/flac")
        val song3 = Song(id = "3", title = "Indie Acoustic", artist = "Folk", album = "C", durationMs = 180000, mediaUri = "content://3", rating = 0, isFavorite = true)

        val allSongs = listOf(song1, song2, song3)

        // 5-Star tracks filter
        val fiveStarTracks = allSongs.filter { it.rating == 5 }
        assertEquals(1, fiveStarTracks.size)
        assertEquals("1", fiveStarTracks.first().id)

        // Long tracks filter (> 5 min)
        val longTracks = allSongs.filter { it.durationMs > 300000L }
        assertEquals(1, longTracks.size)
        assertEquals("Rock Star", longTracks.first().title)

        // High Quality Lossless filter
        val hiResTracks = allSongs.filter { it.mimeType?.contains("flac", ignoreCase = true) == true }
        assertEquals(1, hiResTracks.size)
        assertEquals("Pop Star", hiResTracks.first().title)

        // Favorites filter
        val favorites = allSongs.filter { it.isFavorite }
        assertEquals(1, favorites.size)
        assertEquals("Indie Acoustic", favorites.first().title)
    }

    @Test
    fun testSleepTimerRemainingTimeFormatting() {
        val settings = SleepTimerSettings(
            isRunning = true,
            remainingSeconds = 1662 // 27 minutes and 42 seconds
        )
        assertTrue(settings.isActive)
        assertEquals("27:42", settings.formattedRemainingTime)

        val underMinute = SleepTimerSettings(
            isRunning = true,
            remainingSeconds = 9
        )
        assertTrue(underMinute.isActive)
        assertEquals("0:09", underMinute.formattedRemainingTime)

        val inactive = SleepTimerSettings(
            isRunning = false,
            remainingSeconds = 0
        )
        assertFalse(inactive.isActive)
        assertEquals("Off", inactive.formattedRemainingTime)
    }

    @Test
    fun testLyricsOffsetAdjustment() {
        val lines = listOf(
            LyricLine(1000L, "First line"),
            LyricLine(3000L, "Second line"),
            LyricLine(6000L, "Third line")
        )
        val state = LyricsState.Success(
            lines = lines,
            isSynchronized = true,
            offsetMs = 0L
        )

        // At 3500ms, active line should be index 1 ("Second line")
        assertEquals(1, state.getActiveLineIndex(3500L))

        // If offset is +1000ms, effective playback position becomes 4500ms
        val shiftedState = state.copy(offsetMs = 1000L)
        // With +1000ms offset, at 2500ms real position: 2500 + 1000 = 3500ms -> index 1
        assertEquals(1, shiftedState.getActiveLineIndex(2500L))
    }

    @Test
    fun testBackupJsonStructure() {
        val sampleJson = """
            {
                "version": 4,
                "exportedAt": 1727376000000,
                "favorites": ["song_1", "song_2"],
                "ratings": {
                    "song_1": 5,
                    "song_2": 4
                },
                "playlists": [
                    {
                        "name": "Workout Mix",
                        "songIds": ["song_1"]
                    }
                ],
                "settings": {
                    "theme": "Dark",
                    "gapless": true,
                    "crossfade": 5
                }
            }
        """.trimIndent()

        assertTrue(sampleJson.contains("\"version\": 4"))
        assertTrue(sampleJson.contains("\"ratings\""))
        assertTrue(sampleJson.contains("\"song_1\": 5"))
        assertTrue(sampleJson.contains("\"Workout Mix\""))
        assertTrue(sampleJson.contains("\"theme\": \"Dark\""))
    }
}
