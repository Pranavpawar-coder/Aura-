package com.example.aura.domain.model

data class ListeningStatistics(
    val totalListeningTimeMs: Long = 0L,
    val totalTracksPlayed: Int = 0,
    val mostPlayedTrack: Song? = null,
    val mostPlayedArtist: Pair<String, Int>? = null,
    val mostPlayedAlbum: Pair<String, Int>? = null,
    val favoriteGenre: Pair<String, Int>? = null,
    val localRecommendations: List<Song> = emptyList()
) {
    val formattedTotalTime: String
        get() {
            val totalSeconds = totalListeningTimeMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }
}
