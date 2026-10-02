package com.example.aura.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
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
import com.example.aura.ui.components.AuraGlassPillButton
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
    val currentAccent = LocalAuraAccent.current
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
            .background(AuraDeepBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .background(AuraElevated1)
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

                // Cinematic gradient scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.50f),
                                    Color.Transparent,
                                    AuraDeepBlack.copy(alpha = 0.70f),
                                    AuraDeepBlack
                                )
                            )
                        )
                )

                // Translucent Glass Back Button
                AuraGlassPillButton(
                    onClick = onBack,
                    size = 40.dp,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 20.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title & Details at Bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "ALBUM",
                        color = AuraTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = album.title,
                        color = AuraTextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${album.artist} • ${songs.size} songs • $formattedTotalDuration" +
                                (album.year?.let { " • $it" } ?: ""),
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Action Buttons: Play & Shuffle Glass Capsules
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play Accent Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(currentAccent)
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraDeepBlack, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play All", color = AuraDeepBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Shuffle Glass Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraGlassSurfaceDefault)
                        .border(1.dp, AuraGlassBorderDefault, CircleShape)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = currentAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
    val currentAccent = LocalAuraAccent.current
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
            .background(AuraDeepBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(AuraElevated1)
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
                            tint = AuraTextTertiary,
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
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Transparent,
                                    AuraDeepBlack.copy(alpha = 0.70f),
                                    AuraDeepBlack
                                )
                            )
                        )
                )

                AuraGlassPillButton(
                    onClick = onBack,
                    size = 40.dp,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 20.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "ARTIST",
                        color = AuraTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = artist.name,
                        color = AuraTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${albums.size} albums • ${songs.size} tracks",
                        color = AuraTextSecondary,
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
                        .background(currentAccent)
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraDeepBlack, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play All", color = AuraDeepBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraGlassSurfaceDefault)
                        .border(1.dp, AuraGlassBorderDefault, CircleShape)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = currentAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Albums Carousel
        if (albums.isNotEmpty()) {
            item {
                Text(
                    text = "Albums (${albums.size})",
                    color = AuraTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        Column(
                            modifier = Modifier
                                .width(128.dp)
                                .auraPressable(pressedScale = 0.96f) { onAlbumClick(album) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AuraSurfaceBlack)
                                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(16.dp))
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
                                        Icon(imageVector = Icons.Default.Album, contentDescription = null, tint = AuraTextTertiary, modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = album.title,
                                color = AuraTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${album.trackCount} tracks",
                                color = AuraTextSecondary,
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
                color = AuraTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 8.dp)
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
    val currentAccent = LocalAuraAccent.current
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
            .background(AuraDeepBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraGlassPillButton(
                    onClick = onBack,
                    size = 40.dp
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AuraTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "FOLDER",
                        color = AuraTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Text(text = folder.displayName, color = AuraTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = "${songs.size} tracks", color = AuraTextSecondary, fontSize = 12.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(currentAccent)
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraDeepBlack, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play All", color = AuraDeepBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraGlassSurfaceDefault)
                        .border(1.dp, AuraGlassBorderDefault, CircleShape)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = currentAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
    val currentAccent = LocalAuraAccent.current
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
            .background(AuraDeepBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraGlassPillButton(
                    onClick = onBack,
                    size = 40.dp
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AuraTextPrimary, modifier = Modifier.size(20.dp))
                }
                AuraGlassPillButton(
                    onClick = onDeletePlaylist,
                    size = 40.dp
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Playlist", tint = AuraTextTertiary, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Playlist Title Info
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Text(
                    text = "PLAYLIST",
                    color = AuraTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = playlist.name,
                    color = AuraTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp
                )
                val totalDuration = formatTotalDuration(songs.sumOf { it.durationMs })
                Text(
                    text = "${songs.size} tracks • $totalDuration",
                    color = AuraTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
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
                        .background(currentAccent)
                        .clickable { if (songs.isNotEmpty()) onPlayAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraDeepBlack, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play All", color = AuraDeepBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraGlassSurfaceDefault)
                        .border(1.dp, AuraGlassBorderDefault, CircleShape)
                        .clickable { if (songs.isNotEmpty()) onShuffleAll(songs) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = currentAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Shuffle", color = AuraTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Playlist Songs with Reordering controls
        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .auraPressable(pressedScale = 0.985f) { onSongClick(song) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    color = AuraTextTertiary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(28.dp)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                        Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = AuraTextTertiary, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = song.title, color = AuraTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "${song.artist} • ${song.formattedDuration}", color = AuraTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Reorder Buttons
                if (index > 0) {
                    IconButton(
                        onClick = { onMoveSong(index, index - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = AuraTextTertiary, modifier = Modifier.size(18.dp))
                    }
                }
                if (index < songs.size - 1) {
                    IconButton(
                        onClick = { onMoveSong(index, index + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = AuraTextTertiary, modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(
                    onClick = { menuSong = song },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = AuraTextTertiary, modifier = Modifier.size(18.dp))
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
    val currentAccent = LocalAuraAccent.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .auraPressable(pressedScale = 0.985f, onClick = onSongClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = AuraTextTertiary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(28.dp)
        )

        Box(
            modifier = Modifier
                .size(48.dp)
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
                    iconSize = 22.dp
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, color = AuraTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                Text(text = "${song.artist} • ${song.formattedDuration}", color = AuraTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        AuraAnimatedFavoriteButton(
            isFavorite = song.isFavorite,
            onClick = onToggleFavorite,
            size = 34.dp,
            iconSize = 18.dp
        )

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
