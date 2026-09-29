package com.example.aura.domain.model.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioEffectsStateTest {

    @Test
    fun testDefaultBandsConfiguration() {
        val bands = EqualizerSettings.defaultBands()
        assertEquals("Standard EQ must have 5 bands", 5, bands.size)
        assertEquals(60, bands[0].centerFreqHz)
        assertEquals(230, bands[1].centerFreqHz)
        assertEquals(910, bands[2].centerFreqHz)
        assertEquals(3600, bands[3].centerFreqHz)
        assertEquals(14000, bands[4].centerFreqHz)

        bands.forEach { band ->
            assertEquals("Default band gain should be 0 mB", 0, band.gainMb)
            assertEquals("Default band gain in dB should be 0.0", 0.0f, band.gainDb, 0.001f)
        }
    }

    @Test
    fun testParametricSettingsDefaults() {
        val bands = ParametricSettings.defaultParametricBands()
        assertEquals(5, bands.size)

        val lowShelf = bands[0]
        assertEquals(FilterType.LOW_SHELF, lowShelf.filterType)
        assertEquals(80f, lowShelf.frequencyHz, 0.1f)

        val highShelf = bands[4]
        assertEquals(FilterType.HIGH_SHELF, highShelf.filterType)
        assertEquals(12000f, highShelf.frequencyHz, 0.1f)
    }

    @Test
    fun testActiveBadgeLabelPriority() {
        // When nothing enabled, badge is null
        val stateDefault = AudioEffectsState()
        assertNull(stateDefault.activeBadgeLabel)
        assertFalse(stateDefault.isAdvancedProcessingActive)

        // 4D has highest badge priority
        val state4D = AudioEffectsState(
            spatial4d = Spatial4DSettings(enabled = true),
            equalizer = EqualizerSettings(enabled = true)
        )
        assertEquals("4D", state4D.activeBadgeLabel)
        assertTrue(state4D.isAdvancedProcessingActive)

        // Preview also triggers 4D badge
        val statePreview = AudioEffectsState(
            spatial4d = Spatial4DSettings(enabled = false, isPreviewActive = true)
        )
        assertEquals("4D", statePreview.activeBadgeLabel)
        assertTrue(statePreview.isAdvancedProcessingActive)

        // Parametric EQ badge
        val statePEq = AudioEffectsState(
            parametric = ParametricSettings(enabled = true)
        )
        assertEquals("P-EQ", statePEq.activeBadgeLabel)

        // Graphic EQ badge
        val stateEq = AudioEffectsState(
            equalizer = EqualizerSettings(enabled = true)
        )
        assertEquals("EQ", stateEq.activeBadgeLabel)

        // Bass/Treble badge
        val stateBass = AudioEffectsState(
            bassTreble = BassTrebleSettings(bassEnabled = true)
        )
        assertEquals("BASS", stateBass.activeBadgeLabel)
    }

    @Test
    fun testDeviceProfileDefaults() {
        val speakerProfile = AudioProfile.defaultProfiles()[AudioDeviceType.PHONE_SPEAKER]
        assertNotNull(speakerProfile)
        assertEquals(AudioDeviceType.PHONE_SPEAKER, speakerProfile?.deviceType)
        // Phone speaker should have compressor enabled by default for protection and clarity
        assertTrue("Phone speaker profile should enable compressor", speakerProfile?.compressor?.enabled == true)

        val dacProfile = AudioProfile.defaultProfiles()[AudioDeviceType.USB_DAC]
        assertNotNull(dacProfile)
        assertEquals(AudioDeviceType.USB_DAC, dacProfile?.deviceType)
        // USB DAC should have pure bit-perfect flat response by default
        assertFalse("USB DAC should be pure/flat by default", dacProfile?.equalizer?.enabled == true)
    }

    @Test
    fun testAudioOutputInfoFormatting() {
        val info = AudioOutputInfo(
            sourceCodec = "FLAC",
            sourceBitrate = 1411,
            sourceSampleRate = 96000,
            sourceBitDepth = 24,
            sourceChannels = 2,
            isLossless = true,
            isHiRes = true,
            deviceName = "Sony WH-1000XM5 (LDAC)",
            deviceType = AudioDeviceType.BLUETOOTH_HEADPHONES,
            outputSampleRate = 96000,
            outputEncoding = "24-bit PCM"
        )

        assertEquals("FLAC", info.sourceCodec)
        assertTrue("Source quality should be flagged lossless", info.isLossless)
        assertTrue("Accurate Hi-Res flag when 24-bit 96kHz", info.isHiRes)
        assertEquals(AudioDeviceType.BLUETOOTH_HEADPHONES, info.deviceType)
    }
}
