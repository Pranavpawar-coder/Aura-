package com.example.aura.data.metadata

import android.content.Context
import android.net.Uri
import com.example.aura.data.local.dao.SongDao
import com.example.aura.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetadataEditor(
    private val context: Context,
    private val songDao: SongDao
) {

    suspend fun updateSongMetadata(
        songId: String,
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int?,
        trackNumber: Int?,
        customArtworkUri: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val existing = songDao.getSongById(songId) ?: return@withContext false
            val updated = existing.copy(
                title = title.ifBlank { existing.title },
                artist = artist.ifBlank { existing.artist },
                album = album.ifBlank { existing.album },
                genre = genre ?: existing.genre,
                year = year ?: existing.year,
                trackNumber = trackNumber ?: existing.trackNumber,
                artworkUri = customArtworkUri ?: existing.artworkUri
            )
            songDao.upsertSongs(listOf(updated))
            true
        } catch (_: Exception) {
            false
        }
    }
}
