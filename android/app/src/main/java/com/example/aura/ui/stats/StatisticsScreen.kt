package com.example.aura.ui.stats

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.domain.model.ListeningStatistics
import com.example.aura.domain.model.Song
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSecondary
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh

import androidx.activity.compose.BackHandler

@Composable
fun StatisticsScreen(
    stats: ListeningStatistics,
    onSongClick: (Song) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmClear by remember { mutableStateOf(false) }

    BackHandler(enabled = showConfirmClear) {
        showConfirmClear = false
    }
    BackHandler(enabled = !showConfirmClear, onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDeepBlack)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraOnSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Listening Statistics",
                    color = AuraOnSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { showConfirmClear = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear History",
                    tint = AuraOnSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Listening Time",
                        value = stats.formattedTotalTime,
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Tracks Played",
                        value = "${stats.totalTracksPlayed}",
                        icon = Icons.Default.Headphones,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Top Items Cards
            item {
                Text(
                    text = "Your Top Music",
                    color = AuraOnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (stats.mostPlayedTrack != null) {
                item {
                    TopRankItem(
                        category = "Top Track",
                        title = stats.mostPlayedTrack.title,
                        subtitle = stats.mostPlayedTrack.artist,
                        icon = Icons.Default.MusicNote
                    )
                }
            }

            if (stats.mostPlayedArtist != null) {
                item {
                    TopRankItem(
                        category = "Top Artist",
                        title = stats.mostPlayedArtist.first,
                        subtitle = "${stats.mostPlayedArtist.second} plays recorded",
                        icon = Icons.Default.Person
                    )
                }
            }

            if (stats.mostPlayedAlbum != null) {
                item {
                    TopRankItem(
                        category = "Top Album",
                        title = stats.mostPlayedAlbum.first,
                        subtitle = "${stats.mostPlayedAlbum.second} plays recorded",
                        icon = Icons.Default.Album
                    )
                }
            }

            if (stats.favoriteGenre != null) {
                item {
                    TopRankItem(
                        category = "Top Genre",
                        title = stats.favoriteGenre.first,
                        subtitle = "${stats.favoriteGenre.second} plays",
                        icon = Icons.Default.ThumbUp
                    )
                }
            }

            // Local Smart Recommendations
            if (stats.localRecommendations.isNotEmpty()) {
                item {
                    Text(
                        text = "Because you listen frequently",
                        color = AuraOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(stats.localRecommendations) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraSurfaceContainer)
                            .clickable { onSongClick(song) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AuraPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = AuraPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = AuraOnSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${song.artist} • ${song.album}",
                                color = AuraOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            containerColor = AuraSurfaceContainer,
            title = { Text("Clear Statistics?", color = AuraOnSurface, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will reset your local listening time and play counters. Your music files and playlists will not be affected.",
                    color = AuraOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showConfirmClear = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraPrimary, contentColor = AuraOnPrimary)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel", color = AuraOnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AuraSurfaceContainer)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AuraPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = value, color = AuraOnSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = title, color = AuraOnSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun TopRankItem(
    category: String,
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AuraSurfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AuraPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = category.uppercase(), color = AuraPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = AuraOnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = AuraOnSurfaceVariant, fontSize = 12.sp)
        }
    }
}
