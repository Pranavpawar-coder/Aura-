package com.example.aura.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.domain.model.Album
import com.example.aura.domain.model.Artist
import com.example.aura.domain.model.FolderGroup
import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.Song
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraPrimaryAccent
import com.example.aura.theme.AuraPrimaryContainer
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerLowest
import com.example.aura.theme.auraPressable
import com.example.aura.ui.components.AuraAnimatedFavoriteButton
import com.example.aura.ui.components.AuraFallbackArtwork
import com.example.aura.ui.components.SongContextMenuBottomSheet

@Composable
fun AlbumDetailScreen(
    album: Album,
    songs: List<Song>,
    playlists: List<Playlist> = emptyList(),
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Playlist, Song) -> Unit,
    onCreatePlaylist: (String, Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuSong by remember { mutableStateOf<Song?>(null) }

    if (menuSong != null) {
        val s = menuSong!!
        SongContextMenuBottomSheet(
            song = s,
            playlists = playlists,
            onDismiss = { menuSong = null },
            onPlay = { onSongClick(s) },
            onPlayNext = { /* Managed by caller or viewmodel */ },
            onAddToQueue = { /* Managed by caller */ },
            onToggleFavorite = { onToggleFavorite(s) },
            onAddToPlaylist = { onAddToPlaylist(it, s) },
            onCreatePlaylist = { onCreatePlaylist(it, s) }
        )
    }

    val totalDurationMs = songs.sumOf { it.durationMs }
    val formattedTotalDuration = formatTotalDuration(totalDurationMs)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurface),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(AuraSurfaceContainerLowest)
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
                        cornerRadius = 0.dp,
                        iconSize = 72.dp
                    )
                }

                // Gradient scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    AuraSurface
                                )
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .clip(CircleShape)
                        .background(AuraSurface.copy(alpha = 0.7f))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraOnSurface
                    )
                }

                // Title & Details at Bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = album.title,
                        color = AuraOnSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${album.artist} • ${songs.size} songs • $formattedTotalDuration" +
                                (album.year?.let { " • $it" } ?: ""),
                        color = AuraOnSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Action Buttons: Play & Shuffle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(AuraPrimary, AuraPrimaryContainer)))
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraOnPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play", color = AuraOnPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Shuffle Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraSurfaceContainerHigh)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = AuraPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Song List
        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            DetailSongRow(
                index = index + 1,
                song = song,
                onSongClick = { onSongClick(song) },
                onMenuClick = { menuSong = song },
                onToggleFavorite = { onToggleFavorite(song) }
            )
        }
    }
}

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    songs: List<Song>,
    albums: List<Album>,
    playlists: List<Playlist> = emptyList(),
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Playlist, Song) -> Unit,
    onCreatePlaylist: (String, Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuSong by remember { mutableStateOf<Song?>(null) }

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
            onCreatePlaylist = { onCreatePlaylist(it, s) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurface),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(AuraSurfaceContainerLowest)
            ) {
                if (artist.artworkUri != null) {
                    AsyncImage(
                        model = artist.artworkUri,
                        contentDescription = artist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AuraOutline,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    AuraSurface
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .clip(CircleShape)
                        .background(AuraSurface.copy(alpha = 0.7f))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraOnSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = artist.name,
                        color = AuraOnSurface,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${albums.size} albums • ${songs.size} tracks",
                        color = AuraOnSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Play / Shuffle Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(AuraPrimary, AuraPrimaryContainer)))
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraOnPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play All", color = AuraOnPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraSurfaceContainerHigh)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = AuraPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Albums Carousel
        if (albums.isNotEmpty()) {
            item {
                Text(
                    text = "Albums (${albums.size})",
                    color = AuraOnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 6.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { onAlbumClick(album) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AuraSurfaceContainerHigh)
                            ) {
                                if (album.artworkUri != null) {
                                    AsyncImage(
                                        model = album.artworkUri,
                                        contentDescription = album.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Album, contentDescription = null, tint = AuraOutline, modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                            Text(
                                text = album.title,
                                color = AuraOnSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Text(
                                text = "${album.trackCount} tracks",
                                color = AuraOnSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Songs Header
        item {
            Text(
                text = "Songs (${songs.size})",
                color = AuraOnSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)
            )
        }

        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            DetailSongRow(
                index = index + 1,
                song = song,
                onSongClick = { onSongClick(song) },
                onMenuClick = { menuSong = song },
                onToggleFavorite = { onToggleFavorite(song) }
            )
        }
    }
}

@Composable
fun FolderDetailScreen(
    folder: FolderGroup,
    songs: List<Song>,
    playlists: List<Playlist> = emptyList(),
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Playlist, Song) -> Unit,
    onCreatePlaylist: (String, Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuSong by remember { mutableStateOf<Song?>(null) }

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
            onCreatePlaylist = { onCreatePlaylist(it, s) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurface),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AuraOnSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = folder.displayName, color = AuraOnSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = "${songs.size} tracks", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(AuraPrimary, AuraPrimaryContainer)))
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraOnPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play All", color = AuraOnPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraSurfaceContainerHigh)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = AuraPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            DetailSongRow(
                index = index + 1,
                song = song,
                onSongClick = { onSongClick(song) },
                onMenuClick = { menuSong = song },
                onToggleFavorite = { onToggleFavorite(song) }
            )
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    songs: List<Song>,
    playlists: List<Playlist> = emptyList(),
    onBack: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onRemoveSong: (Song) -> Unit,
    onMoveSong: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuSong by remember { mutableStateOf<Song?>(null) }

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
            onAddToPlaylist = {},
            onCreatePlaylist = {}
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurface),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AuraOnSurface)
                }
                IconButton(onClick = onDeletePlaylist) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Playlist", tint = AuraOutline)
                }
            }
        }

        // Playlist Title Info
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = playlist.name,
                    color = AuraOnSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                val totalDuration = formatTotalDuration(songs.sumOf { it.durationMs })
                Text(
                    text = "${songs.size} tracks • $totalDuration",
                    color = AuraOnSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Play / Shuffle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(AuraPrimary, AuraPrimaryContainer)))
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraOnPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play", color = AuraOnPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraSurfaceContainerHigh)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = AuraPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Playlist Songs with Reordering controls
        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSongClick(song) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    color = AuraOutline,
                    fontSize = 13.sp,
                    modifier = Modifier.width(28.dp)
                )

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AuraSurfaceContainerHigh),
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
                        Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = AuraOutline, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = song.title, color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "${song.artist} • ${song.formattedDuration}", color = AuraOnSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Reorder Buttons
                if (index > 0) {
                    IconButton(
                        onClick = { onMoveSong(index, index - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = AuraOutline, modifier = Modifier.size(18.dp))
                    }
                }
                if (index < songs.size - 1) {
                    IconButton(
                        onClick = { onMoveSong(index, index + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = AuraOutline, modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(
                    onClick = { menuSong = song },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = AuraOutline, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailSongRow(
    index: Int,
    song: Song,
    onSongClick: () -> Unit,
    onMenuClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .auraPressable(pressedScale = 0.98f, onClick = onSongClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = AuraOutline,
            fontSize = 13.sp,
            modifier = Modifier.width(28.dp)
        )

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AuraSurfaceContainerHigh),
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
                    cornerRadius = 10.dp,
                    iconSize = 22.dp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "${song.artist} • ${song.formattedDuration}", color = AuraOnSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        AuraAnimatedFavoriteButton(
            isFavorite = song.isFavorite,
            onClick = onToggleFavorite,
            size = 36.dp,
            iconSize = 18.dp
        )

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(36.dp)
                .auraPressable(pressedScale = 0.88f, onClick = onMenuClick)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = AuraOutline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatTotalDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}
