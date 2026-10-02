package com.example.aura.ui.search

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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.aura.domain.model.SearchResults
import com.example.aura.domain.model.Song
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraGlassBorderDefault
import com.example.aura.theme.AuraGlassSurfaceDefault
import com.example.aura.theme.AuraSurfaceBlack
import com.example.aura.theme.AuraTextDisabled
import com.example.aura.theme.AuraTextPrimary
import com.example.aura.theme.AuraTextSecondary
import com.example.aura.theme.AuraTextTertiary
import com.example.aura.theme.LocalAuraAccent
import com.example.aura.theme.auraPressable
import com.example.aura.ui.components.AuraAnimatedFavoriteButton
import com.example.aura.ui.components.AuraFallbackArtwork
import com.example.aura.ui.components.SongContextMenuBottomSheet

@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: SearchResults,
    playlists: List<Playlist> = emptyList(),
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit = { onQueryChange("") },
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onFolderClick: (FolderGroup) -> Unit = {},
    onGenreClick: (Genre) -> Unit = {},
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
    var selectedCategory by remember { mutableStateOf("All") }
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

    val categories = listOf("All", "Songs", "Albums", "Artists", "Genres", "Folders")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDeepBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Search Header Title
        Text(
            text = "SEARCH",
            color = AuraTextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Explore Library",
            color = AuraTextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.8).sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Translucent Glass Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CircleShape)
                .background(AuraGlassSurfaceDefault)
                .border(1.dp, AuraGlassBorderDefault, CircleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = AuraTextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                TextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Tracks, artists, albums, folders...",
                            color = AuraTextTertiary,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary,
                        cursorColor = currentAccent
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = onClearQuery,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = AuraTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) currentAccent else AuraGlassSurfaceDefault)
                        .border(
                            1.dp,
                            if (isSelected) currentAccent.copy(alpha = 0.5f) else AuraGlassBorderDefault,
                            CircleShape
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) AuraDeepBlack else AuraTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results or Empty State
        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AuraSurfaceBlack)
                            .border(1.dp, AuraGlassBorderDefault, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AuraTextTertiary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Global Library Search",
                        color = AuraTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Search across tracks, artists, albums, and local folders.",
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else if (searchResults.isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No results for \"$searchQuery\"",
                        color = AuraTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Check your spelling or try searching another title or artist.",
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Artists Section
                if ((selectedCategory == "All" || selectedCategory == "Artists") && searchResults.artists.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Artists (${searchResults.artists.size})")
                    }
                    items(searchResults.artists, key = { "artist_${it.id}" }) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onArtistClick(artist) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
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
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AuraTextTertiary, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = artist.name, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${artist.trackCount} tracks", color = AuraTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Albums Section
                if ((selectedCategory == "All" || selectedCategory == "Albums") && searchResults.albums.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Albums (${searchResults.albums.size})")
                    }
                    items(searchResults.albums, key = { "album_${it.id}" }) { album ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onAlbumClick(album) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AuraSurfaceBlack)
                                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(10.dp)),
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
                                        cornerRadius = 10.dp,
                                        iconSize = 22.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = album.title, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "${album.artist} • ${album.trackCount} tracks", color = AuraTextSecondary, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }

                // Songs Section
                if ((selectedCategory == "All" || selectedCategory == "Songs") && searchResults.songs.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Songs (${searchResults.songs.size})")
                    }
                    items(searchResults.songs, key = { "song_${it.id}" }) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .auraPressable(pressedScale = 0.985f) { onSongClick(song) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AuraSurfaceBlack)
                                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(10.dp)),
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

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = song.title, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (song.isLossless) {
                                        Text(
                                            text = "FLAC",
                                            color = Color(0xFF34D399),
                                            fontSize = 9.sp,
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

                // Folders Section
                if ((selectedCategory == "All" || selectedCategory == "Folders") && searchResults.folders.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Folders (${searchResults.folders.size})")
                    }
                    items(searchResults.folders, key = { "folder_${it.path}" }) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onFolderClick(folder) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AuraSurfaceBlack)
                                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = currentAccent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = folder.displayName, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${folder.trackCount} tracks", color = AuraTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Genres Section
                if ((selectedCategory == "All" || selectedCategory == "Genres") && searchResults.genres.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Genres (${searchResults.genres.size})")
                    }
                    items(searchResults.genres, key = { "genre_${it.name}" }) { genre ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onGenreClick(genre) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AuraSurfaceBlack)
                                    .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = currentAccent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = genre.name, color = AuraTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${genre.trackCount} tracks", color = AuraTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        color = AuraTextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}
