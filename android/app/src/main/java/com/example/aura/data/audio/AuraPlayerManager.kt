package com.example.aura.data.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.aura.data.local.UserPreferences
import com.example.aura.domain.model.PlaybackState
import com.example.aura.domain.model.PlaybackStatus
import com.example.aura.domain.model.QueueState
import com.example.aura.domain.model.RepeatMode
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.audio.AudioDeviceType
import com.example.aura.domain.model.audio.AudioEffectsState
import com.example.aura.domain.model.audio.CompressorSettings
import com.example.aura.domain.model.audio.EqualizerBand
import com.example.aura.domain.model.audio.EqualizerPreset
import com.example.aura.domain.model.audio.EqualizerSettings
import com.example.aura.domain.model.audio.LimiterSettings
import com.example.aura.domain.model.audio.LoudnessSettings
import com.example.aura.domain.model.audio.ParametricBand
import com.example.aura.domain.model.audio.ParametricSettings
import com.example.aura.domain.model.audio.PlaybackSettings
import com.example.aura.domain.model.audio.ReplayGainSettings
import com.example.aura.domain.model.audio.ReverbSettings
import com.example.aura.domain.model.audio.SleepTimerSettings
import com.example.aura.domain.model.audio.Spatial4DSettings
import com.example.aura.domain.model.audio.SpatialPreset
import com.example.aura.domain.model.audio.VirtualizerSettings
import com.example.aura.domain.model.audio.VisualizerSettings
import com.example.aura.domain.repository.MusicRepository
import com.example.aura.widget.AuraWidgetManager
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages player communication with AuraAudioService via AndroidX Media3 MediaController,
 * and coordinates all real-time DSP audio processing, hardware effects, 4D spatial audio,
 * output profiles, visualizer data streaming, and sleep timer.
 *
 * Architecture:
 * UI / ViewModel
 *      ↓
 * AuraPlayerManager
 *      ↓
 * MediaController / MediaSessionService
 *      ↓
 * AuraAudioProcessor (PCM DSP) + AuraHardwareEffectsManager (Native AudioEffects)
 *      ↓
 * ExoPlayer AudioSink Output
 */
class AuraPlayerManager(
    private val context: Context,
    private val musicRepository: MusicRepository? = null
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var crossfadeJob: Job? = null

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val userPreferences = UserPreferences(context.applicationContext)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _queueState = MutableStateFlow(QueueState())
    val queueState: StateFlow<QueueState> = _queueState.asStateFlow()

    private val _audioEffectsState = MutableStateFlow(AudioEffectsState())
    val audioEffectsState: StateFlow<AudioEffectsState> = _audioEffectsState.asStateFlow()

    private val _visualizerData = MutableStateFlow(FloatArray(0))
    val visualizerData: StateFlow<FloatArray> = _visualizerData.asStateFlow()

    private var currentFavoriteIds: Set<String> = emptySet()
    var onSongChanged: ((Song) -> Unit)? = null

    // Pending action queue while MediaController is connecting asynchronously
    private val pendingActions = mutableListOf<(Player) -> Unit>()

    /** Returns true when the display is on and interactive. Widget updates are skipped when false. */
    private fun isScreenOn(): Boolean {
        val pm = context.getSystemService(android.os.PowerManager::class.java)
        return pm?.isInteractive ?: true
    }

    // Sleep Timer
    val sleepTimer = AuraSleepTimer(
        scope = scope,
        onFadeVolume = { volume ->
            withPlayer { it.volume = volume }
        },
        onTimeExpired = {
            pause()
        }
    )

    // Output Device & Fidelity Manager
    private val outputManager = AudioOutputManager(context.applicationContext) { deviceType, outputInfo ->
        _audioEffectsState.value = _audioEffectsState.value.copy(
            currentDeviceType = deviceType,
            outputInfo = outputInfo
        )
    }

    init {
        initializeMediaController()
        loadPersistedAudioSettings()
        observeStateForWidgets()
    }

    private fun observeStateForWidgets() {
        scope.launch {
            var lastSongId: String? = null
            var lastStatus: PlaybackStatus? = null
            _playbackState.collect { state ->
                val songChanged = state.currentSong?.id != lastSongId
                val statusChanged = state.status != lastStatus
                if (songChanged || statusChanged) {
                    lastSongId = state.currentSong?.id
                    lastStatus = state.status
                    AuraWidgetManager.updateAllWidgets(context, state)
                }
            }
        }
    }

    private fun loadPersistedAudioSettings() {
        scope.launch {
            userPreferences.audioEffectsFlow.collect { saved ->
                _audioEffectsState.value = saved.copy(
                    outputInfo = outputManager.updateCurrentOutput(_playbackState.value.currentSong),
                    currentDeviceType = outputManager.detectCurrentDeviceType()
                )
                syncDspAndHardwareEffects(_audioEffectsState.value)
            }
        }
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, AuraAudioService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener({
            try {
                val controller = future.get()
                mediaController = controller
                setupPlayerListeners(controller)
                executePendingActions(controller)
                wireAudioProcessorVisualizer()
                syncDspAndHardwareEffects(_audioEffectsState.value)
            } catch (e: Exception) {
                android.util.Log.e("AuraPlayerManager", "Failed to connect MediaController", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun wireAudioProcessorVisualizer() {
        AuraAudioService.audioProcessor?.onAudioFrameListener = { frame ->
            _visualizerData.value = frame
        }
    }

    private fun withPlayer(action: (Player) -> Unit) {
        val controller = mediaController
        if (controller != null) {
            action(controller)
        } else {
            pendingActions.add(action)
        }
    }

    private fun executePendingActions(player: Player) {
        while (pendingActions.isNotEmpty()) {
            val action = pendingActions.removeAt(0)
            action(player)
        }
    }

    private fun createMediaItem(song: Song): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.artworkUri?.let { Uri.parse(it) })

        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(Uri.parse(song.mediaUri))
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    private fun setupPlayerListeners(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> updateStatus(PlaybackStatus.BUFFERING)
                    Player.STATE_READY -> {
                        updateStatus(if (player.isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED)
                        updateDuration(player)
                    }
                    Player.STATE_ENDED -> {
                        updateStatus(PlaybackStatus.ENDED)
                        if (sleepTimer.isEndOfSongTimerActive()) {
                            sleepTimer.cancelTimer()
                            pause()
                        }
                    }
                    Player.STATE_IDLE -> updateStatus(PlaybackStatus.IDLE)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateStatus(if (isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED)
                if (isPlaying) {
                    startProgressUpdates(player)
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("AuraPlayerManager", "Playback error: [${error.errorCodeName}] ${error.message}", error)
                updateStatus(PlaybackStatus.ERROR)
                stopProgressUpdates()

                // Graceful error recovery: advance to next playable track if in queue
                val currentQueue = _queueState.value
                if (currentQueue.songs.size > 1 && currentQueue.nextSong != null) {
                    scope.launch {
                        delay(1200)
                        if (_playbackState.value.status == PlaybackStatus.ERROR) {
                            next()
                        }
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (sleepTimer.isEndOfSongTimerActive() && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    sleepTimer.cancelTimer()
                    pause()
                    return
                }
                if (mediaItem != null) {
                    val mediaId = mediaItem.mediaId
                    val songs = _queueState.value.songs
                    val idx = songs.indexOfFirst { it.id == mediaId }
                    if (idx != -1) {
                        val song = songs[idx].copy(isFavorite = currentFavoriteIds.contains(mediaId))
                        _queueState.value = _queueState.value.copy(currentIndex = idx)
                        val initialDuration = if (song.durationMs > 0) song.durationMs else player.duration.coerceAtLeast(0L)
                        _playbackState.value = _playbackState.value.copy(
                            currentSong = song,
                            durationMs = initialDuration,
                            currentPositionMs = 0L
                        )

                        // Update technical audio output info and ReplayGain for current song
                        updateSongReplayGainAndFidelity(song)

                        onSongChanged?.invoke(song)
                        persistCurrentQueue()
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updatePosition(player)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _playbackState.value = _playbackState.value.copy(repeatMode = mode)
                _queueState.value = _queueState.value.copy(repeatMode = mode)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.value = _playbackState.value.copy(isShuffle = shuffleModeEnabled)
                _queueState.value = _queueState.value.copy(isShuffle = shuffleModeEnabled)
            }
        })
    }

    private fun updateSongReplayGainAndFidelity(song: Song) {
        val outputInfo = outputManager.updateCurrentOutput(song)
        scope.launch(Dispatchers.IO) {
            val details = AudioMetadataHelper.extractTechnicalDetails(
                context,
                Uri.parse(song.mediaUri),
                song.filePath,
                song.mimeType
            )
            val updatedReplayGain = _audioEffectsState.value.replayGain.copy(
                trackGainDb = details.trackGainDb,
                albumGainDb = details.albumGainDb
            )
            _audioEffectsState.value = _audioEffectsState.value.copy(
                replayGain = updatedReplayGain,
                outputInfo = outputInfo
            )
            AuraAudioService.audioProcessor?.updateEffects(_audioEffectsState.value)
        }
    }

    private fun updateStatus(status: PlaybackStatus) {
        _playbackState.value = _playbackState.value.copy(status = status)
    }

    private fun updateDuration(player: Player) {
        val duration = player.duration.coerceAtLeast(0L)
        if (duration > 0) {
            val current = _playbackState.value.currentSong
            val updatedSong = if (current != null && current.durationMs <= 0L) {
                current.copy(durationMs = duration)
            } else {
                current
            }
            _playbackState.value = _playbackState.value.copy(
                durationMs = duration,
                currentSong = updatedSong
            )
        }
    }

    private fun updatePosition(player: Player) {
        val pos = player.currentPosition.coerceAtLeast(0L)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = pos)
    }

    private fun startProgressUpdates(player: Player) {
        progressJob?.cancel()
        progressJob = scope.launch {
            var tick = 0
            while (isActive) {
                updatePosition(player)
                tick++
                // Only push widget RemoteViews updates when screen is on (saves battery)
                if (tick % 4 == 0 && isScreenOn()) {
                    AuraWidgetManager.updateAllWidgets(context, _playbackState.value)
                }
                if (tick % 20 == 0) { // Persist position every ~5s
                    val state = _playbackState.value
                    val songId = state.currentSong?.id
                    if (songId != null && state.currentPositionMs > 0) {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            userPreferences.saveLastPlaybackPosition(songId, state.currentPositionMs)
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
        // Persist exact pause position so resume starts at the right timestamp
        val state = _playbackState.value
        val songId = state.currentSong?.id
        if (songId != null && state.currentPositionMs > 0) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                userPreferences.saveLastPlaybackPosition(songId, state.currentPositionMs)
            }
        }
        AuraWidgetManager.updateAllWidgets(context, _playbackState.value)
    }

    private fun ensureServiceStarted() {
        try {
            val intent = Intent(context, AuraAudioService::class.java)
            context.startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("AuraPlayerManager", "Failed to start AuraAudioService", e)
        }
    }

    private fun executeWithCrossfade(onTransition: () -> Unit) {
        val crossfade = _audioEffectsState.value.playback
        if (!crossfade.crossfadeEnabled || crossfade.crossfadeDurationSeconds <= 0) {
            onTransition()
            return
        }

        crossfadeJob?.cancel()
        crossfadeJob = scope.launch {
            val steps = 8
            val stepDelay = ((crossfade.crossfadeDurationSeconds.coerceAtMost(3) * 500L) / steps).coerceAtLeast(15L)

            // Smooth fade out
            for (step in steps downTo 1) {
                withPlayer { it.volume = (step.toFloat() / steps.toFloat()) }
                delay(stepDelay)
            }

            onTransition()

            // Smooth fade in
            for (step in 1..steps) {
                withPlayer { it.volume = (step.toFloat() / steps.toFloat()) }
                delay(stepDelay)
            }
            withPlayer { it.volume = 1.0f }
        }
    }

    fun play(song: Song, queue: List<Song> = listOf(song)) {
        ensureServiceStarted()

        val index = queue.indexOfFirst { it.id == song.id }.let { if (it == -1) 0 else it }
        val updatedQueueState = _queueState.value.withNewQueue(queue, index)
        _queueState.value = updatedQueueState

        val activeSong = song.copy(isFavorite = currentFavoriteIds.contains(song.id))

        _playbackState.value = _playbackState.value.copy(
            currentSong = activeSong,
            durationMs = song.durationMs,
            currentPositionMs = 0L,
            isShuffle = updatedQueueState.isShuffle,
            repeatMode = updatedQueueState.repeatMode
        )

        executeWithCrossfade {
            withPlayer { player ->
                val mediaItems = updatedQueueState.songs.map { createMediaItem(it) }
                player.setMediaItems(mediaItems, index, 0L)
                player.repeatMode = when (updatedQueueState.repeatMode) {
                    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                }
                player.shuffleModeEnabled = updatedQueueState.isShuffle
                player.prepare()
                player.play()
            }
        }

        updateSongReplayGainAndFidelity(activeSong)
        onSongChanged?.invoke(activeSong)
        persistCurrentQueue()
    }

    fun pause() {
        withPlayer { player ->
            player.pause()
            // Persist position so it survives process death
            val songId = _playbackState.value.currentSong?.id
            if (songId != null) {
                val posMs = player.currentPosition.coerceAtLeast(0L)
                _playbackState.value = _playbackState.value.copy(currentPositionMs = posMs)
                scope.launch {
                    userPreferences.saveLastPlaybackPosition(songId, posMs)
                }
            }
        }
    }

    fun resume() {
        ensureServiceStarted()
        withPlayer { it.play() }
    }

    fun togglePlayPause() {
        withPlayer { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                ensureServiceStarted()
                if (player.mediaItemCount == 0) {
                    scope.launch {
                        val saved = musicRepository?.getSavedQueue()
                        if (saved != null && saved.first.isNotEmpty()) {
                            val (songs, index) = saved
                            val song = songs.getOrElse(index) { songs.first() }
                            play(song, songs)
                        } else {
                            player.play()
                        }
                    }
                } else {
                    player.play()
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        withPlayer { player ->
            player.seekTo(positionMs.coerceIn(0L, _playbackState.value.durationMs))
            updatePosition(player)
        }
    }

    fun seekPercent(percent: Float) {
        val duration = _playbackState.value.durationMs
        if (duration > 0) {
            seekTo((duration * percent).toLong())
        }
    }

    fun next() {
        executeWithCrossfade {
            withPlayer { player ->
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                } else {
                    val nextSong = _queueState.value.nextSong
                    if (nextSong != null) {
                        play(nextSong, _queueState.value.songs)
                    } else {
                        player.pause()
                        player.seekTo(0)
                    }
                }
            }
        }
    }

    fun previous() {
        withPlayer { player ->
            if (player.currentPosition > 3000L) {
                player.seekTo(0L)
                return@withPlayer
            }
            executeWithCrossfade {
                if (player.hasPreviousMediaItem()) {
                    player.seekToPreviousMediaItem()
                } else {
                    val prevSong = _queueState.value.previousSong
                    if (prevSong != null) {
                        play(prevSong, _queueState.value.songs)
                    } else {
                        player.seekTo(0L)
                    }
                }
            }
        }
    }

    fun toggleShuffle() {
        val updated = _queueState.value.toggleShuffle()
        _queueState.value = updated
        _playbackState.value = _playbackState.value.copy(isShuffle = updated.isShuffle)
        withPlayer { it.shuffleModeEnabled = updated.isShuffle }
        persistCurrentQueue()
    }

    fun cycleRepeat() {
        val modes = RepeatMode.values()
        val nextMode = modes[(_queueState.value.repeatMode.ordinal + 1) % modes.size]
        _queueState.value = _queueState.value.copy(repeatMode = nextMode)
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
        withPlayer { player ->
            player.repeatMode = when (nextMode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }
        }
    }

    fun addToQueue(song: Song) {
        _queueState.value = _queueState.value.addToQueue(song)
        withPlayer { it.addMediaItem(createMediaItem(song)) }
        persistCurrentQueue()
    }

    fun playNext(song: Song) {
        _queueState.value = _queueState.value.playNext(song)
        withPlayer { player ->
            val insertIndex = (player.currentMediaItemIndex + 1).coerceAtMost(player.mediaItemCount)
            player.addMediaItem(insertIndex, createMediaItem(song))
        }
        persistCurrentQueue()
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        _queueState.value = _queueState.value.reorder(fromIndex, toIndex)
        withPlayer { player ->
            if (fromIndex in 0 until player.mediaItemCount && toIndex in 0 until player.mediaItemCount) {
                player.moveMediaItem(fromIndex, toIndex)
            }
        }
        persistCurrentQueue()
    }

    fun removeFromQueue(index: Int) {
        val currentIdx = _queueState.value.currentIndex
        val removingCurrent = (index == currentIdx)
        val updated = _queueState.value.removeAt(index)
        _queueState.value = updated

        withPlayer { player ->
            if (index in 0 until player.mediaItemCount) {
                player.removeMediaItem(index)
            }
        }

        if (removingCurrent) {
            val newCurrent = updated.currentSong
            if (newCurrent != null) {
                play(newCurrent, updated.songs)
            } else {
                pause()
                _playbackState.value = _playbackState.value.copy(currentSong = null)
            }
        }
        persistCurrentQueue()
    }

    fun clearQueue() {
        _queueState.value = _queueState.value.clear()
        val current = _playbackState.value.currentSong
        withPlayer { player ->
            if (current != null) {
                val mediaItem = player.currentMediaItem
                player.clearMediaItems()
                if (mediaItem != null) {
                    player.setMediaItem(mediaItem)
                }
            } else {
                player.clearMediaItems()
            }
        }
        persistCurrentQueue()
    }

    fun updateFavoriteIds(favIds: Set<String>) {
        currentFavoriteIds = favIds
        val current = _playbackState.value.currentSong
        if (current != null) {
            val isFav = favIds.contains(current.id)
            if (current.isFavorite != isFav) {
                _playbackState.value = _playbackState.value.copy(
                    currentSong = current.copy(isFavorite = isFav)
                )
            }
        }
    }

    fun toggleFavorite(song: Song? = _playbackState.value.currentSong) {
        val target = song ?: return
        val currentFav = currentFavoriteIds.contains(target.id)
        val newFav = !currentFav
        val newFavIds = if (newFav) currentFavoriteIds + target.id else currentFavoriteIds - target.id
        updateFavoriteIds(newFavIds)
        if (musicRepository != null) {
            scope.launch {
                musicRepository.toggleFavorite(target.id)
            }
        }
        AuraWidgetManager.updateAllWidgets(context, _playbackState.value)
    }

    /** Returns the last persisted (songId, positionMs) pair, or null if nothing was saved. */
    suspend fun getLastPlaybackPosition(): Pair<String, Long>? {
        return userPreferences.getLastPlaybackPosition()
    }

    private fun persistCurrentQueue() {
        val q = _queueState.value
        if (musicRepository != null && q.songs.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                musicRepository.saveQueue(q.songs, q.currentIndex)
            }
        }
    }


    // ==========================================
    // AUDIO PROCESSING & EFFECT CONTROL API
    // ==========================================

    private fun applyAudioEffectsState(newState: AudioEffectsState, saveToPrefs: Boolean = true) {
        _audioEffectsState.value = newState
        syncDspAndHardwareEffects(newState)
        if (saveToPrefs) {
            scope.launch {
                userPreferences.saveAudioEffects(newState)
            }
        }
    }

    private fun syncDspAndHardwareEffects(state: AudioEffectsState) {
        AuraAudioService.audioProcessor?.updateEffects(state)
        AuraAudioService.hardwareEffectsManager?.applyEffectsState(state)
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        val eq = _audioEffectsState.value.equalizer.copy(enabled = enabled)
        applyAudioEffectsState(_audioEffectsState.value.copy(equalizer = eq))
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        val bands = when (preset) {
            EqualizerPreset.FLAT -> EqualizerSettings.defaultBands()
            EqualizerPreset.ACOUSTIC -> listOf(
                EqualizerBand(0, 60, 400),
                EqualizerBand(1, 230, 300),
                EqualizerBand(2, 910, 100),
                EqualizerBand(3, 3600, 250),
                EqualizerBand(4, 14000, 350)
            )
            EqualizerPreset.BASS, EqualizerPreset.BASS_BOOST -> listOf(
                EqualizerBand(0, 60, 600),
                EqualizerBand(1, 230, 450),
                EqualizerBand(2, 910, 100),
                EqualizerBand(3, 3600, 0),
                EqualizerBand(4, 14000, 0)
            )
            EqualizerPreset.CLASSICAL -> listOf(
                EqualizerBand(0, 60, 400),
                EqualizerBand(1, 230, 250),
                EqualizerBand(2, 910, -100),
                EqualizerBand(3, 3600, 200),
                EqualizerBand(4, 14000, 300)
            )
            EqualizerPreset.DANCE -> listOf(
                EqualizerBand(0, 60, 500),
                EqualizerBand(1, 230, 400),
                EqualizerBand(2, 910, 150),
                EqualizerBand(3, 3600, 250),
                EqualizerBand(4, 14000, 400)
            )
            EqualizerPreset.DEEP -> listOf(
                EqualizerBand(0, 60, 500),
                EqualizerBand(1, 230, 350),
                EqualizerBand(2, 910, 0),
                EqualizerBand(3, 3600, -100),
                EqualizerBand(4, 14000, -250)
            )
            EqualizerPreset.ELECTRONIC -> listOf(
                EqualizerBand(0, 60, 550),
                EqualizerBand(1, 230, 350),
                EqualizerBand(2, 910, 0),
                EqualizerBand(3, 3600, 200),
                EqualizerBand(4, 14000, 450)
            )
            EqualizerPreset.HIP_HOP -> listOf(
                EqualizerBand(0, 60, 600),
                EqualizerBand(1, 230, 400),
                EqualizerBand(2, 910, 0),
                EqualizerBand(3, 3600, 150),
                EqualizerBand(4, 14000, 300)
            )
            EqualizerPreset.JAZZ -> listOf(
                EqualizerBand(0, 60, 300),
                EqualizerBand(1, 230, 200),
                EqualizerBand(2, 910, -100),
                EqualizerBand(3, 3600, 150),
                EqualizerBand(4, 14000, 350)
            )
            EqualizerPreset.POP -> listOf(
                EqualizerBand(0, 60, -100),
                EqualizerBand(1, 230, 200),
                EqualizerBand(2, 910, 400),
                EqualizerBand(3, 3600, 200),
                EqualizerBand(4, 14000, -100)
            )
            EqualizerPreset.ROCK -> listOf(
                EqualizerBand(0, 60, 450),
                EqualizerBand(1, 230, 200),
                EqualizerBand(2, 910, -150),
                EqualizerBand(3, 3600, 250),
                EqualizerBand(4, 14000, 450)
            )
            EqualizerPreset.VOCAL -> listOf(
                EqualizerBand(0, 60, -200),
                EqualizerBand(1, 230, 100),
                EqualizerBand(2, 910, 500),
                EqualizerBand(3, 3600, 350),
                EqualizerBand(4, 14000, 100)
            )
            EqualizerPreset.CUSTOM -> _audioEffectsState.value.equalizer.bands
        }
        val eq = _audioEffectsState.value.equalizer.copy(preset = preset, bands = bands)
        applyAudioEffectsState(_audioEffectsState.value.copy(equalizer = eq))
    }

    fun setEqualizerBandGain(index: Int, gainMb: Int) {
        val bands = _audioEffectsState.value.equalizer.bands.map {
            if (it.index == index) it.copy(gainMb = gainMb) else it
        }
        val eq = _audioEffectsState.value.equalizer.copy(
            preset = EqualizerPreset.CUSTOM,
            bands = bands
        )
        applyAudioEffectsState(_audioEffectsState.value.copy(equalizer = eq))
    }

    fun setEqualizerPreamp(preampGainDb: Float) {
        val eq = _audioEffectsState.value.equalizer.copy(preampGainDb = preampGainDb.coerceIn(-12f, 12f))
        applyAudioEffectsState(_audioEffectsState.value.copy(equalizer = eq))
    }

    fun setAutoHeadroomEnabled(enabled: Boolean) {
        val eq = _audioEffectsState.value.equalizer.copy(autoHeadroomEnabled = enabled)
        applyAudioEffectsState(_audioEffectsState.value.copy(equalizer = eq))
    }

    fun resetEqualizer() {
        val eq = EqualizerSettings(
            enabled = _audioEffectsState.value.equalizer.enabled,
            preset = EqualizerPreset.FLAT,
            bands = EqualizerSettings.defaultBands(),
            preampGainDb = 0f,
            autoHeadroomEnabled = true
        )
        applyAudioEffectsState(_audioEffectsState.value.copy(equalizer = eq))
    }

    fun setParametricEnabled(enabled: Boolean) {
        val peq = _audioEffectsState.value.parametric.copy(enabled = enabled)
        applyAudioEffectsState(_audioEffectsState.value.copy(parametric = peq))
    }

    fun setParametricBands(bands: List<ParametricBand>) {
        val peq = _audioEffectsState.value.parametric.copy(bands = bands)
        applyAudioEffectsState(_audioEffectsState.value.copy(parametric = peq))
    }

    fun addParametricBand(band: ParametricBand) {
        val bands = _audioEffectsState.value.parametric.bands + band
        setParametricBands(bands)
    }

    fun updateParametricBand(band: ParametricBand) {
        val bands = _audioEffectsState.value.parametric.bands.map {
            if (it.id == band.id) band else it
        }
        setParametricBands(bands)
    }

    fun removeParametricBand(bandId: String) {
        val bands = _audioEffectsState.value.parametric.bands.filterNot { it.id == bandId }
        setParametricBands(bands)
    }

    fun setBassTreble(
        bassLevel: Float,
        trebleLevel: Float,
        bassEnabled: Boolean = true,
        trebleEnabled: Boolean = true
    ) {
        val bt = _audioEffectsState.value.bassTreble.copy(
            bassEnabled = bassEnabled,
            bassLevel = bassLevel,
            trebleEnabled = trebleEnabled,
            trebleLevel = trebleLevel
        )
        applyAudioEffectsState(_audioEffectsState.value.copy(bassTreble = bt))
    }

    fun setBalance(balance: Float) {
        val b = _audioEffectsState.value.balance.copy(balance = balance)
        applyAudioEffectsState(_audioEffectsState.value.copy(balance = b))
    }

    fun setCompressor(settings: CompressorSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(compressor = settings))
    }

    fun setLimiter(settings: LimiterSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(limiter = settings))
    }

    fun setReverb(settings: ReverbSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(reverb = settings))
    }

    fun setVirtualizer(settings: VirtualizerSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(virtualizer = settings))
    }

    fun setLoudness(settings: LoudnessSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(loudness = settings))
    }

    fun setReplayGain(settings: ReplayGainSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(replayGain = settings))
    }

    fun setPlaybackSettings(settings: PlaybackSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(playback = settings))
    }

    fun setSpatial4DSettings(settings: Spatial4DSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(spatial4d = settings))
    }

    fun setSpatial4DPreset(preset: SpatialPreset) {
        val s = _audioEffectsState.value.spatial4d.copy(preset = preset)
        applyAudioEffectsState(_audioEffectsState.value.copy(spatial4d = s))
    }

    fun setSpatial4DPreview(previewActive: Boolean) {
        val s = _audioEffectsState.value.spatial4d.copy(isPreviewActive = previewActive)
        // Preview does not overwrite persistent saved profile
        _audioEffectsState.value = _audioEffectsState.value.copy(spatial4d = s)
        syncDspAndHardwareEffects(_audioEffectsState.value)
    }

    fun setVisualizerSettings(settings: VisualizerSettings) {
        applyAudioEffectsState(_audioEffectsState.value.copy(visualizer = settings))
    }

    fun setLyricsOffset(offsetMs: Long) {
        applyAudioEffectsState(_audioEffectsState.value.copy(lyricsOffsetMs = offsetMs))
    }

    fun saveDeviceProfile(deviceType: AudioDeviceType) {
        scope.launch {
            userPreferences.saveDeviceProfile(deviceType, _audioEffectsState.value)
        }
    }

    fun release() {
        // Persist final position before tearing down
        val state = _playbackState.value
        val songId = state.currentSong?.id
        if (songId != null) {
            scope.launch {
                userPreferences.saveLastPlaybackPosition(songId, state.currentPositionMs)
            }
        }
        stopProgressUpdates()
        crossfadeJob?.cancel()
        sleepTimer.cancelTimer()
        outputManager.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        scope.cancel() // Cancel all coroutines — prevents leaks on process recreation
    }
}

object AuraPlayerSingleton {
    @Volatile
    private var instance: AuraPlayerManager? = null

    fun getPlayerManager(context: Context, musicRepository: MusicRepository? = null): AuraPlayerManager {
        return instance ?: synchronized(this) {
            instance ?: AuraPlayerManager(context.applicationContext, musicRepository).also { instance = it }
        }
    }

    /**
     * Release the current player manager and clear the singleton instance.
     * Must be called so that the next [getPlayerManager] creates a fresh, live manager
     * rather than returning a released/dead one.
     */
    fun release() {
        synchronized(this) {
            instance?.release()
            instance = null
        }
    }
}
