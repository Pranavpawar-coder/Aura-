package com.example.aura.data.exclusion

import android.net.Uri
import com.example.aura.data.local.entity.SongEntity
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.exclusion.SmartScanSettings
import java.io.File
import java.util.regex.Pattern

/**
 * High-performance evaluator implementing the multi-stage exclusion pipeline:
 * Folder $\to$ Extension $\to$ Filename Rules $\to$ Duration $\to$ Hidden Track
 */
class LibraryExclusionFilter(
    private val settings: SmartScanSettings
) {
    private val excludedExtensions = settings.excludedExtensions.map { it.lowercase().trimStart('.') }.toSet()
    private val minDurationMs = (settings.minDurationSeconds * 1000L).coerceAtLeast(0L)

    private val hiddenSourceUris = settings.hiddenTracks.map { it.sourceUri }.toSet()
    private val hiddenSongIds = settings.hiddenTracks.map { it.songId }.toSet()

    // Pre-normalize enabled folder rules
    private val enabledFolderRules = settings.excludedFolders
        .filter { it.isEnabled && it.path.isNotBlank() }
        .map { rule ->
            val clean = rule.path.replace('\\', '/').trim().trimEnd('/')
            clean.lowercase()
        }

    // Pre-compile enabled filename pattern regexes
    private val enabledFilenamePatterns = settings.filenamePatterns
        .filter { it.isEnabled && it.pattern.isNotBlank() }
        .map { rule ->
            val raw = rule.pattern.trim().lowercase()
            if (raw.contains('*') || raw.contains('?')) {
                val regexStr = buildRegexFromWildcard(raw)
                Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE)
            } else {
                // Exact substring
                Pattern.compile(Pattern.quote(raw), Pattern.CASE_INSENSITIVE)
            }
        }

    /**
     * Checks if a folder path or URI matches any enabled excluded folder rule (recursively).
     */
    fun isFolderExcluded(folderPathOrUri: String?): Boolean {
        if (folderPathOrUri.isNullOrBlank() || enabledFolderRules.isEmpty()) return false
        val normalized = folderPathOrUri.replace('\\', '/').lowercase().trimEnd('/')

        for (rulePath in enabledFolderRules) {
            // 1. Exact match
            if (normalized == rulePath) return true

            // 2. Direct parent / ancestor path match (e.g. /storage/emulated/0/Recordings/2026/...)
            if (normalized.startsWith("$rulePath/")) return true

            // 3. Name segment match if rulePath is a simple folder name (e.g. "Recordings", "WhatsApp Audio")
            if (!rulePath.contains('/')) {
                if (normalized.endsWith("/$rulePath") || normalized.contains("/$rulePath/")) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks if a file extension is in the excluded extensions list.
     */
    fun isExtensionExcluded(extension: String?): Boolean {
        if (extension.isNullOrBlank() || excludedExtensions.isEmpty()) return false
        val clean = extension.lowercase().trimStart('.')
        return excludedExtensions.contains(clean)
    }

    /**
     * Checks if a filename matches any enabled filename pattern or keyword.
     */
    fun isFilenameExcluded(filename: String?): Boolean {
        if (filename.isNullOrBlank() || enabledFilenamePatterns.isEmpty()) return false
        val clean = filename.lowercase()
        for (pattern in enabledFilenamePatterns) {
            if (pattern.matcher(clean).find()) return true
        }
        return false
    }

    /**
     * Checks if duration is under the configured minimum threshold.
     */
    fun isDurationExcluded(durationMs: Long): Boolean {
        if (minDurationMs <= 0L) return false
        // Exclude if duration is known and strictly less than threshold
        return durationMs in 1 until minDurationMs
    }

    /**
     * Checks if track is manually hidden by source identity or songId.
     */
    fun isTrackHidden(
        sourceUriOrId: String? = null,
        sourceUri: String? = null,
        songId: String? = null
    ): Boolean {
        if (!sourceUriOrId.isNullOrBlank() && (hiddenSourceUris.contains(sourceUriOrId) || hiddenSongIds.contains(sourceUriOrId))) return true
        if (!sourceUri.isNullOrBlank() && hiddenSourceUris.contains(sourceUri)) return true
        if (!songId.isNullOrBlank() && hiddenSongIds.contains(songId)) return true
        return false
    }

    /**
     * Master evaluation function used by scanner and repositories.
     * Evaluates computationally cheapest checks first.
     */
    fun shouldExclude(
        filePath: String? = null,
        uri: Uri? = null,
        uriString: String? = null,
        filename: String? = null,
        fileName: String? = null,
        durationMs: Long? = null,
        canonicalSource: String? = null,
        songId: String? = null
    ): Boolean {
        // 1. Manual hide check
        if (canonicalSource != null && hiddenSourceUris.contains(canonicalSource)) return true
        if (songId != null && hiddenSongIds.contains(songId)) return true

        val parsedUri = uri ?: if (!uriString.isNullOrBlank()) {
            try { Uri.parse(uriString) } catch (_: Exception) { null }
        } else null

        // 2. Folder check (cheapest string check)
        val candidatePath = filePath ?: parsedUri?.path
        if (candidatePath != null) {
            val parent = File(candidatePath).parent
            if (isFolderExcluded(parent) || isFolderExcluded(candidatePath)) return true
        }
        if (parsedUri != null && isFolderExcluded(parsedUri.toString())) return true
        if (!uriString.isNullOrBlank() && isFolderExcluded(uriString)) return true

        // 3. Extension check
        val rawName = filename ?: fileName
        val ext = filePath?.substringAfterLast('.', "")
            ?: parsedUri?.lastPathSegment?.substringAfterLast('.', "")
            ?: rawName?.substringAfterLast('.', "")
        if (isExtensionExcluded(ext)) return true

        // 4. Filename pattern check
        val nameToCheck = rawName
            ?: filePath?.let { File(it).name }
            ?: parsedUri?.lastPathSegment
        if (isFilenameExcluded(nameToCheck)) return true

        // 5. Duration check
        if (durationMs != null && isDurationExcluded(durationMs)) return true

        return false
    }

    fun shouldExcludeSong(song: Song): Boolean {
        return shouldExclude(
            filePath = song.filePath,
            uri = try { Uri.parse(song.mediaUri) } catch (_: Exception) { null },
            filename = song.title,
            durationMs = song.durationMs,
            canonicalSource = song.sourceUri,
            songId = song.id
        )
    }

    fun shouldExcludeSongEntity(entity: SongEntity): Boolean {
        return shouldExclude(
            filePath = entity.filePath,
            uri = try { Uri.parse(entity.mediaUri) } catch (_: Exception) { null },
            filename = entity.title,
            durationMs = entity.durationMs,
            canonicalSource = entity.sourceUri,
            songId = entity.id
        )
    }

    private fun buildRegexFromWildcard(wildcard: String): String {
        val sb = StringBuilder("^.*")
        for (char in wildcard) {
            when (char) {
                '*' -> sb.append(".*")
                '?' -> sb.append('.')
                else -> {
                    if ("\\+()^$.{}|[]".contains(char)) {
                        sb.append('\\')
                    }
                    sb.append(char)
                }
            }
        }
        sb.append(".*$")
        return sb.toString()
    }
}
