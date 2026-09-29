package com.example.aura.data.local

import com.example.aura.data.local.entity.SongEntity
import com.example.aura.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityMappingTest {

    @Test
    fun testSongEntityToDomainAndBack() {
        val original = Song(
            id = "test_123",
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 243000L,
            mediaUri = "content://media/external/audio/media/123",
            artworkUri = "content://media/external/audio/albumart/45",
            trackNumber = 1,
            genre = "Electronic",
            dateAdded = 1600000000L,
            codec = "FLAC",
            sampleRate = 96000,
            bitDepth = 24,
            isLossless = true,
            isFavorite = true
        )

        val entity = SongEntity.fromDomain(original)
        assertEquals(original.id, entity.id)
        assertEquals(original.title, entity.title)
        assertEquals(original.artist, entity.artist)
        assertEquals(original.album, entity.album)
        assertEquals(original.durationMs, entity.durationMs)
        assertEquals(original.mediaUri, entity.mediaUri)
        assertEquals(original.artworkUri, entity.artworkUri)

        val convertedBack = entity.toDomain(isFavorite = true)
        assertEquals(original, convertedBack)
        assertTrue(convertedBack.isFavorite)
    }
}
