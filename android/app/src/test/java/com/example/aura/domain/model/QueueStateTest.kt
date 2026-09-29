package com.example.aura.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueStateTest {

    private val songA = Song(id = "1", title = "Song A", artist = "Artist 1", album = "Album 1", durationMs = 180000, mediaUri = "uri1")
    private val songB = Song(id = "2", title = "Song B", artist = "Artist 2", album = "Album 2", durationMs = 200000, mediaUri = "uri2")
    private val songC = Song(id = "3", title = "Song C", artist = "Artist 3", album = "Album 3", durationMs = 220000, mediaUri = "uri3")
    private val initialSongs = listOf(songA, songB, songC)

    @Test
    fun testInitialQueueAndNavigation() {
        var queue = QueueState().withNewQueue(initialSongs, startIndex = 0)

        assertEquals(3, queue.songs.size)
        assertEquals(0, queue.currentIndex)
        assertEquals(songA, queue.currentSong)
        assertEquals(songB, queue.nextSong)
        assertNull(queue.previousSong) // repeat mode OFF at start

        // Move to B
        queue = queue.copy(currentIndex = 1)
        assertEquals(songB, queue.currentSong)
        assertEquals(songC, queue.nextSong)
        assertEquals(songA, queue.previousSong)

        // Move to C
        queue = queue.copy(currentIndex = 2)
        assertEquals(songC, queue.currentSong)
        assertNull(queue.nextSong) // repeat mode OFF at end
        assertEquals(songB, queue.previousSong)
    }

    @Test
    fun testRepeatModeAll() {
        val queue = QueueState(repeatMode = RepeatMode.ALL).withNewQueue(initialSongs, startIndex = 2)
        assertEquals(songC, queue.currentSong)
        // At end, next song wraps around to first song (Song A)
        assertEquals(songA, queue.nextSong)

        // At beginning, previous song wraps around to last song (Song C)
        val queueAtStart = queue.copy(currentIndex = 0)
        assertEquals(songC, queueAtStart.previousSong)
    }

    @Test
    fun testRepeatModeOne() {
        val queue = QueueState(repeatMode = RepeatMode.ONE).withNewQueue(initialSongs, startIndex = 1)
        assertEquals(songB, queue.currentSong)
        // Repeat ONE always repeats the current song
        assertEquals(songB, queue.nextSong)
        assertEquals(songB, queue.previousSong)
    }

    @Test
    fun testReorderQueue() {
        // Initial order: A (index 0), B (index 1), C (index 2)
        var queue = QueueState().withNewQueue(initialSongs, startIndex = 0)
        assertEquals(songA, queue.currentSong)

        // Move C (index 2) to top (index 0) -> Order: C, A, B
        queue = queue.reorder(fromIndex = 2, toIndex = 0)
        assertEquals(listOf(songC, songA, songB), queue.songs)
        // Current song was A (originally index 0), now shifted to index 1
        assertEquals(1, queue.currentIndex)
        assertEquals(songA, queue.currentSong)

        // Next song after A is now B
        assertEquals(songB, queue.nextSong)
        // Previous song before A is now C
        assertEquals(songC, queue.previousSong)
    }

    @Test
    fun testReorderCurrentlyPlayingSong() {
        // Currently playing A (index 0)
        var queue = QueueState().withNewQueue(initialSongs, startIndex = 0)
        // Move A from index 0 to index 2 -> Order: B, C, A
        queue = queue.reorder(fromIndex = 0, toIndex = 2)
        assertEquals(listOf(songB, songC, songA), queue.songs)
        assertEquals(2, queue.currentIndex)
        assertEquals(songA, queue.currentSong)
    }

    @Test
    fun testShuffleToggle() {
        var queue = QueueState().withNewQueue(initialSongs, startIndex = 1) // Song B
        assertEquals(songB, queue.currentSong)
        assertFalse(queue.isShuffle)

        // Turn shuffle ON
        queue = queue.toggleShuffle()
        assertTrue(queue.isShuffle)
        assertEquals(3, queue.songs.size)
        // Currently playing song remains at index 0 in shuffled queue
        assertEquals(songB, queue.currentSong)
        assertTrue(queue.songs.containsAll(initialSongs))

        // Turn shuffle OFF
        queue = queue.toggleShuffle()
        assertFalse(queue.isShuffle)
        // Restores original list
        assertEquals(initialSongs, queue.songs)
        assertEquals(songB, queue.currentSong)
        assertEquals(1, queue.currentIndex)
    }

    @Test
    fun testAddToQueueAndPlayNext() {
        var queue = QueueState().withNewQueue(initialSongs, startIndex = 0)
        val songD = Song(id = "4", title = "Song D", artist = "Artist 4", album = "Album 4", durationMs = 210000, mediaUri = "uri4")

        // Add to queue
        queue = queue.addToQueue(songD)
        assertEquals(4, queue.songs.size)
        assertEquals(songD, queue.songs.last())

        // Play next (insert immediately after current song A at index 0)
        val songE = Song(id = "5", title = "Song E", artist = "Artist 5", album = "Album 5", durationMs = 230000, mediaUri = "uri5")
        queue = queue.playNext(songE)
        assertEquals(5, queue.songs.size)
        assertEquals(songE, queue.songs[1]) // inserted at index 1
        assertEquals(songE, queue.nextSong) // next up is Song E!
    }

    @Test
    fun testRemoveFromQueue() {
        var queue = QueueState().withNewQueue(initialSongs, startIndex = 1) // Song B is current
        assertEquals(songB, queue.currentSong)

        // Remove Song A (index 0, before current)
        queue = queue.removeAt(0)
        assertEquals(listOf(songB, songC), queue.songs)
        assertEquals(0, queue.currentIndex)
        assertEquals(songB, queue.currentSong)

        // Remove Song C (index 1, after current)
        queue = queue.removeAt(1)
        assertEquals(listOf(songB), queue.songs)
        assertEquals(0, queue.currentIndex)
        assertEquals(songB, queue.currentSong)
        assertNull(queue.nextSong)
    }

    @Test
    fun testClearQueue() {
        var queue = QueueState().withNewQueue(initialSongs, startIndex = 1)
        queue = queue.clear()
        // Leaves only current song
        assertEquals(1, queue.songs.size)
        assertEquals(songB, queue.currentSong)
        assertEquals(0, queue.currentIndex)
    }
}
