package com.example.aura.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.domain.model.Album
import com.example.aura.domain.model.Artist
import com.example.aura.domain.model.FolderGroup
import com.example.aura.domain.model.Genre
import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.SongSortOrder
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraElevated1
import com.example.aura.theme.AuraGlassBorderDefault
import com.example.aura.theme.AuraGlassHighlightDefault
import com.example.aura.theme.AuraGlassSurfaceDefault
import com.example.aura.theme.AuraSoftBlack
import com.example.aura.theme.AuraSurfaceBlack
import com.example.aura.theme.AuraTextDisabled
import com.example.aura.theme.AuraTextPrimary
import com.example.aura.theme.AuraTextSecondary
import com.example.aura.theme.AuraTextTertiary
import com.example.aura.theme.LocalAuraAccent
import com.example.aura.theme.auraPressable
import com.example.aura.ui.components.AuraAnimatedFavoriteButton
import com.example.aura.ui.components.AuraFallbackArtwork
import com.example.aura.ui.components.AuraGlassButton
import com.example.aura.ui.components.AuraGlassControl
import com.example.aura.ui.components.AuraGlassPill
import com.example.aura.ui.components.SongContextMenuBottomSheet

import androidx.activity.compose.BackHandler

@Composable
fun LibraryScreen(
    initialFilter: String = "Songs",
    songs: List<Song>,
    albums: List<Album> = emptyList(),
    artists: List<Artist> = emptyList(),
    genres: List<Genre> = emptyList(),
    folderGroups: List<FolderGroup> = emptyList(),
    favorites: List<Song> = emptyList(),
    playlists: List<Playlist> = emptyList(),
    playlistSongs: List<Song> = emptyList(),
    sortOrder: SongSortOrder = SongSortOrder.TITLE_AZ,
    isScanning: Boolean = false,
    onRescan: () -> Unit = {},
    onSortOrderChanged: (SongSortOrder) -> Unit = {},
    onFilterSelected: (String) -> Unit = {},
    onSongClick: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit = { if (it.isNotEmpty()) onSongClick(it.first()) },
    onShuffleAll: (List<Song>) -> Unit = {},
    onNewPlaylist: () -> Unit = {},
    onDeletePlaylist: (Long) -> Unit = {},
    onReorderPlaylist: (Long, List<String>) -> Unit = { _, _ -> },
    onToggleFavorite: (Song) -> Unit = {},
    onAddToPlaylist: (Playlist, Song) -> Unit = { _, _ -> },
    onCreatePlaylist: (String, Song) -> Unit = { _, _ -> },
    onHideSong: (Song) -> Unit = {},
    onHideFolder: (String) -> Unit = {},
    onHideSimilar: (String) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAuraAccent.current
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var showSortSheet by remember { mutableStateOf(false) }

    // Active Detail Screen Target
    var activeAlbum by remember { mutableStateOf<Album?>(null) }
    var activeArtist by remember { mutableStateOf<Artist?>(null) }
    var activeFolder by remember { mutableStateOf<FolderGroup?>(null) }
    var activePlaylist by remember { mutableStateOf<Playlist?>(null) }

    // Context Menu Target
    var menuSong by remember { mutableStateOf<Song?>(null) }

    // Back button handling within LibraryScreen
    BackHandler(enabled = menuSong != null) {
        menuSong = null
    }
    BackHandler(enabled = showSortSheet && menuSong == null) {
        showSortSheet = false
    }
    BackHandler(enabled = activeAlbum != null && menuSong == null && !showSortSheet) {
        activeAlbum = null
    }
    BackHandler(enabled = activeArtist != null && menuSong == null && !showSortSheet) {
        activeArtist = null
    }
    BackHandler(enabled = activeFolder != null && menuSong == null && !showSortSheet) {
        activeFolder = null
    }
    BackHandler(enabled = activePlaylist != null && menuSong == null && !showSortSheet) {
        activePlaylist = null
    }

    LaunchedEffect(initialFilter) {
        selectedFilter = initialFilter
    }

    if (menuSong != null) {
        val s = menuSong!!
        SongContextMenuBottomSheet(
            song = s,
            playlists = playlists,
            onDismiss = { menuSong = null },
            onPlay = { onSongClick(s) },
            onPlayNext = {},
            onAddToQueue = {},
            onToggleFavorite = { onToggleFavorite(s) },
            onAddToPlaylist = { onAddToPlaylist(it, s) },
            onCreatePlaylist = { onCreatePlaylist(it, s) },
            onHideSong = { onHideSong(s) },
            onHideFolder = { onHideFolder(it) },
            onHideSimilar = { onHideSimilar(it) }
        )
    }

    // Detail Screen Overlays
    if (activeAlbum != null) {
        val album = activeAlbum!!
        val albumSongs = songs.filter { it.album.equals(album.title, ignoreCase = true) }
        AlbumDetailScreen(
            album = album,
            songs = albumSongs,
            playlists = playlists,
            onBack = { activeAlbum = null },
            onSongClick = onSongClick,
            onPlayAll = onPlayAll,
            onShuffleAll = onShuffleAll,
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylist = onAddToPlaylist,
            onCreatePlaylist = onCreatePlaylist
        )
        return
    }

    if (activeArtist != null) {
        val artist = activeArtist!!
        val artistSongs = songs.filter { it.artist.equals(artist.name, ignoreCase = true) }
        val artistAlbums = albums.filter { it.artist.equals(artist.name, ignoreCase = true) }
        ArtistDetailScreen(
            artist = artist,
            songs = artistSongs,
            albums = artistAlbums,
            playlists = playlists,
            onBack = { activeArtist = null },
            onAlbumClick = { activeAlbum = it },
            onSongClick = onSongClick,
            onPlayAll = onPlayAll,
            onShuffleAll = onShuffleAll,
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylist = onAddToPlaylist,
            onCreatePlaylist = onCreatePlaylist
        )
        return
    }

    if (activeFolder != null) {
        val folder = activeFolder!!
        val folderSongs = songs.filter {
            it.filePath?.let { p -> java.io.File(p).parent } == folder.path ||
                    (folder.path.startsWith("content://") && it.mediaUri.startsWith(folder.path))
        }
        FolderDetailScreen(
            folder = folder,
            songs = folderSongs,
            playlists = playlists,
            onBack = { activeFolder = null },
            onSongClick = onSongClick,
            onPlayAll = onPlayAll,
            onShuffleAll = onShuffleAll,
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylist = onAddToPlaylist,
            onCreatePlaylist = onCreatePlaylist
        )
        return
    }

    if (activePlaylist != null) {
        val pl = activePlaylist!!
        PlaylistDetailScreen(
            playlist = pl,
            songs = playlistSongs,
            playlists = playlists,
            onBack = { activePlaylist = null },
            onDeletePlaylist = {
                onDeletePlaylist(pl.id)
                activePlaylist = null
            },
            onSongClick = onSongClick,
            onPlayAll = onPlayAll,
            onShuffleAll = onShuffleAll,
            onToggleFavorite = onToggleFavorite,
            onRemoveSong = { /* caller */ },
            onMoveSong = { from, to ->
                val list = playlistSongs.map { it.id }.toMutableList()
                if (from in list.indices && to in list.indices) {
                    val item = list.removeAt(from)
                    list.add(to, item)
                    onReorderPlaylist(pl.id, list)
                }
            }
        )
        return
    }

    val filters = listOf("Songs", "Albums", "Artists", "Genres", "Folders", "Playlists", "Favorites")

    Box(modifier = modifier.fillMaxSize().background(AuraDeepBlack)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COLLECTION",
                            color = AuraTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your Library",
                            color = AuraTextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Rescan Button Glass Pill
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AuraGlassSurfaceDefault)
                                .border(1.dp, AuraGlassBorderDefault, CircleShape)
                                .auraPressable(onClick = onRescan),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = currentAccent, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Rescan", tint = AuraTextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sort Button Glass Pill
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AuraGlassSurfaceDefault)
                                .border(1.dp, AuraGlassBorderDefault, CircleShape)
                                .auraPressable { showSortSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = currentAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = sortOrder.displayName, color = AuraTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Filter Tabs Row (Translucent Glass Pills)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters, key = { it }) { f ->
                        val isSel = f == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSel) currentAccent else AuraGlassSurfaceDefault)
                                .border(
                                    1.dp,
                                    if (isSel) currentAccent.copy(alpha = 0.5f) else AuraGlassBorderDefault,
                                    CircleShape
                                )
                                .clickable {
                                    selectedFilter = f
                                    onFilterSelected(f)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = f,
                                color = if (isSel) AuraDeepBlack else AuraTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Empty state check
            if (songs.isEmpty() && !isScanning) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AuraSurfaceBlack)
                                .border(1.dp, AuraGlassBorderDefault, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.LibraryMusic, contentDescription = null, tint = AuraTextTertiary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No music in library", color = AuraTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Add music folders in Settings or tap Rescan to discover local audio files.",
                            color = AuraTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        AuraGlassControl(
                            onClick = onRescan,
                            backgroundColor = currentAccent,
                            borderColor = currentAccent
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rescan",
                                tint = AuraDeepBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rescan Library",
                                color = AuraDeepBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                when (selectedFilter) {
                    "Songs" -> {
                        item {
                            Text(
                                text = "All Songs (${songs.size})",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(songs, key = { it.id }, contentType = { "song" }) { song ->
                            SongRowItem(
                                song = song,
                                onSongClick = onSongClick,
                                onToggleFavorite = onToggleFavorite,
                                onMenuClick = { menuSong = song }
                            )
                        }
                    }

                    "Albums" -> {
                        item {
                            Text(
                                text = "Albums (${albums.size})",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(albums, key = { it.id }) { album ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .auraPressable(pressedScale = 0.985f) { activeAlbum = album }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AuraSurfaceBlack)
                                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (album.artworkUri != null) {
                                        AsyncImage(
                                            model = album.artworkUri,
                                            contentDescription = album.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        AuraFallbackArtwork(
                                            modifier = Modifier.fillMaxSize(),
                                            cornerRadius = 12.dp,
                                            iconSize = 24.dp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = album.title, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = "${album.artist} • ${album.trackCount} tracks" + (album.year?.let { " • $it" } ?: ""),
                                        color = AuraTextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    "Artists" -> {
                        item {
                            Text(
                                text = "Artists (${artists.size})",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(artists, key = { it.id }) { artist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .auraPressable(pressedScale = 0.985f) { activeArtist = artist }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(AuraSurfaceBlack)
                                        .border(1.dp, AuraGlassBorderDefault, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (artist.artworkUri != null) {
                                        AsyncImage(
                                            model = artist.artworkUri,
                                            contentDescription = artist.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AuraTextTertiary, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = artist.name, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = "${artist.trackCount} tracks", color = AuraTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    "Genres" -> {
                        item {
                            Text(
                                text = "Genres (${genres.size})",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(genres, key = { it.name }) { genre ->
                            val genreSongs = songs.filter { it.genre.equals(genre.name, ignoreCase = true) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .auraPressable(pressedScale = 0.985f) { if (genreSongs.isNotEmpty()) onPlayAll(genreSongs) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AuraSurfaceBlack)
                                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = currentAccent, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = genre.name, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "${genre.trackCount} tracks", color = AuraTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    "Folders" -> {
                        item {
                            Text(
                                text = "Folders (${folderGroups.size})",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(folderGroups, key = { it.path }) { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .auraPressable(pressedScale = 0.985f) { activeFolder = folder }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AuraSurfaceBlack)
                                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = currentAccent, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = folder.displayName, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "${folder.trackCount} tracks", color = AuraTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    "Playlists" -> {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Playlists (${playlists.size})",
                                    color = AuraTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(currentAccent)
                                        .auraPressable(onClick = onNewPlaylist)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AuraDeepBlack, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "New", color = AuraDeepBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        items(playlists, key = { it.id }) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .auraPressable(pressedScale = 0.985f) { activePlaylist = playlist }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AuraSurfaceBlack)
                                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = currentAccent, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = playlist.name, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Custom playlist", color = AuraTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    "Favorites" -> {
                        item {
                            Text(
                                text = "Favorites (${favorites.size})",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(favorites, key = { it.id }, contentType = { "song" }) { song ->
                            SongRowItem(
                                song = song,
                                onSongClick = onSongClick,
                                onToggleFavorite = onToggleFavorite,
                                onMenuClick = { menuSong = song }
                            )
                        }
                    }
                }
            }
        }

        // Sorting Bottom Sheet
        AnimatedVisibility(
            visible = showSortSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(AuraSurfaceBlack)
                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sort Library By", color = AuraTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showSortSheet = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AuraTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val allOrders = SongSortOrder.values()
                    allOrders.forEach { order ->
                        val isSelected = order == sortOrder
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.06f) else Color.Transparent)
                                .clickable {
                                    onSortOrderChanged(order)
                                    showSortSheet = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.displayName,
                                color = if (isSelected) currentAccent else AuraTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = currentAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRowItem(
    song: Song,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onMenuClick: () -> Unit
) {
    val currentAccent = LocalAuraAccent.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .auraPressable(pressedScale = 0.985f) { onSongClick(song) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraSurfaceBlack)
                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (song.artworkUri != null) {
                    AsyncImage(
                        model = song.artworkUri,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AuraFallbackArtwork(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 12.dp,
                        iconSize = 24.dp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = song.title,
                    color = AuraTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (song.isLossless) {
                        Text(
                            text = "FLAC",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF34D399).copy(alpha = 0.12f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "${song.artist} • ${song.formattedDuration}",
                        color = AuraTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onToggleFavorite(song) },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) currentAccent else AuraTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = AuraTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
