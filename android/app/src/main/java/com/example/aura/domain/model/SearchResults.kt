package com.example.aura.domain.model

data class SearchResults(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val folders: List<FolderGroup> = emptyList()
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() &&
                genres.isEmpty() && playlists.isEmpty() && folders.isEmpty()

    val totalCount: Int
        get() = songs.size + albums.size + artists.size + genres.size + playlists.size + folders.size
}
