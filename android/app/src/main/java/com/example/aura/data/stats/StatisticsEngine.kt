package com.example.aura.data.stats

import com.example.aura.data.local.dao.ListeningHistoryDao
import com.example.aura.data.local.dao.RecentlyPlayedDao
import com.example.aura.data.local.dao.SongDao
import com.example.aura.domain.model.ListeningStatistics
import com.example.aura.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class StatisticsEngine(
    private val songDao: SongDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val listeningHistoryDao: ListeningHistoryDao
) {

    suspend fun computeStatistics(allSongs: List<Song>): ListeningStatistics = withContext(Dispatchers.IO) {
        val totalMs = listeningHistoryDao.getTotalListeningTimeMs() ?: 0L
        val totalTracks = listeningHistoryDao.getTotalTracksPlayedCount()

        val recentlyPlayed = recentlyPlayedDao.getMostPlayed().first()
        val songMap = allSongs.associateBy { it.id }

        // 1. Most Played Track
        val topTrackEntry = recentlyPlayed.maxByOrNull { it.playCount }
        val topTrack = topTrackEntry?.let { songMap[it.songId] }

        // 2. Artist play counts
        val artistPlayCounts = mutableMapOf<String, Int>()
        val albumPlayCounts = mutableMapOf<String, Int>()
        val genrePlayCounts = mutableMapOf<String, Int>()

        for (entry in recentlyPlayed) {
            val s = songMap[entry.songId] ?: continue
            val count = entry.playCount
            if (s.artist.isNotBlank() && !s.artist.equals("Unknown", ignoreCase = true)) {
                artistPlayCounts[s.artist] = (artistPlayCounts[s.artist] ?: 0) + count
            }
            if (s.album.isNotBlank() && !s.album.equals("Unknown", ignoreCase = true)) {
                albumPlayCounts[s.album] = (albumPlayCounts[s.album] ?: 0) + count
            }
            if (!s.genre.isNullOrBlank() && !s.genre.equals("Unknown", ignoreCase = true)) {
                genrePlayCounts[s.genre] = (genrePlayCounts[s.genre] ?: 0) + count
            }
        }

        val topArtist = artistPlayCounts.maxByOrNull { it.value }?.toPair()
        val topAlbum = albumPlayCounts.maxByOrNull { it.value }?.toPair()
        val topGenre = genrePlayCounts.maxByOrNull { it.value }?.toPair()

        // 3. Local Smart Recommendations
        // Recommend unplayed or low-play tracks from user's favorite artists and genres
        val playedSongIds = recentlyPlayed.map { it.songId }.toSet()
        val favoriteArtists = artistPlayCounts.entries.sortedByDescending { it.value }.take(3).map { it.key }.toSet()

        val recommendations = mutableListOf<Song>()
        if (favoriteArtists.isNotEmpty()) {
            val candidateSongs = allSongs.filter { s ->
                favoriteArtists.contains(s.artist) && !playedSongIds.contains(s.id)
            }
            recommendations.addAll(candidateSongs.take(15))
        }

        // If recommendations still small, add top genre tracks
        if (recommendations.size < 5 && topGenre != null) {
            val genreCandidates = allSongs.filter { s ->
                s.genre.equals(topGenre.first, ignoreCase = true) && !playedSongIds.contains(s.id)
            }
            recommendations.addAll(genreCandidates.take(10 - recommendations.size))
        }

        ListeningStatistics(
            totalListeningTimeMs = totalMs,
            totalTracksPlayed = totalTracks,
            mostPlayedTrack = topTrack,
            mostPlayedArtist = topArtist,
            mostPlayedAlbum = topAlbum,
            favoriteGenre = topGenre,
            localRecommendations = recommendations.distinctBy { it.id }
        )
    }

    suspend fun recordListeningSession(songId: String, durationMs: Long) = withContext(Dispatchers.IO) {
        if (durationMs > 2000L) { // Only log meaningful sessions > 2s
            listeningHistoryDao.recordHistory(
                com.example.aura.data.local.entity.ListeningHistoryEntity(
                    songId = songId,
                    playedAt = System.currentTimeMillis(),
                    durationListenedMs = durationMs
                )
            )
        }
    }
}
