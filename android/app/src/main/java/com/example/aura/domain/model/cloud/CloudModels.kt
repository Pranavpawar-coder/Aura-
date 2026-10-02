package com.example.aura.domain.model.cloud

enum class AudioQuality(val displayName: String, val badge: String, val formatExtension: String) {
    LOSSLESS_FLAC("FLAC Lossless (16-bit / 44.1kHz)", "FLAC", "flac"),
    HIRES_FLAC("Hi-Res FLAC (24-bit / 96kHz)", "Hi-Res FLAC", "flac"),
    HIGH_MP3("MP3 (320 kbps CBR)", "MP3 320k", "mp3"),
    STANDARD_MP3("MP3 (192 kbps)", "MP3 192k", "mp3")
}

data class CloudTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val coverUrl: String? = null,
    val releaseYear: Int? = null,
    val trackNumber: Int? = null,
    val isrc: String? = null,
    val sourceUrl: String = "",
    val sourceType: String = "spotify_track", // spotify_track, spotify_album, spotify_playlist, itunes, direct
    val availableQualities: List<AudioQuality> = listOf(
        AudioQuality.LOSSLESS_FLAC,
        AudioQuality.HIRES_FLAC,
        AudioQuality.HIGH_MP3
    )
) {
    val formattedDuration: String
        get() {
            val totalSec = durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return "%d:%02d".format(min, sec)
        }
}

data class CloudCollection(
    val id: String,
    val title: String,
    val creator: String,
    val type: String, // "track", "album", "playlist"
    val coverUrl: String? = null,
    val totalTracks: Int = 0,
    val tracks: List<CloudTrack> = emptyList()
)

enum class DownloadStatus {
    QUEUED,
    RESOLVING,
    DOWNLOADING,
    TAGGING,
    COMPLETED,
    FAILED,
    CANCELLED,
    PAUSED
}

data class DownloadTask(
    val id: String,
    val track: CloudTrack,
    val targetQuality: AudioQuality = AudioQuality.LOSSLESS_FLAC,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0.0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val outputFilePath: String? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val formattedSpeed: String
        get() {
            return when {
                speedBytesPerSec > 1024 * 1024 -> "%.1f MB/s".format(speedBytesPerSec / (1024.0 * 1024.0))
                speedBytesPerSec > 1024 -> "%.0f KB/s".format(speedBytesPerSec / 1024.0)
                speedBytesPerSec > 0 -> "$speedBytesPerSec B/s"
                else -> "--"
            }
        }

    val formattedSize: String
        get() {
            val bytes = if (totalBytes > 0) totalBytes else downloadedBytes
            return when {
                bytes > 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
                bytes > 1024 -> "%.0f KB".format(bytes / 1024.0)
                bytes > 0 -> "$bytes B"
                else -> "--"
            }
        }
}
