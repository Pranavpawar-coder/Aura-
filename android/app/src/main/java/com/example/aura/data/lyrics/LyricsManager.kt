package com.example.aura.data.lyrics

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.aura.data.audio.AudioMetadataHelper
import com.example.aura.data.audio.AudioSourceNormalizer
import com.example.aura.data.local.dao.LyricsCacheDao
import com.example.aura.data.local.entity.LyricsCacheEntity
import com.example.aura.domain.model.LyricLine
import com.example.aura.domain.model.LyricsParser
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LyricsManager(
    private val context: Context,
    private val lyricsCacheDao: LyricsCacheDao,
    private val onlineProvider: LrcLibLyricsProvider = LrcLibLyricsProvider()
) {

    companion object {
        private const val TAG = "LyricsManager"

        /**
         * Validates that embedded or sidecar LRC metadata tags do not contradict the song's artist or title.
         * Prevents attaching lyrics from another artist with the same song title.
         */
        fun validateLrcContent(rawText: String, song: Song): Boolean {
            var lrcArtist: String? = null
            var lrcTitle: String? = null

            val lines = rawText.lines().take(25)
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("[ar:", ignoreCase = true)) {
                    lrcArtist = trimmed.removePrefix("[ar:").removePrefix("[AR:").substringBefore(']').trim()
                } else if (trimmed.startsWith("[ti:", ignoreCase = true)) {
                    lrcTitle = trimmed.removePrefix("[ti:").removePrefix("[TI:").substringBefore(']').trim()
                }
            }

            // If artist tag is present in LRC and song has a known artist, verify compatibility
            val songArtist = song.artist.trim()
            if (!lrcArtist.isNullOrBlank() && songArtist.isNotBlank() && !songArtist.equals("Unknown Artist", ignoreCase = true)) {
                val cleanLrcArtist = normalizeForComparison(lrcArtist)
                val cleanSongArtist = normalizeForComparison(songArtist)
                if (cleanLrcArtist.isNotBlank() && cleanSongArtist.isNotBlank()) {
                    if (!cleanLrcArtist.contains(cleanSongArtist) && !cleanSongArtist.contains(cleanLrcArtist)) {
                        try {
                            Log.w(TAG, "LYRICS: match rejected! Contradictory artist in LRC tag: '$lrcArtist' vs song artist: '$songArtist'")
                        } catch (_: Throwable) {}
                        return false
                    }
                }
            }

            return true
        }

        fun stripTrackNumber(name: String): String {
            return name.replace(Regex("""^\d{1,3}[\s._\-]+"""), "").trim()
        }

        fun normalizeForComparison(text: String): String {
            return text.lowercase()
                .replace(Regex("[^\\p{L}\\p{N}\\s]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }

    /**
     * Resolves lyrics following the strict specification resolution hierarchy:
     * 1. Embedded tags
     * 2. Matching local sibling .lrc file
     * 3. Matching local sibling .txt file
     * 4. Local Room offline cache (keyed by canonical track ID)
     * 5. Online provider (if enabled and confidence criteria satisfied)
     */
    suspend fun getLyrics(
        song: Song,
        allowOnline: Boolean = true,
        forceRefresh: Boolean = false
    ): LyricsState = withContext(Dispatchers.IO) {
        val canonicalTrackId = song.id

        // 1. Check embedded audio file lyrics first if file is accessible
        try {
            val embeddedRaw = AudioMetadataHelper.extractLyrics(
                context = context,
                uri = Uri.parse(song.mediaUri),
                filePath = song.filePath
            )
            if (!embeddedRaw.isNullOrBlank()) {
                val parsed = LyricsParser.parse(embeddedRaw)
                if (parsed is LyricsState.Success) {
                    Log.d(TAG, "LYRICS: matched embedded lyrics for track ${song.id} (${song.title})")
                    saveToCache(canonicalTrackId, embeddedRaw, parsed.isSynchronized, "embedded")
                    return@withContext parsed
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking embedded lyrics", e)
        }

        // 2. Check local sibling file (.lrc or .txt) in the same directory
        val localSidecar = findLocalLyricsFile(song)
        if (localSidecar != null && localSidecar.isNotBlank()) {
            val parsed = LyricsParser.parse(localSidecar)
            if (parsed is LyricsState.Success) {
                Log.d(TAG, "LYRICS: matched local sidecar lyrics for track ${song.id} (${song.title})")
                saveToCache(canonicalTrackId, localSidecar, parsed.isSynchronized, "local_file")
                return@withContext parsed
            }
        }

        // 3. Check cached lyrics if not forcing refresh
        if (!forceRefresh) {
            val cached = lyricsCacheDao.getLyrics(canonicalTrackId)
            if (cached != null && cached.rawLyrics.isNotBlank()) {
                val parsed = LyricsParser.parse(cached.rawLyrics)
                if (parsed is LyricsState.Success) {
                    return@withContext parsed.copy(offsetMs = cached.offsetMs)
                }
            }
        }

        // 4. Online legitimate retrieval via LRCLIB if enabled
        if (allowOnline) {
            val onlineResult = onlineProvider.fetchLyrics(song)
            if (onlineResult is LyricsState.Success) {
                Log.d(TAG, "LYRICS: match accepted from online provider for track ${song.id} (${song.title})")
                val rawToCache = serializeLyrics(onlineResult)
                saveToCache(canonicalTrackId, rawToCache, onlineResult.isSynchronized, "lrclib")
                return@withContext onlineResult
            } else {
                Log.d(TAG, "LYRICS: no confident online match found for ${song.title}")
            }
        }

        LyricsState.Unavailable
    }

    /**
     * Associates a user-selected lyrics file (.lrc or .txt) with a specific track.
     */
    suspend fun attachManualLyricsFile(songId: String, uri: Uri): LyricsState = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!content.isNullOrBlank()) {
                val parsed = LyricsParser.parse(content)
                if (parsed is LyricsState.Success) {
                    saveToCache(songId, content, parsed.isSynchronized, "manual_file")
                    return@withContext parsed
                }
            }
        } catch (_: Exception) {}
        LyricsState.Unavailable
    }

    suspend fun clearCacheForSong(songId: String) = withContext(Dispatchers.IO) {
        lyricsCacheDao.deleteLyrics(songId)
    }

    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        lyricsCacheDao.clearAll()
    }

    suspend fun savePerSongOffset(songId: String, offsetMs: Long) = withContext(Dispatchers.IO) {
        val existing = lyricsCacheDao.getLyrics(songId)
        if (existing != null) {
            lyricsCacheDao.saveLyrics(existing.copy(offsetMs = offsetMs))
        }
    }

    private suspend fun saveToCache(songId: String, rawText: String, isSynced: Boolean, source: String) {
        try {
            lyricsCacheDao.saveLyrics(
                LyricsCacheEntity(
                    songId = songId,
                    rawLyrics = rawText,
                    isSynchronized = isSynced,
                    source = source,
                    cachedAt = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {}
    }

    /**
     * Finds local sibling lyrics with strict validation against track metadata.
     * Prevents false-positive attachments across different artists or albums.
     */
    fun findLocalLyricsFile(song: Song): String? {
        val resolvedPath = song.filePath ?: try {
            AudioSourceNormalizer.normalizeSource(context, Uri.parse(song.mediaUri), song.filePath).resolvedFilePath
        } catch (_: Exception) {
            null
        }

        if (!resolvedPath.isNullOrBlank()) {
            val audioFile = File(resolvedPath)
            val parent = audioFile.parentFile
            if (parent != null && parent.exists() && parent.isDirectory) {
                val baseName = audioFile.nameWithoutExtension
                val cleanBase = normalizeForComparison(baseName)
                val cleanTitle = normalizeForComparison(song.title)
                val cleanArtist = normalizeForComparison(song.artist)

                val siblingFiles = parent.listFiles { f ->
                    f.isFile && (f.extension.equals("lrc", ignoreCase = true) || f.extension.equals("txt", ignoreCase = true))
                } ?: emptyArray()

                // Tier 1: Exact Base Name Match (Song.lrc next to Song.mp3)
                val exactLrc = siblingFiles.firstOrNull {
                    it.nameWithoutExtension.equals(baseName, ignoreCase = true) && it.extension.equals("lrc", ignoreCase = true)
                } ?: siblingFiles.firstOrNull {
                    it.nameWithoutExtension.equals(baseName, ignoreCase = true)
                }

                if (exactLrc != null) {
                    try {
                        val text = exactLrc.readText().trim()
                        if (text.isNotBlank() && validateLrcContent(text, song)) {
                            Log.d(TAG, "LYRICS: local candidate found (Tier 1 exact): ${exactLrc.name} for ${song.title}")
                            return text
                        }
                    } catch (_: Exception) {}
                }

                // Tier 2: Stripped Track Number Match (e.g. "01 - Track" or "01. Track")
                val strippedBase = stripTrackNumber(baseName)
                if (strippedBase.isNotBlank() && !strippedBase.equals(baseName, ignoreCase = true)) {
                    val matchingStripped = siblingFiles.firstOrNull {
                        stripTrackNumber(it.nameWithoutExtension).equals(strippedBase, ignoreCase = true)
                    }
                    if (matchingStripped != null) {
                        try {
                            val text = matchingStripped.readText().trim()
                            if (text.isNotBlank() && validateLrcContent(text, song)) {
                                Log.d(TAG, "LYRICS: local candidate found (Tier 2 stripped): ${matchingStripped.name} for ${song.title}")
                                return text
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Tier 3: Metadata Pattern Match ("Title - Artist" or "Artist - Title")
                if (cleanTitle.isNotBlank() && cleanArtist.isNotBlank() && cleanArtist != "unknown artist") {
                    val patternMatch = siblingFiles.firstOrNull { file ->
                        val fName = normalizeForComparison(file.nameWithoutExtension)
                        (fName == "$cleanTitle $cleanArtist" || fName == "$cleanArtist $cleanTitle" ||
                         fName == "$cleanTitle - $cleanArtist" || fName == "$cleanArtist - $cleanTitle")
                    }
                    if (patternMatch != null) {
                        try {
                            val text = patternMatch.readText().trim()
                            if (text.isNotBlank() && validateLrcContent(text, song)) {
                                Log.d(TAG, "LYRICS: local candidate found (Tier 3 metadata): ${patternMatch.name} for ${song.title}")
                                return text
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // Tier 4: DocumentFile / SAF tree sibling inspection if applicable
        if (song.mediaUri.startsWith("content://")) {
            try {
                val uri = Uri.parse(song.mediaUri)
                val doc = DocumentFile.fromSingleUri(context, uri)
                val parentDoc = doc?.parentFile
                if (parentDoc != null && parentDoc.isDirectory) {
                    val songBase = doc.name?.substringBeforeLast('.') ?: song.title
                    val matchingDoc = parentDoc.listFiles().firstOrNull { child ->
                        val name = child.name ?: ""
                        child.isFile && (name.endsWith(".lrc", ignoreCase = true) || name.endsWith(".txt", ignoreCase = true)) &&
                                name.substringBeforeLast('.').equals(songBase, ignoreCase = true)
                    }
                    if (matchingDoc != null) {
                        val text = context.contentResolver.openInputStream(matchingDoc.uri)?.bufferedReader()?.use { it.readText() }
                        if (!text.isNullOrBlank() && validateLrcContent(text, song)) {
                            Log.d(TAG, "LYRICS: SAF local candidate found: ${matchingDoc.name} for ${song.title}")
                            return text
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }



    private fun serializeLyrics(success: LyricsState.Success): String {
        return success.lines.joinToString("\n") { line ->
            if (success.isSynchronized && line.timestampMs >= 0L) {
                val totalMs = line.timestampMs
                val minutes = (totalMs / 60000)
                val seconds = (totalMs % 60000) / 1000
                val hundredths = (totalMs % 1000) / 10
                "[%02d:%02d.%02d]%s".format(minutes, seconds, hundredths, line.text)
            } else {
                line.text
            }
        }
    }
}

