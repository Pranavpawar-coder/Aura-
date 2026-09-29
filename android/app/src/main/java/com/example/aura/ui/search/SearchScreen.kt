package com.example.aura.ui.search

import androidx.compose.foundation.background
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
import com.example.aura.theme.auraPressable
import com.example.aura.ui.components.AuraFallbackArtwork
import com.example.aura.domain.model.Album
import com.example.aura.domain.model.Artist
import com.example.aura.domain.model.FolderGroup
import com.example.aura.domain.model.Genre
import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.SearchResults
import com.example.aura.domain.model.Song
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
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
            .background(AuraSurface)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(CircleShape)
                .background(AuraSurfaceContainer.copy(alpha = 0.85f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = AuraOnSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )

                TextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search songs, albums, artists, genres...",
                            color = AuraOutline,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = AuraOnSurface,
                        unfocusedTextColor = AuraOnSurface,
                        cursorColor = AuraPrimary
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
                            tint = AuraOnSurfaceVariant,
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
                        .background(if (isSelected) AuraPrimary else AuraSurfaceContainerHigh)
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) AuraOnPrimary else AuraOnSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
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
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AuraOutline.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Global Library Search",
                        color = AuraOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Search across tracks, artists, albums, and local folders.",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
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
                        text = "No results found for \"$searchQuery\"",
                        color = AuraOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Check your spelling or try searching another title, album or artist.",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
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
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onArtistClick(artist) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AuraSurfaceContainerHigh),
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
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AuraOutline, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = artist.name, color = AuraOnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${artist.trackCount} tracks", color = AuraOnSurfaceVariant, fontSize = 12.sp)
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
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onAlbumClick(album) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraSurfaceContainerHigh),
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
                                        cornerRadius = 8.dp,
                                        iconSize = 22.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = album.title, color = AuraOnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "${album.artist} • ${album.trackCount} tracks", color = AuraOnSurfaceVariant, fontSize = 12.sp, maxLines = 1)
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
                                .clip(RoundedCornerShape(12.dp))
                                .auraPressable(pressedScale = 0.98f) { onSongClick(song) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                Text(
                                    text = song.title,
                                    color = AuraOnSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${song.artist} • ${song.album}",
                                    color = AuraOnSurfaceVariant,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

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

                // Folders Section
                if ((selectedCategory == "All" || selectedCategory == "Folders") && searchResults.folders.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Folders (${searchResults.folders.size})")
                    }
                    items(searchResults.folders, key = { "folder_${it.path}" }) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onFolderClick(folder) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraSurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = AuraPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = folder.displayName, color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${folder.trackCount} tracks", color = AuraOnSurfaceVariant, fontSize = 12.sp)
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
        color = AuraPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
    )
}
