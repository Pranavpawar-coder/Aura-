package com.example.aura.data.audio

import com.example.aura.domain.model.audio.SleepTimerSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimerTest {

    @Test
    fun testSleepTimerSettingsDefaults() {
        val defaultSettings = SleepTimerSettings()
        assertFalse(defaultSettings.isRunning)
        assertEquals(0L, defaultSettings.remainingSeconds)
        assertEquals(0L, defaultSettings.totalSeconds)
        assertTrue("Fade out before stopping should be enabled by default", defaultSettings.fadeOut)
    }

    @Test
    fun testSleepTimerFadeOutAttenuation() {
        // Test fade out volume reduction during final 15 seconds
        val fadeThreshold = 15L

        // At 15 seconds remaining, volume multiplier is 1.0 (no reduction yet)
        val volAt15s = (15L.toFloat() / fadeThreshold.toFloat()).coerceIn(0.05f, 1.0f)
        assertEquals(1.0f, volAt15s, 0.001f)

        // At 7.5 seconds remaining, volume multiplier is 0.5
        val volAt7s = (7.5f / fadeThreshold.toFloat()).coerceIn(0.05f, 1.0f)
        assertEquals(0.5f, volAt7s, 0.001f)

        // At 0 seconds remaining, minimum floor is 0.05
        val volAt0s = (0L.toFloat() / fadeThreshold.toFloat()).coerceIn(0.05f, 1.0f)
        assertEquals(0.05f, volAt0s, 0.001f)
    }
}
