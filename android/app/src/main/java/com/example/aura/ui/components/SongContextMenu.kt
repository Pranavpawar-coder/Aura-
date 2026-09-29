package com.example.aura.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.domain.model.Playlist
import com.example.aura.domain.model.Song
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutlineVariant
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSecondary
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongContextMenuBottomSheet(
    song: Song,
    playlists: List<Playlist> = emptyList(),
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onHideSong: (() -> Unit)? = null,
    onHideFolder: ((folderPath: String) -> Unit)? = null,
    onHideSimilar: ((pattern: String) -> Unit)? = null
) {
    var showTrackInfo by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showTrackInfo) {
        TrackInfoDialog(song = song, onDismiss = { showTrackInfo = false })
    }

    if (showPlaylistPicker) {
        PlaylistSelectionDialog(
            song = song,
            playlists = playlists,
            onSelectPlaylist = {
                onAddToPlaylist(it)
                showPlaylistPicker = false
                onDismiss()
            },
            onCreatePlaylist = {
                onCreatePlaylist(it)
                showPlaylistPicker = false
                onDismiss()
            },
            onDismiss = { showPlaylistPicker = false }
        )
    }

    if (showHideDialog) {
        HideFromLibraryDialog(
            song = song,
            onDismiss = {
                showHideDialog = false
                onDismiss()
            },
            onHideSong = {
                onHideSong?.invoke()
                showHideDialog = false
                onDismiss()
            },
            onHideFolder = { folder ->
                onHideFolder?.invoke(folder)
                showHideDialog = false
                onDismiss()
            },
            onHideSimilar = { pattern ->
                onHideSimilar?.invoke(pattern)
                showHideDialog = false
                onDismiss()
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AuraSurfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Song Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                            cornerRadius = 12.dp,
                            iconSize = 26.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = AuraOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    song.qualityBadge?.let { badge ->
                        Text(
                            text = badge,
                            color = AuraSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = AuraOutlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Items
            ContextMenuItem(
                icon = Icons.Default.PlayArrow,
                title = "Play Now",
                onClick = {
                    onPlay()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                title = "Play Next",
                onClick = {
                    onPlayNext()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.Queue,
                title = "Add to Queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                title = "Add to Playlist",
                onClick = {
                    showPlaylistPicker = true
                }
            )

            ContextMenuItem(
                icon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = if (song.isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = if (song.isFavorite) AuraPrimary else AuraOnSurfaceVariant,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.Info,
                title = "Song Information",
                onClick = {
                    showTrackInfo = true
                }
            )

            ContextMenuItem(
                icon = Icons.Default.VisibilityOff,
                title = "Hide from Library",
                tint = AuraOnSurfaceVariant,
                onClick = {
                    showHideDialog = true
                }
            )
        }
    }
}

@Composable
fun HideFromLibraryDialog(
    song: Song,
    onDismiss: () -> Unit,
    onHideSong: () -> Unit,
    onHideFolder: (String) -> Unit,
    onHideSimilar: (String) -> Unit
) {
    val folderPath = remember(song.filePath) {
        song.filePath?.let { File(it).parent }
    }
    val folderDisplayName = remember(folderPath) {
        if (!folderPath.isNullOrBlank()) File(folderPath).name.ifBlank { folderPath } else "Current Folder"
    }

    val suggestedPattern = remember(song.title, song.filePath) {
        val name = song.filePath?.let { File(it).nameWithoutExtension } ?: song.title
        when {
            name.startsWith("AUD-", ignoreCase = true) -> "AUD-*"
            name.startsWith("VID-", ignoreCase = true) -> "VID-*"
            name.startsWith("WhatsApp", ignoreCase = true) -> "WhatsApp*"
            name.startsWith("Recording", ignoreCase = true) -> "Recording*"
            name.startsWith("Voice", ignoreCase = true) -> "Voice*"
            name.contains("-") -> name.substringBefore("-").trim() + "*"
            name.contains("_") -> name.substringBefore("_").trim() + "*"
            else -> name.take(12) + "*"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(AuraSurfaceContainer)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Hide from Library",
                            tint = AuraPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hide from Library",
                            color = AuraOnSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AuraOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hidden items won't appear in AURA. Files remain untouched on device storage and can be restored anytime in Settings → Library → Excluded Items.",
                    color = AuraOnSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Option 1: Hide this song
                HideOptionCard(
                    icon = Icons.Default.MusicNote,
                    title = "Hide this song",
                    subtitle = "Only hides \"${song.title}\"",
                    onClick = onHideSong
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Hide this folder
                if (!folderPath.isNullOrBlank()) {
                    HideOptionCard(
                        icon = Icons.Default.FolderOff,
                        title = "Hide this folder",
                        subtitle = "Hides all tracks in \"$folderDisplayName\"",
                        onClick = { onHideFolder(folderPath) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Option 3: Hide similar files
                HideOptionCard(
                    icon = Icons.Default.FilterAlt,
                    title = "Hide similar files",
                    subtitle = "Hides tracks matching \"$suggestedPattern\"",
                    onClick = { onHideSimilar(suggestedPattern) }
                )
            }
        }
    }
}

@Composable
private fun HideOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AuraSurfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AuraPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AuraOnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = AuraOnSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    title: String,
    tint: androidx.compose.ui.graphics.Color = AuraOnSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = AuraOnSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
