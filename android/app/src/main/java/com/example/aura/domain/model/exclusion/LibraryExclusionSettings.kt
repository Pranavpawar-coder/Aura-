package com.example.aura.domain.model.exclusion

import org.json.JSONArray
import org.json.JSONObject

/**
 * Rule for excluding an entire folder and its subdirectories from library scans and views.
 */
data class ExcludedFolderRule(
    val path: String,
    val displayName: String,
    val isEnabled: Boolean = true,
    val isPreset: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("path", path)
        put("displayName", displayName)
        put("isEnabled", isEnabled)
        put("isPreset", isPreset)
    }

    companion object {
        fun fromJson(json: JSONObject): ExcludedFolderRule {
            return ExcludedFolderRule(
                path = json.optString("path", ""),
                displayName = json.optString("displayName", "Folder"),
                isEnabled = json.optBoolean("isEnabled", true),
                isPreset = json.optBoolean("isPreset", false)
            )
        }
    }
}

/**
 * Rule for excluding audio files matching specific keywords or wildcard patterns.
 * Supports exact substring contains (e.g. "recording", "voice", "ringtone")
 * or wildcard patterns (e.g. "WhatsApp*", "AUD_*", "VID_*").
 */
data class FilenamePatternRule(
    val pattern: String,
    val isEnabled: Boolean = true
) {
    fun isWildcard(): Boolean = pattern.contains('*') || pattern.contains('?')

    fun toJson(): JSONObject = JSONObject().apply {
        put("pattern", pattern)
        put("isEnabled", isEnabled)
    }

    companion object {
        fun fromJson(json: JSONObject): FilenamePatternRule {
            return FilenamePatternRule(
                pattern = json.optString("pattern", ""),
                isEnabled = json.optBoolean("isEnabled", true)
            )
        }
    }
}

/**
 * Individual track manually hidden by the user from song context menu (⋮ → Hide from Library).
 * Stored with canonical normalized source identity so rescans never recreate it.
 */
data class HiddenTrackItem(
    val sourceUri: String,
    val songId: String,
    val title: String = "Unknown Track",
    val artist: String = "Unknown Artist",
    val filePath: String? = null,
    val hiddenAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("sourceUri", sourceUri)
        put("songId", songId)
        put("title", title)
        put("artist", artist)
        filePath?.let { put("filePath", it) }
        put("hiddenAt", hiddenAt)
    }

    companion object {
        fun fromJson(json: JSONObject): HiddenTrackItem {
            return HiddenTrackItem(
                sourceUri = json.optString("sourceUri", ""),
                songId = json.optString("songId", ""),
                title = json.optString("title", "Unknown Track"),
                artist = json.optString("artist", "Unknown Artist"),
                filePath = if (json.has("filePath")) json.getString("filePath") else null,
                hiddenAt = json.optLong("hiddenAt", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Comprehensive Smart Scan & Library Exclusion Configuration.
 */
data class SmartScanSettings(
    val minDurationSeconds: Int = 10,
    val excludedExtensions: Set<String> = setOf("amr", "opus"),
    val excludedFolders: List<ExcludedFolderRule> = defaultExcludedFolders(),
    val filenamePatterns: List<FilenamePatternRule> = defaultFilenamePatterns(),
    val hiddenTracks: List<HiddenTrackItem> = emptyList()
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("minDurationSeconds", minDurationSeconds)

        val extArray = JSONArray()
        excludedExtensions.forEach { extArray.put(it) }
        root.put("excludedExtensions", extArray)

        val folderArray = JSONArray()
        excludedFolders.forEach { folderArray.put(it.toJson()) }
        root.put("excludedFolders", folderArray)

        val patternArray = JSONArray()
        filenamePatterns.forEach { patternArray.put(it.toJson()) }
        root.put("filenamePatterns", patternArray)

        val hiddenArray = JSONArray()
        hiddenTracks.forEach { hiddenArray.put(it.toJson()) }
        root.put("hiddenTracks", hiddenArray)

        return root.toString()
    }

    companion object {
        val ALL_SUPPORTED_EXTENSIONS = listOf(
            "mp3", "m4a", "flac", "wav", "aac", "ogg", "opus", "amr",
            "wma", "alac", "aiff", "aif", "ape", "wv", "dsf", "dff", "mka", "mid"
        )
        val SUPPORTED_EXTENSIONS = ALL_SUPPORTED_EXTENSIONS

        fun defaultExcludedFolders(): List<ExcludedFolderRule> = listOf(
            ExcludedFolderRule("Recordings", "Recordings", isEnabled = true, isPreset = true),
            ExcludedFolderRule("Ringtones", "Ringtones", isEnabled = true, isPreset = true),
            ExcludedFolderRule("Alarms", "Alarms", isEnabled = true, isPreset = true),
            ExcludedFolderRule("Notifications", "Notifications", isEnabled = true, isPreset = true),
            ExcludedFolderRule("Podcasts", "Podcasts", isEnabled = false, isPreset = true),
            ExcludedFolderRule("WhatsApp Audio", "WhatsApp Audio", isEnabled = true, isPreset = true),
            ExcludedFolderRule("Voice Recorder", "Voice Recorder", isEnabled = true, isPreset = true),
            ExcludedFolderRule("Call Recordings", "Call Recordings", isEnabled = true, isPreset = true)
        )

        fun defaultFilenamePatterns(): List<FilenamePatternRule> = listOf(
            FilenamePatternRule("recording", isEnabled = true),
            FilenamePatternRule("voice", isEnabled = true),
            FilenamePatternRule("ringtone", isEnabled = true),
            FilenamePatternRule("notification", isEnabled = true),
            FilenamePatternRule("alarm", isEnabled = true),
            FilenamePatternRule("AUD-*", isEnabled = true),
            FilenamePatternRule("AUD_*", isEnabled = true),
            FilenamePatternRule("VID_*", isEnabled = true),
            FilenamePatternRule("WhatsApp*", isEnabled = true),
            FilenamePatternRule("PTT-*", isEnabled = true)
        )

        fun fromJson(jsonStr: String?): SmartScanSettings {
            if (jsonStr.isNullOrBlank()) return SmartScanSettings()
            return try {
                val root = JSONObject(jsonStr)
                val minDur = root.optInt("minDurationSeconds", 10)

                val extSet = mutableSetOf<String>()
                root.optJSONArray("excludedExtensions")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        extSet.add(arr.getString(i).lowercase())
                    }
                }

                val folders = mutableListOf<ExcludedFolderRule>()
                root.optJSONArray("excludedFolders")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        folders.add(ExcludedFolderRule.fromJson(arr.getJSONObject(i)))
                    }
                }

                val patterns = mutableListOf<FilenamePatternRule>()
                root.optJSONArray("filenamePatterns")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        patterns.add(FilenamePatternRule.fromJson(arr.getJSONObject(i)))
                    }
                }

                val hidden = mutableListOf<HiddenTrackItem>()
                root.optJSONArray("hiddenTracks")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        hidden.add(HiddenTrackItem.fromJson(arr.getJSONObject(i)))
                    }
                }

                SmartScanSettings(
                    minDurationSeconds = minDur,
                    excludedExtensions = if (root.has("excludedExtensions")) extSet else setOf("amr", "opus"),
                    excludedFolders = if (root.has("excludedFolders")) folders else defaultExcludedFolders(),
                    filenamePatterns = if (root.has("filenamePatterns")) patterns else defaultFilenamePatterns(),
                    hiddenTracks = hidden
                )
            } catch (_: Exception) {
                SmartScanSettings()
            }
        }
    }
}
