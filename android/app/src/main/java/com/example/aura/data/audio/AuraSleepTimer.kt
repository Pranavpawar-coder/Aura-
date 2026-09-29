package com.example.aura.data.audio

import com.example.aura.domain.model.audio.SleepTimerSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Robust Sleep Timer implementation with countdown updates and smooth volume fade-out.
 */
class AuraSleepTimer(
    private val scope: CoroutineScope,
    private val onFadeVolume: (Float) -> Unit,
    private val onTimeExpired: () -> Unit
) {
    private var timerJob: Job? = null
    private val _settings = MutableStateFlow(SleepTimerSettings())
    val settings: StateFlow<SleepTimerSettings> = _settings.asStateFlow()

    fun startTimer(minutes: Int, fadeOut: Boolean = true) {
        startTimerSeconds(minutes * 60L, fadeOut)
    }

    fun startTimerSeconds(totalSeconds: Long, fadeOut: Boolean = true) {
        cancelTimer()
        if (totalSeconds <= 0L) return

        _settings.value = SleepTimerSettings(
            isRunning = true,
            remainingSeconds = totalSeconds,
            totalSeconds = totalSeconds,
            fadeOut = fadeOut
        )

        timerJob = scope.launch(Dispatchers.Default) {
            var remaining = totalSeconds
            val fadeThreshold = 15L // Start fading out 15 seconds before completion

            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                _settings.value = _settings.value.copy(remainingSeconds = remaining)

                if (fadeOut && remaining <= fadeThreshold) {
                    val fadeFraction = (remaining.toFloat() / fadeThreshold.toFloat()).coerceIn(0.05f, 1.0f)
                    onFadeVolume(fadeFraction)
                }
            }

            if (isActive) {
                _settings.value = SleepTimerSettings(isRunning = false, remainingSeconds = 0, totalSeconds = 0)
                onFadeVolume(1.0f) // Reset volume for next session
                onTimeExpired()
            }
        }
    }

    fun startTimerEndOfSong(fadeOut: Boolean = true) {
        cancelTimer()
        _settings.value = SleepTimerSettings(
            isRunning = true,
            remainingSeconds = -1L, // -1L sentinel indicates "End of Current Track"
            totalSeconds = -1L,
            fadeOut = fadeOut
        )
    }

    fun isEndOfSongTimerActive(): Boolean = _settings.value.isRunning && _settings.value.remainingSeconds == -1L

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _settings.value = SleepTimerSettings(isRunning = false, remainingSeconds = 0, totalSeconds = 0)
        onFadeVolume(1.0f)
    }
}
