package com.example.aura.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.domain.model.exclusion.ExcludedFolderRule
import com.example.aura.domain.model.exclusion.FilenamePatternRule
import com.example.aura.domain.model.exclusion.HiddenTrackItem
import com.example.aura.domain.model.exclusion.SmartScanSettings
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraOutlineVariant
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSecondary
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerLow
import com.example.aura.theme.LocalAuraAccent
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExcludedItemsScreen(
    settings: SmartScanSettings,
    onBack: () -> Unit,
    onToggleFolder: (path: String, isEnabled: Boolean) -> Unit,
    onAddFolder: (path: String, displayName: String) -> Unit,
    onRemoveFolder: (path: String) -> Unit,
    onToggleExtension: (extension: String, isExcluded: Boolean) -> Unit,
    onSetMinDuration: (seconds: Int) -> Unit,
    onAddFilenamePattern: (pattern: String) -> Unit,
    onRemoveFilenamePattern: (pattern: String) -> Unit,
    onToggleFilenamePattern: (pattern: String, isEnabled: Boolean) -> Unit,
    onUnhideTrack: (sourceUriOrId: String) -> Unit,
    onRestoreAllHiddenTracks: () -> Unit,
    onApplyCleanPreset: () -> Unit,
    onRescanLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeAccent = LocalAuraAccent.current

    var newPatternText by remember { mutableStateOf("") }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val path = it.path ?: it.toString()
            val name = it.lastPathSegment?.substringAfterLast(':') ?: "Custom Folder"
            onAddFolder(path, name)
            Toast.makeText(context, "Added excluded folder: $name", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AuraOnSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Library Exclusions",
                    color = AuraOnSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Smart filtering to keep music library clean",
                    color = AuraOnSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            // Quick clean preset button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(activeAccent.copy(alpha = 0.15f))
                    .clickable {
                        onApplyCleanPreset()
                        Toast.makeText(context, "Applied Clean Music Library preset", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Preset",
                    tint = activeAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Clean Preset",
                    color = activeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(color = AuraOutlineVariant.copy(alpha = 0.3f))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Safety Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AuraSurfaceContainerLow)
                        .border(1.dp, AuraOutlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Safe Filter",
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Safe Non-Destructive Filtering",
                                color = AuraOnSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Exclusions only hide audio from AURA. Files on device storage, playlists, favorites, and ratings remain completely safe and untouched.",
                                color = AuraOnSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // SECTION 1: DURATION FILTER
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(
                        title = "Short-Duration Audio Filter",
                        icon = Icons.Default.HourglassBottom,
                        badge = if (settings.minDurationSeconds > 0) "< ${settings.minDurationSeconds}s ignored" else "Off"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Automatically filter out ringtones, notification chimes, UI sounds, and voice clips shorter than threshold using actual media duration.",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val presets = listOf(
                        0 to "Off",
                        5 to "5s",
                        10 to "10s",
                        15 to "15s",
                        20 to "20s",
                        30 to "30s",
                        60 to "60s"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presets.forEach { (sec, label) ->
                            val isSelected = settings.minDurationSeconds == sec
                            FilterChipItem(
                                label = label,
                                isSelected = isSelected,
                                activeColor = activeAccent,
                                onClick = { onSetMinDuration(sec) }
                            )
                        }
                    }
                }
            }

            // SECTION 2: EXCLUDED FOLDERS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(
                            title = "Excluded Folders (${settings.excludedFolders.count { it.isEnabled }}/${settings.excludedFolders.size})",
                            icon = Icons.Default.FolderOff
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeAccent.copy(alpha = 0.15f))
                                    .clickable { folderPickerLauncher.launch(null) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Pick Folder", tint = activeAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Pick", color = activeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Folders and all their subfolders are ignored before metadata extraction to maximize scan speed.",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (settings.excludedFolders.isEmpty()) {
                        EmptyCardMessage(
                            icon = Icons.Default.Folder,
                            message = "No excluded folders. Tap 'Clean Preset' or 'Pick' to add folders."
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            settings.excludedFolders.forEach { folderRule ->
                                FolderExclusionRow(
                                    rule = folderRule,
                                    activeAccent = activeAccent,
                                    onToggle = { onToggleFolder(folderRule.path, it) },
                                    onDelete = { onRemoveFolder(folderRule.path) }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: FILE FORMATS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(
                        title = "Supported File Formats & Extensions",
                        icon = Icons.Default.Tune
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uncheck extensions you want AURA to ignore during scanning and playback.",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val allSupportedFormats = SmartScanSettings.SUPPORTED_EXTENSIONS

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (ext in allSupportedFormats) {
                            val isExcluded = settings.excludedExtensions.contains(ext.lowercase())
                            val isIncluded = !isExcluded
                            FormatToggleChip(
                                extension = ext.uppercase(),
                                isIncluded = isIncluded,
                                activeColor = activeAccent,
                                onToggle = {
                                    onToggleExtension(ext, isIncluded)
                                }
                            )
                        }
                    }
                }
            }

            // SECTION 4: ADVANCED FILENAME PATTERNS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(
                        title = "Advanced Filename Rules (${settings.filenamePatterns.size})",
                        icon = Icons.Default.Search
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Case-insensitive keywords and wildcard rules (e.g., 'recording', 'WhatsApp*', 'VID_*', 'AUD_*').",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPatternText,
                            onValueChange = { newPatternText = it },
                            placeholder = { Text("Add pattern (e.g. Ringtone*, voice)", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeAccent,
                                unfocusedBorderColor = AuraOutlineVariant,
                                focusedTextColor = AuraOnSurface,
                                unfocusedTextColor = AuraOnSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newPatternText.isNotBlank()) {
                                    onAddFilenamePattern(newPatternText.trim())
                                    newPatternText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(activeAccent)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Pattern", tint = AuraOnPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (settings.filenamePatterns.isEmpty()) {
                        EmptyCardMessage(
                            icon = Icons.Default.Search,
                            message = "No pattern rules defined. Add keywords or wildcards above."
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            settings.filenamePatterns.forEach { patternRule ->
                                PatternRuleRow(
                                    rule = patternRule,
                                    activeAccent = activeAccent,
                                    onToggle = { onToggleFilenamePattern(patternRule.pattern, it) },
                                    onDelete = { onRemoveFilenamePattern(patternRule.pattern) }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 5: MANUALLY HIDDEN TRACKS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(
                            title = "Hidden Tracks (${settings.hiddenTracks.size})",
                            icon = Icons.Default.MusicOff
                        )
                        if (settings.hiddenTracks.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraSurfaceContainerHigh)
                                    .clickable {
                                        onRestoreAllHiddenTracks()
                                        Toast.makeText(context, "Restored all hidden tracks", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = "Restore All", tint = activeAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Restore All", color = activeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tracks hidden directly via the song context menu (⋮ → Hide from Library).",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (settings.hiddenTracks.isEmpty()) {
                        EmptyCardMessage(
                            icon = Icons.Default.MusicNote,
                            message = "No individual tracks hidden. Use ⋮ → 'Hide from Library' on any song to hide it."
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            settings.hiddenTracks.forEach { track ->
                                HiddenTrackRow(
                                    track = track,
                                    activeAccent = activeAccent,
                                    onRestore = {
                                        onUnhideTrack(track.sourceUri)
                                        Toast.makeText(context, "Restored \"${track.title}\"", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 6: RESCAN ACTION
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onRescanLibrary()
                        Toast.makeText(context, "Scanning library with exclusion rules...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = AuraOnPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Rescan Library with Current Rules", color = AuraOnPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: ImageVector,
    badge: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = LocalAuraAccent.current,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = AuraOnSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        if (badge != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = badge,
                color = LocalAuraAccent.current,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) activeColor else AuraSurfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) AuraOnPrimary else AuraOnSurface,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun FormatToggleChip(
    extension: String,
    isIncluded: Boolean,
    activeColor: Color,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isIncluded) activeColor.copy(alpha = 0.18f) else AuraSurfaceContainerLow)
            .border(
                width = 1.dp,
                color = if (isIncluded) activeColor.copy(alpha = 0.5f) else AuraOutlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isIncluded) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isIncluded) "Included" else "Excluded",
                tint = if (isIncluded) activeColor else AuraOnSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = extension,
                color = if (isIncluded) AuraOnSurface else AuraOnSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = if (isIncluded) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun FolderExclusionRow(
    rule: ExcludedFolderRule,
    activeAccent: Color,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AuraSurfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (rule.isEnabled) Icons.Default.FolderOff else Icons.Default.Folder,
            contentDescription = "Folder",
            tint = if (rule.isEnabled) activeAccent else AuraOnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = rule.displayName,
                    color = if (rule.isEnabled) AuraOnSurface else AuraOnSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (rule.isPreset) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Preset",
                        color = activeAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(activeAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = rule.path,
                color = AuraOnSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AuraOnPrimary,
                    checkedTrackColor = activeAccent,
                    uncheckedThumbColor = AuraOnSurfaceVariant,
                    uncheckedTrackColor = AuraSurfaceContainerHigh
                ),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = AuraOutline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PatternRuleRow(
    rule: FilenamePatternRule,
    activeAccent: Color,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AuraSurfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.pattern,
                color = if (rule.isEnabled) AuraOnSurface else AuraOnSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (rule.isWildcard()) "Wildcard pattern match" else "Keyword contains match",
                color = AuraOnSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AuraOnPrimary,
                    checkedTrackColor = activeAccent,
                    uncheckedThumbColor = AuraOnSurfaceVariant,
                    uncheckedTrackColor = AuraSurfaceContainerHigh
                ),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = AuraOutline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HiddenTrackRow(
    track: HiddenTrackItem,
    activeAccent: Color,
    onRestore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AuraSurfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Track",
            tint = AuraOnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = AuraOnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.artist} • ${track.filePath ?: track.sourceUri}",
                color = AuraOnSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRestore) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = "Restore Track",
                tint = activeAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyCardMessage(
    icon: ImageVector,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AuraSurfaceContainerLow)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraOnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                color = AuraOnSurfaceVariant,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
