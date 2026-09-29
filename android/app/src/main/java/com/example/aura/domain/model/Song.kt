package com.example.aura.domain.model

data class Song(
    val id: String,
    val sourceUri: String = "",
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String? = null,
    val trackNumber: Int? = null,
    val genre: String? = null,
    val year: Int? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val mimeType: String? = null,
    val fileSize: Long = 0L,
    val codec: String? = null,
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val bitDepth: Int = 0,
    val channelCount: Int = 2,
    val isLossless: Boolean = false,
    val isFavorite: Boolean = false,
    val rating: Int = 0
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    /**
     * Truthful audio quality badge based strictly on real technical facts.
     * Never displays fabricated Hi-Res/Lossless labels.
     */
    val qualityBadge: String?
        get() {
            if (codec.isNullOrBlank()) return null
            return when {
                isLossless -> {
                    val rateKhz = if (sampleRate > 0) "${sampleRate / 1000}kHz" else null
                    val depthStr = if (bitDepth > 0) "${bitDepth}-bit" else null
                    if (sampleRate >= 88200 || bitDepth >= 24) {
                        listOfNotNull("Hi-Res Lossless", codec, depthStr, rateKhz).joinToString(" • ")
                    } else {
                        listOfNotNull("Lossless", codec, depthStr, rateKhz).joinToString(" • ")
                    }
                }
                bitrate > 0 -> {
                    val kbps = "${bitrate / 1000} kbps"
                    "$codec • $kbps"
                }
                else -> codec
            }
        }

    /**
     * Exhaustive technical specification string according to Phase 1 Section 6.
     * E.g. "FLAC • 24-bit • 96.0 kHz • Lossless • Stereo"
     * Or for MP3: "MP3 • 320 kbps • 44.1 kHz • Lossy • Stereo"
     */
    val technicalSpecs: String
        get() {
            val parts = mutableListOf<String>()
            if (!codec.isNullOrBlank()) parts.add(codec)
            if (bitDepth > 0 && sampleRate > 0) {
                parts.add("${bitDepth}-bit • ${sampleRate / 1000.0} kHz")
            } else if (bitrate > 0 && sampleRate > 0) {
                parts.add("${bitrate / 1000} kbps • ${sampleRate / 1000.0} kHz")
            } else if (bitrate > 0) {
                parts.add("${bitrate / 1000} kbps")
            } else if (sampleRate > 0) {
                parts.add("${sampleRate / 1000.0} kHz")
            }

            if (isLossless) {
                parts.add("Lossless")
            } else if (!codec.isNullOrBlank()) {
                parts.add("Lossy")
            }

            val channelStr = when (channelCount) {
                1 -> "Mono"
                2 -> "Stereo"
                6 -> "5.1 Surround"
                else -> if (channelCount > 2) "$channelCount Channels" else null
            }
            if (channelStr != null) parts.add(channelStr)

            return if (parts.isNotEmpty()) parts.joinToString(" • ") else "Local Audio"
        }
}

data class Artist(
    val id: String,
    val name: String,
    val trackCount: Int = 0,
    val artworkUri: String? = null
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUri: String? = null,
    val year: Int? = null,
    val trackCount: Int = 0
)

data class Genre(
    val name: String,
    val trackCount: Int = 0,
    val artworkUri: String? = null
)

data class FolderGroup(
    val path: String,
    val displayName: String,
    val trackCount: Int = 0
)

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val artworkUri: String? = null
)

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlaybackState(
    val currentSong: Song? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedCurrentPosition: String
        get() {
            val totalSec = currentPositionMs / 1000
            return "%d:%02d".format(totalSec / 60, totalSec % 60)
        }

    val formattedRemainingTime: String
        get() {
            val remainingSec = ((durationMs - currentPositionMs) / 1000).coerceAtLeast(0)
            return "-%d:%02d".format(remainingSec / 60, remainingSec % 60)
        }
}
