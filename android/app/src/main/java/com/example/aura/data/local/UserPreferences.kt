package com.example.aura.data.local

import android.content.Context
import java.io.File
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aura.domain.model.SongSortOrder
import com.example.aura.domain.model.audio.AudioDeviceType
import com.example.aura.domain.model.audio.AudioEffectsState
import com.example.aura.domain.model.audio.BalanceSettings
import com.example.aura.domain.model.audio.BassTrebleSettings
import com.example.aura.domain.model.audio.CompressorSettings
import com.example.aura.domain.model.audio.EqualizerBand
import com.example.aura.domain.model.audio.EqualizerPreset
import com.example.aura.domain.model.audio.EqualizerSettings
import com.example.aura.domain.model.audio.FilterType
import com.example.aura.domain.model.audio.LimiterSettings
import com.example.aura.domain.model.audio.LoudnessSettings
import com.example.aura.domain.model.audio.ParametricBand
import com.example.aura.domain.model.audio.ParametricSettings
import com.example.aura.domain.model.audio.PlaybackSettings
import com.example.aura.domain.model.audio.ReplayGainMode
import com.example.aura.domain.model.audio.ReplayGainSettings
import com.example.aura.domain.model.audio.ReverbPreset
import com.example.aura.domain.model.audio.ReverbSettings
import com.example.aura.domain.model.audio.Spatial4DSettings
import com.example.aura.domain.model.audio.SpatialMovementMode
import com.example.aura.domain.model.audio.SpatialPreset
import com.example.aura.domain.model.audio.VirtualizerSettings
import com.example.aura.domain.model.audio.VisualizerSettings
import com.example.aura.domain.model.audio.VisualizerStyle
import com.example.aura.domain.model.settings.AnimationSettings
import com.example.aura.domain.model.settings.AppearanceSettings
import com.example.aura.domain.model.settings.BluetoothGestureSettings
import com.example.aura.domain.model.settings.GesturesSettings
import com.example.aura.domain.model.settings.HapticSettings
import com.example.aura.domain.model.settings.LibrarySettings
import com.example.aura.domain.model.settings.LyricsDisplaySettings
import com.example.aura.domain.model.settings.NotificationSettings
import com.example.aura.domain.model.settings.PlaybackBehaviorSettings
import com.example.aura.domain.model.settings.PrivacyAndDataSettings
import com.example.aura.domain.model.exclusion.ExcludedFolderRule
import com.example.aura.domain.model.exclusion.FilenamePatternRule
import com.example.aura.domain.model.exclusion.HiddenTrackItem
import com.example.aura.domain.model.exclusion.SmartScanSettings
import com.example.aura.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_user_prefs")

class UserPreferences(private val context: Context) {

    // General app settings
    private val SORT_ORDER_KEY = stringPreferencesKey("library_sort_order")
    private val SELECTED_TAB_KEY = stringPreferencesKey("library_selected_tab")
    private val THEME_KEY = stringPreferencesKey("app_theme_preference")

    // Phase 3 Audio Keys
    private val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
    private val EQ_PRESET = stringPreferencesKey("eq_preset")
    private val EQ_BANDS = stringPreferencesKey("eq_bands_levels")
    private val EQ_PREAMP = floatPreferencesKey("eq_preamp")
    private val EQ_AUTO_HEADROOM = booleanPreferencesKey("eq_auto_headroom")

    private val PEQ_ENABLED = booleanPreferencesKey("peq_enabled")
    private val PEQ_BANDS = stringPreferencesKey("peq_bands_data")

    private val BASS_ENABLED = booleanPreferencesKey("bass_enabled")
    private val BASS_LEVEL = floatPreferencesKey("bass_level")
    private val TREBLE_ENABLED = booleanPreferencesKey("treble_enabled")
    private val TREBLE_LEVEL = floatPreferencesKey("treble_level")

    private val BALANCE_VAL = floatPreferencesKey("channel_balance")

    private val COMP_ENABLED = booleanPreferencesKey("comp_enabled")
    private val COMP_THRESHOLD = floatPreferencesKey("comp_threshold")
    private val COMP_RATIO = floatPreferencesKey("comp_ratio")
    private val COMP_ATTACK = floatPreferencesKey("comp_attack")
    private val COMP_RELEASE = floatPreferencesKey("comp_release")
    private val COMP_MAKEUP = floatPreferencesKey("comp_makeup")
    private val COMP_ADVANCED = booleanPreferencesKey("comp_advanced")

    private val LIM_ENABLED = booleanPreferencesKey("lim_enabled")
    private val LIM_CEILING = floatPreferencesKey("lim_ceiling")

    private val REV_ENABLED = booleanPreferencesKey("rev_enabled")
    private val REV_PRESET = stringPreferencesKey("rev_preset")
    private val REV_AMOUNT = floatPreferencesKey("rev_amount")
    private val REV_SIZE = floatPreferencesKey("rev_size")

    private val VIRT_ENABLED = booleanPreferencesKey("virt_enabled")
    private val VIRT_WIDTH = floatPreferencesKey("virt_width")
    private val VIRT_STRENGTH = intPreferencesKey("virt_strength")

    private val LOUD_ENABLED = booleanPreferencesKey("loud_enabled")
    private val LOUD_GAIN = intPreferencesKey("loud_gain")

    private val RG_MODE = stringPreferencesKey("rg_mode")
    private val RG_PREAMP = floatPreferencesKey("rg_preamp")

    private val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
    private val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
    private val GAPLESS_ENABLED = booleanPreferencesKey("gapless_enabled")

    private val SPATIAL_ENABLED = booleanPreferencesKey("spatial4d_enabled")
    private val SPATIAL_INTENSITY = floatPreferencesKey("spatial4d_intensity")
    private val SPATIAL_WIDTH = floatPreferencesKey("spatial4d_width")
    private val SPATIAL_DEPTH = floatPreferencesKey("spatial4d_depth")
    private val SPATIAL_DISTANCE = floatPreferencesKey("spatial4d_distance")
    private val SPATIAL_ROT_SPEED = floatPreferencesKey("spatial4d_rot_speed")
    private val SPATIAL_MODE = stringPreferencesKey("spatial4d_mode")
    private val SPATIAL_PRESET = stringPreferencesKey("spatial4d_preset")
    private val SPATIAL_POS_X = floatPreferencesKey("spatial4d_pos_x")
    private val SPATIAL_POS_Y = floatPreferencesKey("spatial4d_pos_y")
    private val SPATIAL_POS_Z = floatPreferencesKey("spatial4d_pos_z")

    private val VIS_ENABLED = booleanPreferencesKey("vis_enabled")
    private val VIS_STYLE = stringPreferencesKey("vis_style")

    private val LYRICS_OFFSET_MS = longPreferencesKey("lyrics_offset_ms")

    // Playback position resume (survives process death and app restart)
    private val LAST_SONG_ID = stringPreferencesKey("last_song_id")
    private val LAST_POSITION_MS = longPreferencesKey("last_position_ms")

    // Playback Position Persistence
    suspend fun saveLastPlaybackPosition(songId: String, positionMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SONG_ID] = songId
            prefs[LAST_POSITION_MS] = positionMs
        }
    }

    suspend fun getLastPlaybackPosition(): Pair<String, Long>? {
        val prefs = context.dataStore.data.first()
        val songId = prefs[LAST_SONG_ID] ?: return null
        val positionMs = prefs[LAST_POSITION_MS] ?: 0L
        return Pair(songId, positionMs)
    }

    // General preferences flows & setters
    val sortOrderFlow: Flow<SongSortOrder> = context.dataStore.data.map { preferences ->
        val name = preferences[SORT_ORDER_KEY]
        SongSortOrder.fromName(name)
    }

    suspend fun setSortOrder(sortOrder: SongSortOrder) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.name
        }
    }

    val selectedLibraryTabFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_TAB_KEY] ?: "Songs"
    }

    suspend fun setSelectedLibraryTab(tab: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_TAB_KEY] = tab
        }
    }

    val themePreferenceFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "Cinematic Dark"
    }

    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    // Audio Effects State Flow
    val audioEffectsFlow: Flow<AudioEffectsState> = context.dataStore.data.map { prefs ->
        // Parse EQ Bands
        val rawBands = prefs[EQ_BANDS]
        val eqBands = if (!rawBands.isNullOrEmpty()) {
            try {
                rawBands.split(";").mapNotNull { part ->
                    val tokens = part.split(":")
                    if (tokens.size >= 3) {
                        EqualizerBand(
                            index = tokens[0].toInt(),
                            centerFreqHz = tokens[1].toInt(),
                            gainMb = tokens[2].toInt()
                        )
                    } else null
                }
            } catch (_: Exception) {
                EqualizerSettings.defaultBands()
            }
        } else {
            EqualizerSettings.defaultBands()
        }

        // Parse Parametric Bands
        val rawPeqBands = prefs[PEQ_BANDS]
        val peqBands = if (!rawPeqBands.isNullOrEmpty()) {
            try {
                rawPeqBands.split(";").mapNotNull { part ->
                    val tokens = part.split(":")
                    if (tokens.size >= 5) {
                        ParametricBand(
                            id = tokens[0],
                            frequencyHz = tokens[1].toFloat(),
                            gainDb = tokens[2].toFloat(),
                            qFactor = tokens[3].toFloat(),
                            filterType = FilterType.valueOf(tokens[4]),
                            enabled = tokens.getOrNull(5)?.toBoolean() ?: true
                        )
                    } else null
                }
            } catch (_: Exception) {
                ParametricSettings.defaultParametricBands()
            }
        } else {
            ParametricSettings.defaultParametricBands()
        }

        AudioEffectsState(
            equalizer = EqualizerSettings(
                enabled = prefs[EQ_ENABLED] ?: false,
                preset = prefs[EQ_PRESET]?.let { runCatching { EqualizerPreset.valueOf(it) }.getOrNull() } ?: EqualizerPreset.FLAT,
                bands = eqBands,
                preampGainDb = prefs[EQ_PREAMP] ?: 0f,
                autoHeadroomEnabled = prefs[EQ_AUTO_HEADROOM] ?: true
            ),
            parametric = ParametricSettings(
                enabled = prefs[PEQ_ENABLED] ?: false,
                bands = peqBands
            ),
            bassTreble = BassTrebleSettings(
                bassEnabled = prefs[BASS_ENABLED] ?: false,
                bassLevel = prefs[BASS_LEVEL] ?: 0f,
                trebleEnabled = prefs[TREBLE_ENABLED] ?: false,
                trebleLevel = prefs[TREBLE_LEVEL] ?: 0f
            ),
            balance = BalanceSettings(
                balance = prefs[BALANCE_VAL] ?: 0f
            ),
            compressor = CompressorSettings(
                enabled = prefs[COMP_ENABLED] ?: false,
                thresholdDb = prefs[COMP_THRESHOLD] ?: -18f,
                ratio = prefs[COMP_RATIO] ?: 4f,
                attackMs = prefs[COMP_ATTACK] ?: 15f,
                releaseMs = prefs[COMP_RELEASE] ?: 100f,
                makeupGainDb = prefs[COMP_MAKEUP] ?: 2f,
                isAdvancedMode = prefs[COMP_ADVANCED] ?: false
            ),
            limiter = LimiterSettings(
                enabled = prefs[LIM_ENABLED] ?: true,
                ceilingDb = prefs[LIM_CEILING] ?: -0.5f
            ),
            reverb = ReverbSettings(
                enabled = prefs[REV_ENABLED] ?: false,
                preset = prefs[REV_PRESET]?.let { runCatching { ReverbPreset.valueOf(it) }.getOrNull() } ?: ReverbPreset.OFF,
                amount = prefs[REV_AMOUNT] ?: 0.3f,
                size = prefs[REV_SIZE] ?: 0.5f
            ),
            virtualizer = VirtualizerSettings(
                enabled = prefs[VIRT_ENABLED] ?: false,
                stereoWidth = prefs[VIRT_WIDTH] ?: 1.0f,
                strength = prefs[VIRT_STRENGTH] ?: 500
            ),
            loudness = LoudnessSettings(
                enabled = prefs[LOUD_ENABLED] ?: false,
                gainMb = prefs[LOUD_GAIN] ?: 400
            ),
            replayGain = ReplayGainSettings(
                mode = prefs[RG_MODE]?.let { runCatching { ReplayGainMode.valueOf(it) }.getOrNull() } ?: ReplayGainMode.OFF,
                preampGainDb = prefs[RG_PREAMP] ?: 0f
            ),
            playback = PlaybackSettings(
                crossfadeEnabled = prefs[CROSSFADE_ENABLED] ?: false,
                crossfadeDurationSeconds = prefs[CROSSFADE_DURATION] ?: 3,
                gaplessEnabled = prefs[GAPLESS_ENABLED] ?: true
            ),
            spatial4d = Spatial4DSettings(
                enabled = prefs[SPATIAL_ENABLED] ?: false,
                intensity = prefs[SPATIAL_INTENSITY] ?: 0.7f,
                width = prefs[SPATIAL_WIDTH] ?: 1.2f,
                depth = prefs[SPATIAL_DEPTH] ?: 0.5f,
                distance = prefs[SPATIAL_DISTANCE] ?: 0.3f,
                rotationSpeed = prefs[SPATIAL_ROT_SPEED] ?: 0.5f,
                movementMode = prefs[SPATIAL_MODE]?.let { runCatching { SpatialMovementMode.valueOf(it) }.getOrNull() } ?: SpatialMovementMode.STATIC,
                preset = prefs[SPATIAL_PRESET]?.let { runCatching { SpatialPreset.valueOf(it) }.getOrNull() } ?: SpatialPreset.NORMAL,
                positionX = prefs[SPATIAL_POS_X] ?: 0f,
                positionY = prefs[SPATIAL_POS_Y] ?: 0.5f,
                positionZ = prefs[SPATIAL_POS_Z] ?: 0f
            ),
            visualizer = VisualizerSettings(
                enabled = prefs[VIS_ENABLED] ?: true,
                style = prefs[VIS_STYLE]?.let { runCatching { VisualizerStyle.valueOf(it) }.getOrNull() } ?: VisualizerStyle.SPECTRUM
            ),
            lyricsOffsetMs = prefs[LYRICS_OFFSET_MS] ?: 0L
        )
    }

    suspend fun saveAudioEffects(state: AudioEffectsState) {
        context.dataStore.edit { prefs ->
            prefs[EQ_ENABLED] = state.equalizer.enabled
            prefs[EQ_PRESET] = state.equalizer.preset.name
            prefs[EQ_BANDS] = state.equalizer.bands.joinToString(";") { "${it.index}:${it.centerFreqHz}:${it.gainMb}" }
            prefs[EQ_PREAMP] = state.equalizer.preampGainDb
            prefs[EQ_AUTO_HEADROOM] = state.equalizer.autoHeadroomEnabled

            prefs[PEQ_ENABLED] = state.parametric.enabled
            prefs[PEQ_BANDS] = state.parametric.bands.joinToString(";") {
                "${it.id}:${it.frequencyHz}:${it.gainDb}:${it.qFactor}:${it.filterType.name}:${it.enabled}"
            }

            prefs[BASS_ENABLED] = state.bassTreble.bassEnabled
            prefs[BASS_LEVEL] = state.bassTreble.bassLevel
            prefs[TREBLE_ENABLED] = state.bassTreble.trebleEnabled
            prefs[TREBLE_LEVEL] = state.bassTreble.trebleLevel

            prefs[BALANCE_VAL] = state.balance.balance

            prefs[COMP_ENABLED] = state.compressor.enabled
            prefs[COMP_THRESHOLD] = state.compressor.thresholdDb
            prefs[COMP_RATIO] = state.compressor.ratio
            prefs[COMP_ATTACK] = state.compressor.attackMs
            prefs[COMP_RELEASE] = state.compressor.releaseMs
            prefs[COMP_MAKEUP] = state.compressor.makeupGainDb
            prefs[COMP_ADVANCED] = state.compressor.isAdvancedMode

            prefs[LIM_ENABLED] = state.limiter.enabled
            prefs[LIM_CEILING] = state.limiter.ceilingDb

            prefs[REV_ENABLED] = state.reverb.enabled
            prefs[REV_PRESET] = state.reverb.preset.name
            prefs[REV_AMOUNT] = state.reverb.amount
            prefs[REV_SIZE] = state.reverb.size

            prefs[VIRT_ENABLED] = state.virtualizer.enabled
            prefs[VIRT_WIDTH] = state.virtualizer.stereoWidth
            prefs[VIRT_STRENGTH] = state.virtualizer.strength

            prefs[LOUD_ENABLED] = state.loudness.enabled
            prefs[LOUD_GAIN] = state.loudness.gainMb

            prefs[RG_MODE] = state.replayGain.mode.name
            prefs[RG_PREAMP] = state.replayGain.preampGainDb

            prefs[CROSSFADE_ENABLED] = state.playback.crossfadeEnabled
            prefs[CROSSFADE_DURATION] = state.playback.crossfadeDurationSeconds
            prefs[GAPLESS_ENABLED] = state.playback.gaplessEnabled

            prefs[SPATIAL_ENABLED] = state.spatial4d.enabled
            prefs[SPATIAL_INTENSITY] = state.spatial4d.intensity
            prefs[SPATIAL_WIDTH] = state.spatial4d.width
            prefs[SPATIAL_DEPTH] = state.spatial4d.depth
            prefs[SPATIAL_DISTANCE] = state.spatial4d.distance
            prefs[SPATIAL_ROT_SPEED] = state.spatial4d.rotationSpeed
            prefs[SPATIAL_MODE] = state.spatial4d.movementMode.name
            prefs[SPATIAL_PRESET] = state.spatial4d.preset.name
            prefs[SPATIAL_POS_X] = state.spatial4d.positionX
            prefs[SPATIAL_POS_Y] = state.spatial4d.positionY
            prefs[SPATIAL_POS_Z] = state.spatial4d.positionZ

            prefs[VIS_ENABLED] = state.visualizer.enabled
            prefs[VIS_STYLE] = state.visualizer.style.name

            prefs[LYRICS_OFFSET_MS] = state.lyricsOffsetMs
        }
    }

    suspend fun saveDeviceProfile(deviceType: AudioDeviceType, state: AudioEffectsState) {
        val key = stringPreferencesKey("profile_${deviceType.name}")
        context.dataStore.edit { prefs ->
            prefs[key] = "saved"
        }
        // Save current state as active
        saveAudioEffects(state)
    }

    // Phase 4 Categorized Preferences Keys
    private val PB_AUTO_PLAY = booleanPreferencesKey("pb_auto_play")
    private val PB_RESUME_POS = booleanPreferencesKey("pb_resume_pos")
    private val PB_PAUSE_DISCONNECT = booleanPreferencesKey("pb_pause_disconnect")
    private val PB_RESUME_CONNECT = booleanPreferencesKey("pb_resume_connect")
    private val PB_INTEL_SHUFFLE = booleanPreferencesKey("pb_intel_shuffle")

    private val BT_SINGLE_TAP = stringPreferencesKey("bt_single_tap")
    private val BT_DOUBLE_TAP_L = stringPreferencesKey("bt_double_tap_l")
    private val BT_DOUBLE_TAP_R = stringPreferencesKey("bt_double_tap_r")
    private val BT_TRIPLE_TAP = stringPreferencesKey("bt_triple_tap")
    private val BT_LONG_PRESS = stringPreferencesKey("bt_long_press")
    private val BT_AUTO_PROFILE = booleanPreferencesKey("bt_auto_profile")

    private val LYRICS_SOURCE = stringPreferencesKey("lyrics_source")
    private val LYRICS_AUTO_SEARCH = booleanPreferencesKey("lyrics_auto_search")
    private val LYRICS_ONLINE_ENABLED = booleanPreferencesKey("lyrics_online_enabled")
    private val LYRICS_WIFI_ONLY = booleanPreferencesKey("lyrics_wifi_only")
    private val LYRICS_AUTO_DOWNLOAD = booleanPreferencesKey("lyrics_auto_download")
    private val LYRICS_LOCAL_LRC = booleanPreferencesKey("lyrics_local_lrc")
    private val LYRICS_LOCAL_TXT = booleanPreferencesKey("lyrics_local_txt")
    private val LYRICS_CACHED_SUPPORT = booleanPreferencesKey("lyrics_cached_support")
    private val LYRICS_EMBEDDED = booleanPreferencesKey("lyrics_embedded")
    private val LYRICS_FONT_SIZE = floatPreferencesKey("lyrics_font_size")
    private val LYRICS_LINE_SPACING = floatPreferencesKey("lyrics_line_spacing")
    private val LYRICS_TEXT_ALIGNMENT = stringPreferencesKey("lyrics_text_alignment")
    private val LYRICS_CURRENT_HIGHLIGHT = booleanPreferencesKey("lyrics_current_highlight")
    private val LYRICS_AUTO_SCROLL = booleanPreferencesKey("lyrics_auto_scroll")
    private val LYRICS_ANIMATION = booleanPreferencesKey("lyrics_animation")
    private val LYRICS_REDUCE_MOTION = booleanPreferencesKey("lyrics_reduce_motion")
    private val LYRICS_GLOBAL_OFFSET = longPreferencesKey("lyrics_global_offset_ms")
    private val LYRICS_TAP_TO_SEEK = booleanPreferencesKey("lyrics_tap_to_seek")
    private val LYRICS_FULL_SCREEN = booleanPreferencesKey("lyrics_full_screen")

    private val APP_ACCENT_HEX = stringPreferencesKey("app_accent_hex")
    private val APP_DYNAMIC_COLORS = booleanPreferencesKey("app_dynamic_colors")
    private val APP_ARTWORK_RADIUS = intPreferencesKey("app_artwork_radius")
    private val APP_ARTWORK_STYLE = stringPreferencesKey("app_artwork_style")
    private val APP_BG_STYLE = stringPreferencesKey("app_bg_style")

    private val ANIM_ENABLED = booleanPreferencesKey("anim_enabled")
    private val ANIM_SCALE = floatPreferencesKey("anim_scale")
    private val ANIM_REDUCE_MOTION = booleanPreferencesKey("anim_reduce_motion")

    private val GESTURE_SWIPE_L = stringPreferencesKey("gesture_swipe_l")
    private val GESTURE_SWIPE_R = stringPreferencesKey("gesture_swipe_r")
    private val GESTURE_ARTWORK_TAP = stringPreferencesKey("gesture_artwork_tap")
    private val GESTURE_ARTWORK_DOUBLE = stringPreferencesKey("gesture_artwork_double")

    private val NOTIF_CONTROLS = booleanPreferencesKey("notif_controls")
    private val NOTIF_PROGRESS = booleanPreferencesKey("notif_progress")
    private val NOTIF_FAVORITE = booleanPreferencesKey("notif_favorite")
    private val NOTIF_COMPACT = booleanPreferencesKey("notif_compact")
    private val NOTIF_PERSISTENT = booleanPreferencesKey("notif_persistent")

    private val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    private val HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")

    private val ANIM_VIS_ENABLED = booleanPreferencesKey("anim_vis_enabled")

    // Flows
    val playbackBehaviorFlow: Flow<PlaybackBehaviorSettings> = context.dataStore.data.map { p ->
        PlaybackBehaviorSettings(
            autoPlayOnLaunch = p[PB_AUTO_PLAY] ?: false,
            resumeLastPosition = p[PB_RESUME_POS] ?: true,
            pauseOnDisconnect = p[PB_PAUSE_DISCONNECT] ?: true,
            resumeOnConnect = p[PB_RESUME_CONNECT] ?: false,
            intelligentShuffle = p[PB_INTEL_SHUFFLE] ?: true
        )
    }

    suspend fun updatePlaybackBehavior(settings: PlaybackBehaviorSettings) {
        context.dataStore.edit { p ->
            p[PB_AUTO_PLAY] = settings.autoPlayOnLaunch
            p[PB_RESUME_POS] = settings.resumeLastPosition
            p[PB_PAUSE_DISCONNECT] = settings.pauseOnDisconnect
            p[PB_RESUME_CONNECT] = settings.resumeOnConnect
            p[PB_INTEL_SHUFFLE] = settings.intelligentShuffle
        }
    }

    val bluetoothGestureFlow: Flow<BluetoothGestureSettings> = context.dataStore.data.map { p ->
        BluetoothGestureSettings(
            singleTapAction = p[BT_SINGLE_TAP] ?: "Play / Pause",
            doubleTapLeftAction = p[BT_DOUBLE_TAP_L] ?: "Previous Track",
            doubleTapRightAction = p[BT_DOUBLE_TAP_R] ?: "Next Track",
            tripleTapAction = p[BT_TRIPLE_TAP] ?: "Toggle 4D Spatial Audio",
            longPressAction = p[BT_LONG_PRESS] ?: "Toggle Favorite",
            autoLoadDeviceProfile = p[BT_AUTO_PROFILE] ?: true
        )
    }

    suspend fun updateBluetoothGestures(settings: BluetoothGestureSettings) {
        context.dataStore.edit { p ->
            p[BT_SINGLE_TAP] = settings.singleTapAction
            p[BT_DOUBLE_TAP_L] = settings.doubleTapLeftAction
            p[BT_DOUBLE_TAP_R] = settings.doubleTapRightAction
            p[BT_TRIPLE_TAP] = settings.tripleTapAction
            p[BT_LONG_PRESS] = settings.longPressAction
            p[BT_AUTO_PROFILE] = settings.autoLoadDeviceProfile
        }
    }

    val lyricsDisplayFlow: Flow<LyricsDisplaySettings> = context.dataStore.data.map { p ->
        LyricsDisplaySettings(
            lyricsSource = p[LYRICS_SOURCE] ?: "Automatic (All Sources)",
            autoSearchOnline = p[LYRICS_AUTO_SEARCH] ?: true,
            onlineLyricsEnabled = p[LYRICS_ONLINE_ENABLED] ?: true,
            wifiOnly = p[LYRICS_WIFI_ONLY] ?: false,
            autoDownloadLyrics = p[LYRICS_AUTO_DOWNLOAD] ?: true,
            localLyricsSupport = p[LYRICS_LOCAL_LRC] ?: true,
            embeddedLyricsSupport = p[LYRICS_EMBEDDED] ?: true,
            localTxtSupport = p[LYRICS_LOCAL_TXT] ?: true,
            cachedLyricsSupport = p[LYRICS_CACHED_SUPPORT] ?: true,
            fontSizeSp = p[LYRICS_FONT_SIZE] ?: 18f,
            lineSpacingSp = p[LYRICS_LINE_SPACING] ?: 10f,
            textAlignment = p[LYRICS_TEXT_ALIGNMENT] ?: "Center",
            currentLineHighlight = p[LYRICS_CURRENT_HIGHLIGHT] ?: true,
            autoScroll = p[LYRICS_AUTO_SCROLL] ?: true,
            lyricsAnimation = p[LYRICS_ANIMATION] ?: true,
            reduceMotion = p[LYRICS_REDUCE_MOTION] ?: false,
            globalOffsetMs = p[LYRICS_GLOBAL_OFFSET] ?: 0L,
            tapToSeek = p[LYRICS_TAP_TO_SEEK] ?: true,
            fullScreenLyrics = p[LYRICS_FULL_SCREEN] ?: false
        )
    }

    suspend fun updateLyricsDisplay(settings: LyricsDisplaySettings) {
        context.dataStore.edit { p ->
            p[LYRICS_SOURCE] = settings.lyricsSource
            p[LYRICS_AUTO_SEARCH] = settings.autoSearchOnline
            p[LYRICS_ONLINE_ENABLED] = settings.onlineLyricsEnabled
            p[LYRICS_WIFI_ONLY] = settings.wifiOnly
            p[LYRICS_AUTO_DOWNLOAD] = settings.autoDownloadLyrics
            p[LYRICS_LOCAL_LRC] = settings.localLyricsSupport
            p[LYRICS_EMBEDDED] = settings.embeddedLyricsSupport
            p[LYRICS_LOCAL_TXT] = settings.localTxtSupport
            p[LYRICS_CACHED_SUPPORT] = settings.cachedLyricsSupport
            p[LYRICS_FONT_SIZE] = settings.fontSizeSp
            p[LYRICS_LINE_SPACING] = settings.lineSpacingSp
            p[LYRICS_TEXT_ALIGNMENT] = settings.textAlignment
            p[LYRICS_CURRENT_HIGHLIGHT] = settings.currentLineHighlight
            p[LYRICS_AUTO_SCROLL] = settings.autoScroll
            p[LYRICS_ANIMATION] = settings.lyricsAnimation
            p[LYRICS_REDUCE_MOTION] = settings.reduceMotion
            p[LYRICS_GLOBAL_OFFSET] = settings.globalOffsetMs
            p[LYRICS_TAP_TO_SEEK] = settings.tapToSeek
            p[LYRICS_FULL_SCREEN] = settings.fullScreenLyrics
        }
    }

    val appearanceFlow: Flow<AppearanceSettings> = context.dataStore.data.map { p ->
        AppearanceSettings(
            theme = p[THEME_KEY] ?: "Dark",
            accentColorHex = p[APP_ACCENT_HEX] ?: "#7C5CFC",
            dynamicColors = p[APP_DYNAMIC_COLORS] ?: true,
            playerArtworkCornerRadiusDp = p[APP_ARTWORK_RADIUS] ?: 24,
            artworkStyle = p[APP_ARTWORK_STYLE] ?: "Rounded",
            backgroundStyle = p[APP_BG_STYLE] ?: "Adaptive Gradient",
            miniPlayerStyle = p[stringPreferencesKey("mini_player_style")] ?: "Standard"
        )
    }

    suspend fun updateAppearance(settings: AppearanceSettings) {
        context.dataStore.edit { p ->
            p[THEME_KEY] = settings.theme
            p[APP_ACCENT_HEX] = settings.accentColorHex
            p[APP_DYNAMIC_COLORS] = settings.dynamicColors
            p[APP_ARTWORK_RADIUS] = settings.playerArtworkCornerRadiusDp
            p[APP_ARTWORK_STYLE] = settings.artworkStyle
            p[APP_BG_STYLE] = settings.backgroundStyle
        }
    }

    val animationFlow: Flow<AnimationSettings> = context.dataStore.data.map { p ->
        AnimationSettings(
            animationsEnabled = p[ANIM_ENABLED] ?: true,
            animationScale = p[ANIM_SCALE] ?: 1.0f,
            reduceMotion = p[ANIM_REDUCE_MOTION] ?: false,
            visualizerAnimationEnabled = p[ANIM_VIS_ENABLED] ?: true
        )
    }

    suspend fun updateAnimations(settings: AnimationSettings) {
        context.dataStore.edit { p ->
            p[ANIM_ENABLED] = settings.animationsEnabled
            p[ANIM_SCALE] = settings.animationScale
            p[ANIM_REDUCE_MOTION] = settings.reduceMotion
            p[ANIM_VIS_ENABLED] = settings.visualizerAnimationEnabled
        }
    }

    val notificationFlow: Flow<NotificationSettings> = context.dataStore.data.map { p ->
        NotificationSettings(
            showControls = p[NOTIF_CONTROLS] ?: true,
            showProgress = p[NOTIF_PROGRESS] ?: true,
            showFavorite = p[NOTIF_FAVORITE] ?: true,
            compactNotification = p[NOTIF_COMPACT] ?: false,
            persistentNotification = p[NOTIF_PERSISTENT] ?: true
        )
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        context.dataStore.edit { p ->
            p[NOTIF_CONTROLS] = settings.showControls
            p[NOTIF_PROGRESS] = settings.showProgress
            p[NOTIF_FAVORITE] = settings.showFavorite
            p[NOTIF_COMPACT] = settings.compactNotification
            p[NOTIF_PERSISTENT] = settings.persistentNotification
        }
    }

    val gesturesFlow: Flow<GesturesSettings> = context.dataStore.data.map { p ->
        GesturesSettings(
            swipeLeftPlayer = p[GESTURE_SWIPE_L] ?: "Next Track",
            swipeRightPlayer = p[GESTURE_SWIPE_R] ?: "Previous Track",
            artworkTap = p[GESTURE_ARTWORK_TAP] ?: "Toggle Lyrics",
            artworkDoubleTap = p[GESTURE_ARTWORK_DOUBLE] ?: "Toggle Favorite"
        )
    }

    suspend fun updateGestures(settings: GesturesSettings) {
        context.dataStore.edit { p ->
            p[GESTURE_SWIPE_L] = settings.swipeLeftPlayer
            p[GESTURE_SWIPE_R] = settings.swipeRightPlayer
            p[GESTURE_ARTWORK_TAP] = settings.artworkTap
            p[GESTURE_ARTWORK_DOUBLE] = settings.artworkDoubleTap
        }
    }

    val hapticFlow: Flow<HapticSettings> = context.dataStore.data.map { p ->
        HapticSettings(
            hapticEnabled = p[HAPTIC_ENABLED] ?: true,
            intensity = p[HAPTIC_INTENSITY] ?: "Medium"
        )
    }

    suspend fun updateHaptics(settings: HapticSettings) {
        context.dataStore.edit { p ->
            p[HAPTIC_ENABLED] = settings.hapticEnabled
            p[HAPTIC_INTENSITY] = settings.intensity
        }
    }

    // ==========================================
    // SMART LIBRARY EXCLUSIONS & FILTERING
    // ==========================================
    private val SMART_SCAN_KEY = stringPreferencesKey("smart_scan_settings_json")

    val smartScanSettingsFlow: Flow<SmartScanSettings> = context.dataStore.data.map { p ->
        SmartScanSettings.fromJson(p[SMART_SCAN_KEY])
    }

    suspend fun getSmartScanSettingsSync(): SmartScanSettings {
        return smartScanSettingsFlow.first()
    }

    suspend fun updateSmartScanSettings(settings: SmartScanSettings) {
        context.dataStore.edit { p ->
            p[SMART_SCAN_KEY] = settings.toJson()
        }
    }

    suspend fun addExcludedFolder(path: String, displayName: String) {
        val current = getSmartScanSettingsSync()
        if (current.excludedFolders.none { it.path.equals(path, ignoreCase = true) }) {
            val updated = current.copy(
                excludedFolders = current.excludedFolders + ExcludedFolderRule(
                    path = path,
                    displayName = displayName,
                    isEnabled = true,
                    isPreset = false
                )
            )
            updateSmartScanSettings(updated)
        }
    }

    suspend fun removeExcludedFolder(path: String) {
        val current = getSmartScanSettingsSync()
        val updated = current.copy(
            excludedFolders = current.excludedFolders.filterNot { it.path.equals(path, ignoreCase = true) }
        )
        updateSmartScanSettings(updated)
    }

    suspend fun toggleExcludedFolder(path: String, isEnabled: Boolean) {
        val current = getSmartScanSettingsSync()
        val updated = current.copy(
            excludedFolders = current.excludedFolders.map {
                if (it.path.equals(path, ignoreCase = true)) it.copy(isEnabled = isEnabled) else it
            }
        )
        updateSmartScanSettings(updated)
    }

    suspend fun toggleExcludedExtension(extension: String, isExcluded: Boolean? = null) {
        val clean = extension.lowercase().trimStart('.')
        val current = getSmartScanSettingsSync()
        val shouldExclude = isExcluded ?: !current.excludedExtensions.contains(clean)
        val newSet = if (shouldExclude) {
            current.excludedExtensions + clean
        } else {
            current.excludedExtensions - clean
        }
        updateSmartScanSettings(current.copy(excludedExtensions = newSet))
    }

    suspend fun setMinDurationSeconds(seconds: Int) {
        val current = getSmartScanSettingsSync()
        updateSmartScanSettings(current.copy(minDurationSeconds = seconds.coerceAtLeast(0)))
    }

    suspend fun addFilenamePattern(pattern: String) {
        val clean = pattern.trim()
        if (clean.isBlank()) return
        val current = getSmartScanSettingsSync()
        if (current.filenamePatterns.none { it.pattern.equals(clean, ignoreCase = true) }) {
            val updated = current.copy(
                filenamePatterns = current.filenamePatterns + FilenamePatternRule(clean, isEnabled = true)
            )
            updateSmartScanSettings(updated)
        }
    }

    suspend fun removeFilenamePattern(pattern: String) {
        val current = getSmartScanSettingsSync()
        val updated = current.copy(
            filenamePatterns = current.filenamePatterns.filterNot { it.pattern.equals(pattern, ignoreCase = true) }
        )
        updateSmartScanSettings(updated)
    }

    suspend fun toggleFilenamePattern(pattern: String, isEnabled: Boolean) {
        val current = getSmartScanSettingsSync()
        val updated = current.copy(
            filenamePatterns = current.filenamePatterns.map {
                if (it.pattern.equals(pattern, ignoreCase = true)) it.copy(isEnabled = isEnabled) else it
            }
        )
        updateSmartScanSettings(updated)
    }

    suspend fun hideTrack(song: Song) {
        val current = getSmartScanSettingsSync()
        val canonicalSource = song.sourceUri.ifBlank { song.mediaUri }
        if (current.hiddenTracks.none { it.sourceUri == canonicalSource || it.songId == song.id }) {
            val item = HiddenTrackItem(
                sourceUri = canonicalSource,
                songId = song.id,
                title = song.title,
                artist = song.artist,
                filePath = song.filePath
            )
            updateSmartScanSettings(current.copy(hiddenTracks = current.hiddenTracks + item))
        }
    }

    suspend fun hideFolder(folderPath: String, displayName: String? = null) {
        val clean = folderPath.trim()
        if (clean.isBlank()) return
        val name = displayName ?: File(clean).name.ifBlank { clean }
        addExcludedFolder(clean, name)
    }

    suspend fun hideSimilar(pattern: String) {
        addFilenamePattern(pattern)
    }

    suspend fun unhideTrack(sourceUriOrId: String) {
        val current = getSmartScanSettingsSync()
        val updated = current.copy(
            hiddenTracks = current.hiddenTracks.filterNot { it.sourceUri == sourceUriOrId || it.songId == sourceUriOrId }
        )
        updateSmartScanSettings(updated)
    }

    suspend fun restoreAllHiddenTracks() {
        val current = getSmartScanSettingsSync()
        updateSmartScanSettings(current.copy(hiddenTracks = emptyList()))
    }

    suspend fun applyCleanMusicLibraryPreset() {
        val current = getSmartScanSettingsSync()
        val defaultFolders = SmartScanSettings.defaultExcludedFolders()
        val defaultPatterns = SmartScanSettings.defaultFilenamePatterns()

        val existingFolderPaths = current.excludedFolders.map { it.path.lowercase() }.toSet()
        val mergedFolders = current.excludedFolders.map { f ->
            if (f.isPreset) f.copy(isEnabled = true) else f
        }.toMutableList()
        for (df in defaultFolders) {
            if (!existingFolderPaths.contains(df.path.lowercase())) {
                mergedFolders.add(df)
            }
        }

        val existingPatternStrs = current.filenamePatterns.map { it.pattern.lowercase() }.toSet()
        val mergedPatterns = current.filenamePatterns.map { p -> p.copy(isEnabled = true) }.toMutableList()
        for (dp in defaultPatterns) {
            if (!existingPatternStrs.contains(dp.pattern.lowercase())) {
                mergedPatterns.add(dp)
            }
        }

        val updated = current.copy(
            minDurationSeconds = 10,
            excludedExtensions = current.excludedExtensions + setOf("amr", "opus"),
            excludedFolders = mergedFolders,
            filenamePatterns = mergedPatterns
        )
        updateSmartScanSettings(updated)
    }
}
