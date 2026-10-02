package com.example.aura.data.cloud

import com.example.aura.domain.model.cloud.AudioQuality
import com.example.aura.domain.model.cloud.CloudCollection
import com.example.aura.domain.model.cloud.CloudTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Universal Metadata Resolver supporting Spotify URLs, Albums, Playlists,
 * and multi-source public query fallback (iTunes & Deezer APIs).
 */
class SpotifyMetadataResolver {

    private val spotifyTrackPattern = Pattern.compile("spotify\\.com/track/([a-zA-Z0-9]+)|spotify:track:([a-zA-Z0-9]+)")
    private val spotifyAlbumPattern = Pattern.compile("spotify\\.com/album/([a-zA-Z0-9]+)|spotify:album:([a-zA-Z0-9]+)")
    private val spotifyPlaylistPattern = Pattern.compile("spotify\\.com/playlist/([a-zA-Z0-9]+)|spotify:playlist:([a-zA-Z0-9]+)")

    suspend fun resolveInput(input: String): Result<CloudCollection> = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        try {
            when {
                spotifyTrackPattern.matcher(trimmed).find() -> resolveSpotifyTrack(trimmed)
                spotifyAlbumPattern.matcher(trimmed).find() -> resolveSpotifyAlbum(trimmed)
                spotifyPlaylistPattern.matcher(trimmed).find() -> resolveSpotifyPlaylist(trimmed)
                trimmed.startsWith("http://") || trimmed.startsWith("https://") -> resolveGenericUrl(trimmed)
                else -> searchByKeywords(trimmed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveSpotifyTrack(url: String): Result<CloudCollection> {
        val matcher = spotifyTrackPattern.matcher(url)
        val trackId = if (matcher.find()) {
            matcher.group(1) ?: matcher.group(2) ?: ""
        } else ""

        // Try Spotify Embed API first
        try {
            val embedUrl = "https://open.spotify.com/oembed?url=https://open.spotify.com/track/$trackId"
            val json = fetchJson(embedUrl)
            if (json != null) {
                val titleRaw = json.optString("title", "Unknown Track")
                // Usually formatted as "Title by Artist" or just "Title"
                var title = titleRaw
                var artist = "Unknown Artist"
                if (titleRaw.contains(" by ")) {
                    val parts = titleRaw.split(" by ")
                    title = parts[0].trim()
                    artist = parts.getOrNull(1)?.trim() ?: "Unknown Artist"
                } else if (titleRaw.contains(" - ")) {
                    val parts = titleRaw.split(" - ")
                    artist = parts[0].trim()
                    title = parts.getOrNull(1)?.trim() ?: titleRaw
                }

                val thumb = json.optString("thumbnail_url", "").ifBlank { null }
                val track = CloudTrack(
                    id = "spotify_$trackId",
                    title = title,
                    artist = artist,
                    album = "Single",
                    coverUrl = thumb,
                    sourceUrl = "https://open.spotify.com/track/$trackId",
                    sourceType = "spotify_track"
                )
                return Result.success(
                    CloudCollection(
                        id = trackId,
                        title = title,
                        creator = artist,
                        type = "track",
                        coverUrl = thumb,
                        totalTracks = 1,
                        tracks = listOf(track)
                    )
                )
            }
        } catch (_: Exception) {
        }

        // Fallback: search track via query
        return searchByKeywords(url)
    }

    private suspend fun resolveSpotifyAlbum(url: String): Result<CloudCollection> {
        val matcher = spotifyAlbumPattern.matcher(url)
        val albumId = if (matcher.find()) {
            matcher.group(1) ?: matcher.group(2) ?: ""
        } else ""

        try {
            val embedUrl = "https://open.spotify.com/oembed?url=https://open.spotify.com/album/$albumId"
            val json = fetchJson(embedUrl)
            if (json != null) {
                val titleRaw = json.optString("title", "Album")
                val thumb = json.optString("thumbnail_url", "").ifBlank { null }
                val artist = if (titleRaw.contains(" by ")) {
                    titleRaw.substringAfter(" by ").trim()
                } else {
                    "Various Artists"
                }
                val albumName = if (titleRaw.contains(" by ")) {
                    titleRaw.substringBefore(" by ").trim()
                } else {
                    titleRaw
                }

                // Query iTunes/Deezer for full album tracklist
                val tracks = fetchAlbumTracksFromDeezer(albumName, artist)
                val finalTracks = if (tracks.isNotEmpty()) {
                    tracks
                } else {
                    listOf(
                        CloudTrack(
                            id = "spotify_album_${albumId}_1",
                            title = albumName,
                            artist = artist,
                            album = albumName,
                            coverUrl = thumb,
                            sourceUrl = "https://open.spotify.com/album/$albumId",
                            sourceType = "spotify_album"
                        )
                    )
                }

                return Result.success(
                    CloudCollection(
                        id = albumId,
                        title = albumName,
                        creator = artist,
                        type = "album",
                        coverUrl = thumb,
                        totalTracks = finalTracks.size,
                        tracks = finalTracks
                    )
                )
            }
        } catch (_: Exception) {
        }

        return searchByKeywords(url)
    }

    private suspend fun resolveSpotifyPlaylist(url: String): Result<CloudCollection> {
        val matcher = spotifyPlaylistPattern.matcher(url)
        val playlistId = if (matcher.find()) {
            matcher.group(1) ?: matcher.group(2) ?: ""
        } else ""

        try {
            val embedUrl = "https://open.spotify.com/oembed?url=https://open.spotify.com/playlist/$playlistId"
            val json = fetchJson(embedUrl)
            if (json != null) {
                val title = json.optString("title", "Spotify Playlist")
                val thumb = json.optString("thumbnail_url", "").ifBlank { null }
                val creator = if (title.contains(" by ")) title.substringAfter(" by ").trim() else "Curator"

                return Result.success(
                    CloudCollection(
                        id = playlistId,
                        title = title,
                        creator = creator,
                        type = "playlist",
                        coverUrl = thumb,
                        totalTracks = 1,
                        tracks = listOf(
                            CloudTrack(
                                id = "spotify_playlist_${playlistId}_sample",
                                title = title,
                                artist = creator,
                                album = "Playlist",
                                coverUrl = thumb,
                                sourceUrl = "https://open.spotify.com/playlist/$playlistId",
                                sourceType = "spotify_playlist"
                            )
                        )
                    )
                )
            }
        } catch (_: Exception) {
        }

        return searchByKeywords(url)
    }

    private suspend fun resolveGenericUrl(url: String): Result<CloudCollection> {
        return searchByKeywords(url)
    }

    suspend fun searchByKeywords(query: String): Result<CloudCollection> = withContext(Dispatchers.IO) {
        val clean = query.replace("https://", "").replace("http://", "").trim()
        val itunesTracks = searchItunes(clean)
        val saavnTracks = searchSaavn(clean)
        val deezerTracks = if (itunesTracks.size + saavnTracks.size < 5) searchDeezer(clean) else emptyList()

        val combined = (itunesTracks + saavnTracks + deezerTracks).distinctBy { "${it.title.lowercase().trim()}_${it.artist.lowercase().trim()}" }

        if (combined.isNotEmpty()) {
            Result.success(
                CloudCollection(
                    id = "search_${clean.hashCode()}",
                    title = "Search: $clean",
                    creator = "Multi-Source Engine",
                    type = "search",
                    coverUrl = combined.firstOrNull()?.coverUrl,
                    totalTracks = combined.size,
                    tracks = combined
                )
            )
        } else {
            Result.failure(Exception("No tracks found matching '$query'"))
        }
    }

    private fun searchSaavn(query: String): List<CloudTrack> {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://www.jiosaavn.com/api.php?__call=autocomplete.get&_format=json&_marker=0&cc=in&includeMetaTags=1&query=$encoded"
            val json = fetchJson(urlString) ?: return emptyList()
            val songsObj = json.optJSONObject("songs") ?: return emptyList()
            val data = songsObj.optJSONArray("data") ?: return emptyList()

            val list = mutableListOf<CloudTrack>()
            for (i in 0 until data.length()) {
                val item = data.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                val title = item.optString("title", "").ifBlank { null } ?: continue
                val moreInfo = item.optJSONObject("more_info")
                val artist = moreInfo?.optString("primary_artists", item.optString("description", "Unknown Artist")) ?: "Unknown Artist"
                val album = item.optString("album", "")
                val imageRaw = item.optString("image", "")
                val highResImage = if (imageRaw.isNotBlank()) imageRaw.replace("50x50.jpg", "500x500.jpg").replace("150x150.jpg", "500x500.jpg") else null

                list.add(
                    CloudTrack(
                        id = "saavn_$id",
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = 0L,
                        coverUrl = highResImage,
                        sourceUrl = item.optString("url", ""),
                        sourceType = "direct_cdn"
                    )
                )
            }
            return list
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun searchItunes(query: String): List<CloudTrack> {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://itunes.apple.com/search?term=$encoded&entity=song&limit=25"
            val json = fetchJson(urlString) ?: return emptyList()
            val results = json.optJSONArray("results") ?: return emptyList()

            val list = mutableListOf<CloudTrack>()
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val trackId = item.optLong("trackId", 0L).toString()
                val trackName = item.optString("trackName", "").ifBlank { null } ?: continue
                val artistName = item.optString("artistName", "Unknown Artist")
                val collectionName = item.optString("collectionName", "")
                val durationMs = item.optLong("trackTimeMillis", 0L)
                val releaseDate = item.optString("releaseDate", "")
                val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4).toIntOrNull() else null
                val artworkRaw = item.optString("artworkUrl100", "")
                val highResArtwork = if (artworkRaw.isNotBlank()) {
                    artworkRaw.replace("100x100bb", "1000x1000bb")
                } else null

                list.add(
                    CloudTrack(
                        id = "itunes_$trackId",
                        title = trackName,
                        artist = artistName,
                        album = collectionName,
                        durationMs = durationMs,
                        coverUrl = highResArtwork,
                        releaseYear = year,
                        trackNumber = item.optInt("trackNumber", 1),
                        sourceUrl = item.optString("trackViewUrl", ""),
                        sourceType = "itunes"
                    )
                )
            }
            return list
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun searchDeezer(query: String): List<CloudTrack> {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://api.deezer.com/search?q=$encoded&limit=20"
            val json = fetchJson(urlString) ?: return emptyList()
            val data = json.optJSONArray("data") ?: return emptyList()

            val list = mutableListOf<CloudTrack>()
            for (i in 0 until data.length()) {
                val item = data.optJSONObject(i) ?: continue
                val id = item.optLong("id", 0L).toString()
                val title = item.optString("title", "").ifBlank { null } ?: continue
                val artistObj = item.optJSONObject("artist")
                val artistName = artistObj?.optString("name", "Unknown Artist") ?: "Unknown Artist"
                val albumObj = item.optJSONObject("album")
                val albumTitle = albumObj?.optString("title", "") ?: ""
                val coverUrl = albumObj?.optString("cover_xl") ?: albumObj?.optString("cover_big")
                val durationSec = item.optLong("duration", 0L)

                list.add(
                    CloudTrack(
                        id = "deezer_$id",
                        title = title,
                        artist = artistName,
                        album = albumTitle,
                        durationMs = durationSec * 1000,
                        coverUrl = coverUrl,
                        sourceUrl = item.optString("link", ""),
                        sourceType = "deezer"
                    )
                )
            }
            return list
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun fetchAlbumTracksFromDeezer(albumName: String, artist: String): List<CloudTrack> {
        try {
            val query = "$albumName $artist"
            val encoded = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://api.deezer.com/search/album?q=$encoded&limit=3"
            val json = fetchJson(urlString) ?: return emptyList()
            val data = json.optJSONArray("data") ?: return emptyList()
            if (data.length() == 0) return emptyList()

            val firstAlbum = data.getJSONObject(0)
            val tracklistUrl = firstAlbum.optString("tracklist", "")
            if (tracklistUrl.isBlank()) return emptyList()

            val tracklistJson = fetchJson(tracklistUrl) ?: return emptyList()
            val tracksArray = tracklistJson.optJSONArray("data") ?: return emptyList()
            val coverUrl = firstAlbum.optString("cover_xl", firstAlbum.optString("cover_big", ""))

            val list = mutableListOf<CloudTrack>()
            for (i in 0 until tracksArray.length()) {
                val trackObj = tracksArray.getJSONObject(i)
                val trackId = trackObj.optLong("id", 0L).toString()
                val title = trackObj.optString("title", "")
                val durationSec = trackObj.optLong("duration", 0L)
                val trackArtist = trackObj.optJSONObject("artist")?.optString("name", artist) ?: artist

                list.add(
                    CloudTrack(
                        id = "deezer_track_$trackId",
                        title = title,
                        artist = trackArtist,
                        album = albumName,
                        durationMs = durationSec * 1000,
                        coverUrl = coverUrl.ifBlank { null },
                        trackNumber = i + 1,
                        sourceUrl = trackObj.optString("link", ""),
                        sourceType = "deezer_album_track"
                    )
                )
            }
            return list
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun fetchJson(urlString: String): JSONObject? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; AURA-Audio/1.2)")
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                JSONObject(response)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
