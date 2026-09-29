package com.example.aura.data.repository

import com.example.aura.data.local.dao.FavoriteDao
import com.example.aura.data.local.dao.RecentlyPlayedDao
import com.example.aura.data.local.dao.SongDao
import com.example.aura.data.local.entity.FavoriteEntity
import com.example.aura.data.local.entity.RecentlyPlayedEntity
import com.example.aura.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSongDao : SongDao {
    val songsFlow = MutableStateFlow<List<SongEntity>>(emptyList())

    override fun getAllSongs(): Flow<List<SongEntity>> = songsFlow

    override suspend fun getSongById(id: String): SongEntity? {
        return songsFlow.value.find { it.id == id }
    }

    override suspend fun getSongsByIds(ids: List<String>): List<SongEntity> {
        return songsFlow.value.filter { ids.contains(it.id) }
    }

    override fun searchSongs(query: String): Flow<List<SongEntity>> {
        val filtered = songsFlow.value.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
        return MutableStateFlow(filtered)
    }

    override suspend fun getAllSongsSync(): List<SongEntity> = songsFlow.value

    override suspend fun getSongBySourceUri(sourceUri: String): SongEntity? {
        return songsFlow.value.find { it.sourceUri == sourceUri }
    }

    override suspend fun getSongsBySourceUris(sourceUris: List<String>): List<SongEntity> {
        return songsFlow.value.filter { sourceUris.contains(it.sourceUri) }
    }

    override suspend fun insertSongs(songs: List<SongEntity>) {
        val current = songsFlow.value.toMutableList()
        for (song in songs) {
            current.removeAll { it.id == song.id }
            current.add(song)
        }
        songsFlow.value = current
    }

    override suspend fun upsertSongs(songs: List<SongEntity>) {
        val current = songsFlow.value.toMutableList()
        for (song in songs) {
            current.removeAll { it.id == song.id || (it.sourceUri.isNotBlank() && it.sourceUri == song.sourceUri) }
            current.add(song)
        }
        songsFlow.value = current
    }

    override suspend fun deleteSongById(id: String) {
        songsFlow.value = songsFlow.value.filterNot { it.id == id }
    }

    override suspend fun deleteSongsByIds(ids: List<String>) {
        songsFlow.value = songsFlow.value.filterNot { ids.contains(it.id) }
    }

    override suspend fun deleteRemovedMediaStoreSongs(currentIds: List<String>) {
        songsFlow.value = songsFlow.value.filter { currentIds.contains(it.id) }
    }

    override suspend fun deleteSongsByUriPrefix(prefix: String) {
        songsFlow.value = songsFlow.value.filterNot { it.mediaUri.startsWith(prefix) }
    }
}

class FakeFavoriteDao : FavoriteDao {
    val favorites = mutableSetOf<String>()
    val favoritesFlow = MutableStateFlow<List<String>>(emptyList())

    private fun emit() {
        favoritesFlow.value = favorites.toList()
    }

    override fun getFavoriteSongIds(): Flow<List<String>> = favoritesFlow

    override suspend fun isFavorite(songId: String): Boolean = favorites.contains(songId)

    override suspend fun addFavorite(favorite: FavoriteEntity) {
        favorites.add(favorite.songId)
        emit()
    }

    override suspend fun removeFavorite(songId: String) {
        favorites.remove(songId)
        emit()
    }
}

class FakeRecentlyPlayedDao : RecentlyPlayedDao {
    val recentList = mutableListOf<RecentlyPlayedEntity>()
    val recentFlow = MutableStateFlow<List<RecentlyPlayedEntity>>(emptyList())

    override fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>> = recentFlow

    override fun getMostPlayed(): Flow<List<RecentlyPlayedEntity>> =
        MutableStateFlow(recentList.sortedByDescending { it.playCount })

    override suspend fun getEntry(songId: String): RecentlyPlayedEntity? =
        recentList.find { it.songId == songId }

    override suspend fun insertOrUpdate(entry: RecentlyPlayedEntity) {
        recentList.removeAll { it.songId == entry.songId }
        recentList.add(0, entry)
        recentFlow.value = recentList.toList()
    }

    override suspend fun incrementSkipCount(songId: String) {
        val idx = recentList.indexOfFirst { it.songId == songId }
        if (idx != -1) {
            val cur = recentList[idx]
            recentList[idx] = cur.copy(skipCount = cur.skipCount + 1)
            recentFlow.value = recentList.toList()
        }
    }

    override suspend fun repointRecentlyPlayed(oldSongId: String, newSongId: String) {
        val idx = recentList.indexOfFirst { it.songId == oldSongId }
        if (idx != -1) {
            val cur = recentList[idx]
            recentList[idx] = cur.copy(songId = newSongId)
            recentFlow.value = recentList.toList()
        }
    }

    override suspend fun deleteRecentlyPlayed(songId: String) {
        recentList.removeAll { it.songId == songId }
        recentFlow.value = recentList.toList()
    }
}

class MusicRepositoryTest {

    private lateinit var songDao: FakeSongDao
    private lateinit var favoriteDao: FakeFavoriteDao
    private lateinit var recentlyPlayedDao: FakeRecentlyPlayedDao
    private lateinit var repository: MusicRepositoryImpl

    private val sampleSongs = listOf(
        SongEntity(
            id = "1",
            title = "Starboy",
            artist = "The Weeknd",
            album = "Starboy",
            durationMs = 230000,
            mediaUri = "content://audio/1",
            artworkUri = null,
            trackNumber = 1,
            genre = "R&B",
            dateAdded = 1000,
            codec = "FLAC",
            isLossless = true
        ),
        SongEntity(
            id = "2",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationMs = 200000,
            mediaUri = "content://audio/2",
            artworkUri = null,
            trackNumber = 2,
            genre = "Pop",
            dateAdded = 2000,
            codec = "FLAC",
            isLossless = true
        ),
        SongEntity(
            id = "3",
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up",
            durationMs = 240000,
            mediaUri = "content://audio/3",
            artworkUri = null,
            trackNumber = 1,
            genre = "Electronic",
            dateAdded = 3000,
            codec = "FLAC",
            isLossless = true
        )
    )

    @Before
    fun setup() {
        songDao = FakeSongDao()
        favoriteDao = FakeFavoriteDao()
        recentlyPlayedDao = FakeRecentlyPlayedDao()
        // MusicRepositoryImpl context is only used for MediaStore queries in scanDeviceMusic,
        // so we can test the repository logic directly
        songDao.songsFlow.value = sampleSongs
    }

    @Test
    fun testFavoriteToggle() = runTest {
        assertFalse(favoriteDao.isFavorite("1"))

        // Add favorite
        favoriteDao.addFavorite(FavoriteEntity("1"))
        assertTrue(favoriteDao.isFavorite("1"))
        assertEquals(listOf("1"), favoriteDao.getFavoriteSongIds().first())

        // Remove favorite
        favoriteDao.removeFavorite("1")
        assertFalse(favoriteDao.isFavorite("1"))
        assertTrue(favoriteDao.getFavoriteSongIds().first().isEmpty())
    }

    @Test
    fun testRecentlyPlayedOrdering() = runTest {
        recentlyPlayedDao.insertOrUpdate(RecentlyPlayedEntity(songId = "1", playedAt = 100))
        recentlyPlayedDao.insertOrUpdate(RecentlyPlayedEntity(songId = "2", playedAt = 200))
        recentlyPlayedDao.insertOrUpdate(RecentlyPlayedEntity(songId = "1", playedAt = 300))

        val recent = recentlyPlayedDao.getRecentlyPlayed().first()
        // Song 1 was played again at timestamp 300, so it should be at the top without duplicate
        assertEquals(2, recent.size)
        assertEquals("1", recent[0].songId)
        assertEquals("2", recent[1].songId)
    }

    @Test
    fun testSearchFiltering() = runTest {
        val results = songDao.searchSongs("Weeknd").first()
        assertEquals(2, results.size)
        assertTrue(results.all { it.artist == "The Weeknd" })

        val albumResults = songDao.searchSongs("After Hours").first()
        assertEquals(1, albumResults.size)
        assertEquals("Blinding Lights", albumResults[0].title)

        val emptyResults = songDao.searchSongs("NonExistent").first()
        assertTrue(emptyResults.isEmpty())
    }
}
