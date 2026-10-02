package com.example.aura.ui.cloud

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.domain.model.cloud.AudioQuality
import com.example.aura.domain.model.cloud.CloudCollection
import com.example.aura.domain.model.cloud.CloudTrack
import com.example.aura.domain.model.cloud.DownloadStatus
import com.example.aura.domain.model.cloud.DownloadTask
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraGlassBorderDefault
import com.example.aura.theme.AuraGlassSurfaceDefault
import com.example.aura.theme.AuraMotion
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraRadius
import com.example.aura.theme.AuraSoftBlack
import com.example.aura.theme.AuraSurfaceBlack
import com.example.aura.theme.AuraTextDisabled
import com.example.aura.theme.AuraTextPrimary
import com.example.aura.theme.AuraTextSecondary
import com.example.aura.theme.AuraTextTertiary
import com.example.aura.ui.viewmodel.CloudDownloaderViewModel

@Composable
fun CloudDownloaderScreen(
    viewModel: CloudDownloaderViewModel,
    onPlayDownloadedTrack: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val collection by viewModel.resolvedCollection.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val customExtension by viewModel.customExtensionUrl.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showExtensionDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Explore & Fetch",
        "Active Queue (${activeTasks.size})",
        "Completed (${completedTasks.size})"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDeepBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Subtitle & Custom Extension Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "LOSSLESS CLOUD ENGINE",
                        color = AuraPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Lossless FLAC & Spotify Resolver",
                        color = AuraTextSecondary,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = { showExtensionDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(AuraGlassSurfaceDefault, CircleShape)
                        .border(1.dp, AuraGlassBorderDefault, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Extension Settings",
                        tint = if (customExtension.isNotBlank()) AuraPrimary else AuraTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Tabs Header
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = AuraTextPrimary,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = AuraGlassBorderDefault) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = AuraPrimary,
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) AuraPrimary else AuraTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTabIndex) {
                0 -> ExploreFetchTab(
                    searchQuery = searchQuery,
                    isResolving = isResolving,
                    collection = collection,
                    selectedQuality = selectedQuality,
                    errorMessage = errorMessage,
                    onQueryChange = { viewModel.updateQuery(it) },
                    onPaste = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            viewModel.updateQuery(clip)
                            viewModel.resolveInput(clip)
                            focusManager.clearFocus()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.resolveInput(searchQuery)
                    },
                    onQualitySelect = { viewModel.setQuality(it) },
                    onDownloadTrack = { track ->
                        viewModel.downloadTrack(track, selectedQuality)
                        Toast.makeText(context, "Enqueued: ${track.title}", Toast.LENGTH_SHORT).show()
                        selectedTabIndex = 1
                    },
                    onDownloadAll = { tracks ->
                        viewModel.downloadAll(tracks, selectedQuality)
                        Toast.makeText(context, "Enqueued ${tracks.size} tracks", Toast.LENGTH_SHORT).show()
                        selectedTabIndex = 1
                    }
                )
                1 -> QueueTab(
                    tasks = activeTasks,
                    onPause = { viewModel.pauseDownload(it) },
                    onResume = { viewModel.resumeDownload(it) },
                    onCancel = { viewModel.cancelDownload(it) },
                    onRetry = { viewModel.retryDownload(it) }
                )
                2 -> CompletedTab(
                    tasks = completedTasks,
                    onPlayTrack = onPlayDownloadedTrack,
                    onClearAll = { viewModel.clearCompleted() }
                )
            }
        }

        // Extension Settings Dialog
        if (showExtensionDialog) {
            ExtensionSettingsDialog(
                currentEndpoint = customExtension,
                onDismiss = { showExtensionDialog = false },
                onSave = { endpoint ->
                    viewModel.setCustomExtension(endpoint)
                    showExtensionDialog = false
                    Toast.makeText(context, "Provider endpoint updated", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun ExploreFetchTab(
    searchQuery: String,
    isResolving: Boolean,
    collection: CloudCollection?,
    selectedQuality: AudioQuality,
    errorMessage: String?,
    onQueryChange: (String) -> Unit,
    onPaste: () -> Unit,
    onSearch: () -> Unit,
    onQualitySelect: (AudioQuality) -> Unit,
    onDownloadTrack: (CloudTrack) -> Unit,
    onDownloadAll: (List<CloudTrack>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            // Input Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(AuraRadius.Medium)),
                    placeholder = {
                        Text(
                            text = "Paste Spotify Track/Album URL or Song Name...",
                            color = AuraTextDisabled,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AuraPrimary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = AuraTextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = onPaste) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = AuraPrimary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AuraGlassSurfaceDefault,
                        unfocusedContainerColor = AuraGlassSurfaceDefault,
                        focusedBorderColor = AuraPrimary,
                        unfocusedBorderColor = AuraGlassBorderDefault,
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary,
                        cursorColor = AuraPrimary
                    ),
                    shape = RoundedCornerShape(AuraRadius.Medium)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quality Selector Bar
                Text(
                    text = "TARGET AUDIO FIDELITY",
                    color = AuraTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AudioQuality.values()) { quality ->
                        val isSelected = quality == selectedQuality
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) AuraPrimary.copy(alpha = 0.2f) else AuraGlassSurfaceDefault,
                            animationSpec = tween(AuraMotion.DurationQuick, easing = FastOutSlowInEasing)
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) AuraPrimary else AuraGlassBorderDefault,
                            animationSpec = tween(AuraMotion.DurationQuick)
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AuraRadius.Pill))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(AuraRadius.Pill))
                                .clickable { onQualitySelect(quality) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (isSelected) AuraPrimary else AuraTextDisabled,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = quality.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AuraPrimary else AuraTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fetch Action Button
                Button(
                    onClick = onSearch,
                    enabled = searchQuery.isNotBlank() && !isResolving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(AuraRadius.Medium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraPrimary,
                        disabledContainerColor = AuraPrimary.copy(alpha = 0.3f),
                        contentColor = AuraDeepBlack
                    )
                ) {
                    if (isResolving) {
                        CircularProgressIndicator(
                            color = AuraDeepBlack,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Resolving Hi-Fi Metadata...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetch & Resolve Tracks", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1212).copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(AuraRadius.Medium),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color.Red.copy(alpha = 0.5f), Color.Red.copy(alpha = 0.2f))))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFFF5252))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = errorMessage, color = AuraTextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        // Collection Results
        if (collection != null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CollectionHeaderCard(
                    collection = collection,
                    selectedQuality = selectedQuality,
                    onDownloadAll = { onDownloadAll(collection.tracks) }
                )
            }

            items(collection.tracks) { track ->
                CloudTrackRow(
                    track = track,
                    targetQuality = selectedQuality,
                    onDownload = { onDownloadTrack(track) }
                )
            }
        } else if (!isResolving && errorMessage == null) {
            item {
                EmptyExplorePlaceholder(
                    onSampleQuery = { sample ->
                        onQueryChange(sample)
                        onSearch()
                    }
                )
            }
        }
    }
}

@Composable
private fun CollectionHeaderCard(
    collection: CloudCollection,
    selectedQuality: AudioQuality,
    onDownloadAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AuraSurfaceBlack),
        shape = RoundedCornerShape(AuraRadius.Large),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AuraPrimary.copy(alpha = 0.4f), AuraGlassBorderDefault)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = collection.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(AuraRadius.Small))
                        .background(AuraSoftBlack),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.title,
                        color = AuraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${collection.creator} • ${collection.totalTracks} Tracks",
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Source: ${collection.type.uppercase()}",
                        color = AuraPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (collection.tracks.size > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDownloadAll,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AuraRadius.Medium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraGlassSurfaceDefault,
                        contentColor = AuraPrimary
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AuraPrimary, AuraGlassBorderDefault)))
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Batch Download All (${collection.tracks.size}) in ${selectedQuality.badge}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudTrackRow(
    track: CloudTrack,
    targetQuality: AudioQuality,
    onDownload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(AuraRadius.Medium))
            .background(AuraGlassSurfaceDefault)
            .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(AuraRadius.Medium))
            .clickable { onDownload() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(AuraRadius.Small))
                .background(AuraSoftBlack),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = AuraTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist + if (track.album.isNotBlank()) " • ${track.album}" else "",
                color = AuraTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.durationMs > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${track.formattedDuration} • ${targetQuality.badge}",
                    color = AuraPrimary,
                    fontSize = 11.sp
                )
            }
        }

        IconButton(
            onClick = onDownload,
            modifier = Modifier
                .size(38.dp)
                .background(AuraPrimary.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, AuraPrimary.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download Track",
                tint = AuraPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun QueueTab(
    tasks: List<DownloadTask>,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
    onRetry: (String) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = AuraTextDisabled,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Downloads",
                    color = AuraTextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Search or paste links in Explore to download lossless audio.",
                    color = AuraTextDisabled,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                DownloadTaskCard(
                    task = task,
                    onPause = { onPause(task.id) },
                    onResume = { onResume(task.id) },
                    onCancel = { onCancel(task.id) },
                    onRetry = { onRetry(task.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DownloadTaskCard(
    task: DownloadTask,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    val progressAnimated by animateFloatAsState(
        targetValue = task.progress,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "dl_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AuraGlassSurfaceDefault),
        shape = RoundedCornerShape(AuraRadius.Medium),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AuraGlassBorderDefault, AuraPrimary.copy(alpha = 0.2f))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = task.track.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(AuraRadius.Small))
                        .background(AuraSoftBlack),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.track.title,
                        color = AuraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${task.track.artist} • ${task.targetQuality.badge}",
                        color = AuraTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (task.status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.RESOLVING, DownloadStatus.TAGGING -> {
                            IconButton(onClick = onPause, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = AuraTextSecondary)
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            IconButton(onClick = onResume, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = AuraPrimary)
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = AuraPrimary)
                            }
                        }
                        else -> {}
                    }

                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Cancel", tint = AuraTextDisabled)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (task.status == DownloadStatus.FAILED) Color(0xFFFF5252) else AuraPrimary,
                trackColor = AuraGlassBorderDefault
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status details & speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusText = when (task.status) {
                    DownloadStatus.QUEUED -> "Queued in background..."
                    DownloadStatus.RESOLVING -> "Resolving Lossless Stream..."
                    DownloadStatus.DOWNLOADING -> "Downloading (${(task.progress * 100).toInt()}%)"
                    DownloadStatus.TAGGING -> "Injecting High-Res Tags & Art..."
                    DownloadStatus.COMPLETED -> "Ready in AURA Library"
                    DownloadStatus.PAUSED -> "Paused"
                    DownloadStatus.FAILED -> task.errorMessage ?: "Failed"
                    DownloadStatus.CANCELLED -> "Cancelled"
                }

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = if (task.status == DownloadStatus.FAILED) Color(0xFFFF5252) else AuraPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (task.status == DownloadStatus.DOWNLOADING && task.speedBytesPerSec > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = AuraTextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.formattedSpeed} • ${task.formattedSize}",
                            fontSize = 11.sp,
                            color = AuraTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedTab(
    tasks: List<DownloadTask>,
    onPlayTrack: (String) -> Unit,
    onClearAll: () -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AuraTextDisabled,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Downloaded Tracks Yet",
                    color = AuraTextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Completed lossless audio files will appear here and in your AURA Library.",
                    color = AuraTextDisabled,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tasks.size} COMPLETED TRACKS",
                        color = AuraTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = onClearAll) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = AuraTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear List", color = AuraTextSecondary, fontSize = 12.sp)
                    }
                }
            }

            items(tasks, key = { it.id }) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(AuraRadius.Medium))
                        .background(AuraGlassSurfaceDefault)
                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(AuraRadius.Medium))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = task.track.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(AuraRadius.Small))
                            .background(AuraSoftBlack),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.track.title,
                            color = AuraTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${task.track.artist} • ${task.targetQuality.badge}",
                            color = AuraTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Saved to /Music/Aura", color = Color(0xFF4CAF50), fontSize = 11.sp)
                        }
                    }

                    if (!task.outputFilePath.isNullOrBlank()) {
                        IconButton(
                            onClick = { onPlayTrack(task.outputFilePath) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(AuraPrimary, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraDeepBlack, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyExplorePlaceholder(
    onSampleQuery: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = AuraPrimary.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Audiophile Cloud Acquisition",
            color = AuraTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Paste any Spotify track/album link or search artists to retrieve lossless FLAC audio with synchronized tags.",
            color = AuraTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "TRY SAMPLE QUERIES",
            color = AuraTextDisabled,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val samples = listOf("Daft Punk - Get Lucky", "Pink Floyd - Time", "Hans Zimmer - Interstellar")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(samples) { sample ->
                Text(
                    text = sample,
                    color = AuraPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AuraRadius.Pill))
                        .background(AuraGlassSurfaceDefault)
                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(AuraRadius.Pill))
                        .clickable { onSampleQuery(sample) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ExtensionSettingsDialog(
    currentEndpoint: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var endpoint by remember { mutableStateOf(currentEndpoint) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lossless Provider Settings",
                color = AuraTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Configure custom SpotiFLAC-compatible extension or lossless mirror endpoint URL:",
                    color = AuraTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    placeholder = { Text("https://your-extension-mirror.api/resolve", color = AuraTextDisabled, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraPrimary,
                        unfocusedBorderColor = AuraGlassBorderDefault,
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(endpoint) },
                colors = ButtonDefaults.buttonColors(containerColor = AuraPrimary, contentColor = AuraDeepBlack)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AuraTextSecondary)
            }
        },
        containerColor = AuraSurfaceBlack
    )
}
