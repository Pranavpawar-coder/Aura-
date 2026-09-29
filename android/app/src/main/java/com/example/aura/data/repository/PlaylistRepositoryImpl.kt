package com.example.aura.data.repository

import com.example.aura.data.local.dao.FavoriteDao
import com.example.aura.data.local.dao.PlaylistDao
import com.example.aura.data.local.entity.PlaylistEntity
import com.example.aura.data.local.entity.PlaylistSongCrossRef
import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.Song
import com.example.aura.domain.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists().map { list ->
            list.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    artworkUri = entity.artworkUri
                )
            }
        }
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return combine(playlistDao.getSongsForPlaylist(playlistId), favoriteDao.getFavoriteSongIds()) { entities, favIds ->
            val favSet = favIds.toSet()
            entities.map { it.toDomain(isFavorite = favSet.contains(it.id)) }
        }
    }

    override suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.createPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(playlistId, newName)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun reorderPlaylist(playlistId: Long, songIds: List<String>) = withContext(Dispatchers.IO) {
        playlistDao.reorderPlaylist(playlistId, songIds)
    }

    override suspend fun isSongInPlaylist(playlistId: Long, songId: String): Boolean = withContext(Dispatchers.IO) {
        playlistDao.isSongInPlaylist(playlistId, songId)
    }
}

