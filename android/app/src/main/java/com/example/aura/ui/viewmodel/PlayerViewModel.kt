package com.example.aura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.data.audio.AuraPlayerManager
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.PlaybackState
import com.example.aura.domain.model.PlayerState
import com.example.aura.domain.model.QueueState
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.audio.AudioDeviceType
import com.example.aura.domain.model.audio.AudioEffectsState
import com.example.aura.domain.model.audio.CompressorSettings
import com.example.aura.domain.model.audio.EqualizerPreset
import com.example.aura.domain.model.audio.LimiterSettings
import com.example.aura.domain.model.audio.LoudnessSettings
import com.example.aura.domain.model.audio.ParametricBand
import com.example.aura.domain.model.audio.PlaybackSettings
import com.example.aura.domain.model.audio.ReplayGainSettings
import com.example.aura.domain.model.audio.ReverbSettings
import com.example.aura.domain.model.audio.SleepTimerSettings
import com.example.aura.domain.model.audio.Spatial4DSettings
import com.example.aura.domain.model.audio.SpatialPreset
import com.example.aura.domain.model.audio.VirtualizerSettings
import com.example.aura.domain.model.audio.VisualizerSettings
import com.example.aura.domain.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerManager: AuraPlayerManager,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState
    val queueState: StateFlow<QueueState> = playerManager.queueState
    val audioEffectsState: StateFlow<AudioEffectsState> = playerManager.audioEffectsState
    val visualizerData: StateFlow<FloatArray> = playerManager.visualizerData
    val sleepTimerSettings: StateFlow<SleepTimerSettings> = playerManager.sleepTimer.settings

    private val _isFullPlayerOpen = MutableStateFlow(false)
    val isFullPlayerOpen: StateFlow<Boolean> = _isFullPlayerOpen.asStateFlow()

    private val _isQueueOpen = MutableStateFlow(false)
    val isQueueOpen: StateFlow<Boolean> = _isQueueOpen.asStateFlow()

    private val _isAudioEffectsOpen = MutableStateFlow(false)
    val isAudioEffectsOpen: StateFlow<Boolean> = _isAudioEffectsOpen.asStateFlow()

    val playerState: StateFlow<PlayerState> = combine(
        playerManager.playbackState,
        playerManager.queueState,
        _isFullPlayerOpen,
        _isQueueOpen
    ) { playback, queue, fullOpen, queueOpen ->
        PlayerState(
            isFullPlayerOpen = fullOpen,
            isMiniPlayerVisible = playback.currentSong != null && !fullOpen,
            isQueueOpen = queueOpen,
            currentTrack = playback.currentSong,
            queue = queue.songs,
            playbackState = playback.status,
            currentTime = playback.currentPositionMs,
            duration = playback.durationMs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlayerState()
    )

    private val _lyricsState = MutableStateFlow<LyricsState>(LyricsState.Loading)
    val lyricsState: StateFlow<LyricsState> = _lyricsState.asStateFlow()

    init {
        // Sync favorite state automatically with Room database
        viewModelScope.launch {
            musicRepository.getFavoriteSongIds().collect { favIds ->
                playerManager.updateFavoriteIds(favIds.toSet())
            }
        }

        playerManager.onSongChanged = { song ->
            viewModelScope.launch {
                musicRepository.recordRecentlyPlayed(song.id)
            }
            loadLyrics(song)
        }

        // Restore saved queue from Room on startup if available
        viewModelScope.launch {
            val savedQueue = musicRepository.getSavedQueue()
            if (savedQueue != null && playbackState.value.currentSong == null) {
                val (songs, index) = savedQueue
                if (songs.isNotEmpty()) {
                    val initialSong = songs.getOrElse(index) { songs.first() }
                    loadLyrics(initialSong)

                    // Restore last playback position — seek after a short buffer delay
                    val lastPosition = playerManager.getLastPlaybackPosition()
                    if (lastPosition != null) {
                        val (lastSongId, lastPositionMs) = lastPosition
                        if (lastSongId == initialSong.id && lastPositionMs > 2000L) {
                            kotlinx.coroutines.delay(600L)
                            playerManager.seekTo(lastPositionMs)
                        }
                    }
                }
            }
        }

        // Load lyrics for the initially loaded song if any
        playbackState.value.currentSong?.let {
            loadLyrics(it)
        }
    }

    fun openFullPlayer() {
        _isFullPlayerOpen.value = true
    }

    fun collapseToMiniPlayer() {
        _isFullPlayerOpen.value = false
        _isQueueOpen.value = false
        _isAudioEffectsOpen.value = false
    }

    fun openQueue() {
        _isQueueOpen.value = true
    }

    fun closeQueue() {
        _isQueueOpen.value = false
    }

    fun toggleQueue() {
        _isQueueOpen.value = !_isQueueOpen.value
    }

    fun openAudioEffects() {
        _isAudioEffectsOpen.value = true
    }

    fun closeAudioEffects() {
        _isAudioEffectsOpen.value = false
    }

    fun toggleAudioEffects() {
        _isAudioEffectsOpen.value = !_isAudioEffectsOpen.value
    }

    fun loadLyrics(song: Song) {
        viewModelScope.launch {
            _lyricsState.value = LyricsState.Loading
            try {
                val rawLyrics = musicRepository.getLyrics(song)
                val offset = audioEffectsState.value.lyricsOffsetMs
                when (rawLyrics) {
                    is LyricsState.Success -> {
                        _lyricsState.value = rawLyrics.copy(offsetMs = offset)
                    }
                    else -> _lyricsState.value = rawLyrics
                }
            } catch (_: Exception) {
                _lyricsState.value = LyricsState.Unavailable
            }
        }
    }

    fun setLyricsOffset(offsetMs: Long) {
        playerManager.setLyricsOffset(offsetMs)
        val current = _lyricsState.value
        if (current is LyricsState.Success) {
            _lyricsState.value = current.copy(offsetMs = offsetMs)
        }
    }

    fun play(song: Song, queue: List<Song> = listOf(song)) {
        playerManager.play(song, queue)
        loadLyrics(song)
        _isFullPlayerOpen.value = true
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun pause() {
        playerManager.pause()
    }

    fun resume() {
        playerManager.resume()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun seekPercent(percent: Float) {
        playerManager.seekPercent(percent)
    }

    fun next() {
        playerManager.next()
    }

    fun previous() {
        playerManager.previous()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun cycleRepeat() {
        playerManager.cycleRepeat()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id)
        }
    }

    fun addToQueue(song: Song) {
        playerManager.addToQueue(song)
    }

    fun playNext(song: Song) {
        playerManager.playNext(song)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        playerManager.reorderQueue(fromIndex, toIndex)
    }

    fun removeFromQueue(index: Int) {
        playerManager.removeFromQueue(index)
    }

    fun clearQueue() {
        playerManager.clearQueue()
    }

    // ==========================================
    // PHASE 3 AUDIO EFFECTS ACTIONS
    // ==========================================

    fun setEqualizerEnabled(enabled: Boolean) = playerManager.setEqualizerEnabled(enabled)
    fun setEqualizerPreset(preset: EqualizerPreset) = playerManager.setEqualizerPreset(preset)
    fun setEqualizerBandGain(index: Int, gainMb: Int) = playerManager.setEqualizerBandGain(index, gainMb)
    fun setEqualizerPreamp(preampGainDb: Float) = playerManager.setEqualizerPreamp(preampGainDb)
    fun setAutoHeadroomEnabled(enabled: Boolean) = playerManager.setAutoHeadroomEnabled(enabled)
    fun resetEqualizer() = playerManager.resetEqualizer()

    fun setParametricEnabled(enabled: Boolean) = playerManager.setParametricEnabled(enabled)
    fun setParametricBands(bands: List<ParametricBand>) = playerManager.setParametricBands(bands)
    fun addParametricBand(band: ParametricBand) = playerManager.addParametricBand(band)
    fun updateParametricBand(band: ParametricBand) = playerManager.updateParametricBand(band)
    fun removeParametricBand(bandId: String) = playerManager.removeParametricBand(bandId)

    fun setBassTreble(
        bassLevel: Float,
        trebleLevel: Float,
        bassEnabled: Boolean = true,
        trebleEnabled: Boolean = true
    ) = playerManager.setBassTreble(bassLevel, trebleLevel, bassEnabled, trebleEnabled)

    fun setBalance(balance: Float) = playerManager.setBalance(balance)

    fun setCompressor(settings: CompressorSettings) = playerManager.setCompressor(settings)
    fun setLimiter(settings: LimiterSettings) = playerManager.setLimiter(settings)
    fun setReverb(settings: ReverbSettings) = playerManager.setReverb(settings)
    fun setVirtualizer(settings: VirtualizerSettings) = playerManager.setVirtualizer(settings)
    fun setLoudness(settings: LoudnessSettings) = playerManager.setLoudness(settings)
    fun setReplayGain(settings: ReplayGainSettings) = playerManager.setReplayGain(settings)
    fun setPlaybackSettings(settings: PlaybackSettings) = playerManager.setPlaybackSettings(settings)

    fun setSpatial4DSettings(settings: Spatial4DSettings) = playerManager.setSpatial4DSettings(settings)
    fun setSpatial4DPreset(preset: SpatialPreset) = playerManager.setSpatial4DPreset(preset)
    fun setSpatial4DPreview(preview: Boolean) = playerManager.setSpatial4DPreview(preview)

    fun setVisualizerSettings(settings: VisualizerSettings) = playerManager.setVisualizerSettings(settings)

    fun startSleepTimer(minutes: Int, fadeOut: Boolean) = playerManager.sleepTimer.startTimer(minutes, fadeOut)
    fun startSleepTimerEndOfSong(fadeOut: Boolean = true) = playerManager.sleepTimer.startTimerEndOfSong(fadeOut)
    fun cancelSleepTimer() = playerManager.sleepTimer.cancelTimer()

    fun attachManualLyrics(uri: android.net.Uri) {
        val currentSong = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            _lyricsState.value = LyricsState.Loading
            val res = musicRepository.attachManualLyrics(currentSong.id, uri)
            _lyricsState.value = res
        }
    }

    fun refreshLyrics() {
        val currentSong = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            _lyricsState.value = LyricsState.Loading
            val res = musicRepository.getLyrics(currentSong, allowOnline = true, forceRefresh = true)
            _lyricsState.value = res
        }
    }

    fun clearLyricsCache() {
        val currentSong = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            musicRepository.clearLyricsCache(currentSong.id)
            loadLyrics(currentSong)
        }
    }

    fun clearLyricsForCurrentSong() {
        val currentSong = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            musicRepository.clearLyricsCache(currentSong.id)
            _lyricsState.value = LyricsState.Unavailable
        }
    }

    fun setRating(song: Song, rating: Int) {
        viewModelScope.launch {
            if (rating <= 0) {
                musicRepository.removeRating(song.id)
            } else {
                musicRepository.setRating(song.id, rating)
            }
        }
    }

    fun saveDeviceProfile(deviceType: AudioDeviceType) = playerManager.saveDeviceProfile(deviceType)

    override fun onCleared() {
        super.onCleared()
        // Do NOT release playerManager or AuraAudioService here so playback continues in background!
    }
}
