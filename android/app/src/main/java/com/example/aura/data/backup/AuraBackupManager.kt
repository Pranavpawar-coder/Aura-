package com.example.aura.data.backup

import android.content.Context
import android.net.Uri
import com.example.aura.data.local.dao.FavoriteDao
import com.example.aura.data.local.dao.PlaylistDao
import com.example.aura.data.local.dao.SongRatingDao
import com.example.aura.data.local.entity.FavoriteEntity
import com.example.aura.data.local.entity.PlaylistEntity
import com.example.aura.data.local.entity.PlaylistSongCrossRef
import com.example.aura.data.local.entity.SongRatingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class AuraBackupManager(
    private val context: Context,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val songRatingDao: SongRatingDao
) {

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("app", "AURA Music Player")

        // 1. Favorites
        val favorites = favoriteDao.getFavoriteSongIds().first()
        val favArray = JSONArray()
        favorites.forEach { favArray.put(it) }
        root.put("favorites", favArray)

        // 2. Ratings
        val ratings = songRatingDao.getAllRatings().first()
        val ratingsArray = JSONArray()
        ratings.forEach { r ->
            val obj = JSONObject()
            obj.put("songId", r.songId)
            obj.put("rating", r.rating)
            ratingsArray.put(obj)
        }
        root.put("ratings", ratingsArray)

        // 3. Playlists
        val playlists = playlistDao.getPlaylists().first()
        val playlistsArray = JSONArray()
        for (pl in playlists) {
            val plObj = JSONObject()
            plObj.put("name", pl.name)
            plObj.put("createdAt", pl.createdAt)
            val songs = playlistDao.getSongsForPlaylist(pl.id).first()
            val songIds = JSONArray()
            songs.forEach { songIds.put(it.id) }
            plObj.put("songIds", songIds)
            playlistsArray.put(plObj)
        }
        root.put("playlists", playlistsArray)

        root.toString(2)
    }

    suspend fun restoreBackup(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // Restore Favorites
            if (root.has("favorites")) {
                val favArray = root.getJSONArray("favorites")
                for (i in 0 until favArray.length()) {
                    val songId = favArray.getString(i)
                    favoriteDao.addFavorite(FavoriteEntity(songId = songId))
                }
            }

            // Restore Ratings
            if (root.has("ratings")) {
                val ratingsArray = root.getJSONArray("ratings")
                for (i in 0 until ratingsArray.length()) {
                    val obj = ratingsArray.getJSONObject(i)
                    val songId = obj.getString("songId")
                    val rating = obj.getInt("rating")
                    songRatingDao.setRating(SongRatingEntity(songId = songId, rating = rating))
                }
            }

            // Restore Playlists
            if (root.has("playlists")) {
                val playlistsArray = root.getJSONArray("playlists")
                for (i in 0 until playlistsArray.length()) {
                    val plObj = playlistsArray.getJSONObject(i)
                    val name = plObj.getString("name")
                    val newId = playlistDao.createPlaylist(PlaylistEntity(name = name))
                    if (plObj.has("songIds")) {
                        val songsArray = plObj.getJSONArray("songIds")
                        for (j in 0 until songsArray.length()) {
                            val songId = songsArray.getString(j)
                            playlistDao.addSongToPlaylist(
                                PlaylistSongCrossRef(playlistId = newId, songId = songId, orderIndex = j)
                            )
                        }
                    }
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun exportToFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = createBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray())
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun importFromFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            } ?: return@withContext false
            restoreBackup(json)
        } catch (_: Exception) {
            false
        }
    }
}
