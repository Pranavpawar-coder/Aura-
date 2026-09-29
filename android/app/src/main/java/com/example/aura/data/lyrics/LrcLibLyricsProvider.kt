package com.example.aura.data.lyrics

import com.example.aura.domain.model.LyricLine
import com.example.aura.domain.model.LyricsParser
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Open, legitimate lyrics provider using the LRCLIB REST API (https://lrclib.net).
 * Community-driven, zero-scraping, rate-limited, free service for music players.
 */
class LrcLibLyricsProvider {

    suspend fun fetchLyrics(song: Song): LyricsState = withContext(Dispatchers.IO) {
        val title = song.title.trim()
        val artist = song.artist.trim()
        if (title.isBlank() || title.equals("Unknown", ignoreCase = true)) {
            return@withContext LyricsState.Unavailable
        }

        try {
            val encodedTitle = URLEncoder.encode(cleanQuery(title), "UTF-8")
            val encodedArtist = URLEncoder.encode(cleanQuery(artist), "UTF-8")
            val durationSec = (song.durationMs / 1000).toInt()

            val urlString = buildString {
                append("https://lrclib.net/api/get?")
                append("track_name=").append(encodedTitle)
                if (artist.isNotBlank() && !artist.equals("Unknown", ignoreCase = true)) {
                    append("&artist_name=").append(encodedArtist)
                }
                if (!song.album.isNullOrBlank() && !song.album.equals("Unknown", ignoreCase = true)) {
                    append("&album_name=").append(URLEncoder.encode(cleanQuery(song.album), "UTF-8"))
                }
                if (durationSec > 0) {
                    append("&duration=").append(durationSec)
                }
            }

            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "AURA-Music-Player/1.0 (Android)")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(response)

                val returnedArtist = json.optString("artistName", "").trim()
                val returnedTrack = json.optString("trackName", "").trim()
                val returnedDuration = json.optInt("duration", 0)

                // Validate confidence
                if (isValidMatch(song, returnedTrack, returnedArtist, returnedDuration)) {
                    val syncedLyrics = json.optString("syncedLyrics", "")
                    val plainLyrics = json.optString("plainLyrics", "")

                    // Prioritize synchronized LRC lyrics
                    if (syncedLyrics.isNotBlank()) {
                        val parsed = LyricsParser.parse(syncedLyrics)
                        if (parsed is LyricsState.Success) {
                            return@withContext parsed
                        }
                    }

                    // Fallback to plain lyrics
                    if (plainLyrics.isNotBlank()) {
                        val lines = plainLyrics.lines().map { LyricLine(0L, it) }
                        return@withContext LyricsState.Success(lines = lines, isSynchronized = false)
                    }
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // Try fallback search query with strict validation
                return@withContext searchFallback(song)
            }
        } catch (_: Exception) {
            // Network failure, timeout, or parsing error -> graceful fallback
        }

        return@withContext LyricsState.Unavailable
    }

    private suspend fun searchFallback(song: Song): LyricsState = withContext(Dispatchers.IO) {
        val title = song.title.trim()
        val artist = song.artist.trim()
        try {
            val query = if (artist.isNotBlank() && !artist.equals("Unknown Artist", ignoreCase = true)) {
                "$title $artist".trim()
            } else {
                title
            }
            val encodedQuery = URLEncoder.encode(cleanQuery(query), "UTF-8")
            val searchUrl = "https://lrclib.net/api/search?q=$encodedQuery"

            val connection = (URL(searchUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "AURA-Music-Player/1.0 (Android)")
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val array = org.json.JSONArray(response)
                
                // Inspect all candidates and select the first verified match
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val returnedTrack = item.optString("trackName", "")
                    val returnedArtist = item.optString("artistName", "")
                    val returnedDuration = item.optInt("duration", 0)

                    if (isValidMatch(song, returnedTrack, returnedArtist, returnedDuration)) {
                        val syncedLyrics = item.optString("syncedLyrics", "")
                        val plainLyrics = item.optString("plainLyrics", "")

                        if (syncedLyrics.isNotBlank()) {
                            val parsed = LyricsParser.parse(syncedLyrics)
                            if (parsed is LyricsState.Success) return@withContext parsed
                        }
                        if (plainLyrics.isNotBlank()) {
                            val lines = plainLyrics.lines().map { LyricLine(0L, it) }
                            return@withContext LyricsState.Success(lines = lines, isSynchronized = false)
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        LyricsState.Unavailable
    }

    private fun isValidMatch(song: Song, returnedTrack: String, returnedArtist: String, returnedDuration: Int): Boolean {
        val songTitleClean = cleanQuery(song.title).lowercase()
        val returnedTrackClean = cleanQuery(returnedTrack).lowercase()

        // Track name must match
        if (songTitleClean.isBlank() || returnedTrackClean.isBlank()) return false
        if (!songTitleClean.equals(returnedTrackClean, ignoreCase = true) &&
            !songTitleClean.contains(returnedTrackClean) &&
            !returnedTrackClean.contains(songTitleClean)
        ) {
            return false
        }

        // Artist validation if known
        val songArtist = song.artist.trim()
        if (songArtist.isNotBlank() && !songArtist.equals("Unknown Artist", ignoreCase = true) && !songArtist.equals("Unknown", ignoreCase = true)) {
            val songArtistClean = cleanQuery(songArtist).lowercase()
            val returnedArtistClean = cleanQuery(returnedArtist).lowercase()
            if (returnedArtistClean.isBlank()) return false
            if (!songArtistClean.equals(returnedArtistClean, ignoreCase = true) &&
                !songArtistClean.contains(returnedArtistClean) &&
                !returnedArtistClean.contains(songArtistClean)
            ) {
                return false
            }
        }

        // Duration validation if duration is available (tolerance: +/- 6 seconds)
        if (song.durationMs > 0 && returnedDuration > 0) {
            val songDurationSec = (song.durationMs / 1000).toInt()
            if (Math.abs(songDurationSec - returnedDuration) > 6) {
                return false
            }
        }

        return true
    }

    private fun cleanQuery(text: String): String {
        return text.replace(Regex("\\[.*?\\]|\\(.*?\\)"), "") // Remove [feat. ...] or (Remix)
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ") // Strip special punctuation
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
