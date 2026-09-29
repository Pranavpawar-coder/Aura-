package com.example.aura.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.data.local.UserPreferences
import com.example.aura.data.local.entity.MusicFolderEntity
import com.example.aura.domain.model.Album
import com.example.aura.domain.model.Artist
import com.example.aura.domain.model.FolderGroup
import com.example.aura.domain.model.Genre
import com.example.aura.domain.model.ListeningStatistics
import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.SearchResults
import com.example.aura.domain.model.SmartPlaylist
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.SongSortOrder
import com.example.aura.domain.model.exclusion.SmartScanSettings
import com.example.aura.domain.repository.MusicRepository
import com.example.aura.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val userPreferences: UserPreferences? = null
) : ViewModel() {

    val smartScanSettings: StateFlow<SmartScanSettings> = musicRepository.getSmartScanSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SmartScanSettings())

    val rawSongs: StateFlow<List<Song>> = musicRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = musicRepository.getArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = musicRepository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val genres: StateFlow<List<Genre>> = musicRepository.getGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folderGroups: StateFlow<List<FolderGroup>> = musicRepository.getFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Song>> = musicRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = musicRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = musicRepository.getMostPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<Song>> = musicRepository.getRecentlyAdded()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val musicFolders: StateFlow<List<MusicFolderEntity>> = musicRepository.getMusicFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartPlaylists: StateFlow<List<SmartPlaylist>> = musicRepository.getSmartPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ratings: StateFlow<Map<String, Int>> = musicRepository.getRatings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _statistics = MutableStateFlow<ListeningStatistics?>(null)
    val statistics: StateFlow<ListeningStatistics?> = _statistics.asStateFlow()

    private val _sortOrder = MutableStateFlow(SongSortOrder.TITLE_AZ)
    val sortOrder: StateFlow<SongSortOrder> = _sortOrder.asStateFlow()

    private val _selectedTab = MutableStateFlow("Songs")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Detail Screen Navigation States
    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _selectedFolder = MutableStateFlow<FolderGroup?>(null)
    val selectedFolder: StateFlow<FolderGroup?> = _selectedFolder.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    val selectedPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylist
        .flatMapLatest { playlist ->
            if (playlist != null) {
                playlistRepository.getPlaylistSongs(playlist.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (userPreferences != null) {
            viewModelScope.launch {
                userPreferences.sortOrderFlow.collect {
                    _sortOrder.value = it
                }
            }
            viewModelScope.launch {
                userPreferences.selectedLibraryTabFlow.collect {
                    _selectedTab.value = it
                }
            }
        }
    }

    val songs: StateFlow<List<Song>> = combine(
        rawSongs,
        recentlyPlayed,
        mostPlayed,
        _sortOrder
    ) { songList, recents, mosts, order ->
        when (order) {
            SongSortOrder.DATE_ADDED -> songList.sortedByDescending { it.dateAdded }
            SongSortOrder.DATE_ADDED_OLD -> songList.sortedBy { it.dateAdded }
            SongSortOrder.LAST_PLAYED -> {
                val recentMap = recents.mapIndexed { idx, s -> s.id to idx }.toMap()
                songList.sortedWith(compareBy<Song> { recentMap[it.id] ?: Int.MAX_VALUE }.thenBy { it.title.lowercase() })
            }
            SongSortOrder.MOST_PLAYED -> {
                val mostMap = mosts.mapIndexed { idx, s -> s.id to idx }.toMap()
                songList.sortedWith(compareBy<Song> { mostMap[it.id] ?: Int.MAX_VALUE }.thenBy { it.title.lowercase() })
            }
            SongSortOrder.TITLE_AZ -> songList.sortedBy { it.title.lowercase() }
            SongSortOrder.TITLE_ZA -> songList.sortedByDescending { it.title.lowercase() }
            SongSortOrder.ARTIST_AZ -> songList.sortedWith(compareBy<Song> { it.artist.lowercase() }.thenBy { it.title.lowercase() })
            SongSortOrder.ARTIST_ZA -> songList.sortedWith(compareByDescending<Song> { it.artist.lowercase() }.thenBy { it.title.lowercase() })
            SongSortOrder.ALBUM_AZ -> songList.sortedWith(compareBy<Song> { it.album.lowercase() }.thenBy { it.trackNumber ?: 0 })
            SongSortOrder.ALBUM_ZA -> songList.sortedWith(compareByDescending<Song> { it.album.lowercase() }.thenBy { it.trackNumber ?: 0 })
            SongSortOrder.DURATION_DESC -> songList.sortedByDescending { it.durationMs }
            SongSortOrder.DURATION_ASC -> songList.sortedBy { it.durationMs }
            SongSortOrder.YEAR_DESC -> songList.sortedByDescending { it.year ?: 0 }
            SongSortOrder.YEAR_ASC -> songList.sortedBy { it.year ?: Int.MAX_VALUE }
            SongSortOrder.FILE_SIZE_DESC -> songList.sortedByDescending { it.fileSize }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _searchQuery
        .flatMapLatest { query ->
            musicRepository.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalSearchResults: StateFlow<SearchResults> = _searchQuery
        .flatMapLatest { query ->
            musicRepository.searchAll(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun setSortOrder(order: SongSortOrder) {
        _sortOrder.value = order
        userPreferences?.let {
            viewModelScope.launch {
                it.setSortOrder(order)
            }
        }
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
        userPreferences?.let {
            viewModelScope.launch {
                it.setSelectedLibraryTab(tab)
            }
        }
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun selectFolder(folder: FolderGroup?) {
        _selectedFolder.value = folder
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun scanMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                musicRepository.scanDeviceMusic()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun addMusicFolder(uri: Uri, displayName: String = uri.lastPathSegment ?: "Music Folder") {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                musicRepository.addMusicFolder(uri, displayName)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun removeMusicFolder(folder: MusicFolderEntity) {
        removeMusicFolder(folder.uriString)
    }

    fun removeMusicFolder(uriString: String) {
        viewModelScope.launch {
            musicRepository.removeMusicFolder(uriString)
        }
    }

    fun rescanFolder(folder: MusicFolderEntity) {
        rescanMusicFolder(folder.uriString)
    }

    fun rescanMusicFolder(uriString: String) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                musicRepository.rescanFolder(uriString)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id)
        }
    }

    fun createPlaylist(name: String, initialSongs: List<Song> = emptyList()) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            initialSongs.forEach { song ->
                playlistRepository.addSongToPlaylist(playlistId, song.id)
            }
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, song.id)
        }
    }

    fun reorderPlaylist(playlistId: Long, songIds: List<String>) {
        viewModelScope.launch {
            playlistRepository.reorderPlaylist(playlistId, songIds)
        }
    }

    // Phase 4: Ratings
    fun setRating(songId: String, rating: Int) {
        viewModelScope.launch {
            if (rating <= 0) {
                musicRepository.removeRating(songId)
            } else {
                musicRepository.setRating(songId, rating)
            }
        }
    }

    // Phase 4: Metadata Editing
    fun updateSongMetadata(
        songId: String,
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int?,
        trackNumber: Int?,
        artworkUri: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = musicRepository.updateSongMetadata(
                songId, title, artist, album, genre, year, trackNumber, artworkUri
            )
            onComplete(success)
        }
    }

    // Phase 4: Listening Statistics
    fun loadStatistics() {
        viewModelScope.launch {
            _statistics.value = musicRepository.getStatistics()
        }
    }

    fun clearStatistics() {
        viewModelScope.launch {
            _statistics.value = null
        }
    }

    // Phase 4: Backup & Restore
    fun exportBackup(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = musicRepository.exportBackup(uri)
            onResult(success)
        }
    }

    fun importBackup(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = musicRepository.importBackup(uri)
            if (success) {
                // Refresh data
                musicRepository.scanDeviceMusic()
            }
            onResult(success)
        }
    }

    // Smart Library Exclusions & Filtering Actions
    fun addExcludedFolder(path: String, displayName: String) {
        viewModelScope.launch {
            musicRepository.addExcludedFolder(path, displayName)
        }
    }

    fun removeExcludedFolder(path: String) {
        viewModelScope.launch {
            musicRepository.removeExcludedFolder(path)
        }
    }

    fun toggleExcludedFolder(path: String, isEnabled: Boolean) {
        viewModelScope.launch {
            musicRepository.toggleExcludedFolder(path, isEnabled)
        }
    }

    fun toggleExcludedExtension(extension: String, isExcluded: Boolean) {
        viewModelScope.launch {
            musicRepository.toggleExcludedExtension(extension, isExcluded)
        }
    }

    fun setMinDurationSeconds(seconds: Int) {
        viewModelScope.launch {
            musicRepository.setMinDurationSeconds(seconds)
        }
    }

    fun addFilenamePattern(pattern: String) {
        viewModelScope.launch {
            musicRepository.addFilenamePattern(pattern)
        }
    }

    fun removeFilenamePattern(pattern: String) {
        viewModelScope.launch {
            musicRepository.removeFilenamePattern(pattern)
        }
    }

    fun toggleFilenamePattern(pattern: String, isEnabled: Boolean) {
        viewModelScope.launch {
            musicRepository.toggleFilenamePattern(pattern, isEnabled)
        }
    }

    fun hideSong(song: Song) {
        viewModelScope.launch {
            musicRepository.hideSong(song)
        }
    }

    fun hideFolder(folderPath: String, displayName: String? = null) {
        viewModelScope.launch {
            musicRepository.hideFolder(folderPath, displayName)
        }
    }

    fun hideSimilar(pattern: String) {
        viewModelScope.launch {
            musicRepository.hideSimilar(pattern)
        }
    }

    fun unhideTrack(sourceUriOrId: String) {
        viewModelScope.launch {
            musicRepository.unhideTrack(sourceUriOrId)
        }
    }

    fun restoreAllHiddenTracks() {
        viewModelScope.launch {
            musicRepository.restoreAllHiddenTracks()
        }
    }

    fun applyCleanMusicLibraryPreset() {
        viewModelScope.launch {
            musicRepository.applyCleanMusicLibraryPreset()
        }
    }
}

