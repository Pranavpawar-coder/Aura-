package com.example.aura.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SongSortTest {

    private val songs = listOf(
        Song(
            id = "1",
            title = "Zebra",
            artist = "Alice",
            album = "Beta",
            durationMs = 300000,
            mediaUri = "content://1",
            dateAdded = 1000
        ),
        Song(
            id = "2",
            title = "Apple",
            artist = "Bob",
            album = "Alpha",
            durationMs = 150000,
            mediaUri = "content://2",
            dateAdded = 3000
        ),
        Song(
            id = "3",
            title = "Mango",
            artist = "Charlie",
            album = "Gamma",
            durationMs = 240000,
            mediaUri = "content://3",
            dateAdded = 2000
        )
    )

    @Test
    fun testTitleSortAZ() {
        val sorted = songs.sortedBy { it.title.lowercase() }
        assertEquals(listOf("Apple", "Mango", "Zebra"), sorted.map { it.title })
    }

    @Test
    fun testTitleSortZA() {
        val sorted = songs.sortedByDescending { it.title.lowercase() }
        assertEquals(listOf("Zebra", "Mango", "Apple"), sorted.map { it.title })
    }

    @Test
    fun testArtistSortAZ() {
        val sorted = songs.sortedWith(compareBy<Song> { it.artist.lowercase() }.thenBy { it.title.lowercase() })
        assertEquals(listOf("Alice", "Bob", "Charlie"), sorted.map { it.artist })
    }

    @Test
    fun testArtistSortZA() {
        val sorted = songs.sortedWith(compareByDescending<Song> { it.artist.lowercase() }.thenBy { it.title.lowercase() })
        assertEquals(listOf("Charlie", "Bob", "Alice"), sorted.map { it.artist })
    }

    @Test
    fun testRecentlyAdded() {
        val sorted = songs.sortedByDescending { it.dateAdded }
        assertEquals(listOf("2", "3", "1"), sorted.map { it.id })
    }

    @Test
    fun testDuration() {
        val sorted = songs.sortedByDescending { it.durationMs }
        assertEquals(listOf(300000L, 240000L, 150000L), sorted.map { it.durationMs })
    }
}
