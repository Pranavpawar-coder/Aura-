package com.example.aura.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import com.example.aura.data.local.UserPreferences
import com.example.aura.data.local.entity.MusicFolderEntity
import com.example.aura.domain.model.settings.AnimationSettings
import com.example.aura.domain.model.settings.AppearanceSettings
import com.example.aura.domain.model.settings.BluetoothGestureSettings
import com.example.aura.domain.model.settings.GesturesSettings
import com.example.aura.domain.model.settings.HapticSettings
import com.example.aura.domain.model.settings.LyricsDisplaySettings
import com.example.aura.domain.model.settings.PlaybackBehaviorSettings
import com.example.aura.domain.model.settings.SettingsCategory
import com.example.aura.domain.model.settings.SettingsSearchResult
import com.example.aura.theme.LocalAuraAccent
import com.example.aura.theme.LocalAuraArtworkCornerRadius
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraGlassBorderDefault
import com.example.aura.theme.AuraGlassHighlightDefault
import com.example.aura.theme.AuraGlassSurfaceDefault
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSecondary
import com.example.aura.theme.AuraSoftBlack
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceBlack
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerHighest
import com.example.aura.theme.AuraSurfaceContainerLow
import com.example.aura.theme.AuraTextDisabled
import com.example.aura.theme.AuraTextPrimary
import com.example.aura.theme.AuraTextSecondary
import com.example.aura.theme.AuraTextTertiary
import com.example.aura.theme.auraPressable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    folders: List<MusicFolderEntity> = emptyList(),
    userPreferences: UserPreferences? = null,
    playbackDspSettings: com.example.aura.domain.model.audio.PlaybackSettings = com.example.aura.domain.model.audio.PlaybackSettings(),
    onUpdatePlaybackSettings: (com.example.aura.domain.model.audio.PlaybackSettings) -> Unit = {},
    onAddFolder: (Uri) -> Unit = {},
    onRemoveFolder: (MusicFolderEntity) -> Unit = {},
    onRescanFolder: (MusicFolderEntity) -> Unit = {},
    onRescanLibrary: () -> Unit = {},
    onOpenAudioEffects: () -> Unit = {},
    onExportBackup: (Uri, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onImportBackup: (Uri, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onOpenStats: () -> Unit = {},
    onAttachLyricsFile: (Uri) -> Unit = {},
    onRefreshLyrics: () -> Unit = {},
    onClearLyricsForCurrentSong: () -> Unit = {},
    onClearLyricsCache: () -> Unit = {},
    isScanning: Boolean,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeAccent = LocalAuraAccent.current

    var searchQuery by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf<SettingsCategory?>(null) }

    // Collect DataStore settings flows for live and persistent reactive updates
    val appearance by (userPreferences?.appearanceFlow ?: flowOf(AppearanceSettings()))
        .collectAsState(initial = AppearanceSettings())
    val lyricsSettings by (userPreferences?.lyricsDisplayFlow ?: flowOf(LyricsDisplaySettings()))
        .collectAsState(initial = LyricsDisplaySettings())
    val playbackSettings by (userPreferences?.playbackBehaviorFlow ?: flowOf(PlaybackBehaviorSettings()))
        .collectAsState(initial = PlaybackBehaviorSettings())
    val bluetoothSettings by (userPreferences?.bluetoothGestureFlow ?: flowOf(BluetoothGestureSettings()))
        .collectAsState(initial = BluetoothGestureSettings())
    val animationSettings by (userPreferences?.animationFlow ?: flowOf(AnimationSettings()))
        .collectAsState(initial = AnimationSettings())
    val gesturesSettings by (userPreferences?.gesturesFlow ?: flowOf(GesturesSettings()))
        .collectAsState(initial = GesturesSettings())
    val hapticSettings by (userPreferences?.hapticFlow ?: flowOf(HapticSettings()))
        .collectAsState(initial = HapticSettings())
    val notificationSettings by (userPreferences?.notificationFlow ?: flowOf(com.example.aura.domain.model.settings.NotificationSettings()))
        .collectAsState(initial = com.example.aura.domain.model.settings.NotificationSettings())
    val smartScanSettings by (userPreferences?.smartScanSettingsFlow ?: flowOf(com.example.aura.domain.model.exclusion.SmartScanSettings()))
        .collectAsState(initial = com.example.aura.domain.model.exclusion.SmartScanSettings())
    var showExcludedItemsScreen by remember { mutableStateOf(false) }

    BackHandler(enabled = showExcludedItemsScreen) {
        showExcludedItemsScreen = false
    }
    BackHandler(enabled = activeCategory != null && !showExcludedItemsScreen) {
        activeCategory = null
    }

    // Document Pickers
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { onAddFolder(it) }
    }

    val lyricsFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onAttachLyricsFile(it)
            Toast.makeText(context, "Imported lyrics file", Toast.LENGTH_SHORT).show()
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            onExportBackup(it) { success ->
                val msg = if (success) "Backup exported successfully!" else "Backup export failed."
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onImportBackup(it) { success ->
                val msg = if (success) "Backup restored successfully!" else "Failed to restore backup."
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Curated accent colors
    val accentColors = remember {
        listOf(
            "#7C5CFC" to "Violet",
            "#3B82F6" to "Blue",
            "#06B6D4" to "Cyan",
            "#10B981" to "Emerald",
            "#F59E0B" to "Amber",
            "#EF4444" to "Crimson",
            "#EC4899" to "Pink"
        )
    }

    // Settings Search Index for quick navigation
    val searchIndex = remember {
        listOf(
            SettingsSearchResult("Equalizer & Parametric EQ", "10-band presets, Q-factor, parametric filters", SettingsCategory.AUDIO, "eq"),
            SettingsSearchResult("4D Spatial Audio", "Head tracking, 3D width, depth, rotation", SettingsCategory.AUDIO, "4d"),
            SettingsSearchResult("Bass & Treble", "Bass boost, high-shelf treble control", SettingsCategory.AUDIO, "bass"),
            SettingsSearchResult("Channel Balance & Limiter", "Left/right channel balance, peak limiter", SettingsCategory.AUDIO, "balance"),
            SettingsSearchResult("Compressor & Reverb", "Dynamic range compression, acoustics simulation", SettingsCategory.AUDIO, "comp"),
            SettingsSearchResult("Gapless Playback", "Eliminates silence between album tracks", SettingsCategory.PLAYBACK, "gapless"),
            SettingsSearchResult("Crossfade Duration", "Smooth fade duration between tracks", SettingsCategory.PLAYBACK, "crossfade"),
            SettingsSearchResult("Auto-Play on Launch", "Resume playback on launch", SettingsCategory.PLAYBACK, "autoplay"),
            SettingsSearchResult("Intelligent Shuffle", "Avoids recently played tracks", SettingsCategory.PLAYBACK, "shuffle"),
            SettingsSearchResult("Lyrics Source & Online Search", "Automatic search, LRCLIB API, local and embedded LRC", SettingsCategory.LYRICS, "lyrics_source"),
            SettingsSearchResult("Lyrics Typography & Sizing", "Font size, line spacing, text alignment, highlight", SettingsCategory.LYRICS, "lyrics_typo"),
            SettingsSearchResult("Lyrics Timing & Offset", "Global and per-song synchronization offset calibration", SettingsCategory.LYRICS, "lyrics_offset"),
            SettingsSearchResult("Clear Lyrics Cache", "Clear saved lyrics from local storage", SettingsCategory.LYRICS, "lyrics_cache"),
            SettingsSearchResult("Bluetooth Gestures", "Earbuds tap actions & headset controls", SettingsCategory.BLUETOOTH, "bt_gestures"),
            SettingsSearchResult("Audio Device Profiles", "Auto-switch EQ for wired, bluetooth, DAC", SettingsCategory.BLUETOOTH, "profiles"),
            SettingsSearchResult("Theme (System, Light, Dark)", "Active system or custom light/dark theme", SettingsCategory.APPEARANCE, "theme"),
            SettingsSearchResult("Accent Color Palette", "Custom vibrant UI accent color", SettingsCategory.APPEARANCE, "accent"),
            SettingsSearchResult("Player Artwork Style", "Corner radius, squircle, adaptive background", SettingsCategory.APPEARANCE, "artwork_style"),
            SettingsSearchResult("Player Swipe & Tap Gestures", "Swipe to skip, artwork tap actions", SettingsCategory.GESTURES, "gestures"),
            SettingsSearchResult("Music Folders", "Select and scan local music folders", SettingsCategory.LIBRARY, "folders"),
            SettingsSearchResult("Library Exclusions & Smart Scan", "Ignore unwanted folders, ringtones, file types, patterns", SettingsCategory.LIBRARY, "exclusions"),
            SettingsSearchResult("Clean Music Library Preset", "Automatically exclude recordings, ringtones, WhatsApp audio", SettingsCategory.LIBRARY, "clean_preset"),
            SettingsSearchResult("Short-Duration Audio Filter", "Ignore tracks shorter than 5s, 10s, 30s", SettingsCategory.LIBRARY, "duration_filter"),
            SettingsSearchResult("Excluded File Types", "Ignore AMR, OPUS, OGG, or other file formats", SettingsCategory.LIBRARY, "file_types"),
            SettingsSearchResult("Hidden Songs & Tracks", "Restore or manage songs hidden from library", SettingsCategory.LIBRARY, "hidden_tracks"),
            SettingsSearchResult("Rescan Library", "Scan storage for newly added songs", SettingsCategory.LIBRARY, "rescan"),
            SettingsSearchResult("Notification Controls", "Playback notification customization", SettingsCategory.NOTIFICATIONS, "notif"),
            SettingsSearchResult("Listening Statistics", "Total listening time, top artists, local insights", SettingsCategory.STORAGE, "stats"),
            SettingsSearchResult("Storage & Database", "Clear lyrics cache, rebuild library database", SettingsCategory.STORAGE, "storage"),
            SettingsSearchResult("Privacy & Offline", "Zero tracking, offline-first operation", SettingsCategory.PRIVACY, "privacy"),
            SettingsSearchResult("Backup & Restore", "Export & restore playlists, favorites, audio EQ, settings JSON", SettingsCategory.BACKUP, "backup"),
            SettingsSearchResult("Accessibility & Haptics", "Haptic vibrations, high contrast, reduced motion", SettingsCategory.ACCESSIBILITY, "accessibility"),
            SettingsSearchResult("Performance & Animations", "Spring physics, artwork quality, battery optimization", SettingsCategory.PERFORMANCE, "perf"),
            SettingsSearchResult("Diagnostics & Engine", "Media3 audio pipeline, buffer health, debug stats", SettingsCategory.ADVANCED, "advanced"),
            SettingsSearchResult("About AURA", "Version 1.0, Media3 engine, licenses", SettingsCategory.ABOUT, "about")
        )
    }

    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else searchIndex.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true) ||
            it.category.title.contains(searchQuery, ignoreCase = true)
        }
    }

    // Excluded Items Full Sub-Screen
    if (showExcludedItemsScreen) {
        ExcludedItemsScreen(
            settings = smartScanSettings,
            onBack = { showExcludedItemsScreen = false },
            onToggleFolder = { path, isEnabled ->
                coroutineScope.launch { userPreferences?.toggleExcludedFolder(path, isEnabled) }
            },
            onAddFolder = { path, displayName ->
                coroutineScope.launch { userPreferences?.addExcludedFolder(path, displayName) }
            },
            onRemoveFolder = { path ->
                coroutineScope.launch { userPreferences?.removeExcludedFolder(path) }
            },
            onToggleExtension = { ext, isExcluded ->
                coroutineScope.launch { userPreferences?.toggleExcludedExtension(ext, isExcluded) }
            },
            onSetMinDuration = { seconds ->
                coroutineScope.launch { userPreferences?.setMinDurationSeconds(seconds) }
            },
            onAddFilenamePattern = { pattern ->
                coroutineScope.launch { userPreferences?.addFilenamePattern(pattern) }
            },
            onRemoveFilenamePattern = { pattern ->
                coroutineScope.launch { userPreferences?.removeFilenamePattern(pattern) }
            },
            onToggleFilenamePattern = { pattern, isEnabled ->
                coroutineScope.launch { userPreferences?.toggleFilenamePattern(pattern, isEnabled) }
            },
            onUnhideTrack = { sourceUriOrId ->
                coroutineScope.launch { userPreferences?.unhideTrack(sourceUriOrId) }
            },
            onRestoreAllHiddenTracks = {
                coroutineScope.launch { userPreferences?.restoreAllHiddenTracks() }
            },
            onApplyCleanPreset = {
                coroutineScope.launch { userPreferences?.applyCleanMusicLibraryPreset() }
            },
            onRescanLibrary = onRescanLibrary
        )
        return
    }

    // Detail Bottom Sheet for Active Category
    if (activeCategory != null) {
        val cat = activeCategory!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { activeCategory = null },
            sheetState = sheetState,
            containerColor = AuraSurfaceBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Category Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(activeAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = cat.title, color = AuraOnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = cat.subtitle, color = AuraOnSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Detail Content per Category
                when (cat) {
                    SettingsCategory.AUDIO -> {
                        SettingsClickableItem(
                            title = "Open DSP Audio Effects Suite",
                            subtitle = "Equalizer, Parametric EQ, 4D Spatial Audio, Bass, Treble, Compressor, Reverb",
                            icon = Icons.Default.GraphicEq,
                            onClick = {
                                activeCategory = null
                                onOpenAudioEffects()
                            }
                        )
                        SettingsToggleItem(
                            title = "Gapless Playback Engine",
                            subtitle = "Pre-buffers audio chunks to eliminate silence between tracks",
                            checked = playbackDspSettings.gaplessEnabled,
                            onCheckedChange = {
                                onUpdatePlaybackSettings(playbackDspSettings.copy(gaplessEnabled = it))
                            }
                        )
                    }

                    SettingsCategory.PLAYBACK -> {
                        SettingsToggleItem(
                            title = "Gapless Playback",
                            subtitle = "Eliminates silence between continuous tracks",
                            checked = playbackDspSettings.gaplessEnabled,
                            onCheckedChange = {
                                onUpdatePlaybackSettings(playbackDspSettings.copy(gaplessEnabled = it))
                            }
                        )
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Crossfade Duration", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (playbackDspSettings.crossfadeEnabled) "${playbackDspSettings.crossfadeDurationSeconds}s" else "Off",
                                    color = activeAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = if (playbackDspSettings.crossfadeEnabled) playbackDspSettings.crossfadeDurationSeconds.toFloat() else 0f,
                                onValueChange = { v ->
                                    val secs = v.toInt()
                                    onUpdatePlaybackSettings(
                                        playbackDspSettings.copy(
                                            crossfadeEnabled = secs > 0,
                                            crossfadeDurationSeconds = secs.coerceAtLeast(1)
                                        )
                                    )
                                },
                                valueRange = 0f..15f,
                                steps = 14,
                                colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                            )
                        }
                        SettingsToggleItem(
                            title = "Auto-Play on Launch",
                            subtitle = "Automatically resumes music when AURA starts",
                            checked = playbackSettings.autoPlayOnLaunch,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updatePlaybackBehavior(playbackSettings.copy(autoPlayOnLaunch = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Resume Last Position",
                            subtitle = "Restores track and timestamp from previous session",
                            checked = playbackSettings.resumeLastPosition,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updatePlaybackBehavior(playbackSettings.copy(resumeLastPosition = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Pause on Disconnect",
                            subtitle = "Pauses playback when headphones are disconnected",
                            checked = playbackSettings.pauseOnDisconnect,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updatePlaybackBehavior(playbackSettings.copy(pauseOnDisconnect = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Resume on Connect",
                            subtitle = "Resumes playback when headphones reconnect",
                            checked = playbackSettings.resumeOnConnect,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updatePlaybackBehavior(playbackSettings.copy(resumeOnConnect = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Intelligent Shuffle",
                            subtitle = "Weights playback queue to avoid recently played tracks",
                            checked = playbackSettings.intelligentShuffle,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updatePlaybackBehavior(playbackSettings.copy(intelligentShuffle = it))
                                }
                            }
                        )
                    }

                    // ADVANCED LYRICS SETTINGS ORGANIZED INTO 6 STRUCTURED SECTIONS
                    SettingsCategory.LYRICS -> {
                        // 1. Lyrics Sources
                        SettingsSectionHeader("Lyrics Sources")

                        SettingsToggleItem(
                            title = "Embedded lyrics",
                            subtitle = "Read synchronized or unsynchronized lyrics from audio file metadata tags",
                            checked = lyricsSettings.embeddedLyricsSupport,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(embeddedLyricsSupport = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Local .lrc",
                            subtitle = "Read synchronized .lrc files located alongside audio tracks",
                            checked = lyricsSettings.localLyricsSupport,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(localLyricsSupport = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Local .txt",
                            subtitle = "Read plain-text lyrics .txt files alongside audio tracks",
                            checked = lyricsSettings.localTxtSupport,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(localTxtSupport = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Cached lyrics",
                            subtitle = "Use previously downloaded lyrics stored in local cache",
                            checked = lyricsSettings.cachedLyricsSupport,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(cachedLyricsSupport = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Online lyrics",
                            subtitle = "Fetch synchronized LRC lyrics from community databases (LRCLIB)",
                            checked = lyricsSettings.onlineLyricsEnabled,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(onlineLyricsEnabled = it))
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Automatic Lyrics
                        SettingsSectionHeader("Automatic Lyrics")

                        SettingsToggleItem(
                            title = "Automatic search",
                            subtitle = "Automatically search local, tag, and online sources upon playback",
                            checked = lyricsSettings.autoSearchOnline,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(autoSearchOnline = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Automatic download",
                            subtitle = "Automatically cache found online lyrics to device for offline playback",
                            checked = lyricsSettings.autoDownloadLyrics,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(autoDownloadLyrics = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Wi-Fi only",
                            subtitle = "Only search and download online lyrics while connected to Wi-Fi",
                            checked = lyricsSettings.wifiOnly,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(wifiOnly = it))
                                }
                            }
                        )
                        SettingsClickableItem(
                            title = "Refresh lyrics",
                            subtitle = "Force re-fetch lyrics for currently playing track",
                            icon = Icons.Default.Refresh,
                            onClick = {
                                onRefreshLyrics()
                                Toast.makeText(context, "Refreshing lyrics...", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Lyrics Display
                        SettingsSectionHeader("Lyrics Display")

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Font size", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "${lyricsSettings.fontSizeSp.toInt()} sp", color = activeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = lyricsSettings.fontSizeSp,
                                onValueChange = {
                                    coroutineScope.launch {
                                        userPreferences?.updateLyricsDisplay(lyricsSettings.copy(fontSizeSp = it))
                                    }
                                },
                                valueRange = 12f..32f,
                                steps = 19,
                                colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Line spacing", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "${lyricsSettings.lineSpacingSp.toInt()} sp", color = activeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = lyricsSettings.lineSpacingSp,
                                onValueChange = {
                                    coroutineScope.launch {
                                        userPreferences?.updateLyricsDisplay(lyricsSettings.copy(lineSpacingSp = it))
                                    }
                                },
                                valueRange = 4f..28f,
                                steps = 23,
                                colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(text = "Alignment", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Left", "Center", "Right").forEach { align ->
                                    val isSelected = lyricsSettings.textAlignment == align
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) activeAccent else AuraSurfaceContainerHigh)
                                            .clickable {
                                                coroutineScope.launch {
                                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(textAlignment = align))
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = align,
                                            color = if (isSelected) AuraOnPrimary else AuraOnSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        SettingsToggleItem(
                            title = "Current-line highlight",
                            subtitle = "Prominently accentuates the active synchronized lyric line",
                            checked = lyricsSettings.currentLineHighlight,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(currentLineHighlight = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Auto-scroll",
                            subtitle = "Automatically keeps current lyrics line centered during playback",
                            checked = lyricsSettings.autoScroll,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(autoScroll = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Animation",
                            subtitle = "Smooth kinetic scrolling transitions between lyric lines",
                            checked = lyricsSettings.lyricsAnimation,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(lyricsAnimation = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Reduced motion",
                            subtitle = "Disables sliding animations for high efficiency",
                            checked = lyricsSettings.reduceMotion,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateLyricsDisplay(lyricsSettings.copy(reduceMotion = it))
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Synchronization
                        SettingsSectionHeader("Synchronization")

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Global offset", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "${lyricsSettings.globalOffsetMs} ms", color = activeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = lyricsSettings.globalOffsetMs.toFloat(),
                                onValueChange = {
                                    coroutineScope.launch {
                                        userPreferences?.updateLyricsDisplay(lyricsSettings.copy(globalOffsetMs = it.toLong()))
                                    }
                                },
                                valueRange = -3000f..3000f,
                                steps = 59,
                                colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = "Reset Global Offset",
                                    color = activeAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            userPreferences?.updateLyricsDisplay(lyricsSettings.copy(globalOffsetMs = 0L))
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5. Storage
                        SettingsSectionHeader("Storage")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Lyrics cache size", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Offline cached lyrics storage", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                            }
                            Text(text = "Local Room DB", color = activeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        SettingsClickableItem(
                            title = "Clear lyrics cache",
                            subtitle = "Frees database space by removing all saved offline lyrics",
                            icon = Icons.Default.DeleteOutline,
                            onClick = {
                                onClearLyricsCache()
                                Toast.makeText(context, "Lyrics cache cleared", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6. Manual
                        SettingsSectionHeader("Manual")

                        SettingsClickableItem(
                            title = "Load lyrics file",
                            subtitle = "Pick and attach a local .lrc or .txt file to the current song",
                            icon = Icons.Default.Upload,
                            onClick = {
                                lyricsFilePickerLauncher.launch(arrayOf("text/*", "application/octet-stream", "*/*"))
                            }
                        )

                        SettingsClickableItem(
                            title = "Clear current lyrics",
                            subtitle = "Removes loaded lyrics for the current song",
                            icon = Icons.Default.Clear,
                            onClick = {
                                onClearLyricsForCurrentSong()
                                Toast.makeText(context, "Lyrics cleared for current song", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    SettingsCategory.BLUETOOTH -> {
                        SettingsSectionHeader("Earbuds & Headset Tap Gestures")
                        GestureOptionRow("Single Tap", bluetoothSettings.singleTapAction) {
                            coroutineScope.launch {
                                userPreferences?.updateBluetoothGestures(bluetoothSettings.copy(singleTapAction = it))
                            }
                        }
                        GestureOptionRow("Double Tap Left", bluetoothSettings.doubleTapLeftAction) {
                            coroutineScope.launch {
                                userPreferences?.updateBluetoothGestures(bluetoothSettings.copy(doubleTapLeftAction = it))
                            }
                        }
                        GestureOptionRow("Double Tap Right", bluetoothSettings.doubleTapRightAction) {
                            coroutineScope.launch {
                                userPreferences?.updateBluetoothGestures(bluetoothSettings.copy(doubleTapRightAction = it))
                            }
                        }
                        GestureOptionRow("Triple Tap", bluetoothSettings.tripleTapAction) {
                            coroutineScope.launch {
                                userPreferences?.updateBluetoothGestures(bluetoothSettings.copy(tripleTapAction = it))
                            }
                        }
                        GestureOptionRow("Long Press", bluetoothSettings.longPressAction) {
                            coroutineScope.launch {
                                userPreferences?.updateBluetoothGestures(bluetoothSettings.copy(longPressAction = it))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsToggleItem(
                            title = "Auto-Load Device Profiles",
                            subtitle = "Loads custom EQ & 4D settings when specific device connects",
                            checked = bluetoothSettings.autoLoadDeviceProfile,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateBluetoothGestures(bluetoothSettings.copy(autoLoadDeviceProfile = it))
                                }
                            }
                        )
                    }

                    // 7. FIX APPEARANCE SETTINGS (Theme, Accent, Player Appearance, Radius)
                    SettingsCategory.APPEARANCE -> {
                        SettingsSectionHeader("Theme Mode")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("System", "Dark", "Light").forEach { themeName ->
                                val selected = appearance.theme.equals(themeName, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) activeAccent else AuraSurfaceContainerHigh)
                                        .clickable {
                                            coroutineScope.launch {
                                                userPreferences?.updateAppearance(appearance.copy(theme = themeName))
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = themeName,
                                        color = if (selected) AuraOnPrimary else AuraOnSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        SettingsSectionHeader("Accent Color Palette")
                        Text(
                            text = "Select an accent color to theme buttons, sliders, icons, and indicators",
                            color = AuraOnSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            accentColors.forEach { (hex, name) ->
                                val color = remember(hex) {
                                    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(activeAccent)
                                }
                                val isSelected = appearance.accentColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable {
                                            coroutineScope.launch {
                                                userPreferences?.updateAppearance(appearance.copy(accentColorHex = hex))
                                            }
                                        }
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = name,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        SettingsSectionHeader("Player Appearance")

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(text = "Artwork Style", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Rounded", "Squircle", "Square").forEach { style ->
                                    val isSelected = appearance.artworkStyle == style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) activeAccent else AuraSurfaceContainerHigh)
                                            .clickable {
                                                val radius = when (style) {
                                                    "Square" -> 4
                                                    "Squircle" -> 16
                                                    else -> 24
                                                }
                                                coroutineScope.launch {
                                                    userPreferences?.updateAppearance(
                                                        appearance.copy(artworkStyle = style, playerArtworkCornerRadiusDp = radius)
                                                    )
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = style,
                                            color = if (isSelected) AuraOnPrimary else AuraOnSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Artwork Corner Radius", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "${appearance.playerArtworkCornerRadiusDp} dp", color = activeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = appearance.playerArtworkCornerRadiusDp.toFloat(),
                                onValueChange = {
                                    coroutineScope.launch {
                                        userPreferences?.updateAppearance(
                                            appearance.copy(playerArtworkCornerRadiusDp = it.toInt())
                                        )
                                    }
                                },
                                valueRange = 4f..36f,
                                steps = 31,
                                colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(text = "Background Style", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Adaptive Gradient", "Minimal Dark", "Deep Black").forEach { bgStyle ->
                                    val isSelected = appearance.backgroundStyle == bgStyle
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) activeAccent else AuraSurfaceContainerHigh)
                                            .clickable {
                                                coroutineScope.launch {
                                                    userPreferences?.updateAppearance(appearance.copy(backgroundStyle = bgStyle))
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = bgStyle,
                                            color = if (isSelected) AuraOnPrimary else AuraOnSurface,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SettingsCategory.GESTURES -> {
                        SettingsSectionHeader("Player Touch & Swipe Gestures")
                        // Read-only display of configured gesture actions from DataStore
                        val gestureRows = listOf(
                            "Swipe Left" to gesturesSettings.swipeLeftPlayer,
                            "Swipe Right" to gesturesSettings.swipeRightPlayer,
                            "Swipe Down" to "Minimize Full Player",
                            "Swipe Up" to "Open Playback Queue",
                            "Artwork Tap" to gesturesSettings.artworkTap,
                            "Artwork Double Tap" to gesturesSettings.artworkDoubleTap
                        )
                        gestureRows.forEach { (gesture, action) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = gesture, color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = action, color = activeAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AuraSurfaceContainerLow)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Gesture actions are fixed to AURA's core navigation. Custom gesture remapping is not available in this version.",
                                color = AuraOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    SettingsCategory.LIBRARY -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onRescanLibrary)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Rescan",
                                    tint = activeAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = if (isScanning) "Scanning Audio Files..." else "Rescan Device Music",
                                        color = AuraOnSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Search local storage for newly added tracks",
                                        color = AuraOnSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (isScanning) {
                                CircularProgressIndicator(color = activeAccent, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsSectionHeader("Smart Library Exclusions & Filtering")

                        // Excluded Items Screen entry
                        val activeExcludedFoldersCount = smartScanSettings.excludedFolders.count { it.isEnabled }
                        val hiddenCount = smartScanSettings.hiddenTracks.size
                        val durationDesc = if (smartScanSettings.minDurationSeconds > 0) "< ${smartScanSettings.minDurationSeconds}s" else "off"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showExcludedItemsScreen = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Exclusions",
                                    tint = activeAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Excluded Items & Smart Rules",
                                        color = AuraOnSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$activeExcludedFoldersCount folders • $hiddenCount hidden tracks • Duration: $durationDesc",
                                        color = AuraOnSurfaceVariant,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Exclusions",
                                tint = AuraOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Clean Preset Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraSurfaceContainerLow)
                                .clickable {
                                    coroutineScope.launch {
                                        userPreferences?.applyCleanMusicLibraryPreset()
                                        Toast.makeText(context, "Clean Music Library preset applied", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Clean Preset",
                                    tint = activeAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Clean Music Library (1-Tap Preset)",
                                        color = AuraOnSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Excludes Recordings, Ringtones, Notifications, WhatsApp Audio, and <10s clips",
                                        color = AuraOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Quick duration selector
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ignore Tracks Shorter Than",
                                    color = AuraOnSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (smartScanSettings.minDurationSeconds > 0) "${smartScanSettings.minDurationSeconds}s" else "Off",
                                    color = activeAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0 to "Off", 5 to "5s", 10 to "10s", 15 to "15s", 30 to "30s", 60 to "60s").forEach { (sec, label) ->
                                    val isSelected = smartScanSettings.minDurationSeconds == sec
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) activeAccent else AuraSurfaceContainerHigh)
                                            .clickable {
                                                coroutineScope.launch {
                                                    userPreferences?.setMinDurationSeconds(sec)
                                                }
                                            }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) AuraOnPrimary else AuraOnSurface,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        SettingsSectionHeader("Custom Music Folders (SAF)")

                        // Music Folders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Music Folders (${folders.size})", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(activeAccent.copy(alpha = 0.15f))
                                    .clickable { folderPickerLauncher.launch(null) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Folder", tint = activeAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Add Folder", color = activeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (folders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AuraSurfaceContainerLow)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "No custom folders added. AURA scans standard device audio storage by default.",
                                    color = AuraOnSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            folders.forEach { folder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AuraSurfaceContainerLow)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = folder.displayName, color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = "${folder.trackCount} songs scanned", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                                    }
                                    Row {
                                        IconButton(onClick = { onRescanFolder(folder) }) {
                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Rescan", tint = AuraOnSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { onRemoveFolder(folder) }) {
                                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Remove", tint = AuraOutline, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsCategory.NOTIFICATIONS -> {
                        SettingsSectionHeader("Media Notification")
                        SettingsToggleItem(
                            title = "Media Playback Controls",
                            subtitle = "Show play, pause, skip buttons on lock screen and notification shade",
                            checked = notificationSettings.showControls,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateNotificationSettings(notificationSettings.copy(showControls = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Seekbar in Notification",
                            subtitle = "Enables progress scrubber directly in system media notification",
                            checked = notificationSettings.showProgress,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateNotificationSettings(notificationSettings.copy(showProgress = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Favorite Button in Notification",
                            subtitle = "Quickly like or favorite the currently playing track",
                            checked = notificationSettings.showFavorite,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateNotificationSettings(notificationSettings.copy(showFavorite = it))
                                }
                            }
                        )
                    }

                    SettingsCategory.STORAGE -> {
                        SettingsClickableItem(
                            title = "Listening Statistics",
                            subtitle = "View total play time, most played songs, and listening history",
                            icon = Icons.Default.Storage,
                            onClick = {
                                onOpenStats()
                            }
                        )
                        SettingsClickableItem(
                            title = "Clear Cached Lyrics",
                            subtitle = "Frees space by removing downloaded LRC lyrics",
                            icon = Icons.Default.DeleteOutline,
                            onClick = {
                                onClearLyricsCache()
                                Toast.makeText(context, "Lyrics cache cleared", Toast.LENGTH_SHORT).show()
                            }
                        )
                        SettingsClickableItem(
                            title = "Rebuild Music Database",
                            subtitle = "Forces full rescan of device audio library",
                            icon = Icons.Default.Refresh,
                            onClick = {
                                onRescanLibrary()
                                Toast.makeText(context, "Scanning library...", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsSectionHeader("Backup & Restore")
                        SettingsClickableItem(
                            title = "Create JSON Backup",
                            subtitle = "Export playlists, favorites, audio EQ settings, and preferences to a JSON file",
                            icon = Icons.Default.Upload,
                            onClick = {
                                exportBackupLauncher.launch("aura_backup_${System.currentTimeMillis()}.json")
                            }
                        )
                        SettingsClickableItem(
                            title = "Restore Backup",
                            subtitle = "Import and restore data from a previously saved AURA JSON file",
                            icon = Icons.Default.Download,
                            onClick = {
                                importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                            }
                        )
                    }

                    SettingsCategory.PRIVACY -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraSurfaceContainerLow)
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(text = "100% Offline & Private", color = activeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "AURA operates entirely offline on your device. Zero telemetry, zero analytics tracking, and no music metadata or personal data is ever uploaded.",
                                    color = AuraOnSurfaceVariant,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // 6. NO 5-STAR RATING MENTION ANYWHERE IN BACKUP OR UI
                    SettingsCategory.BACKUP -> {
                        SettingsClickableItem(
                            title = "Create JSON Backup",
                            subtitle = "Export playlists, favorites, audio EQ settings, and preferences to a JSON file",
                            icon = Icons.Default.Upload,
                            onClick = {
                                exportBackupLauncher.launch("aura_backup_${System.currentTimeMillis()}.json")
                            }
                        )
                        SettingsClickableItem(
                            title = "Restore Backup",
                            subtitle = "Import and restore data from a previously saved AURA JSON file",
                            icon = Icons.Default.Download,
                            onClick = {
                                importBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            }
                        )
                    }

                    SettingsCategory.ACCESSIBILITY -> {
                        SettingsToggleItem(
                            title = "Haptic Feedback",
                            subtitle = "Tactile vibration when interacting with player controls",
                            checked = hapticSettings.hapticEnabled,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateHaptics(hapticSettings.copy(hapticEnabled = it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Reduced Motion",
                            subtitle = "Disables heavy spring animations for accessible interaction",
                            checked = animationSettings.reduceMotion,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateAnimations(animationSettings.copy(reduceMotion = it))
                                }
                            }
                        )
                    }

                    SettingsCategory.PERFORMANCE -> {
                        SettingsToggleItem(
                            title = "Enable Spring Animations",
                            subtitle = "Physically-modeled motion for player and navigation",
                            checked = !animationSettings.reduceMotion,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateAnimations(animationSettings.copy(reduceMotion = !it))
                                }
                            }
                        )
                        SettingsToggleItem(
                            title = "Live Audio Visualizer",
                            subtitle = "Renders real-time FFT visualizer animation in Now Playing",
                            checked = animationSettings.visualizerAnimationEnabled,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences?.updateAnimations(animationSettings.copy(visualizerAnimationEnabled = it))
                                }
                            }
                        )
                    }

                    SettingsCategory.ADVANCED -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraSurfaceContainerLow)
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(text = "Engine Diagnostics", color = activeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "• Audio Pipeline: Media3 ExoPlayer Direct", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                                Text(text = "• DSP Engine: 32-bit Float Biquad / Spatializer", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                                Text(text = "• Sample Rate: 48,000 Hz Native Stereo", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                                Text(text = "• Buffer Mode: Low-Latency Circular Lockless", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }

                    SettingsCategory.ABOUT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(AuraSurfaceBlack)
                                .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(24.dp))
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent.copy(alpha = 0.12f))
                                        .border(2.dp, activeAccent.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.aura.R.drawable.aura_logo),
                                        contentDescription = "AURA Logo",
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "AURA Music Player",
                                    color = AuraOnSurface,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "A cinematic, offline-first music player designed for a clean and immersive listening experience.",
                                    color = AuraOnSurfaceVariant,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Version & Build Cards
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AuraSurfaceContainerHigh)
                                            .padding(vertical = 10.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "VERSION", color = AuraTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "1.0.0", color = activeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AuraSurfaceContainerHigh)
                                            .padding(vertical = 10.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "BUILD", color = AuraTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "2026.1", color = activeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Features
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AuraSurfaceContainerLow)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "FEATURES",
                                        color = activeAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val features = listOf(
                                        "Local music playback (MP3, FLAC, WAV, M4A, OGG)",
                                        "Synchronized & offline lyrics (LRCLIB & embedded LRC)",
                                        "Smart playlists, favorites, and queue management",
                                        "Artwork-reactive interface & dynamic theming",
                                        "10-Band Parametric Equalizer & sound presets",
                                        "AURA 4D Spatial Audio & acoustics engine",
                                        "Background playback with lock screen media controls"
                                    )
                                    features.forEach { feature ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(text = "•", color = activeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = feature, color = AuraOnSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Offline & Privacy
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AuraSurfaceContainerLow)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "OFFLINE & PRIVACY",
                                        color = activeAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "AURA operates completely offline. Your music library, playback history, statistics, and preferences are stored exclusively on your device. Zero telemetry, zero analytics.",
                                        color = AuraOnSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(1.dp)
                                        .background(AuraGlassBorderDefault)
                                )
                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Created by Pranav",
                                    color = AuraTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Main Settings Screen List
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(AuraDeepBlack),
        contentPadding = PaddingValues(bottom = 120.dp, top = 14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Text(
                    text = "PREFERENCES",
                    color = AuraTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Settings",
                    color = AuraTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp
                )
            }
        }

        // Search Bar Glass Capsule
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(AuraGlassSurfaceDefault)
                    .border(1.dp, AuraGlassBorderDefault, CircleShape)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AuraTextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = AuraTextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(activeAccent),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Search settings (EQ, lyrics, theme, backup...)", color = AuraTextTertiary, fontSize = 13.sp)
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = AuraTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // If searching, display search results
        if (searchQuery.isNotBlank()) {
            if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No settings matching '$searchQuery'", color = AuraTextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                items(searchResults) { result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                searchQuery = ""
                                activeCategory = result.category
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(activeAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(result.category),
                                    contentDescription = null,
                                    tint = activeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = result.title, color = AuraTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = result.subtitle, color = AuraTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = AuraTextTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            // Profile & Tier Glass Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraSurfaceBlack)
                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.aura.R.drawable.aura_logo),
                            contentDescription = "AURA Logo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "AURA Master",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Audiophile Tier • Bit-Perfect Direct",
                                color = activeAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 8 Core Settings Sections
            val primaryCategories = listOf(
                SettingsCategory.PLAYBACK,
                SettingsCategory.AUDIO,
                SettingsCategory.LYRICS,
                SettingsCategory.APPEARANCE,
                SettingsCategory.LIBRARY,
                SettingsCategory.NOTIFICATIONS,
                SettingsCategory.STORAGE,
                SettingsCategory.ABOUT
            )
            items(primaryCategories) { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 3.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AuraSurfaceBlack)
                        .border(1.dp, AuraGlassBorderDefault, RoundedCornerShape(16.dp))
                        .auraPressable(pressedScale = 0.985f) { activeCategory = category }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(activeAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = category.title,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = category.title,
                                color = AuraTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = category.subtitle,
                                color = AuraTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = AuraTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Subtle signature at end of Settings
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Created by Pranav",
                        color = AuraTextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = LocalAuraAccent.current,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun GestureOptionRow(
    label: String,
    currentAction: String,
    onSelectAction: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val activeAccent = LocalAuraAccent.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = AuraOnSurface, fontSize = 13.sp)
        Text(text = currentAction, color = activeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }

    if (expanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AuraSurfaceContainerHigh)
                .padding(8.dp)
        ) {
            BluetoothGestureSettings.AVAILABLE_ACTIONS.forEach { action ->
                Text(
                    text = action,
                    color = if (action == currentAction) activeAccent else AuraOnSurface,
                    fontSize = 12.sp,
                    fontWeight = if (action == currentAction) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectAction(action)
                            expanded = false
                        }
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun getCategoryIcon(category: SettingsCategory): ImageVector {
    return when (category) {
        SettingsCategory.AUDIO -> Icons.Default.GraphicEq
        SettingsCategory.PLAYBACK -> Icons.Default.PlayCircle
        SettingsCategory.LYRICS -> Icons.Default.Mic
        SettingsCategory.BLUETOOTH -> Icons.Default.Headphones
        SettingsCategory.APPEARANCE -> Icons.Default.Brush
        SettingsCategory.GESTURES -> Icons.Default.TouchApp
        SettingsCategory.LIBRARY -> Icons.Default.LibraryMusic
        SettingsCategory.NOTIFICATIONS -> Icons.Default.Notifications
        SettingsCategory.STORAGE -> Icons.Default.Storage
        SettingsCategory.PRIVACY -> Icons.Default.Security
        SettingsCategory.BACKUP -> Icons.Default.Upload
        SettingsCategory.ACCESSIBILITY -> Icons.Default.TouchApp
        SettingsCategory.PERFORMANCE -> Icons.Default.Speed
        SettingsCategory.ADVANCED -> Icons.Default.Tune
        SettingsCategory.ABOUT -> Icons.Default.Info
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val activeAccent = LocalAuraAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = AuraOnSurfaceVariant, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AuraOnPrimary,
                checkedTrackColor = activeAccent,
                uncheckedThumbColor = AuraOutline,
                uncheckedTrackColor = AuraSurfaceContainerHighest
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val activeAccent = LocalAuraAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = activeAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
            }
            Column {
                Text(text = title, color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, color = AuraOnSurfaceVariant, fontSize = 12.sp)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open",
            tint = AuraOutline,
            modifier = Modifier.size(18.dp)
        )
    }
}
