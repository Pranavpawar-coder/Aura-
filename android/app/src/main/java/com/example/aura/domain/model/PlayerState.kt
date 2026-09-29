package com.example.aura.domain.model

/**
 * Unified player state that aggregates audio playback engine state and UI presentation state.
 *
 * PlayerState
 * ├── isFullPlayerOpen
 * ├── isMiniPlayerVisible
 * ├── isQueueOpen
 * ├── currentTrack
 * ├── queue
 * ├── playbackState
 * ├── currentTime
 * └── duration
 */
data class PlayerState(
    val isFullPlayerOpen: Boolean = false,
    val isMiniPlayerVisible: Boolean = false,
    val isQueueOpen: Boolean = false,
    val currentTrack: Song? = null,
    val queue: List<Song> = emptyList(),
    val playbackState: PlaybackStatus = PlaybackStatus.IDLE,
    val currentTime: Long = 0L,
    val duration: Long = 0L
) {
    val isPlaying: Boolean
        get() = playbackState == PlaybackStatus.PLAYING

    val progressPercent: Float
        get() = if (duration > 0) (currentTime.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedCurrentTime: String
        get() {
            val totalSec = currentTime / 1000
            return "%d:%02d".format(totalSec / 60, totalSec % 60)
        }

    val formattedRemainingTime: String
        get() {
            val remainingSec = ((duration - currentTime) / 1000).coerceAtLeast(0)
            return "-%d:%02d".format(remainingSec / 60, remainingSec % 60)
        }
}
