package com.example.aura.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aura.domain.model.Song
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraOutlineVariant
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh

@Composable
fun TrackInfoDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(AuraSurfaceContainer)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AuraSurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AuraPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Track Information",
                            color = AuraOnSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AuraOutline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Section
                InfoSectionHeader(title = "METADATA")
                InfoRow(label = "Title", value = song.title)
                InfoRow(label = "Artist", value = song.artist)
                InfoRow(label = "Album", value = song.album)
                song.albumArtist?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Album Artist", value = it)
                }
                song.genre?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Genre", value = it)
                }
                song.year?.let {
                    InfoRow(label = "Year", value = it.toString())
                }
                song.trackNumber?.let {
                    InfoRow(label = "Track Number", value = it.toString())
                }
                InfoRow(label = "Duration", value = song.formattedDuration)

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AuraOutlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Audio Fidelity Section
                InfoSectionHeader(title = "AUDIO FIDELITY")
                InfoRow(label = "Format / Codec", value = song.codec ?: song.mimeType?.substringAfter('/')?.uppercase() ?: "Unknown")
                if (song.sampleRate > 0) {
                    InfoRow(label = "Sample Rate", value = "${song.sampleRate / 1000.0} kHz (${song.sampleRate} Hz)")
                }
                if (song.bitDepth > 0) {
                    InfoRow(label = "Bit Depth", value = "${song.bitDepth}-bit")
                }
                if (song.bitrate > 0) {
                    InfoRow(label = "Bitrate", value = "${song.bitrate} kbps")
                }
                InfoRow(
                    label = "Channels",
                    value = when (song.channelCount) {
                        1 -> "Mono (1 Channel)"
                        2 -> "Stereo (2 Channels)"
                        6 -> "5.1 Surround"
                        8 -> "7.1 Surround"
                        else -> "${song.channelCount} Channels"
                    }
                )
                InfoRow(
                    label = "Encoding",
                    value = if (song.isLossless) "Lossless" else "Lossy"
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AuraOutlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // File Details Section
                InfoSectionHeader(title = "FILE DETAILS")
                if (song.fileSize > 0) {
                    val sizeMb = song.fileSize / (1024.0 * 1024.0)
                    InfoRow(label = "File Size", value = String.format("%.2f MB", sizeMb))
                }
                song.filePath?.let {
                    InfoRow(label = "Location", value = it)
                } ?: InfoRow(label = "URI", value = song.mediaUri)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InfoSectionHeader(title: String) {
    Text(
        text = title,
        color = AuraPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = AuraOnSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            color = AuraOnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}
