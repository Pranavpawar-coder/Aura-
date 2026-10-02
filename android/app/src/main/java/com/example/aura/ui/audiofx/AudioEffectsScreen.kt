package com.example.aura.ui.audiofx

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.domain.model.audio.AudioDeviceType
import com.example.aura.domain.model.audio.AudioEffectsState
import com.example.aura.domain.model.audio.EqualizerPreset
import com.example.aura.domain.model.audio.FilterType
import com.example.aura.domain.model.audio.ParametricBand
import com.example.aura.domain.model.audio.ReplayGainMode
import com.example.aura.domain.model.audio.ReverbPreset
import com.example.aura.domain.model.audio.SpatialMovementMode
import com.example.aura.domain.model.audio.SpatialPreset
import com.example.aura.domain.model.audio.VisualizerStyle
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceBlack
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerHighest
import com.example.aura.ui.components.AuraVisualizerView
import com.example.aura.ui.components.ParametricEqCurve
import com.example.aura.ui.components.SpatialPositionRadar
import com.example.aura.ui.viewmodel.PlayerViewModel

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEffectsScreen(
    viewModel: PlayerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val effectsState by viewModel.audioEffectsState.collectAsState()
    val visualizerData by viewModel.visualizerData.collectAsState()
    val sleepTimer by viewModel.sleepTimerSettings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("EQ & Tone", "4D Spatial", "Dynamics", "Reverb & Playback", "Visualizer", "Output Profile")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = AuraOnSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AUDIO EFFECTS",
                        color = AuraOnSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (effectsState.isAdvancedProcessingActive) "DSP Active • ${effectsState.activeBadgeLabel ?: "ON"}" else "DSP Inactive",
                        color = if (effectsState.isAdvancedProcessingActive) AuraPrimary else AuraOutline,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Reset All Button
            IconButton(onClick = { viewModel.resetEqualizer() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset EQ", tint = AuraOutline)
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = AuraDeepBlack,
            contentColor = AuraPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) AuraPrimary else AuraOnSurfaceVariant
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            when (selectedTab) {
                0 -> EqAndToneTab(effectsState = effectsState, viewModel = viewModel)
                1 -> Spatial4DTab(effectsState = effectsState, viewModel = viewModel)
                2 -> DynamicsTab(effectsState = effectsState, viewModel = viewModel)
                3 -> ReverbAndPlaybackTab(effectsState = effectsState, viewModel = viewModel, sleepTimer = sleepTimer)
                4 -> VisualizerTab(effectsState = effectsState, visualizerData = visualizerData, viewModel = viewModel)
                5 -> OutputProfileTab(effectsState = effectsState, viewModel = viewModel)
            }
        }
    }
}

// ==========================================
// 1. EQUALIZER & TONE TAB
// ==========================================
@Composable
private fun EqAndToneTab(
    effectsState: AudioEffectsState,
    viewModel: PlayerViewModel
) {
    var selectedPeqBandId by remember { mutableStateOf<String?>(null) }
    var showPresetMenu by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Standard Graphic EQ Section
            EffectSectionCard(
                title = "Graphic Equalizer",
                enabled = effectsState.equalizer.enabled,
                onToggle = { viewModel.setEqualizerEnabled(it) }
            ) {
                // Preset Selector & Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Preset", color = AuraOnSurfaceVariant, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { viewModel.resetEqualizer() },
                            enabled = effectsState.equalizer.enabled
                        ) {
                            Text(text = "Reset", color = if (effectsState.equalizer.enabled) AuraPrimary else AuraOutline, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box {
                            Button(
                                onClick = { showPresetMenu = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceContainerHigh)
                            ) {
                                Text(text = effectsState.equalizer.preset.displayName, color = AuraPrimary, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = showPresetMenu,
                                onDismissRequest = { showPresetMenu = false },
                                modifier = Modifier.background(AuraSurfaceContainerHigh)
                            ) {
                                EqualizerPreset.values().forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset.displayName, color = AuraOnSurface, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.setEqualizerPreset(preset)
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preamp & Headroom Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preamp / Gain: ${if (effectsState.equalizer.preampGainDb >= 0) "+" else ""}${String.format("%.1f", effectsState.equalizer.preampGainDb)} dB",
                        color = AuraOnSurface,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (effectsState.equalizer.autoHeadroomEnabled) "Auto Headroom: Active" else "Headroom: Off",
                        color = if (effectsState.equalizer.autoHeadroomEnabled) AuraPrimary else AuraOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Slider(
                    value = effectsState.equalizer.preampGainDb,
                    onValueChange = { viewModel.setEqualizerPreamp(it) },
                    valueRange = -10f..10f,
                    enabled = effectsState.equalizer.enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Equalizer Preamp gain ${String.format("%.1f", effectsState.equalizer.preampGainDb)} dB"
                        },
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Graphic EQ Bands with Accessibility
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    effectsState.equalizer.bands.forEach { band ->
                        val freqLabel = if (band.centerFreqHz >= 1000) "${band.centerFreqHz / 1000}k" else "${band.centerFreqHz}"
                        val gainText = "${if (band.gainDb >= 0) "+" else ""}${(band.gainDb).toInt()}dB"
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = gainText,
                                fontSize = 10.sp,
                                color = if (effectsState.equalizer.enabled) AuraPrimary else AuraOutline
                            )
                            Slider(
                                value = band.gainMb.toFloat(),
                                onValueChange = { viewModel.setEqualizerBandGain(band.index, it.toInt()) },
                                valueRange = -1500f..1500f,
                                modifier = Modifier
                                    .height(130.dp)
                                    .semantics {
                                        contentDescription = "Equalizer band $freqLabel, gain $gainText"
                                    },
                                enabled = effectsState.equalizer.enabled,
                                colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                            )
                            Text(text = freqLabel, fontSize = 11.sp, color = AuraOnSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parametric EQ Section with Real Frequency Response Curve
            EffectSectionCard(
                title = "Parametric Equalizer",
                enabled = effectsState.parametric.enabled,
                onToggle = { viewModel.setParametricEnabled(it) }
            ) {
                // Interactive Frequency Response Curve
                ParametricEqCurve(
                    bands = effectsState.parametric.bands,
                    accentColor = AuraPrimary,
                    selectedBandId = selectedPeqBandId,
                    onBandDragged = { id, freq, gain ->
                        effectsState.parametric.bands.find { it.id == id }?.let { b ->
                            viewModel.updateParametricBand(b.copy(frequencyHz = freq, gainDb = gain))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Filters (${effectsState.parametric.bands.size})", color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        viewModel.addParametricBand(
                            ParametricBand(frequencyHz = 2000f, gainDb = 3f, qFactor = 1.2f, filterType = FilterType.PEAKING)
                        )
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Filter", tint = AuraPrimary)
                    }
                }

                // Filter band chips/controls
                effectsState.parametric.bands.forEachIndexed { idx, band ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPeqBandId == band.id) AuraPrimary.copy(alpha = 0.15f) else AuraSurfaceContainerHigh)
                            .clickable { selectedPeqBandId = band.id }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Band ${idx + 1}: ${band.filterType.displayName}", color = AuraOnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${band.frequencyHz.toInt()} Hz • ${"%.1f".format(band.gainDb)} dB • Q=${"%.1f".format(band.qFactor)}", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = band.enabled,
                                onCheckedChange = { viewModel.updateParametricBand(band.copy(enabled = it)) },
                                modifier = Modifier.size(36.dp),
                                colors = SwitchDefaults.colors(checkedThumbColor = AuraPrimary, checkedTrackColor = AuraPrimary.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.removeParametricBand(band.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = AuraOutline, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dedicated Bass, Treble & Left/Right Balance
            EffectSectionCard(title = "Bass & Treble & Balance", enabled = true) {
                // Bass
                Text(text = "Bass: ${"%.1f".format(effectsState.bassTreble.bassLevel)} dB", color = AuraOnSurface, fontSize = 13.sp)
                Slider(
                    value = effectsState.bassTreble.bassLevel,
                    onValueChange = { viewModel.setBassTreble(it, effectsState.bassTreble.trebleLevel) },
                    valueRange = -10f..10f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                // Treble
                Text(text = "Treble: ${"%.1f".format(effectsState.bassTreble.trebleLevel)} dB", color = AuraOnSurface, fontSize = 13.sp)
                Slider(
                    value = effectsState.bassTreble.trebleLevel,
                    onValueChange = { viewModel.setBassTreble(effectsState.bassTreble.bassLevel, it) },
                    valueRange = -10f..10f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                // Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val balText = when {
                        effectsState.balance.balance < -0.05f -> "Left ${(-effectsState.balance.balance * 100).toInt()}%"
                        effectsState.balance.balance > 0.05f -> "Right ${(effectsState.balance.balance * 100).toInt()}%"
                        else -> "Center (0)"
                    }
                    Text(text = "Balance: $balText", color = AuraOnSurface, fontSize = 13.sp)
                    Button(
                        onClick = { viewModel.setBalance(0f) },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceContainerHigh)
                    ) {
                        Text(text = "Center", color = AuraPrimary, fontSize = 11.sp)
                    }
                }
                Slider(
                    value = effectsState.balance.balance,
                    onValueChange = { viewModel.setBalance(it) },
                    valueRange = -1f..1f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 2. 4D SPATIAL AUDIO TAB
// ==========================================
@Composable
private fun Spatial4DTab(
    effectsState: AudioEffectsState,
    viewModel: PlayerViewModel
) {
    val spatial = effectsState.spatial4d

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Main 4D Spatial Audio Engine Card
            EffectSectionCard(
                title = "AURA 4D Spatial Audio Engine",
                enabled = spatial.enabled,
                onToggle = { viewModel.setSpatial4DSettings(spatial.copy(enabled = it)) }
            ) {
                // Quick Preview Toggle [Original] [4D]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "A/B Preview", color = AuraOnSurfaceVariant, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AuraSurfaceContainerHigh)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (!spatial.isPreviewActive) AuraPrimary else Color.Transparent)
                                .clickable { viewModel.setSpatial4DPreview(false) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Original", color = if (!spatial.isPreviewActive) AuraOnPrimary else AuraOnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (spatial.isPreviewActive) AuraPrimary else Color.Transparent)
                                .clickable { viewModel.setSpatial4DPreview(true) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = "4D Effect", color = if (spatial.isPreviewActive) AuraOnPrimary else AuraOnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets Horizontal Row
                Text(text = "Spatial Presets", color = AuraOnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SpatialPreset.values()) { preset ->
                        val isSelected = spatial.preset == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AuraPrimary else AuraSurfaceContainerHigh)
                                .clickable { viewModel.setSpatial4DPreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset.displayName,
                                color = if (isSelected) AuraOnPrimary else AuraOnSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 2D Spatial Position Radar
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SpatialPositionRadar(
                        positionX = spatial.positionX,
                        positionY = spatial.positionY,
                        movementMode = spatial.movementMode,
                        accentColor = AuraPrimary,
                        onPositionChanged = { x, y ->
                            viewModel.setSpatial4DSettings(spatial.copy(positionX = x, positionY = y, movementMode = SpatialMovementMode.CUSTOM))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Movement Modes Selector
                Text(text = "Movement Mode", color = AuraOnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpatialMovementMode.values().forEach { mode ->
                        val isSelected = spatial.movementMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AuraPrimary.copy(alpha = 0.2f) else AuraSurfaceContainerHigh)
                                .border(1.dp, if (isSelected) AuraPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { viewModel.setSpatial4DSettings(spatial.copy(movementMode = mode)) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = mode.displayName, color = if (isSelected) AuraPrimary else AuraOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spatial Controls
                // 1. Intensity
                Text(text = "Intensity: ${(spatial.intensity * 100).toInt()}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = spatial.intensity,
                    onValueChange = { viewModel.setSpatial4DSettings(spatial.copy(intensity = it)) },
                    modifier = Modifier.semantics {
                        contentDescription = "4D Spatial Audio Intensity ${(spatial.intensity * 100).toInt()}%"
                    },
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                // 2. Width
                Text(text = "Width: ${(spatial.width * 100).toInt()}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = spatial.width,
                    onValueChange = { viewModel.setSpatial4DSettings(spatial.copy(width = it)) },
                    valueRange = 0f..2.5f,
                    modifier = Modifier.semantics {
                        contentDescription = "4D Soundstage Width ${(spatial.width * 100).toInt()}%"
                    },
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                // 3. Depth
                Text(text = "Depth: ${(spatial.depth * 100).toInt()}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = spatial.depth,
                    onValueChange = { viewModel.setSpatial4DSettings(spatial.copy(depth = it)) },
                    modifier = Modifier.semantics {
                        contentDescription = "4D Room Depth ${(spatial.depth * 100).toInt()}%"
                    },
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                // 4. Distance
                Text(text = "Distance: ${(spatial.distance * 100).toInt()}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = spatial.distance,
                    onValueChange = { viewModel.setSpatial4DSettings(spatial.copy(distance = it)) },
                    modifier = Modifier.semantics {
                        contentDescription = "4D Binaural Distance ${(spatial.distance * 100).toInt()}%"
                    },
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                // 5. Rotation Speed
                Text(text = "Rotation Speed: ${(spatial.rotationSpeed * 100).toInt()}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = spatial.rotationSpeed,
                    onValueChange = { viewModel.setSpatial4DSettings(spatial.copy(rotationSpeed = it)) },
                    valueRange = 0f..2f,
                    modifier = Modifier.semantics {
                        contentDescription = "4D Orbit Rotation Speed ${(spatial.rotationSpeed * 100).toInt()}%"
                    },
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 3. DYNAMICS TAB
// ==========================================
@Composable
private fun DynamicsTab(
    effectsState: AudioEffectsState,
    viewModel: PlayerViewModel
) {
    val comp = effectsState.compressor
    val lim = effectsState.limiter
    val loud = effectsState.loudness
    val rg = effectsState.replayGain

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Range Compressor
            EffectSectionCard(
                title = "Dynamic Compressor",
                enabled = comp.enabled,
                onToggle = { viewModel.setCompressor(comp.copy(enabled = it)) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Mode", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                    Row {
                        FilterChip(
                            selected = !comp.isAdvancedMode,
                            onClick = { viewModel.setCompressor(comp.copy(isAdvancedMode = false)) },
                            label = { Text("Simple", fontSize = 11.sp) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = comp.isAdvancedMode,
                            onClick = { viewModel.setCompressor(comp.copy(isAdvancedMode = true)) },
                            label = { Text("Advanced", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Threshold: ${"%.1f".format(comp.thresholdDb)} dB", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = comp.thresholdDb,
                    onValueChange = { viewModel.setCompressor(comp.copy(thresholdDb = it)) },
                    valueRange = -40f..0f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                Text(text = "Ratio: ${"%.1f".format(comp.ratio)}:1", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = comp.ratio,
                    onValueChange = { viewModel.setCompressor(comp.copy(ratio = it)) },
                    valueRange = 1f..20f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                if (comp.isAdvancedMode) {
                    Text(text = "Attack: ${comp.attackMs.toInt()} ms", color = AuraOnSurface, fontSize = 12.sp)
                    Slider(
                        value = comp.attackMs,
                        onValueChange = { viewModel.setCompressor(comp.copy(attackMs = it)) },
                        valueRange = 0.1f..100f,
                        colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                    )

                    Text(text = "Release: ${comp.releaseMs.toInt()} ms", color = AuraOnSurface, fontSize = 12.sp)
                    Slider(
                        value = comp.releaseMs,
                        onValueChange = { viewModel.setCompressor(comp.copy(releaseMs = it)) },
                        valueRange = 10f..1000f,
                        colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                    )
                }

                Text(text = "Makeup Gain: ${"%.1f".format(comp.makeupGainDb)} dB", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = comp.makeupGainDb,
                    onValueChange = { viewModel.setCompressor(comp.copy(makeupGainDb = it)) },
                    valueRange = 0f..24f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Peak Limiter
            EffectSectionCard(
                title = "Brickwall Limiter & Anti-Clipping",
                enabled = lim.enabled,
                onToggle = { viewModel.setLimiter(lim.copy(enabled = it)) }
            ) {
                Text(text = "Ceiling: ${"%.1f".format(lim.ceilingDb)} dBFS", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = lim.ceilingDb,
                    onValueChange = { viewModel.setLimiter(lim.copy(ceilingDb = it)) },
                    valueRange = -12f..0f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )
                Text(
                    text = "Transparently clamps peaks to prevent digital saturation and hardware clipping.",
                    color = AuraOnSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loudness Enhancement
            EffectSectionCard(
                title = "Loudness Enhancement",
                enabled = loud.enabled,
                onToggle = { viewModel.setLoudness(loud.copy(enabled = it)) }
            ) {
                Text(text = "Gain: ${loud.gainMb} mB (${loud.gainMb / 100f} dB)", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = loud.gainMb.toFloat(),
                    onValueChange = { viewModel.setLoudness(loud.copy(gainMb = it.toInt())) },
                    valueRange = 0f..2000f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ReplayGain
            EffectSectionCard(title = "ReplayGain Normalization", enabled = rg.mode != ReplayGainMode.OFF) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Mode", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                    Row {
                        ReplayGainMode.values().forEach { mode ->
                            FilterChip(
                                selected = rg.mode == mode,
                                onClick = { viewModel.setReplayGain(rg.copy(mode = mode)) },
                                label = { Text(mode.displayName, fontSize = 11.sp) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Preamp Gain: ${"%.1f".format(rg.preampGainDb)} dB", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = rg.preampGainDb,
                    onValueChange = { viewModel.setReplayGain(rg.copy(preampGainDb = it)) },
                    valueRange = -12f..12f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                val tagNote = if (rg.trackGainDb != null) {
                    "Metadata Gain Found: ${rg.trackGainDb} dB (Track) / ${rg.albumGainDb ?: 0f} dB (Album)"
                } else {
                    "No ReplayGain tags in current file. Playback proceeds with neutral gain."
                }
                Text(text = tagNote, color = AuraOnSurfaceVariant, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 4. REVERB & PLAYBACK TAB
// ==========================================
@Composable
private fun ReverbAndPlaybackTab(
    effectsState: AudioEffectsState,
    viewModel: PlayerViewModel,
    sleepTimer: com.example.aura.domain.model.audio.SleepTimerSettings
) {
    val rev = effectsState.reverb
    val virt = effectsState.virtualizer
    val pb = effectsState.playback
    var showReverbMenu by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Reverb
            EffectSectionCard(
                title = "Acoustic Reverb",
                enabled = rev.enabled,
                onToggle = { viewModel.setReverb(rev.copy(enabled = it)) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Room Preset", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                    Box {
                        Button(
                            onClick = { showReverbMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceContainerHigh)
                        ) {
                            Text(text = rev.preset.displayName, color = AuraPrimary, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = showReverbMenu,
                            onDismissRequest = { showReverbMenu = false },
                            modifier = Modifier.background(AuraSurfaceContainerHigh)
                        ) {
                            ReverbPreset.values().forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset.displayName, color = AuraOnSurface, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.setReverb(rev.copy(preset = preset, enabled = preset != ReverbPreset.OFF))
                                        showReverbMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Amount: ${(rev.amount * 100).toInt()}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = rev.amount,
                    onValueChange = { viewModel.setReverb(rev.copy(amount = it)) },
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stereo Expansion & Virtualizer
            EffectSectionCard(
                title = "Stereo Width & Virtualizer",
                enabled = virt.enabled,
                onToggle = { viewModel.setVirtualizer(virt.copy(enabled = it)) }
            ) {
                Text(text = "Stereo Width: ${(virt.stereoWidth * 100).toInt()}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = virt.stereoWidth,
                    onValueChange = { viewModel.setVirtualizer(virt.copy(stereoWidth = it)) },
                    valueRange = 0f..2f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )

                Text(text = "Virtualizer Strength: ${virt.strength / 10}%", color = AuraOnSurface, fontSize = 12.sp)
                Slider(
                    value = virt.strength.toFloat(),
                    onValueChange = { viewModel.setVirtualizer(virt.copy(strength = it.toInt())) },
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Options (Crossfade & Gapless)
            EffectSectionCard(title = "Playback Engine Settings", enabled = true) {
                // Crossfade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Crossfade", color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = pb.crossfadeEnabled,
                        onCheckedChange = { viewModel.setPlaybackSettings(pb.copy(crossfadeEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AuraPrimary)
                    )
                }
                if (pb.crossfadeEnabled) {
                    Text(text = "Duration: ${pb.crossfadeDurationSeconds} seconds", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                    Slider(
                        value = pb.crossfadeDurationSeconds.toFloat(),
                        onValueChange = { viewModel.setPlaybackSettings(pb.copy(crossfadeDurationSeconds = it.toInt())) },
                        valueRange = 1f..15f,
                        colors = SliderDefaults.colors(thumbColor = AuraPrimary, activeTrackColor = AuraPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gapless
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Gapless Playback", color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Eliminate silence between tracks", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                    }
                    Switch(
                        checked = pb.gaplessEnabled,
                        onCheckedChange = { viewModel.setPlaybackSettings(pb.copy(gaplessEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AuraPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Timer
            EffectSectionCard(title = "Sleep Timer", enabled = sleepTimer.isRunning) {
                if (sleepTimer.isRunning) {
                    val min = sleepTimer.remainingSeconds / 60
                    val sec = sleepTimer.remainingSeconds % 60
                    Text(
                        text = "Stopping in %02d:%02d".format(min, sec),
                        color = AuraPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.cancelSleepTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceContainerHigh)
                    ) {
                        Text(text = "Cancel Timer", color = Color.Red, fontSize = 12.sp)
                    }
                } else {
                    Text(text = "Set countdown duration:", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(15, 30, 45, 60, 90).forEach { mins ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraSurfaceContainerHigh)
                                    .clickable { viewModel.startSleepTimer(mins, fadeOut = true) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = "${mins}m", color = AuraOnSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 5. VISUALIZER TAB
// ==========================================
@Composable
private fun VisualizerTab(
    effectsState: AudioEffectsState,
    visualizerData: FloatArray,
    viewModel: PlayerViewModel
) {
    val vis = effectsState.visualizer

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Canvas Preview
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraSurfaceContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    AuraVisualizerView(
                        audioData = visualizerData,
                        style = vis.style,
                        accentColor = AuraPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Style Selection Chips
            Text(text = "Visualization Style", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VisualizerStyle.values().forEach { style ->
                    val isSelected = vis.style == style
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AuraPrimary else AuraSurfaceContainerHigh)
                            .clickable { viewModel.setVisualizerSettings(vis.copy(style = style)) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = style.displayName,
                            color = if (isSelected) AuraOnPrimary else AuraOnSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 6. OUTPUT PROFILE TAB
// ==========================================
@Composable
private fun OutputProfileTab(
    effectsState: AudioEffectsState,
    viewModel: PlayerViewModel
) {
    val info = effectsState.outputInfo

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Technical Output Fidelity Card (Section 19 & 20)
            EffectSectionCard(title = "Hardware Audio Output Fidelity", enabled = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Connected Device", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                        Text(text = info.deviceName, color = AuraOnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AuraPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = info.deviceType.displayName, color = AuraPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Source Quality vs Delivered Output (Truth in audio)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "SOURCE QUALITY", color = AuraPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${info.sourceCodec} ${if (info.isLossless) "Lossless" else "Lossy"}", color = AuraOnSurface, fontSize = 13.sp)
                        if (info.sourceBitDepth > 0) {
                            Text(text = "${info.sourceBitDepth}-bit • ${info.sourceSampleRate / 1000.0} kHz", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                        } else if (info.sourceBitrate > 0) {
                            Text(text = "${info.sourceBitrate / 1000} kbps • ${info.sourceSampleRate / 1000.0} kHz", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DELIVERED OUTPUT", color = AuraPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = info.outputEncoding, color = AuraOnSurface, fontSize = 13.sp)
                        Text(text = "${info.outputSampleRate / 1000} kHz", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraSurfaceContainerHigh)
                        .padding(10.dp)
                ) {
                    Text(text = info.explanation, color = AuraOnSurfaceVariant, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Output Device Profiles (Section 22)
            EffectSectionCard(title = "Device Profiles", enabled = true) {
                Text(
                    text = "AURA automatically associates audio DSP settings with your output devices.",
                    color = AuraOnSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                AudioDeviceType.values().forEach { deviceType ->
                    val isCurrent = effectsState.currentDeviceType == deviceType
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) AuraPrimary.copy(alpha = 0.15f) else AuraSurfaceContainerHigh)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = deviceType.displayName,
                                color = if (isCurrent) AuraPrimary else AuraOnSurface,
                                fontSize = 13.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isCurrent) {
                                Text(text = "Currently Active", color = AuraPrimary, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.saveDeviceProfile(deviceType) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isCurrent) AuraPrimary else AuraSurfaceContainerHighest)
                        ) {
                            Text(text = "Save Profile", color = if (isCurrent) AuraOnPrimary else AuraOnSurface, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// REUSABLE EFFECT CARD WRAPPER
// ==========================================
@Composable
private fun EffectSectionCard(
    title: String,
    enabled: Boolean,
    onToggle: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AuraSurfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (enabled) AuraPrimary else AuraOnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (onToggle != null) {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = AuraPrimary, checkedTrackColor = AuraPrimary.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
