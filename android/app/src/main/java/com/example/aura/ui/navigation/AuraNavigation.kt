package com.example.aura.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import com.example.aura.theme.AuraMotion
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.aura.ui.audiofx.AudioEffectsScreen
import com.example.aura.ui.components.AuraHeader
import com.example.aura.ui.components.MiniPlayer
import com.example.aura.ui.home.HomeScreen
import com.example.aura.ui.library.LibraryScreen
import com.example.aura.ui.nowplaying.NowPlayingScreen
import com.example.aura.ui.search.SearchScreen
import com.example.aura.ui.settings.SettingsScreen
import com.example.aura.ui.stats.StatisticsScreen
import com.example.aura.ui.viewmodel.LibraryViewModel
import com.example.aura.ui.viewmodel.PlayerViewModel
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainerHigh

sealed class AuraScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : AuraScreen("home", "Home", Icons.Default.Home)
    object Library : AuraScreen("library", "Library", Icons.Default.LibraryMusic)
    object Playlists : AuraScreen("playlists", "Playlists", Icons.AutoMirrored.Filled.QueueMusic)
    object Favorites : AuraScreen("favorites", "Favorites", Icons.Default.Favorite)
    object Search : AuraScreen("search", "Search", Icons.Default.Search)
    object Settings : AuraScreen("settings", "Settings", Icons.Default.Tune)
}

@Composable
fun AuraApp(
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    userPreferences: com.example.aura.data.local.UserPreferences? = null,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf<AuraScreen>(AuraScreen.Home) }
    var showStatisticsScreen by remember { mutableStateOf(false) }

    val homeListState = rememberLazyListState()
    val libraryListState = rememberLazyListState()
    val playlistsListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val settingsListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val activeAccent = com.example.aura.theme.LocalAuraAccent.current
    val lyricsSettings by (userPreferences?.lyricsDisplayFlow ?: kotlinx.coroutines.flow.flowOf(com.example.aura.domain.model.settings.LyricsDisplaySettings()))
        .collectAsState(initial = com.example.aura.domain.model.settings.LyricsDisplaySettings())

    val playerState by playerViewModel.playerState.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val queueState by playerViewModel.queueState.collectAsState()
    val lyricsState by playerViewModel.lyricsState.collectAsState()
    val effectsState by playerViewModel.audioEffectsState.collectAsState()
    val visualizerData by playerViewModel.visualizerData.collectAsState()
    val isAudioEffectsOpen by playerViewModel.isAudioEffectsOpen.collectAsState()
    val sleepTimerSettings by playerViewModel.sleepTimerSettings.collectAsState()

    val songs by libraryViewModel.songs.collectAsState()
    val artists by libraryViewModel.artists.collectAsState()
    val albums by libraryViewModel.albums.collectAsState()
    val genres by libraryViewModel.genres.collectAsState()
    val folderGroups by libraryViewModel.folderGroups.collectAsState()
    val favorites by libraryViewModel.favorites.collectAsState()
    val recentlyPlayed by libraryViewModel.recentlyPlayed.collectAsState()
    val mostPlayed by libraryViewModel.mostPlayed.collectAsState()
    val recentlyAdded by libraryViewModel.recentlyAdded.collectAsState()
    val playlists by libraryViewModel.playlists.collectAsState()
    val selectedPlaylistSongs by libraryViewModel.selectedPlaylistSongs.collectAsState()
    val searchResults by libraryViewModel.searchResults.collectAsState()
    val globalSearchResults by libraryViewModel.globalSearchResults.collectAsState()
    val searchQuery by libraryViewModel.searchQuery.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()
    val sortOrder by libraryViewModel.sortOrder.collectAsState()
    val musicFolders by libraryViewModel.musicFolders.collectAsState()

    // Permission handling
    val context = androidx.compose.ui.platform.LocalContext.current
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    var hasPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(context, audioPermission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[audioPermission] == true
        hasPermission = granted
        if (granted) {
            libraryViewModel.scanMusic()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            libraryViewModel.scanMusic()
        } else {
            launcher.launch(permissionsToRequest)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(AuraSurface)) {
        Scaffold(
            topBar = {
                AuraHeader(title = currentTab.title)
            },
            bottomBar = {
                Column {
                    // Floating Mini-Player (Single Source of Playback State)
                    if (playerState.isMiniPlayerVisible) {
                        MiniPlayer(
                            playbackState = playbackState,
                            onPlayPause = { playerViewModel.togglePlayPause() },
                            onNext = { playerViewModel.next() },
                            onPrevious = { playerViewModel.previous() },
                            onToggleFavorite = {
                                playbackState.currentSong?.let { playerViewModel.toggleFavorite(it) }
                            },
                            onClick = { playerViewModel.openFullPlayer() },
                            onSwipeUp = { playerViewModel.openFullPlayer() },
                            onSwipeDown = { playerViewModel.pause() }
                        )
                    }

                    // Bottom Navigation Bar with 6 standard tabs (Section 9)
                    NavigationBar(
                        containerColor = AuraSurface.copy(alpha = 0.95f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        val navItems = listOf(
                            AuraScreen.Home,
                            AuraScreen.Library,
                            AuraScreen.Playlists,
                            AuraScreen.Favorites,
                            AuraScreen.Search,
                            AuraScreen.Settings
                        )
                        navItems.forEach { screen ->
                            val isSelected = currentTab == screen
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.12f else 1.0f,
                                animationSpec = tween(
                                    durationMillis = AuraMotion.DurationQuick,
                                    easing = AuraMotion.CinematicEasing
                                ),
                                label = "nav_scale"
                            )

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentTab == screen) {
                                        coroutineScope.launch {
                                            when (screen) {
                                                AuraScreen.Home -> homeListState.animateScrollToItem(0)
                                                AuraScreen.Library -> libraryListState.animateScrollToItem(0)
                                                AuraScreen.Playlists -> playlistsListState.animateScrollToItem(0)
                                                AuraScreen.Favorites -> favoritesListState.animateScrollToItem(0)
                                                AuraScreen.Search -> searchListState.animateScrollToItem(0)
                                                AuraScreen.Settings -> settingsListState.animateScrollToItem(0)
                                            }
                                        }
                                    } else {
                                        currentTab = screen
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .scale(iconScale)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = activeAccent,
                                    selectedTextColor = activeAccent,
                                    unselectedIconColor = AuraOnSurfaceVariant,
                                    unselectedTextColor = AuraOnSurfaceVariant,
                                    indicatorColor = activeAccent.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
            },
            containerColor = AuraSurface
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!hasPermission && songs.isEmpty()) {
                    // Modern Permission Handling Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AuraSurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AuraPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Audio Permission Required",
                            color = AuraOnSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AURA needs permission to access and play audio files stored on your device.",
                            color = AuraOnSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AuraPrimary)
                                .clickable { launcher.launch(permissionsToRequest) }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Grant Permission",
                                color = AuraOnPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    when (currentTab) {
                        AuraScreen.Home -> {
                            HomeScreen(
                                songs = songs,
                                recentlyPlayed = recentlyPlayed,
                                recentlyAdded = recentlyAdded,
                                mostPlayed = mostPlayed,
                                favorites = favorites,
                                playlists = playlists,
                                playbackState = playbackState,
                                isScanning = isScanning,
                                onScanMusic = { libraryViewModel.scanMusic() },
                                onSongClick = { playerViewModel.play(it, songs) },
                                onPlayAll = { if (songs.isNotEmpty()) playerViewModel.play(songs.first(), songs) },
                                onToggleFavorite = { playerViewModel.toggleFavorite(it) },
                                onAddToPlaylist = { pl, s -> libraryViewModel.addSongToPlaylist(pl.id, s) },
                                onCreatePlaylist = { name, s -> libraryViewModel.createPlaylist(name, listOf(s)) },
                                onHideSong = { libraryViewModel.hideSong(it) },
                                onHideFolder = { libraryViewModel.hideFolder(it) },
                                onHideSimilar = { libraryViewModel.hideSimilar(it) },
                                listState = homeListState
                            )
                        }
                        AuraScreen.Library -> {
                            LibraryScreen(
                                initialFilter = "Songs",
                                songs = songs,
                                albums = albums,
                                artists = artists,
                                genres = genres,
                                folderGroups = folderGroups,
                                favorites = favorites,
                                playlists = playlists,
                                playlistSongs = selectedPlaylistSongs,
                                sortOrder = sortOrder,
                                isScanning = isScanning,
                                onRescan = { libraryViewModel.scanMusic() },
                                onSortOrderChanged = { libraryViewModel.setSortOrder(it) },
                                onSongClick = { playerViewModel.play(it, songs) },
                                onPlayAll = { if (it.isNotEmpty()) playerViewModel.play(it.first(), it) },
                                onShuffleAll = {
                                    if (it.isNotEmpty()) {
                                        playerViewModel.play(it.random(), it)
                                        if (!playbackState.isShuffle) playerViewModel.toggleShuffle()
                                    }
                                },
                                onNewPlaylist = { libraryViewModel.createPlaylist("Mix ${playlists.size + 1}") },
                                onDeletePlaylist = { libraryViewModel.deletePlaylist(it) },
                                onReorderPlaylist = { id, ids -> libraryViewModel.reorderPlaylist(id, ids) },
                                onToggleFavorite = { playerViewModel.toggleFavorite(it) },
                                onAddToPlaylist = { pl, s -> libraryViewModel.addSongToPlaylist(pl.id, s) },
                                onCreatePlaylist = { name, s -> libraryViewModel.createPlaylist(name, listOf(s)) },
                                onHideSong = { libraryViewModel.hideSong(it) },
                                onHideFolder = { libraryViewModel.hideFolder(it) },
                                onHideSimilar = { libraryViewModel.hideSimilar(it) },
                                listState = libraryListState
                            )
                        }
                        AuraScreen.Playlists -> {
                            LibraryScreen(
                                initialFilter = "Playlists",
                                songs = songs,
                                albums = albums,
                                artists = artists,
                                genres = genres,
                                folderGroups = folderGroups,
                                favorites = favorites,
                                playlists = playlists,
                                playlistSongs = selectedPlaylistSongs,
                                sortOrder = sortOrder,
                                isScanning = isScanning,
                                onRescan = { libraryViewModel.scanMusic() },
                                onSortOrderChanged = { libraryViewModel.setSortOrder(it) },
                                onSongClick = { playerViewModel.play(it, songs) },
                                onPlayAll = { if (it.isNotEmpty()) playerViewModel.play(it.first(), it) },
                                onShuffleAll = {
                                    if (it.isNotEmpty()) {
                                        playerViewModel.play(it.random(), it)
                                        if (!playbackState.isShuffle) playerViewModel.toggleShuffle()
                                    }
                                },
                                onNewPlaylist = { libraryViewModel.createPlaylist("Playlist ${playlists.size + 1}") },
                                onDeletePlaylist = { libraryViewModel.deletePlaylist(it) },
                                onReorderPlaylist = { id, ids -> libraryViewModel.reorderPlaylist(id, ids) },
                                onToggleFavorite = { playerViewModel.toggleFavorite(it) },
                                onAddToPlaylist = { pl, s -> libraryViewModel.addSongToPlaylist(pl.id, s) },
                                onCreatePlaylist = { name, s -> libraryViewModel.createPlaylist(name, listOf(s)) },
                                onHideSong = { libraryViewModel.hideSong(it) },
                                onHideFolder = { libraryViewModel.hideFolder(it) },
                                onHideSimilar = { libraryViewModel.hideSimilar(it) },
                                listState = playlistsListState
                            )
                        }
                        AuraScreen.Favorites -> {
                            LibraryScreen(
                                initialFilter = "Favorites",
                                songs = songs,
                                albums = albums,
                                artists = artists,
                                genres = genres,
                                folderGroups = folderGroups,
                                favorites = favorites,
                                playlists = playlists,
                                playlistSongs = selectedPlaylistSongs,
                                sortOrder = sortOrder,
                                isScanning = isScanning,
                                onRescan = { libraryViewModel.scanMusic() },
                                onSortOrderChanged = { libraryViewModel.setSortOrder(it) },
                                onSongClick = { playerViewModel.play(it, favorites) },
                                onPlayAll = { if (it.isNotEmpty()) playerViewModel.play(it.first(), it) },
                                onShuffleAll = {
                                    if (it.isNotEmpty()) {
                                        playerViewModel.play(it.random(), it)
                                        if (!playbackState.isShuffle) playerViewModel.toggleShuffle()
                                    }
                                },
                                onNewPlaylist = { libraryViewModel.createPlaylist("Favorites Mix ${playlists.size + 1}") },
                                onDeletePlaylist = { libraryViewModel.deletePlaylist(it) },
                                onReorderPlaylist = { id, ids -> libraryViewModel.reorderPlaylist(id, ids) },
                                onToggleFavorite = { playerViewModel.toggleFavorite(it) },
                                onAddToPlaylist = { pl, s -> libraryViewModel.addSongToPlaylist(pl.id, s) },
                                onCreatePlaylist = { name, s -> libraryViewModel.createPlaylist(name, listOf(s)) },
                                onHideSong = { libraryViewModel.hideSong(it) },
                                onHideFolder = { libraryViewModel.hideFolder(it) },
                                onHideSimilar = { libraryViewModel.hideSimilar(it) },
                                listState = favoritesListState
                            )
                        }
                        AuraScreen.Search -> {
                            SearchScreen(
                                searchQuery = searchQuery,
                                searchResults = globalSearchResults,
                                playlists = playlists,
                                onQueryChange = { libraryViewModel.updateSearchQuery(it) },
                                onClearQuery = { libraryViewModel.clearSearchQuery() },
                                onSongClick = { playerViewModel.play(it, globalSearchResults.songs) },
                                onAlbumClick = { album ->
                                    val albumSongs = songs.filter { it.album.equals(album.title, ignoreCase = true) }
                                    if (albumSongs.isNotEmpty()) playerViewModel.play(albumSongs.first(), albumSongs)
                                },
                                onArtistClick = { artist ->
                                    val artistSongs = songs.filter { it.artist.equals(artist.name, ignoreCase = true) }
                                    if (artistSongs.isNotEmpty()) playerViewModel.play(artistSongs.first(), artistSongs)
                                },
                                onToggleFavorite = { playerViewModel.toggleFavorite(it) },
                                onAddToPlaylist = { pl, s -> libraryViewModel.addSongToPlaylist(pl.id, s) },
                                onCreatePlaylist = { name, s -> libraryViewModel.createPlaylist(name, listOf(s)) },
                                onHideSong = { libraryViewModel.hideSong(it) },
                                onHideFolder = { libraryViewModel.hideFolder(it) },
                                onHideSimilar = { libraryViewModel.hideSimilar(it) },
                                listState = searchListState
                            )
                        }
                        AuraScreen.Settings -> {
                            SettingsScreen(
                                folders = musicFolders,
                                userPreferences = userPreferences,
                                onAddFolder = { libraryViewModel.addMusicFolder(it) },
                                onRemoveFolder = { libraryViewModel.removeMusicFolder(it) },
                                onRescanFolder = { libraryViewModel.rescanFolder(it) },
                                onRescanLibrary = { libraryViewModel.scanMusic() },
                                onOpenAudioEffects = { playerViewModel.openAudioEffects() },
                                onExportBackup = { uri, cb -> libraryViewModel.exportBackup(uri, cb) },
                                onImportBackup = { uri, cb -> libraryViewModel.importBackup(uri, cb) },
                                onOpenStats = {
                                    libraryViewModel.loadStatistics()
                                    showStatisticsScreen = true
                                },
                                onAttachLyricsFile = { uri -> playerViewModel.attachManualLyrics(uri) },
                                onRefreshLyrics = { playerViewModel.refreshLyrics() },
                                onClearLyricsForCurrentSong = { playerViewModel.clearLyricsForCurrentSong() },
                                onClearLyricsCache = { playerViewModel.clearLyricsCache() },
                                isScanning = isScanning,
                                listState = settingsListState
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Now Playing Overlay with Cinematic Slide & Fade Animation
        AnimatedVisibility(
            visible = playerState.isFullPlayerOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationPlayerExpand,
                    easing = AuraMotion.CinematicEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationQuick,
                    easing = AuraMotion.CinematicEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationPlayerExpand,
                    easing = AuraMotion.CinematicEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationQuick,
                    easing = AuraMotion.CinematicEasing
                )
            )
        ) {
            NowPlayingScreen(
                playbackState = playbackState,
                queueState = queueState,
                lyricsState = lyricsState,
                playerState = playerState,
                effectsState = effectsState,
                visualizerData = visualizerData,
                lyricsSettings = lyricsSettings,
                onPlayPause = { playerViewModel.togglePlayPause() },
                onNext = { playerViewModel.next() },
                onPrevious = { playerViewModel.previous() },
                onSeek = { playerViewModel.seekPercent(it) },
                onSeekTimestamp = { playerViewModel.seekTo(it) },
                onToggleShuffle = { playerViewModel.toggleShuffle() },
                onCycleRepeat = { playerViewModel.cycleRepeat() },
                onToggleFavorite = { playerViewModel.toggleFavorite(it) },
                onDismiss = { playerViewModel.collapseToMiniPlayer() },
                onOpenAudioEffects = { playerViewModel.openAudioEffects() },
                onLyricsOffsetChange = { playerViewModel.setLyricsOffset(it) },
                onPlaySong = { playerViewModel.play(it, queueState.songs) },
                onReorderQueue = { from, to -> playerViewModel.reorderQueue(from, to) },
                onRemoveFromQueue = { playerViewModel.removeFromQueue(it) },
                onClearQueue = { playerViewModel.clearQueue() },
                sleepTimerSettings = sleepTimerSettings,
                onSetSleepTimerMinutes = { mins, fade -> playerViewModel.startSleepTimer(mins, fade) },
                onSetSleepTimerEndOfSong = { fade -> playerViewModel.startSleepTimerEndOfSong(fade) },
                onCancelSleepTimer = { playerViewModel.cancelSleepTimer() },
                onAttachLyricsFile = { uri -> playerViewModel.attachManualLyrics(uri) },
                onRefreshLyrics = { playerViewModel.refreshLyrics() },
                onClearLyricsCache = { playerViewModel.clearLyricsCache() }
            )
        }

        // Fullscreen Audio Effects Overlay with Cinematic Slide & Fade Animation
        AnimatedVisibility(
            visible = isAudioEffectsOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationBottomSheet,
                    easing = AuraMotion.CinematicEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationQuick,
                    easing = AuraMotion.CinematicEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationBottomSheet,
                    easing = AuraMotion.CinematicEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = AuraMotion.DurationQuick,
                    easing = AuraMotion.CinematicEasing
                )
            )
        ) {
            AudioEffectsScreen(
                viewModel = playerViewModel,
                onDismiss = { playerViewModel.closeAudioEffects() }
            )
        }

        // Fullscreen Listening Statistics Overlay with Spring Slide Animation
        AnimatedVisibility(
            visible = showStatisticsScreen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) {
            val stats by libraryViewModel.statistics.collectAsState()
            if (stats != null) {
                StatisticsScreen(
                    stats = stats!!,
                    onSongClick = { playerViewModel.play(it, listOf(it)) },
                    onClearHistory = { libraryViewModel.clearStatistics() },
                    onBack = { showStatisticsScreen = false }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(AuraSurface),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = AuraPrimary)
                }
            }
        }
    }
}
