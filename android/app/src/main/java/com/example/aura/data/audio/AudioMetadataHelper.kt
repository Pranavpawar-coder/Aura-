package com.example.aura.data.audio

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import java.io.File

data class AudioTechnicalDetails(
    val codec: String?,
    val sampleRate: Int,
    val bitDepth: Int,
    val bitrate: Int,
    val channelCount: Int,
    val isLossless: Boolean,
    val durationMs: Long = 0L,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val mimeType: String? = null,
    val embeddedArtworkBytes: ByteArray? = null,
    val trackGainDb: Float? = null,
    val albumGainDb: Float? = null
)

object AudioMetadataHelper {

    fun extractTechnicalDetails(
        context: Context,
        uri: Uri,
        filePath: String? = null,
        mimeTypeHint: String? = null
    ): AudioTechnicalDetails {
        var mimeType: String? = mimeTypeHint
        var bitrate = 0
        var sampleRate = 0
        var bitDepth = 0
        var channelCount = 2
        var durationMs = 0L
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var albumArtist: String? = null
        var genre: String? = null
        var year: Int? = null
        var trackNumber: Int? = null
        var artworkBytes: ByteArray? = null

        val retriever = MediaMetadataRetriever()
        var retrieverLoaded = false

        try {
            if (uri.scheme == "content" || uri.scheme == "android.resource") {
                retriever.setDataSource(context, uri)
                retrieverLoaded = true
            } else if (filePath != null && File(filePath).exists()) {
                retriever.setDataSource(filePath)
                retrieverLoaded = true
            } else if (uri.path != null && File(uri.path!!).exists()) {
                retriever.setDataSource(uri.path!!)
                retrieverLoaded = true
            }
        } catch (_: Exception) {
            // Ignore datasource loading exceptions for unsupported/corrupt files
        }

        if (retrieverLoaded) {
            try {
                if (mimeType.isNullOrEmpty()) {
                    mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                }

                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()?.let {
                    if (it > 0) bitrate = it
                }

                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
                    if (it > 0) durationMs = it
                }

                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)

                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()?.let {
                    year = it
                } ?: run {
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)?.take(4)?.toIntOrNull()?.let {
                        year = it
                    }
                }

                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.let { trackStr ->
                    val cleanTrack = trackStr.substringBefore('/').trim()
                    cleanTrack.toIntOrNull()?.let { trackNumber = it }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()?.let {
                        if (it > 0) sampleRate = it
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)?.toIntOrNull()?.let {
                        if (it > 0) bitDepth = it
                    }
                }

                // Extract embedded cover artwork if available
                artworkBytes = retriever.embeddedPicture
            } catch (_: Exception) {
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }
        }

        // Secondary extractor pass for sample rate and channels if not found
        if (sampleRate == 0 || channelCount <= 0 || bitrate == 0) {
            val extractor = MediaExtractor()
            try {
                if (uri.scheme == "content" || uri.scheme == "android.resource") {
                    extractor.setDataSource(context, uri, null)
                } else if (filePath != null) {
                    extractor.setDataSource(filePath)
                } else if (uri.path != null) {
                    extractor.setDataSource(uri.path!!)
                }

                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val trackMime = format.getString(MediaFormat.KEY_MIME) ?: ""
                    if (trackMime.startsWith("audio/")) {
                        if (mimeType.isNullOrEmpty()) {
                            mimeType = trackMime
                        }
                        if (sampleRate == 0 && format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                            sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        }
                        if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                            channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                        if (bitrate == 0 && format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                            bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)
                        }
                        if (durationMs == 0L && format.containsKey(MediaFormat.KEY_DURATION)) {
                            durationMs = format.getLong(MediaFormat.KEY_DURATION) / 1000
                        }
                        break
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    extractor.release()
                } catch (_: Exception) {
                }
            }
        }

        val pathLower = (filePath ?: uri.path ?: "").lowercase()

        // Normalize codec and lossless characteristics
        val isFlac = mimeType?.contains("flac", ignoreCase = true) == true || pathLower.endsWith(".flac")
        val isWav = mimeType?.contains("wav", ignoreCase = true) == true || pathLower.endsWith(".wav")
        val isAlac = mimeType?.contains("alac", ignoreCase = true) == true || pathLower.endsWith(".alac")
        val isAiff = mimeType?.contains("aiff", ignoreCase = true) == true || pathLower.endsWith(".aiff") || pathLower.endsWith(".aif")
        val isDsd = pathLower.endsWith(".dsf") || pathLower.endsWith(".dff") || mimeType?.contains("dsd", ignoreCase = true) == true
        val isApe = pathLower.endsWith(".ape") || mimeType?.contains("ape", ignoreCase = true) == true
        val isWma = mimeType?.contains("wma", ignoreCase = true) == true || pathLower.endsWith(".wma")
        val isWavpack = pathLower.endsWith(".wv")
        val isOpus = mimeType?.contains("opus", ignoreCase = true) == true || pathLower.endsWith(".opus")
        val isOgg = mimeType?.contains("ogg", ignoreCase = true) == true || pathLower.endsWith(".ogg") || pathLower.endsWith(".oga")
        val isMp3 = mimeType?.contains("mpeg", ignoreCase = true) == true || mimeType?.contains("mp3", ignoreCase = true) == true || pathLower.endsWith(".mp3")
        val isAac = mimeType?.contains("aac", ignoreCase = true) == true || mimeType?.contains("mp4", ignoreCase = true) == true || mimeType?.contains("m4a", ignoreCase = true) == true || pathLower.endsWith(".m4a") || pathLower.endsWith(".aac")

        val isLossless = isFlac || isWav || isAlac || isAiff || isDsd || isApe || isWavpack

        // Set default bit depth for standard lossless formats if not extracted
        if (bitDepth == 0 && isLossless) {
            bitDepth = if (sampleRate >= 96000) 24 else 16
        }

        val codecName = when {
            isDsd -> "DSD"
            isFlac -> "FLAC"
            isWav -> "WAV"
            isAlac -> "ALAC"
            isAiff -> "AIFF"
            isApe -> "APE"
            isWavpack -> "WavPack"
            isOpus -> "Opus"
            isOgg -> "OGG"
            isMp3 -> "MP3"
            isAac -> "AAC"
            isWma -> "WMA"
            else -> mimeType?.removePrefix("audio/")?.uppercase()
        }

        var trackGainDb: Float? = null
        var albumGainDb: Float? = null
        val resolvedPath = filePath ?: uri.path
        if (resolvedPath != null) {
            val rgPair = extractReplayGainFromFile(File(resolvedPath))
            trackGainDb = rgPair.first
            albumGainDb = rgPair.second
        }

        return AudioTechnicalDetails(
            codec = codecName,
            sampleRate = sampleRate,
            bitDepth = bitDepth,
            bitrate = bitrate,
            channelCount = if (channelCount > 0) channelCount else 2,
            isLossless = isLossless,
            durationMs = durationMs,
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            genre = genre,
            year = year,
            trackNumber = trackNumber,
            mimeType = mimeType,
            embeddedArtworkBytes = artworkBytes,
            trackGainDb = trackGainDb,
            albumGainDb = albumGainDb
        )
    }

    private fun extractReplayGainFromFile(file: File): Pair<Float?, Float?> {
        if (!file.exists() || !file.canRead() || file.length() < 64) return Pair(null, null)
        return try {
            val length = file.length().coerceAtMost(65536L).toInt()
            val bytes = ByteArray(length)
            file.inputStream().use { it.read(bytes) }
            val raw = String(bytes, Charsets.ISO_8859_1)

            var trackGain: Float? = null
            var albumGain: Float? = null

            val trackMatch = Regex("""REPLAYGAIN_TRACK_GAIN\s*=\s*([+-]?\d+(?:\.\d+)?)\s*(?:dB)?""", RegexOption.IGNORE_CASE).find(raw)
            if (trackMatch != null) {
                trackGain = trackMatch.groupValues[1].toFloatOrNull()
            }

            val albumMatch = Regex("""REPLAYGAIN_ALBUM_GAIN\s*=\s*([+-]?\d+(?:\.\d+)?)\s*(?:dB)?""", RegexOption.IGNORE_CASE).find(raw)
            if (albumMatch != null) {
                albumGain = albumMatch.groupValues[1].toFloatOrNull()
            }

            Pair(trackGain, albumGain)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    fun extractLyrics(context: Context, uri: Uri, filePath: String? = null): String? {
        val resolvedPath = filePath ?: try {
            AudioSourceNormalizer.normalizeSource(context, uri, filePath).resolvedFilePath
        } catch (_: Exception) {
            if (uri.scheme == "file") uri.path else null
        }

        if (resolvedPath != null) {
            try {
                val audioFile = File(resolvedPath)
                if (audioFile.exists()) {
                    val embedded = extractEmbeddedLyricsFromFile(audioFile)
                    if (!embedded.isNullOrBlank()) {
                        return embedded
                    }

                    val baseName = audioFile.nameWithoutExtension
                    val parent = audioFile.parentFile
                    if (parent != null && parent.exists() && parent.isDirectory) {
                        val directLrc = File(parent, "$baseName.lrc")
                        if (directLrc.exists() && directLrc.isFile) {
                            return directLrc.readText().trim().ifBlank { null }
                        }

                        val matchingLrc = parent.listFiles { file ->
                            file.isFile && (file.extension.equals("lrc", ignoreCase = true) || file.extension.equals("txt", ignoreCase = true)) &&
                                    file.nameWithoutExtension.equals(baseName, ignoreCase = true)
                        }?.firstOrNull()

                        if (matchingLrc != null) {
                            return matchingLrc.readText().trim().ifBlank { null }
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        return null
    }

    private fun extractEmbeddedLyricsFromFile(file: File): String? {
        if (!file.exists() || !file.canRead() || file.length() < 32) return null
        return try {
            val length = file.length().coerceAtMost(131072L).toInt()
            val bytes = ByteArray(length)
            file.inputStream().use { it.read(bytes) }
            val raw = String(bytes, Charsets.ISO_8859_1)

            val lyricsIdx = raw.indexOf("LYRICS=", ignoreCase = true)
            if (lyricsIdx != -1) {
                val start = lyricsIdx + 7
                var end = start
                while (end < length && bytes[end] != 0.toByte() && bytes[end] != '\n'.code.toByte()) {
                    end++
                }
                if (end > start) {
                    val lyricBytes = bytes.copyOfRange(start, end)
                    return String(lyricBytes, Charsets.UTF_8).trim().ifBlank { null }
                }
            }

            val unsyncedIdx = raw.indexOf("UNSYNCEDLYRICS=", ignoreCase = true)
            if (unsyncedIdx != -1) {
                val start = unsyncedIdx + 15
                var end = start
                while (end < length && bytes[end] != 0.toByte() && bytes[end] != '\n'.code.toByte()) {
                    end++
                }
                if (end > start) {
                    val lyricBytes = bytes.copyOfRange(start, end)
                    return String(lyricBytes, Charsets.UTF_8).trim().ifBlank { null }
                }
            }

            val usltIdx = raw.indexOf("USLT")
            if (usltIdx != -1 && usltIdx + 10 < length) {
                val start = (usltIdx + 10).coerceAtMost(length)
                val end = (start + 4096).coerceAtMost(length)
                val text = String(bytes.copyOfRange(start, end), Charsets.UTF_8)
                    .replace(Regex("[^\\p{Print}\\p{Space}]"), "")
                    .trim()
                if (text.length > 15) {
                    return text
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun resolveMimeType(filePath: String): String {
        val lower = filePath.lowercase()
        return when {
            lower.endsWith(".flac") -> "audio/flac"
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".wav") -> "audio/wav"
            lower.endsWith(".m4a") || lower.endsWith(".aac") -> "audio/mp4"
            lower.endsWith(".opus") -> "audio/opus"
            lower.endsWith(".ogg") || lower.endsWith(".oga") -> "audio/ogg"
            lower.endsWith(".alac") -> "audio/alac"
            lower.endsWith(".aiff") || lower.endsWith(".aif") -> "audio/x-aiff"
            lower.endsWith(".wma") -> "audio/x-ms-wma"
            lower.endsWith(".ape") -> "audio/x-ape"
            lower.endsWith(".wv") -> "audio/x-wavpack"
            lower.endsWith(".dsf") || lower.endsWith(".dff") -> "audio/x-dsd"
            else -> "audio/*"
        }
    }
}

