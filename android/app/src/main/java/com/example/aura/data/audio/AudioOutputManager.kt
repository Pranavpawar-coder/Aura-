package com.example.aura.data.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.audio.AudioDeviceType
import com.example.aura.domain.model.audio.AudioOutputInfo

/**
 * Handles audio output device monitoring, technical output fidelity reporting,
 * and external USB DAC detection according to Sections 19, 20, 21 and 22.
 */
class AudioOutputManager(
    private val context: Context,
    private val onDeviceChanged: (AudioDeviceType, AudioOutputInfo) -> Unit
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var deviceCallback: AudioDeviceCallback? = null

    init {
        registerCallback()
    }

    private fun registerCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null) {
            val callback = object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrentOutput(currentSong)
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrentOutput(currentSong)
                }
            }
            audioManager.registerAudioDeviceCallback(callback, null)
            deviceCallback = callback
        }
    }

    private var currentSong: Song? = null

    fun updateCurrentOutput(song: Song?): AudioOutputInfo {
        currentSong = song
        val deviceType = detectCurrentDeviceType()
        val deviceName = detectCurrentDeviceName(deviceType)

        val srcCodec = song?.codec ?: "Unknown"
        val srcBitrate = song?.bitrate ?: 0
        val srcSampleRate = song?.sampleRate ?: 0
        val srcBitDepth = song?.bitDepth ?: 0
        val srcChannels = song?.channelCount ?: 2
        val srcFileSize = song?.fileSize ?: 0L
        val isLossless = song?.isLossless ?: false
        val isHiResSource = isLossless && (srcSampleRate >= 88200 || srcBitDepth >= 24)

        // Output capability estimation
        val (outputSampleRate, outputEncoding, isDirect, note) = when (deviceType) {
            AudioDeviceType.USB_DAC -> {
                val rate = if (srcSampleRate > 48000) srcSampleRate else 96000
                Quad(rate, "24-bit / Direct High-Res USB", true, "Direct digital USB audio stream to external DAC.")
            }
            AudioDeviceType.WIRED_HEADPHONES -> {
                val rate = if (srcSampleRate >= 96000) 96000 else 48000
                Quad(rate, "24-bit PCM (Analog DAC)", true, "High-fidelity wired analog output through internal DAC.")
            }
            AudioDeviceType.BLUETOOTH_HEADPHONES, AudioDeviceType.BLUETOOTH_SPEAKER -> {
                // Bluetooth A2DP transport resamples to 44.1 or 48 kHz lossy compressed transport
                Quad(48000, "16-bit / 48 kHz (Bluetooth A2DP Compressed)", false, "Source is decoded and encoded for Bluetooth transport (e.g. LDAC/AAC/SBC). Actual delivery is compressed.")
            }
            AudioDeviceType.PHONE_SPEAKER -> {
                Quad(48000, "16-bit / 48 kHz (Device Loudspeaker)", false, "Routed to device built-in speaker with platform acoustic protection.")
            }
            AudioDeviceType.OTHER -> {
                Quad(48000, "16-bit / 48 kHz PCM", false, "Standard Android AudioTrack output.")
            }
        }

        val qualityBadge = when {
            deviceType == AudioDeviceType.USB_DAC && isHiResSource -> "Hi-Res Direct USB (Bit-Perfect)"
            (deviceType == AudioDeviceType.WIRED_HEADPHONES || deviceType == AudioDeviceType.USB_DAC) && isLossless -> "Lossless Analog Output"
            deviceType == AudioDeviceType.BLUETOOTH_HEADPHONES -> "Bluetooth Output (A2DP)"
            isLossless -> "Lossless Decoder Output"
            else -> "Standard Output"
        }

        val outputInfo = AudioOutputInfo(
            sourceCodec = srcCodec,
            sourceBitrate = srcBitrate,
            sourceSampleRate = srcSampleRate,
            sourceBitDepth = srcBitDepth,
            sourceChannels = srcChannels,
            sourceFileSize = srcFileSize,
            isLossless = isLossless,
            isHiRes = isHiResSource,
            deviceName = deviceName,
            deviceType = deviceType,
            outputSampleRate = outputSampleRate,
            outputEncoding = outputEncoding,
            isDirectTransport = isDirect,
            explanation = note
        )

        onDeviceChanged(deviceType, outputInfo)
        return outputInfo
    }

    fun detectCurrentDeviceType(): AudioDeviceType {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null) {
            try {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (dev in devices) {
                    when (dev.type) {
                        AudioDeviceInfo.TYPE_USB_DEVICE,
                        AudioDeviceInfo.TYPE_USB_HEADSET -> return AudioDeviceType.USB_DAC

                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_WIRED_HEADSET -> return AudioDeviceType.WIRED_HEADPHONES

                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> return AudioDeviceType.BLUETOOTH_HEADPHONES

                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> return AudioDeviceType.BLUETOOTH_SPEAKER
                    }
                }
            } catch (e: Exception) {
                Log.w("AudioOutputManager", "Error checking audio devices: ${e.message}")
            }
        }
        return AudioDeviceType.PHONE_SPEAKER
    }

    private fun detectCurrentDeviceName(deviceType: AudioDeviceType): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null) {
            try {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (dev in devices) {
                    val productName = dev.productName?.toString()?.trim()
                    if (!productName.isNullOrEmpty() && productName != "Speaker") {
                        return productName
                    }
                }
            } catch (_: Exception) {}
        }
        return deviceType.displayName
    }

    fun release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null && deviceCallback != null) {
            try {
                audioManager.unregisterAudioDeviceCallback(deviceCallback)
            } catch (_: Exception) {}
            deviceCallback = null
        }
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
