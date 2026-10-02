package com.example.aura.ui.home

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
import com.example.aura.ui.components.AuraGlassControl
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
import androidx.compose.ui.text.font.FontFamily
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
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraElevated1
import com.example.aura.theme.AuraGlassBorderDefault
import com.example.aura.theme.AuraGlassHighlightDefault
import com.example.aura.theme.AuraGlassSurfaceDefault
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSoftBlack
import com.example.aura.theme.AuraSurfaceBlack
import com.example.aura.theme.AuraTextDisabled
import com.example.aura.theme.AuraTextPrimary
import com.example.aura.theme.AuraTextSecondary
import com.example.aura.theme.AuraTextTertiary
import com.example.aura.theme.LocalAuraAccent
import com.example.aura.theme.auraPressable
import com.example.aura.ui.components.AnimatedEqualizerBars
import com.example.aura.ui.components.AuraFallbackArtwork
import com.example.aura.ui.components.AuraGlassButton
import com.example.aura.ui.components.AuraGlassSurface
import com.example.aura.ui.components.SongContextMenuBottomSheet
import java.util.Calendar

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
    val currentAccent = LocalAuraAccent.current
    var menuSong by remember { mutableStateOf<Song?>(null) }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
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

    if (songs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AuraDeepBlack)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Atmospheric background glow
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(currentAccent.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
            )

            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = currentAccent,
                        modifier = Modifier.size(44.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "DISCOVERING LOCAL AUDIO",
                        color = AuraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Indexing lossless and local files...",
                        color = AuraTextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                AuraGlassSurface(
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, AuraGlassBorderDefault, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = currentAccent,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Offline Sanctuary",
                            color = AuraTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Uncompressed local audio playing with native AndroidX Media3.",
                            color = AuraTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        AuraGlassControl(
                            onClick = onScanMusic,
                            backgroundColor = currentAccent,
                            borderColor = currentAccent
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = AuraDeepBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan Device Music",
                                color = AuraDeepBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            .background(AuraDeepBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Atmospheric Top Hero Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting.uppercase(),
                            color = AuraTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Listen Now",
                            color = AuraTextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp
                        )
                    }

                    // Scan / Sync badge button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AuraGlassSurfaceDefault)
                            .border(1.dp, AuraGlassBorderDefault, CircleShape)
                            .auraPressable(onClick = onScanMusic)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = AuraTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Sync",
                                color = AuraTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Spotlight Hero Showcase Card
        item {
            val spotlightSong = recentlyPlayed.firstOrNull() ?: songs.first()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(AuraSurfaceBlack)
                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(26.dp))
                    .auraPressable(pressedScale = 0.985f) { onSongClick(spotlightSong) }
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .background(AuraElevated1)
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

                        // Gradient bottom vignette
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            AuraSurfaceBlack.copy(alpha = 0.2f),
                                            AuraSurfaceBlack
                                        )
                                    )
                                )
                        )

                        // Equalizer / Status badge top left
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .clip(CircleShape)
                                .background(AuraDeepBlack.copy(alpha = 0.70f))
                                .border(1.dp, AuraGlassBorderDefault, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AnimatedEqualizerBars(isPlaying = isPlaying)
                            Text(
                                text = if (isPlaying) "NOW PLAYING" else "FEATURED TRACK",
                                color = currentAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Lossless badge top right
                        spotlightSong.qualityBadge?.let { badge ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(14.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AuraDeepBlack.copy(alpha = 0.70f))
                                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badge.uppercase(),
                                    color = Color(0xFF34D399),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // Card Meta & Listen Now CTA
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = spotlightSong.title,
                            color = AuraTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${spotlightSong.artist} • ${spotlightSong.album}",
                            color = AuraTextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Listen Now Glass Pill
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(currentAccent)
                                    .clickable { onSongClick(spotlightSong) }
                                    .padding(horizontal = 18.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = AuraDeepBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play Now",
                                    color = AuraDeepBlack,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = spotlightSong.formattedDuration,
                                color = AuraTextTertiary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Section: Recently Played Carousel
        if (recentlyPlayed.isNotEmpty()) {
            item {
                HorizontalSongCarousel(
                    title = "Recently Played",
                    songs = recentlyPlayed,
                    onSongClick = onSongClick
                )
            }
        }

        // Section: Recently Added Carousel
        if (recentlyAdded.isNotEmpty()) {
            item {
                HorizontalSongCarousel(
                    title = "Recently Added",
                    songs = recentlyAdded,
                    onSongClick = onSongClick
                )
            }
        }

        // Section: Most Played Carousel
        if (mostPlayed.isNotEmpty()) {
            item {
                HorizontalSongCarousel(
                    title = "Most Played",
                    songs = mostPlayed,
                    onSongClick = onSongClick
                )
            }
        }

        // Section: Favorites Carousel
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
                    .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIBRARY",
                        color = AuraTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        text = "All Songs (${songs.size})",
                        color = AuraTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AuraGlassSurfaceDefault)
                        .border(1.dp, AuraGlassBorderDefault, CircleShape)
                        .auraPressable(onClick = onPlayAll)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play All",
                            tint = currentAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Play All",
                            color = currentAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Song Rows with Glass Aesthetic & Context Menu
        items(songs, key = { it.id }, contentType = { "song" }) { song ->
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
                    IconButton(onClick = { onToggleFavorite(song) }, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) currentAccent else AuraTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { menuSong = song }, modifier = Modifier.size(34.dp)) {
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
    }
}

@Composable
private fun HorizontalSongCarousel(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(modifier = Modifier.padding(top = 22.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = AuraTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(songs.take(20), key = { it.id }) { itemSong ->
                Column(
                    modifier = Modifier
                        .width(136.dp)
                        .auraPressable(pressedScale = 0.96f) { onSongClick(itemSong) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(AuraSurfaceBlack)
                            .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(18.dp))
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
                                cornerRadius = 18.dp,
                                iconSize = 42.dp
                            )
                        }
                        itemSong.qualityBadge?.let { badge ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AuraDeepBlack.copy(alpha = 0.75f))
                                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge.uppercase(),
                                    color = Color(0xFF34D399),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = itemSong.title,
                        color = AuraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = itemSong.artist,
                        color = AuraTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
