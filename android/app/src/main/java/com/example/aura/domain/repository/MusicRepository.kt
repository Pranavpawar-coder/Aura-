package com.example.aura.domain.repository

import android.net.Uri
import com.example.aura.data.local.entity.MusicFolderEntity
import com.example.aura.domain.model.Album
import com.example.aura.domain.model.Artist
import com.example.aura.domain.model.FolderGroup
import com.example.aura.domain.model.Genre
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.SearchResults
import com.example.aura.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getArtists(): Flow<List<Artist>>
    fun getAlbums(): Flow<List<Album>>
    fun getGenres(): Flow<List<Genre>>
    fun getFolders(): Flow<List<FolderGroup>>
    fun getFavorites(): Flow<List<Song>>
    fun getRecentlyPlayed(): Flow<List<Song>>
    fun getMostPlayed(): Flow<List<Song>>
    fun getRecentlyAdded(): Flow<List<Song>>
    fun getFavoriteSongIds(): Flow<List<String>>

    // Detail filters
    fun getSongsByAlbum(albumTitle: String): Flow<List<Song>>
    fun getSongsByArtist(artistName: String): Flow<List<Song>>
    fun getSongsByGenre(genreName: String): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>

    // Global Search
    fun search(query: String): Flow<List<Song>>
    fun searchAll(query: String): Flow<SearchResults>

    suspend fun scanDeviceMusic(): Int
    suspend fun toggleFavorite(songId: String): Boolean
    suspend fun recordRecentlyPlayed(songId: String)
    suspend fun recordSkip(songId: String)

    // Music Folders (Storage Access Framework)
    fun getMusicFolders(): Flow<List<MusicFolderEntity>>
    suspend fun addMusicFolder(uri: Uri, displayName: String)
    suspend fun removeMusicFolder(uriString: String)
    suspend fun rescanFolder(uriString: String): Int

    // Queue Persistence (Room)
    suspend fun saveQueue(songs: List<Song>, currentIndex: Int)
    suspend fun getSavedQueue(): Pair<List<Song>, Int>?

    // Lyrics
    suspend fun getLyrics(song: Song, allowOnline: Boolean = true, forceRefresh: Boolean = false): LyricsState
    suspend fun attachManualLyrics(songId: String, uri: Uri): LyricsState
    suspend fun clearLyricsCache(songId: String? = null)
    suspend fun saveLyricsOffset(songId: String, offsetMs: Long)

    // Ratings
    fun getRatings(): Flow<Map<String, Int>>
    suspend fun setRating(songId: String, rating: Int)
    suspend fun removeRating(songId: String)

    // Smart Playlists
    fun getSmartPlaylists(): Flow<List<com.example.aura.domain.model.SmartPlaylist>>

    // Statistics & Listening Analytics
    suspend fun getStatistics(): com.example.aura.domain.model.ListeningStatistics
    suspend fun recordListeningSession(songId: String, durationMs: Long)

    // Tag & Artwork Editing
    suspend fun updateSongMetadata(
        songId: String,
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int?,
        trackNumber: Int?,
        artworkUri: String? = null
    ): Boolean

    // Backup & Restore
    suspend fun createBackupJson(): String
    suspend fun restoreBackupJson(json: String): Boolean
    suspend fun exportBackup(uri: Uri): Boolean
    suspend fun importBackup(uri: Uri): Boolean

    // Smart Library Exclusions & Filtering
    fun getSmartScanSettings(): Flow<com.example.aura.domain.model.exclusion.SmartScanSettings>
    suspend fun getSmartScanSettingsSync(): com.example.aura.domain.model.exclusion.SmartScanSettings
    suspend fun updateSmartScanSettings(settings: com.example.aura.domain.model.exclusion.SmartScanSettings)
    suspend fun addExcludedFolder(path: String, displayName: String)
    suspend fun removeExcludedFolder(path: String)
    suspend fun toggleExcludedFolder(path: String, isEnabled: Boolean)
    suspend fun toggleExcludedExtension(extension: String, isExcluded: Boolean)
    suspend fun setMinDurationSeconds(seconds: Int)
    suspend fun addFilenamePattern(pattern: String)
    suspend fun removeFilenamePattern(pattern: String)
    suspend fun toggleFilenamePattern(pattern: String, isEnabled: Boolean)
    suspend fun hideSong(song: Song)
    suspend fun hideFolder(folderPath: String, displayName: String? = null)
    suspend fun hideSimilar(pattern: String)
    suspend fun unhideTrack(sourceUriOrId: String)
    suspend fun restoreAllHiddenTracks()
    suspend fun applyCleanMusicLibraryPreset()
}

