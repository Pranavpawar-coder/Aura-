package com.example.aura.data.audio

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AudioFormatSupportTest {

    @Test
    fun testResolveMimeTypesForAllFormats() {
        val formats = mapOf(
            "song.flac" to "audio/flac",
            "track.mp3" to "audio/mpeg",
            "audio.wav" to "audio/wav",
            "music.m4a" to "audio/mp4",
            "sample.aac" to "audio/mp4",
            "voice.opus" to "audio/opus",
            "album.ogg" to "audio/ogg",
            "sound.alac" to "audio/alac",
            "master.aiff" to "audio/x-aiff",
            "vintage.wma" to "audio/x-ms-wma",
            "lossless.ape" to "audio/x-ape",
            "audiophile.wv" to "audio/x-wavpack",
            "stream.dsf" to "audio/x-dsd"
        )

        for ((filename, expectedMime) in formats) {
            val resolved = AudioMetadataHelper.resolveMimeType(filePath = "/storage/emulated/0/Music/$filename")
            assertEquals("Failed for format: $filename", expectedMime, resolved)
        }
    }

    @Test
    fun testAudioTechnicalDetailsProperties() {
        val details = AudioTechnicalDetails(
            codec = "FLAC",
            sampleRate = 96000,
            bitDepth = 24,
            bitrate = 1411000,
            channelCount = 2,
            isLossless = true,
            durationMs = 245000L,
            title = "Test Track",
            artist = "Aura Artist",
            album = "Aura Album",
            mimeType = "audio/flac"
        )

        assertEquals("FLAC", details.codec)
        assertEquals(245000L, details.durationMs)
        assertEquals("Test Track", details.title)
        assertEquals("Aura Artist", details.artist)
        assertEquals("Aura Album", details.album)
        assertEquals(true, details.isLossless)
    }
}
