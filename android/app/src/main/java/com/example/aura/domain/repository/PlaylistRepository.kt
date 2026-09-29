package com.example.aura.domain.repository

import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String): Long
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    suspend fun reorderPlaylist(playlistId: Long, songIds: List<String>)
    suspend fun isSongInPlaylist(playlistId: Long, songId: String): Boolean
}
