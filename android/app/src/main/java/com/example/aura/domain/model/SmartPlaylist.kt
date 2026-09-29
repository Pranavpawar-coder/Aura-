package com.example.aura.domain.model

enum class SmartPlaylistType(val displayName: String, val description: String) {
    RECENTLY_ADDED("Recently Added", "Tracks added to library in the last 30 days"),
    RECENTLY_PLAYED("Recently Played", "Your latest listening history"),
    MOST_PLAYED("Most Played", "Frequently played tracks"),
    NEVER_PLAYED("Never Played", "Discovered tracks waiting to be heard"),
    FAVORITES("Favorites", "All your loved tracks"),
    LONG_TRACKS("Long Tracks", "Tracks longer than 5 minutes"),
    HIGH_QUALITY("High Quality", "Lossless and Hi-Res studio master tracks")
}

data class SmartPlaylist(
    val type: SmartPlaylistType,
    val songs: List<Song>
) {
    val trackCount: Int get() = songs.size
}
