package com.example.aura.data.exclusion

import com.example.aura.data.local.entity.SongEntity
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.exclusion.ExcludedFolderRule
import com.example.aura.domain.model.exclusion.FilenamePatternRule
import com.example.aura.domain.model.exclusion.HiddenTrackItem
import com.example.aura.domain.model.exclusion.SmartScanSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicExclusionAndFilteringTest {

    @Test
    fun testFolderExclusion_recursiveAndExact() {
        val settings = SmartScanSettings(
            excludedFolders = listOf(
                ExcludedFolderRule("/storage/emulated/0/Recordings", "Recordings", isEnabled = true),
                ExcludedFolderRule("WhatsApp Audio", "WhatsApp Audio", isEnabled = true),
                ExcludedFolderRule("Ringtones", "Ringtones", isEnabled = true)
            )
        )
        val filter = LibraryExclusionFilter(settings)

        // Exact match
        assertTrue(filter.isFolderExcluded("/storage/emulated/0/Recordings"))
        // Subfolder match
        assertTrue(filter.isFolderExcluded("/storage/emulated/0/Recordings/VoiceNotes/2026"))
        // Relative substring match
        assertTrue(filter.isFolderExcluded("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio/Sent"))
        assertTrue(filter.isFolderExcluded("/storage/emulated/0/Ringtones/Classic"))

        // Normal music folders should NOT be excluded
        assertFalse(filter.isFolderExcluded("/storage/emulated/0/Music"))
        assertFalse(filter.isFolderExcluded("/storage/emulated/0/Music/Coldplay/Parachutes"))
        assertFalse(filter.isFolderExcluded("/storage/emulated/0/Download/Albums"))
    }

    @Test
    fun testFolderExclusion_disabledRuleIsIgnored() {
        val settings = SmartScanSettings(
            excludedFolders = listOf(
                ExcludedFolderRule("/storage/emulated/0/Recordings", "Recordings", isEnabled = false)
            )
        )
        val filter = LibraryExclusionFilter(settings)

        assertFalse(filter.isFolderExcluded("/storage/emulated/0/Recordings"))
        assertFalse(filter.isFolderExcluded("/storage/emulated/0/Recordings/sample.mp3"))
    }

    @Test
    fun testFileExtensionExclusion() {
        val settings = SmartScanSettings(
            excludedExtensions = setOf("amr", "opus", "wav")
        )
        val filter = LibraryExclusionFilter(settings)

        assertTrue(filter.isExtensionExcluded("amr"))
        assertTrue(filter.isExtensionExcluded("AMR"))
        assertTrue(filter.isExtensionExcluded(".opus"))
        assertTrue(filter.isExtensionExcluded("wav"))

        // Common music extensions should NOT be excluded
        assertFalse(filter.isExtensionExcluded("mp3"))
        assertFalse(filter.isExtensionExcluded("flac"))
        assertFalse(filter.isExtensionExcluded("m4a"))
        assertFalse(filter.isExtensionExcluded("aac"))
    }

    @Test
    fun testDurationFilter() {
        val settings = SmartScanSettings(
            minDurationSeconds = 15 // 15 seconds = 15,000 ms
        )
        val filter = LibraryExclusionFilter(settings)

        // Audio shorter than 15s should be excluded
        assertTrue(filter.isDurationExcluded(1000L))  // 1s
        assertTrue(filter.isDurationExcluded(5000L))  // 5s
        assertTrue(filter.isDurationExcluded(14999L)) // 14.999s

        // Audio >= 15s should NOT be excluded
        assertFalse(filter.isDurationExcluded(15000L)) // 15.000s
        assertFalse(filter.isDurationExcluded(180000L)) // 3 minutes
    }

    @Test
    fun testDurationFilter_disabledWhenZero() {
        val settings = SmartScanSettings(
            minDurationSeconds = 0 // Off
        )
        val filter = LibraryExclusionFilter(settings)

        assertFalse(filter.isDurationExcluded(500L))
        assertFalse(filter.isDurationExcluded(1000L))
        assertFalse(filter.isDurationExcluded(60000L))
    }

    @Test
    fun testFilenamePattern_keywordContains() {
        val settings = SmartScanSettings(
            filenamePatterns = listOf(
                FilenamePatternRule("voice", isEnabled = true),
                FilenamePatternRule("alarm", isEnabled = true),
                FilenamePatternRule("notification", isEnabled = true)
            )
        )
        val filter = LibraryExclusionFilter(settings)

        assertTrue(filter.isFilenameExcluded("my_voice_clip.mp3"))
        assertTrue(filter.isFilenameExcluded("Voice-Memo-01.m4a"))
        assertTrue(filter.isFilenameExcluded("Morning_Alarm.ogg"))
        assertTrue(filter.isFilenameExcluded("slack_notification_sound.mp3"))

        assertFalse(filter.isFilenameExcluded("Bohemian Rhapsody.mp3"))
        assertFalse(filter.isFilenameExcluded("Hotel California.flac"))
    }

    @Test
    fun testFilenamePattern_wildcardPrefixAndSuffix() {
        val settings = SmartScanSettings(
            filenamePatterns = listOf(
                FilenamePatternRule("AUD-*", isEnabled = true),
                FilenamePatternRule("VID_*", isEnabled = true),
                FilenamePatternRule("WhatsApp*", isEnabled = true),
                FilenamePatternRule("*_snippet.*", isEnabled = true)
            )
        )
        val filter = LibraryExclusionFilter(settings)

        assertTrue(filter.isFilenameExcluded("AUD-20260928-WA0001.opus"))
        assertTrue(filter.isFilenameExcluded("VID_20260928_102030.mp4"))
        assertTrue(filter.isFilenameExcluded("WhatsApp Audio 2026.mp3"))
        assertTrue(filter.isFilenameExcluded("guitar_riff_snippet.wav"))

        assertFalse(filter.isFilenameExcluded("Track 01.mp3"))
        assertFalse(filter.isFilenameExcluded("Aura Song.flac"))
    }

    @Test
    fun testHiddenTrackExclusion() {
        val settings = SmartScanSettings(
            hiddenTracks = listOf(
                HiddenTrackItem(
                    sourceUri = "content://media/external/audio/media/105",
                    songId = "song-105",
                    title = "Annoying Ringtone",
                    artist = "Unknown"
                )
            )
        )
        val filter = LibraryExclusionFilter(settings)

        assertTrue(filter.isTrackHidden(sourceUri = "content://media/external/audio/media/105", songId = "song-105"))
        assertTrue(filter.isTrackHidden(sourceUri = "content://media/external/audio/media/105", songId = "other-id"))
        assertTrue(filter.isTrackHidden(sourceUri = "other-uri", songId = "song-105"))

        assertFalse(filter.isTrackHidden(sourceUri = "content://media/external/audio/media/999", songId = "song-999"))
    }

    @Test
    fun testComprehensiveSongEntityExclusion() {
        val settings = SmartScanSettings(
            minDurationSeconds = 10,
            excludedFolders = listOf(
                ExcludedFolderRule("/storage/emulated/0/Alarms", "Alarms", isEnabled = true)
            ),
            excludedExtensions = setOf("amr"),
            filenamePatterns = listOf(
                FilenamePatternRule("AUD-*", isEnabled = true)
            ),
            hiddenTracks = listOf(
                HiddenTrackItem(sourceUri = "file:///storage/emulated/0/Music/hide_me.mp3", songId = "id-hide")
            )
        )
        val filter = LibraryExclusionFilter(settings)

        // 1. Valid music track -> NOT excluded
        val validTrack = SongEntity(
            id = "id-1",
            sourceUri = "file:///storage/emulated/0/Music/Coldplay/Yellow.mp3",
            title = "Yellow",
            artist = "Coldplay",
            album = "Parachutes",
            durationMs = 269000L,
            mediaUri = "file:///storage/emulated/0/Music/Coldplay/Yellow.mp3",
            artworkUri = null,
            trackNumber = 1,
            genre = "Rock",
            dateAdded = 1000L,
            filePath = "/storage/emulated/0/Music/Coldplay/Yellow.mp3"
        )
        assertFalse(filter.shouldExcludeSongEntity(validTrack))

        // 2. Track in excluded folder -> EXCLUDED
        val alarmTrack = validTrack.copy(
            id = "id-alarm",
            filePath = "/storage/emulated/0/Alarms/LoudBeep.mp3",
            title = "LoudBeep"
        )
        assertTrue(filter.shouldExcludeSongEntity(alarmTrack))

        // 3. Track with excluded extension (.amr) -> EXCLUDED
        val amrTrack = validTrack.copy(
            id = "id-amr",
            filePath = "/storage/emulated/0/Music/voice.amr"
        )
        assertTrue(filter.shouldExcludeSongEntity(amrTrack))

        // 4. Short track (< 10s) -> EXCLUDED
        val shortTrack = validTrack.copy(
            id = "id-short",
            durationMs = 4500L
        )
        assertTrue(filter.shouldExcludeSongEntity(shortTrack))

        // 5. Track matching wildcard pattern AUD-* -> EXCLUDED
        val audTrack = validTrack.copy(
            id = "id-aud",
            filePath = "/storage/emulated/0/Music/AUD-2026-01.mp3",
            title = "AUD-2026-01"
        )
        assertTrue(filter.shouldExcludeSongEntity(audTrack))

        // 6. Manually hidden track -> EXCLUDED
        val hiddenTrack = validTrack.copy(
            id = "id-hide",
            sourceUri = "file:///storage/emulated/0/Music/hide_me.mp3"
        )
        assertTrue(filter.shouldExcludeSongEntity(hiddenTrack))
    }

    @Test
    fun testCleanMusicLibraryPresetDefaults() {
        val defaultFolders = SmartScanSettings.defaultExcludedFolders()
        val defaultPatterns = SmartScanSettings.defaultFilenamePatterns()

        val folderNames = defaultFolders.map { it.displayName }
        assertTrue(folderNames.contains("Recordings"))
        assertTrue(folderNames.contains("Ringtones"))
        assertTrue(folderNames.contains("Alarms"))
        assertTrue(folderNames.contains("Notifications"))
        assertTrue(folderNames.contains("Podcasts"))
        assertTrue(folderNames.contains("WhatsApp Audio"))

        val patterns = defaultPatterns.map { it.pattern }
        assertTrue(patterns.contains("WhatsApp*"))
        assertTrue(patterns.contains("AUD-*"))
        assertTrue(patterns.contains("VID_*"))
        assertTrue(patterns.contains("recording"))
        assertTrue(patterns.contains("voice"))
    }

    @Test
    fun testSerializationAndDeserialization() {
        val original = SmartScanSettings(
            minDurationSeconds = 20,
            excludedFolders = listOf(
                ExcludedFolderRule("/path/one", "Folder One", isEnabled = true, isPreset = false),
                ExcludedFolderRule("/path/two", "Folder Two", isEnabled = false, isPreset = true)
            ),
            excludedExtensions = setOf("amr", "opus", "wav"),
            filenamePatterns = listOf(
                FilenamePatternRule("AUD-*", isEnabled = true),
                FilenamePatternRule("test", isEnabled = false)
            ),
            hiddenTracks = listOf(
                HiddenTrackItem("source-1", "id-1", "Song Title", "Artist", "/path/file.mp3")
            )
        )

        val json = original.toJson()
        val restored = SmartScanSettings.fromJson(json)

        assertEquals(original.minDurationSeconds, restored.minDurationSeconds)
        assertEquals(original.excludedFolders.size, restored.excludedFolders.size)
        assertEquals(original.excludedFolders[0].path, restored.excludedFolders[0].path)
        assertEquals(original.excludedFolders[1].isEnabled, restored.excludedFolders[1].isEnabled)
        assertEquals(original.excludedExtensions, restored.excludedExtensions)
        assertEquals(original.filenamePatterns.size, restored.filenamePatterns.size)
        assertEquals(original.hiddenTracks.size, restored.hiddenTracks.size)
        assertEquals(original.hiddenTracks[0].title, restored.hiddenTracks[0].title)
    }
}
