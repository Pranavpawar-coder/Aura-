package com.example.aura.data.audio

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.net.URLDecoder
import java.security.MessageDigest

data class NormalizedAudioSource(
    val canonicalIdentity: String,
    val resolvedFilePath: String?,
    val playbackUri: String
)

object AudioSourceNormalizer {

    private const val TAG = "AudioSourceNormalizer"

    private val PRIMARY_STORAGE_ALIASES = listOf(
        "/storage/self/primary",
        "/sdcard",
        "/mnt/user/0/primary",
        "/data/media/0",
        "/mnt/shell/emulated/0",
        "/storage/emulated/legacy"
    )

    fun canonicalizeStoragePath(rawPath: String): String {
        var p = rawPath.replace('\\', '/').trim()
        while (p.contains("//")) {
            p = p.replace("//", "/")
        }
        for (alias in PRIMARY_STORAGE_ALIASES) {
            if (p.startsWith(alias, ignoreCase = true)) {
                p = "/storage/emulated/0" + p.substring(alias.length)
                break
            }
        }
        return p
    }

    private fun logWarn(msg: String, e: Throwable? = null) {
        try { Log.w(TAG, msg, e) } catch (_: Throwable) {}
    }

    /**
     * Resolves a stable, canonical source identity for any audio file via Uri object.
     */
    fun normalizeSource(
        context: Context? = null,
        uri: Uri,
        filePath: String? = null
    ): NormalizedAudioSource {
        val uriStr = try { uri.toString() } catch (_: Throwable) { filePath?.let { "file://$it" } ?: "" }
        return normalizeSourceString(context, uriStr, filePath)
    }

    /**
     * Resolves a stable, canonical source identity for any audio file via URI string.
     * Guarantees: The same physical file always yields the identical canonicalIdentity string.
     */
    fun normalizeSourceString(
        context: Context? = null,
        uriString: String,
        filePath: String? = null
    ): NormalizedAudioSource {
        // 1. Direct or hint file path
        if (!filePath.isNullOrBlank()) {
            val normPath = canonicalizeStoragePath(filePath)
            return NormalizedAudioSource(
                canonicalIdentity = "file://$normPath",
                resolvedFilePath = normPath,
                playbackUri = uriString.ifBlank { "file://$normPath" }
            )
        }

        // 2. File scheme URI
        if (uriString.startsWith("file:", ignoreCase = true)) {
            val path = canonicalizeStoragePath(uriString.removePrefix("file://").removePrefix("file:"))
            return NormalizedAudioSource(
                canonicalIdentity = "file://$path",
                resolvedFilePath = path,
                playbackUri = "file://$path"
            )
        }

        // 3. Storage Access Framework (SAF) Document / Tree URI
        if (isSafUriString(uriString)) {
            val docId = extractDocumentIdFromString(uriString)
            if (docId != null) {
                val resolvedPath = resolveDocIdToPath(docId)
                if (resolvedPath != null) {
                    val normPath = canonicalizeStoragePath(resolvedPath)
                    return NormalizedAudioSource(
                        canonicalIdentity = "file://$normPath",
                        resolvedFilePath = normPath,
                        playbackUri = uriString
                    )
                } else {
                    val authority = uriString.substringAfter("://").substringBefore('/')
                    val normalizedSafUri = "content://$authority/document/$docId"
                    return NormalizedAudioSource(
                        canonicalIdentity = normalizedSafUri,
                        resolvedFilePath = null,
                        playbackUri = uriString
                    )
                }
            }
        }

        // 4. MediaStore Audio Content URI
        if (isMediaStoreUriString(uriString)) {
            val mediaStorePath = if (context != null) {
                try { queryMediaStorePath(context, Uri.parse(uriString)) } catch (_: Throwable) { null }
            } else null

            if (!mediaStorePath.isNullOrBlank()) {
                val normPath = canonicalizeStoragePath(mediaStorePath)
                return NormalizedAudioSource(
                    canonicalIdentity = "file://$normPath",
                    resolvedFilePath = normPath,
                    playbackUri = uriString
                )
            }

            // Fallback for Android 10+ scoped storage: query RELATIVE_PATH and DISPLAY_NAME
            if (context != null) {
                val relativeDetails = try { queryMediaStoreRelativePath(context, Uri.parse(uriString)) } catch (_: Throwable) { null }
                if (relativeDetails != null) {
                    val (relPath, displayName) = relativeDetails
                    val path = "/storage/emulated/0/" + relPath.trim('/') + "/" + displayName
                    val normPath = canonicalizeStoragePath(path)
                    return NormalizedAudioSource(
                        canonicalIdentity = "file://$normPath",
                        resolvedFilePath = normPath,
                        playbackUri = uriString
                    )
                }
            }

            val lastSegment = uriString.substringAfterLast('/')
            val mediaId = lastSegment.toLongOrNull()
            val canonicalMediaStore = if (mediaId != null && mediaId > 0) {
                "content://media/external/audio/media/$mediaId"
            } else {
                uriString
            }
            return NormalizedAudioSource(
                canonicalIdentity = canonicalMediaStore,
                resolvedFilePath = null,
                playbackUri = uriString
            )
        }

        // 5. General Content URI fallback
        val cleanUri = try { URLDecoder.decode(uriString, "UTF-8").trim() } catch (_: Exception) { uriString.trim() }
        return NormalizedAudioSource(
            canonicalIdentity = cleanUri,
            resolvedFilePath = null,
            playbackUri = uriString
        )
    }

    /**
     * Generates a stable deterministic track ID from the canonical source identity.
     */
    fun generateStableTrackId(canonicalIdentity: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(canonicalIdentity.toByteArray(Charsets.UTF_8))
        val hex = hashBytes.take(8).joinToString("") { "%02x".format(it) }
        return "trk_$hex"
    }

    private fun isSafUriString(uriString: String): Boolean {
        return uriString.contains(".documents") ||
                uriString.contains("/document/") ||
                uriString.contains("/tree/")
    }

    private fun isMediaStoreUriString(uriString: String): Boolean {
        return uriString.contains("content://media/") ||
                uriString.contains("com.android.providers.media.documents") ||
                uriString.contains("/audio/media")
    }

    private fun extractDocumentIdFromString(uriString: String): String? {
        return try {
            if (uriString.contains("/document/")) {
                URLDecoder.decode(uriString.substringAfter("/document/"), "UTF-8")
            } else if (uriString.contains("/tree/")) {
                URLDecoder.decode(uriString.substringAfter("/tree/"), "UTF-8")
            } else null
        } catch (_: Exception) {
            if (uriString.contains("/document/")) {
                uriString.substringAfter("/document/")
            } else null
        }
    }

    private fun resolveDocIdToPath(docId: String): String? {
        val decoded = try {
            URLDecoder.decode(docId, "UTF-8")
        } catch (_: Exception) {
            docId
        }

        if (decoded.startsWith("primary:", ignoreCase = true)) {
            val relative = decoded.substringAfter("primary:").trimStart('/', '\\')
            val storageBase = try {
                Environment.getExternalStorageDirectory().absolutePath
            } catch (_: Throwable) {
                "/storage/emulated/0"
            }
            return "$storageBase/$relative"
        }

        if (decoded.startsWith("raw:", ignoreCase = true)) {
            return decoded.substringAfter("raw:")
        }

        // Secondary SD card: format "1234-5678:path"
        val colonIdx = decoded.indexOf(':')
        if (colonIdx > 0 && colonIdx < 16) {
            val volumeId = decoded.substring(0, colonIdx)
            val relative = decoded.substring(colonIdx + 1).trimStart('/', '\\')
            val candidate = File("/storage/$volumeId/$relative")
            if (candidate.exists()) {
                return candidate.absolutePath
            }
            return "/storage/$volumeId/$relative"
        }

        return null
    }

    private fun queryMediaStorePath(context: Context?, uri: Uri): String? {
        if (context == null) return null
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Audio.Media.DATA),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    if (idx != -1) cursor.getString(idx) else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun queryMediaStoreRelativePath(context: Context, uri: Uri): Pair<String, String>? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Audio.Media.RELATIVE_PATH, MediaStore.Audio.Media.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val relIdx = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                    val dispIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    if (relIdx != -1 && dispIdx != -1) {
                        val rel = cursor.getString(relIdx) ?: ""
                        val disp = cursor.getString(dispIdx) ?: ""
                        if (disp.isNotBlank()) Pair(rel, disp) else null
                    } else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
