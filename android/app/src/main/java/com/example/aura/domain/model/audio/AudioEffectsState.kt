package com.example.aura.domain.model.audio

import java.util.UUID

enum class EqualizerPreset(val displayName: String) {
    FLAT("Flat"),
    ACOUSTIC("Acoustic"),
    BASS("Bass"),
    BASS_BOOST("Bass Boost"),
    CLASSICAL("Classical"),
    DANCE("Dance"),
    DEEP("Deep"),
    ELECTRONIC("Electronic"),
    HIP_HOP("Hip-Hop"),
    JAZZ("Jazz"),
    POP("Pop"),
    ROCK("Rock"),
    VOCAL("Vocal"),
    CUSTOM("Custom")
}

enum class FilterType(val displayName: String) {
    PEAKING("Peaking"),
    LOW_SHELF("Low Shelf"),
    HIGH_SHELF("High Shelf"),
    LOW_PASS("Low Pass"),
    HIGH_PASS("High Pass")
}

data class ParametricBand(
    val id: String = UUID.randomUUID().toString(),
    val frequencyHz: Float = 1000f,
    val gainDb: Float = 0f,
    val qFactor: Float = 1.0f,
    val filterType: FilterType = FilterType.PEAKING,
    val enabled: Boolean = true
)

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val gainMb: Int = 0 // Millibels (-1500 to +1500 mB = -15dB to +15dB)
) {
    val gainDb: Float
        get() = gainMb / 100f
}

enum class ReverbPreset(val displayName: String) {
    OFF("Off"),
    SMALL_ROOM("Small Room"),
    ROOM("Room"),
    LARGE_ROOM("Large Room"),
    HALL("Hall"),
    LARGE_HALL("Large Hall"),
    CONCERT("Concert"),
    STUDIO("Studio")
}

enum class ReplayGainMode(val displayName: String) {
    OFF("Off"),
    TRACK("Track"),
    ALBUM("Album")
}

enum class SpatialMovementMode(val displayName: String) {
    STATIC("Static"),
    SLOW_ORBIT("Slow Orbit"),
    MEDIUM_ORBIT("Medium Orbit"),
    FAST_ORBIT("Fast Orbit"),
    CUSTOM("Custom")
}

enum class SpatialPreset(val displayName: String) {
    NORMAL("Normal"),
    WIDE("Wide"),
    SURROUND("Surround"),
    FOUR_D("4D"),
    FOUR_D_SLOW("4D Slow"),
    FOUR_D_FAST("4D Fast"),
    CINEMA("Cinema"),
    CONCERT("Concert"),
    STUDIO("Studio")
}

enum class VisualizerStyle(val displayName: String) {
    SPECTRUM("Spectrum"),
    BARS("Bars"),
    WAVEFORM("Waveform"),
    CIRCULAR("Circular"),
    MINIMAL("Minimal")
}

enum class AudioDeviceType(val displayName: String) {
    PHONE_SPEAKER("Phone Speaker"),
    WIRED_HEADPHONES("Wired Headphones"),
    BLUETOOTH_HEADPHONES("Bluetooth Headphones"),
    BLUETOOTH_SPEAKER("Bluetooth Speaker"),
    USB_DAC("USB DAC"),
    OTHER("External Audio")
}

data class EqualizerSettings(
    val enabled: Boolean = false,
    val preset: EqualizerPreset = EqualizerPreset.FLAT,
    val bands: List<EqualizerBand> = defaultBands(),
    val preampGainDb: Float = 0f, // -12dB to +12dB
    val autoHeadroomEnabled: Boolean = true
) {
    companion object {
        fun defaultBands(): List<EqualizerBand> = listOf(
            EqualizerBand(0, 60, 0),
            EqualizerBand(1, 230, 0),
            EqualizerBand(2, 910, 0),
            EqualizerBand(3, 3600, 0),
            EqualizerBand(4, 14000, 0)
        )
    }
}

data class ParametricSettings(
    val enabled: Boolean = false,
    val bands: List<ParametricBand> = defaultParametricBands()
) {
    companion object {
        fun defaultParametricBands(): List<ParametricBand> = listOf(
            ParametricBand(frequencyHz = 80f, gainDb = 0f, qFactor = 0.8f, filterType = FilterType.LOW_SHELF),
            ParametricBand(frequencyHz = 250f, gainDb = 0f, qFactor = 1.0f, filterType = FilterType.PEAKING),
            ParametricBand(frequencyHz = 1000f, gainDb = 0f, qFactor = 1.4f, filterType = FilterType.PEAKING),
            ParametricBand(frequencyHz = 4000f, gainDb = 0f, qFactor = 1.2f, filterType = FilterType.PEAKING),
            ParametricBand(frequencyHz = 12000f, gainDb = 0f, qFactor = 0.7f, filterType = FilterType.HIGH_SHELF)
        )
    }
}

data class BassTrebleSettings(
    val bassEnabled: Boolean = false,
    val bassLevel: Float = 0f, // -10dB to +10dB
    val trebleEnabled: Boolean = false,
    val trebleLevel: Float = 0f // -10dB to +10dB
)

data class BalanceSettings(
    val balance: Float = 0f // -1.0f (Full Left) to 0.0f (Center) to +1.0f (Full Right)
)

data class CompressorSettings(
    val enabled: Boolean = false,
    val thresholdDb: Float = -18f, // -40dB to 0dB
    val ratio: Float = 4.0f, // 1:1 to 20:1
    val attackMs: Float = 15f, // 0.1ms to 100ms
    val releaseMs: Float = 100f, // 10ms to 1000ms
    val makeupGainDb: Float = 2.0f, // 0dB to 24dB
    val isAdvancedMode: Boolean = false
)

data class LimiterSettings(
    val enabled: Boolean = true,
    val ceilingDb: Float = -0.5f // -12dB to 0dB
)

data class ReverbSettings(
    val enabled: Boolean = false,
    val preset: ReverbPreset = ReverbPreset.OFF,
    val amount: Float = 0.3f, // 0f to 1f
    val size: Float = 0.5f, // 0f to 1f
    val decayMs: Int = 1500
)

data class VirtualizerSettings(
    val enabled: Boolean = false,
    val stereoWidth: Float = 1.0f, // 0f (Mono) to 1.0f (Normal) to 2.0f (Ultra-Wide)
    val strength: Int = 500 // 0 to 1000
)

data class LoudnessSettings(
    val enabled: Boolean = false,
    val gainMb: Int = 400 // 0 to 2000 mB
)

data class ReplayGainSettings(
    val mode: ReplayGainMode = ReplayGainMode.OFF,
    val preampGainDb: Float = 0f, // -12dB to +12dB
    val trackGainDb: Float? = null,
    val albumGainDb: Float? = null
)

data class PlaybackSettings(
    val crossfadeEnabled: Boolean = false,
    val crossfadeDurationSeconds: Int = 3, // 0 to 15s
    val gaplessEnabled: Boolean = true
)

data class Spatial4DSettings(
    val enabled: Boolean = false,
    val intensity: Float = 0.7f, // 0f to 1f
    val width: Float = 1.2f, // 0f to 2f
    val depth: Float = 0.5f, // 0f to 1f
    val distance: Float = 0.3f, // 0f to 1f
    val rotationSpeed: Float = 0.5f, // 0f to 2f
    val movementMode: SpatialMovementMode = SpatialMovementMode.STATIC,
    val positionX: Float = 0f, // -1f (Left) to +1f (Right)
    val positionY: Float = 0.5f, // -1f (Back) to +1f (Front)
    val positionZ: Float = 0f, // -1f (Bottom) to +1f (Top / Height)
    val preset: SpatialPreset = SpatialPreset.NORMAL,
    val isPreviewActive: Boolean = false
)

data class VisualizerSettings(
    val enabled: Boolean = true,
    val style: VisualizerStyle = VisualizerStyle.SPECTRUM
)

data class SleepTimerSettings(
    val isRunning: Boolean = false,
    val remainingSeconds: Long = 0L,
    val totalSeconds: Long = 0L,
    val fadeOut: Boolean = true
) {
    val isActive: Boolean get() = isRunning && remainingSeconds > 0
    val formattedRemainingTime: String
        get() {
            if (!isActive) return "Off"
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return "%d:%02d".format(mins, secs)
        }
}

data class AudioOutputInfo(
    val sourceCodec: String = "Unknown",
    val sourceBitrate: Int = 0,
    val sourceSampleRate: Int = 0,
    val sourceBitDepth: Int = 0,
    val sourceChannels: Int = 2,
    val sourceFileSize: Long = 0L,
    val isLossless: Boolean = false,
    val isHiRes: Boolean = false,
    val deviceName: String = "Default Output",
    val deviceType: AudioDeviceType = AudioDeviceType.PHONE_SPEAKER,
    val outputSampleRate: Int = 48000,
    val outputEncoding: String = "16-bit PCM",
    val isDirectTransport: Boolean = false,
    val explanation: String = ""
)

data class AudioProfile(
    val deviceType: AudioDeviceType,
    val name: String,
    val equalizer: EqualizerSettings = EqualizerSettings(),
    val parametric: ParametricSettings = ParametricSettings(),
    val bassTreble: BassTrebleSettings = BassTrebleSettings(),
    val balance: BalanceSettings = BalanceSettings(),
    val compressor: CompressorSettings = CompressorSettings(),
    val limiter: LimiterSettings = LimiterSettings(),
    val reverb: ReverbSettings = ReverbSettings(),
    val virtualizer: VirtualizerSettings = VirtualizerSettings(),
    val loudness: LoudnessSettings = LoudnessSettings(),
    val spatial4d: Spatial4DSettings = Spatial4DSettings()
) {
    companion object {
        fun defaultProfiles(): Map<AudioDeviceType, AudioProfile> = mapOf(
            AudioDeviceType.PHONE_SPEAKER to AudioProfile(
                deviceType = AudioDeviceType.PHONE_SPEAKER,
                name = "Phone Speaker",
                compressor = CompressorSettings(enabled = true, thresholdDb = -12f, ratio = 3f)
            ),
            AudioDeviceType.WIRED_HEADPHONES to AudioProfile(
                deviceType = AudioDeviceType.WIRED_HEADPHONES,
                name = "Wired Headphones",
                virtualizer = VirtualizerSettings(enabled = true, strength = 300)
            ),
            AudioDeviceType.BLUETOOTH_HEADPHONES to AudioProfile(
                deviceType = AudioDeviceType.BLUETOOTH_HEADPHONES,
                name = "Bluetooth Headphones",
                spatial4d = Spatial4DSettings(enabled = true, preset = SpatialPreset.WIDE)
            ),
            AudioDeviceType.USB_DAC to AudioProfile(
                deviceType = AudioDeviceType.USB_DAC,
                name = "USB DAC",
                limiter = LimiterSettings(enabled = true, ceilingDb = -0.1f)
            )
        )
    }
}

/**
 * Single authoritative state representing all active audio processing parameters.
 */
data class AudioEffectsState(
    val equalizer: EqualizerSettings = EqualizerSettings(),
    val parametric: ParametricSettings = ParametricSettings(),
    val bassTreble: BassTrebleSettings = BassTrebleSettings(),
    val balance: BalanceSettings = BalanceSettings(),
    val compressor: CompressorSettings = CompressorSettings(),
    val limiter: LimiterSettings = LimiterSettings(),
    val reverb: ReverbSettings = ReverbSettings(),
    val virtualizer: VirtualizerSettings = VirtualizerSettings(),
    val loudness: LoudnessSettings = LoudnessSettings(),
    val replayGain: ReplayGainSettings = ReplayGainSettings(),
    val playback: PlaybackSettings = PlaybackSettings(),
    val spatial4d: Spatial4DSettings = Spatial4DSettings(),
    val visualizer: VisualizerSettings = VisualizerSettings(),
    val sleepTimer: SleepTimerSettings = SleepTimerSettings(),
    val outputInfo: AudioOutputInfo = AudioOutputInfo(),
    val currentDeviceType: AudioDeviceType = AudioDeviceType.PHONE_SPEAKER,
    val lyricsOffsetMs: Long = 0L
) {
    val isAdvancedProcessingActive: Boolean
        get() = equalizer.enabled ||
                parametric.enabled ||
                bassTreble.bassEnabled ||
                bassTreble.trebleEnabled ||
                compressor.enabled ||
                reverb.enabled ||
                virtualizer.enabled ||
                loudness.enabled ||
                spatial4d.enabled ||
                spatial4d.isPreviewActive

    val activeBadgeLabel: String?
        get() = when {
            spatial4d.enabled || spatial4d.isPreviewActive -> "4D"
            parametric.enabled -> "P-EQ"
            equalizer.enabled -> "EQ"
            compressor.enabled -> "COMP"
            reverb.enabled -> "REV"
            bassTreble.bassEnabled || bassTreble.trebleEnabled -> "BASS"
            virtualizer.enabled -> "3D"
            else -> null
        }
}
