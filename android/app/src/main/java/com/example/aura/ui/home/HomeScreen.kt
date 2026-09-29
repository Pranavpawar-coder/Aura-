package com.example.aura.ui.home

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.domain.model.PlaybackState
import com.example.aura.domain.model.PlaybackStatus
import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.Song
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraPrimaryAccent
import com.example.aura.theme.AuraPrimaryContainer
import com.example.aura.theme.AuraSecondary
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerLow
import com.example.aura.theme.auraPressable
import com.example.aura.ui.components.AnimatedEqualizerBars
import com.example.aura.ui.components.AuraFallbackArtwork
import com.example.aura.ui.components.SongContextMenuBottomSheet

@Composable
fun HomeScreen(
    songs: List<Song>,
    recentlyPlayed: List<Song> = emptyList(),
    recentlyAdded: List<Song> = emptyList(),
    mostPlayed: List<Song> = emptyList(),
    favorites: List<Song> = emptyList(),
    playlists: List<Playlist> = emptyList(),
    playbackState: PlaybackState,
    isScanning: Boolean = false,
    onScanMusic: () -> Unit = {},
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onToggleFavorite: (Song) -> Unit = {},
    onAddToPlaylist: (Playlist, Song) -> Unit = { _, _ -> },
    onCreatePlaylist: (String, Song) -> Unit = { _, _ -> },
    onHideSong: (Song) -> Unit = {},
    onHideFolder: (String) -> Unit = {},
    onHideSimilar: (String) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val isPlaying = playbackState.status == PlaybackStatus.PLAYING
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
            onCreatePlaylist = { onCreatePlaylist(it, s) },
            onHideSong = { onHideSong(s) },
            onHideFolder = { onHideFolder(it) },
            onHideSimilar = { onHideSimilar(it) }
        )
    }

    if (songs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AuraSurface)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = AuraPrimary,
                        modifier = Modifier.size(44.dp),
                        strokeWidth = 3.5.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scanning audio files...",
                        color = AuraOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AuraSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = AuraOutline,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Offline Music Sanctuary",
                        color = AuraOnSurface,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real local audio plays seamlessly with AndroidX Media3.\nTap scan to discover music on your device.",
                        color = AuraOnSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(AuraPrimary, AuraPrimaryContainer)))
                            .clickable(onClick = onScanMusic)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan",
                            tint = AuraOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Device Music",
                            color = AuraOnPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurface),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Spotlight Hero Card with Real Song
        item {
            val spotlightSong = recentlyPlayed.firstOrNull() ?: songs.first()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AuraSurfaceContainerLow)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .background(AuraSurfaceContainerHigh)
                    ) {
                        if (spotlightSong.artworkUri != null) {
                            AsyncImage(
                                model = spotlightSong.artworkUri,
                                contentDescription = spotlightSong.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AuraFallbackArtwork(
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 0.dp,
                                iconSize = 64.dp
                            )
                        }

                        // Gradient bottom scrim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, AuraSurfaceContainerLow)))
                        )

                        // Equalizer badge top left
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(AuraSurface.copy(alpha = 0.85f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AnimatedEqualizerBars(isPlaying = isPlaying)
                            Text(
                                text = "NOW IN LIBRARY",
                                color = AuraPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Card Meta & Listen Now CTA
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = spotlightSong.title,
                            color = AuraOnSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${spotlightSong.artist} • ${spotlightSong.album}",
                            color = AuraOnSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(AuraPrimary, AuraPrimaryContainer)))
                                    .clickable { onSongClick(spotlightSong) }
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = AuraOnPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Listen Now",
                                    color = AuraOnPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = spotlightSong.formattedDuration,
                                color = AuraOutline,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 8: Recently Played Carousel
        if (recentlyPlayed.isNotEmpty()) {
            item {
                HorizontalSongCarousel(
                    title = "Recently Played",
                    songs = recentlyPlayed,
                    onSongClick = onSongClick
                )
            }
        }

        // Section 10: Recently Added Carousel
        if (recentlyAdded.isNotEmpty()) {
            item {
                HorizontalSongCarousel(
                    title = "Recently Added",
                    songs = recentlyAdded,
                    onSongClick = onSongClick
                )
            }
        }

        // Section 9: Most Played Carousel
        if (mostPlayed.isNotEmpty()) {
            item {
                HorizontalSongCarousel(
                    title = "Most Played",
                    songs = mostPlayed,
                    onSongClick = onSongClick
                )
            }
        }

        // Section 6: Favorites Carousel
        if (favorites.isNotEmpty()) {
            item {
                HorizontalSongCarousel(
                    title = "Favorites",
                    songs = favorites,
                    onSongClick = onSongClick
                )
            }
        }

        // Section: Device Library Tracks Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Songs (${songs.size})",
                    color = AuraOnSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Play All",
                    color = AuraPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onPlayAll)
                )
            }
        }

        // Song Rows with Context Menu
        items(songs, key = { it.id }, contentType = { "song" }) { song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .auraPressable(pressedScale = 0.98f) { onSongClick(song) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
                                iconSize = 24.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = song.title,
                            color = AuraOnSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artist} • ${song.formattedDuration}",
                            color = AuraOnSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleFavorite(song) }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) AuraPrimary else AuraOutline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { menuSong = song }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = AuraOutline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalSongCarousel(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            color = AuraOnSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(songs.take(20), key = { it.id }) { itemSong ->
                Column(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onSongClick(itemSong) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AuraSurfaceContainerHigh)
                    ) {
                        if (itemSong.artworkUri != null) {
                            AsyncImage(
                                model = itemSong.artworkUri,
                                contentDescription = itemSong.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AuraFallbackArtwork(
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 16.dp,
                                iconSize = 40.dp
                            )
                        }
                        itemSong.qualityBadge?.let { badge ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AuraSurface.copy(alpha = 0.85f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    color = AuraSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = itemSong.title,
                        color = AuraOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = itemSong.artist,
                        color = AuraOnSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
