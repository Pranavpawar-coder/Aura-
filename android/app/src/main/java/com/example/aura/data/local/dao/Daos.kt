package com.example.aura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.aura.data.local.entity.FavoriteEntity
import com.example.aura.data.local.entity.MusicFolderEntity
import com.example.aura.data.local.entity.PlaylistEntity
import com.example.aura.data.local.entity.PlaylistSongCrossRef
import com.example.aura.data.local.entity.QueueEntity
import com.example.aura.data.local.entity.RecentlyPlayedEntity
import com.example.aura.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongsSync(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<String>): List<SongEntity>

    @Query("SELECT * FROM songs WHERE sourceUri = :sourceUri LIMIT 1")
    suspend fun getSongBySourceUri(sourceUri: String): SongEntity?

    @Query("SELECT * FROM songs WHERE sourceUri IN (:sourceUris)")
    suspend fun getSongsBySourceUris(sourceUris: List<String>): List<SongEntity>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: String)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<String>)

    /**
     * Removes MediaStore tracks that were deleted from device storage,
     * while preserving custom folder tracks imported via Storage Access Framework.
     */
    @Query("DELETE FROM songs WHERE mediaUri LIKE 'content://media/%' AND id NOT IN (:currentIds)")
    suspend fun deleteRemovedMediaStoreSongs(currentIds: List<String>)

    @Query("DELETE FROM songs WHERE mediaUri LIKE :prefix || '%'")
    suspend fun deleteSongsByUriPrefix(prefix: String)
}

@Dao
interface MusicFolderDao {
    @Query("SELECT * FROM music_folders ORDER BY dateAdded DESC")
    fun getAllFolders(): Flow<List<MusicFolderEntity>>

    @Query("SELECT * FROM music_folders WHERE uriString = :uriString LIMIT 1")
    suspend fun getFolderByUri(uriString: String): MusicFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: MusicFolderEntity)

    @Query("UPDATE music_folders SET trackCount = :trackCount WHERE uriString = :uriString")
    suspend fun updateTrackCount(uriString: String, trackCount: Int)

    @Query("DELETE FROM music_folders WHERE uriString = :uriString")
    suspend fun deleteFolder(uriString: String)
}

@Dao
interface FavoriteDao {
    @Query("SELECT songId FROM favorites")
    fun getFavoriteSongIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: String)
}

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 50")
    fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>>

    @Query("SELECT * FROM recently_played ORDER BY playCount DESC LIMIT 50")
    fun getMostPlayed(): Flow<List<RecentlyPlayedEntity>>

    @Query("SELECT * FROM recently_played WHERE songId = :songId LIMIT 1")
    suspend fun getEntry(songId: String): RecentlyPlayedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: RecentlyPlayedEntity)

    @Query("UPDATE recently_played SET skipCount = skipCount + 1 WHERE songId = :songId")
    suspend fun incrementSkipCount(songId: String)

    @Query("UPDATE OR IGNORE recently_played SET songId = :newSongId WHERE songId = :oldSongId")
    suspend fun repointRecentlyPlayed(oldSongId: String, newSongId: String)

    @Query("DELETE FROM recently_played WHERE songId = :songId")
    suspend fun deleteRecentlyPlayed(songId: String)

    @Transaction
    suspend fun recordPlay(songId: String) {
        val existing = getEntry(songId)
        val newPlayCount = (existing?.playCount ?: 0) + 1
        insertOrUpdate(
            RecentlyPlayedEntity(
                songId = songId,
                playedAt = System.currentTimeMillis(),
                playCount = newPlayCount,
                skipCount = existing?.skipCount ?: 0
            )
        )
    }
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue ORDER BY orderIndex ASC")
    fun getQueue(): Flow<List<QueueEntity>>

    @Query("SELECT * FROM queue ORDER BY orderIndex ASC")
    suspend fun getQueueSync(): List<QueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueEntity>)

    @Query("DELETE FROM queue")
    suspend fun clearQueue()

    @Query("UPDATE queue SET songId = :newSongId WHERE songId = :oldSongId")
    suspend fun repointQueueSongId(oldSongId: String, newSongId: String)

    @Query("DELETE FROM queue WHERE songId = :songId")
    suspend fun deleteQueueSongId(songId: String)

    @Transaction
    suspend fun replaceQueue(items: List<QueueEntity>) {
        clearQueue()
        insertQueueItems(items)
    }
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName WHERE id = :id")
    suspend fun renamePlaylist(id: Long, newName: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deletePlaylistSongs(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("SELECT * FROM playlist_songs WHERE songId = :songId")
    suspend fun getRefsForSong(songId: String): List<PlaylistSongCrossRef>

    @Query("DELETE FROM playlist_songs WHERE songId = :songId")
    suspend fun deleteRefsForSong(songId: String)

    @Transaction
    suspend fun reorderPlaylist(playlistId: Long, songIds: List<String>) {
        deletePlaylistSongs(playlistId)
        val refs = songIds.mapIndexed { index, songId ->
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId, orderIndex = index)
        }
        insertPlaylistSongs(refs)
    }

    @Query("UPDATE OR IGNORE playlist_songs SET songId = :newSongId WHERE songId = :oldSongId")
    suspend fun repointSongId(oldSongId: String, newSongId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun isSongInPlaylist(playlistId: Long, songId: String): Boolean

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.orderIndex ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>
}

@Dao
interface SongRatingDao {
    @Query("SELECT * FROM song_ratings")
    fun getAllRatings(): Flow<List<com.example.aura.data.local.entity.SongRatingEntity>>

    @Query("SELECT rating FROM song_ratings WHERE songId = :songId LIMIT 1")
    fun getRatingForSong(songId: String): Flow<Int?>

    @Query("SELECT rating FROM song_ratings WHERE songId = :songId LIMIT 1")
    suspend fun getRatingSync(songId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setRating(rating: com.example.aura.data.local.entity.SongRatingEntity)

    @Query("DELETE FROM song_ratings WHERE songId = :songId")
    suspend fun removeRating(songId: String)
}

@Dao
interface LyricsCacheDao {
    @Query("SELECT * FROM lyrics_cache WHERE songId = :songId LIMIT 1")
    suspend fun getLyrics(songId: String): com.example.aura.data.local.entity.LyricsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLyrics(entity: com.example.aura.data.local.entity.LyricsCacheEntity)

    @Query("DELETE FROM lyrics_cache WHERE songId = :songId")
    suspend fun deleteLyrics(songId: String)

    @Query("DELETE FROM lyrics_cache")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM lyrics_cache")
    suspend fun getCacheCount(): Int
}

@Dao
interface ListeningHistoryDao {
    @Query("SELECT * FROM listening_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<com.example.aura.data.local.entity.ListeningHistoryEntity>>

    @Insert
    suspend fun recordHistory(entry: com.example.aura.data.local.entity.ListeningHistoryEntity)

    @Query("UPDATE listening_history SET songId = :newSongId WHERE songId = :oldSongId")
    suspend fun repointHistory(oldSongId: String, newSongId: String)

    @Query("SELECT SUM(durationListenedMs) FROM listening_history")
    suspend fun getTotalListeningTimeMs(): Long?

    @Query("SELECT COUNT(*) FROM listening_history")
    suspend fun getTotalTracksPlayedCount(): Int

    @Query("DELETE FROM listening_history")
    suspend fun clearHistory()
}

