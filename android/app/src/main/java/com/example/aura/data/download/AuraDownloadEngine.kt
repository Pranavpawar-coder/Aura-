package com.example.aura.data.download

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.example.aura.data.cloud.LosslessStreamResolver
import com.example.aura.data.cloud.SpotifyMetadataResolver
import com.example.aura.domain.model.cloud.AudioQuality
import com.example.aura.domain.model.cloud.CloudTrack
import com.example.aura.domain.model.cloud.DownloadStatus
import com.example.aura.domain.model.cloud.DownloadTask
import com.example.aura.domain.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Aura Lossless Download & Queue Management Engine.
 * Manages background downloading, chunked streaming, progress calculation,
 * file output organization, and automated MediaStore indexing.
 */
class AuraDownloadEngine(
    private val context: Context,
    private val musicRepository: MusicRepository? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val metadataResolver = SpotifyMetadataResolver()
    val streamResolver = LosslessStreamResolver()

    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val pausedTasks = ConcurrentHashMap.newKeySet<String>()

    companion object {
        @Volatile
        private var instance: AuraDownloadEngine? = null

        fun getInstance(context: Context, musicRepository: MusicRepository? = null): AuraDownloadEngine {
            return instance ?: synchronized(this) {
                instance ?: AuraDownloadEngine(context.applicationContext, musicRepository).also { instance = it }
            }
        }
    }

    /**
     * Enqueues a single track for download.
     */
    fun enqueue(track: CloudTrack, quality: AudioQuality = AudioQuality.LOSSLESS_FLAC): String {
        val taskId = "dl_${UUID.randomUUID().toString().take(8)}"
        val task = DownloadTask(
            id = taskId,
            track = track,
            targetQuality = quality,
            status = DownloadStatus.QUEUED
        )

        _tasks.update { it + task }
        processQueue()
        return taskId
    }

    /**
     * Enqueues a batch of tracks (e.g. from an album or playlist).
     */
    fun enqueueAll(tracks: List<CloudTrack>, quality: AudioQuality = AudioQuality.LOSSLESS_FLAC): List<String> {
        val newTasks = tracks.map { track ->
            DownloadTask(
                id = "dl_${UUID.randomUUID().toString().take(8)}",
                track = track,
                targetQuality = quality,
                status = DownloadStatus.QUEUED
            )
        }
        _tasks.update { it + newTasks }
        processQueue()
        return newTasks.map { it.id }
    }

    /**
     * Pauses an ongoing or queued download.
     */
    fun pause(taskId: String) {
        pausedTasks.add(taskId)
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)

        updateTask(taskId) {
            it.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0L)
        }
        processQueue()
    }

    /**
     * Resumes a paused download.
     */
    fun resume(taskId: String) {
        pausedTasks.remove(taskId)
        updateTask(taskId) {
            it.copy(status = DownloadStatus.QUEUED)
        }
        processQueue()
    }

    /**
     * Cancels and removes a download task.
     */
    fun cancel(taskId: String) {
        pausedTasks.remove(taskId)
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)

        _tasks.update { list -> list.filterNot { it.id == taskId } }
        processQueue()
    }

    /**
     * Retries a failed download.
     */
    fun retry(taskId: String) {
        pausedTasks.remove(taskId)
        updateTask(taskId) {
            it.copy(
                status = DownloadStatus.QUEUED,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = 0L,
                errorMessage = null
            )
        }
        processQueue()
    }

    /**
     * Clears all completed or failed tasks from the list.
     */
    fun clearCompleted() {
        _tasks.update { list ->
            list.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.RESOLVING || it.status == DownloadStatus.QUEUED }
        }
    }

    private fun processQueue() {
        val currentRunning = activeJobs.size
        val maxConcurrent = 2

        if (currentRunning >= maxConcurrent) return

        val nextTask = _tasks.value.firstOrNull {
            it.status == DownloadStatus.QUEUED && !pausedTasks.contains(it.id)
        } ?: return

        startDownloadTask(nextTask)
    }

    private fun startDownloadTask(task: DownloadTask) {
        val taskId = task.id
        val job = scope.launch {
            try {
                // 1. Resolve Audio Stream URL
                updateTask(taskId) { it.copy(status = DownloadStatus.RESOLVING) }
                val streamResult = streamResolver.resolveStreamUrl(task.track, task.targetQuality)

                if (streamResult.isFailure) {
                    val error = streamResult.exceptionOrNull()?.message ?: "Failed to resolve stream"
                    updateTask(taskId) { it.copy(status = DownloadStatus.FAILED, errorMessage = error) }
                    return@launch
                }

                val resolved = streamResult.getOrThrow()
                val formatExt = resolved.format.ifBlank { task.targetQuality.formatExtension }

                // 2. Prepare Output File in /Music/Aura/
                val musicDir = getAuraMusicDirectory()
                val safeTitle = sanitizeFilename(task.track.title)
                val safeArtist = sanitizeFilename(task.track.artist)
                val fileName = "$safeArtist - $safeTitle.$formatExt"
                val outputFile = File(musicDir, fileName)

                // 3. Download Stream with Chunked Buffer & Progress Tracking
                updateTask(taskId) { it.copy(status = DownloadStatus.DOWNLOADING) }
                val downloadSuccess = downloadStreamToFile(
                    url = resolved.url,
                    outputFile = outputFile,
                    taskId = taskId
                )

                if (!downloadSuccess) {
                    if (!pausedTasks.contains(taskId)) {
                        updateTask(taskId) { it.copy(status = DownloadStatus.FAILED, errorMessage = "Download interrupted or network timeout") }
                    }
                    return@launch
                }

                // 4. Finalize & Tag Stage
                updateTask(taskId) { it.copy(status = DownloadStatus.TAGGING, progress = 0.98f) }

                // Direct Room database registration so it immediately shows up in all library views
                val mmr = android.media.MediaMetadataRetriever()
                try {
                    mmr.setDataSource(outputFile.absolutePath)
                    val durationStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMs = durationStr?.toLongOrNull() ?: if (task.track.durationMs > 0) task.track.durationMs else 180000L
                    val title = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() } ?: task.track.title
                    val artist = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() } ?: task.track.artist
                    val album = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() } ?: task.track.album.ifBlank { "Aura Downloads" }

                    val normSource = com.example.aura.data.audio.AudioSourceNormalizer.normalizeSource(context, android.net.Uri.fromFile(outputFile), outputFile.absolutePath)
                    val songId = com.example.aura.data.audio.AudioSourceNormalizer.generateStableTrackId(normSource.canonicalIdentity)
                    val fileUri = android.net.Uri.fromFile(outputFile).toString()

                    val songEntity = com.example.aura.data.local.entity.SongEntity(
                        id = songId,
                        sourceUri = normSource.canonicalIdentity,
                        title = title,
                        artist = artist,
                        album = album,
                        albumArtist = artist,
                        durationMs = durationMs,
                        mediaUri = fileUri,
                        artworkUri = task.track.coverUrl,
                        trackNumber = task.track.trackNumber ?: 1,
                        genre = "Lossless",
                        year = task.track.releaseYear ?: 2024,
                        dateAdded = System.currentTimeMillis(),
                        filePath = outputFile.absolutePath,
                        mimeType = if (formatExt == "flac") "audio/flac" else "audio/mp4",
                        fileSize = outputFile.length(),
                        codec = formatExt.uppercase(),
                        bitrate = if (task.targetQuality == AudioQuality.HIGH_MP3) 320000 else 1411000,
                        sampleRate = 44100,
                        bitDepth = if (task.targetQuality == AudioQuality.HIRES_FLAC) 24 else 16,
                        channelCount = 2,
                        isLossless = task.targetQuality == AudioQuality.LOSSLESS_FLAC || task.targetQuality == AudioQuality.HIRES_FLAC
                    )

                    com.example.aura.data.local.database.AuraDatabase.getInstance(context).songDao().upsertSongs(listOf(songEntity))
                } catch (e: Exception) {
                    android.util.Log.e("AuraDownloadEngine", "Failed to insert into SongDao", e)
                } finally {
                    try { mmr.release() } catch (_: Exception) {}
                }

                // 5. Index into Android MediaStore & Aura Database
                scanFileIntoMediaStore(outputFile)

                // Mark Completed
                updateTask(taskId) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        progress = 1.0f,
                        outputFilePath = outputFile.absolutePath,
                        completedAt = System.currentTimeMillis(),
                        speedBytesPerSec = 0L
                    )
                }

                // Trigger Aura library scan in background
                musicRepository?.scanDeviceMusic()

            } catch (e: Exception) {
                if (!pausedTasks.contains(taskId)) {
                    updateTask(taskId) {
                        it.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = e.message ?: "Unknown error"
                        )
                    }
                }
            } finally {
                activeJobs.remove(taskId)
                processQueue()
            }
        }

        activeJobs[taskId] = job
    }

    private suspend fun downloadStreamToFile(
        url: String,
        outputFile: File,
        taskId: String
    ): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val u = URL(url)
            connection = (u.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 20000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; AURA-Downloader/1.2)")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext false
            }

            val contentLength = connection.contentLengthLong
            val totalBytes = if (contentLength > 0) contentLength else 0L

            inputStream = connection.inputStream
            outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(16 * 1024)
            var bytesRead: Int
            var totalRead = 0L
            var lastTime = System.currentTimeMillis()
            var bytesSinceLastTime = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (pausedTasks.contains(taskId)) {
                    return@withContext false
                }

                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                bytesSinceLastTime += bytesRead

                val currentTime = System.currentTimeMillis()
                val delta = currentTime - lastTime
                if (delta >= 400) {
                    val speed = (bytesSinceLastTime * 1000) / delta
                    val progress = if (totalBytes > 0) (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 0.95f) else 0.5f

                    updateTask(taskId) {
                        it.copy(
                            downloadedBytes = totalRead,
                            totalBytes = totalBytes,
                            progress = progress,
                            speedBytesPerSec = speed
                        )
                    }

                    lastTime = currentTime
                    bytesSinceLastTime = 0L
                }
            }

            outputStream.flush()
            true
        } catch (e: Exception) {
            outputFile.delete()
            false
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
            connection?.disconnect()
        }
    }

    private fun getAuraMusicDirectory(): File {
        val publicMusic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val auraPublic = File(publicMusic, "Aura")
        try {
            if (!auraPublic.exists()) {
                auraPublic.mkdirs()
            }
            val testFile = File(auraPublic, ".test_write")
            if (testFile.createNewFile()) {
                testFile.delete()
                return auraPublic
            }
        } catch (_: Exception) {}

        val extFiles = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (extFiles != null) {
            if (!extFiles.exists()) extFiles.mkdirs()
            return extFiles
        }
        return context.filesDir
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
    }

    private fun scanFileIntoMediaStore(file: File) {
        try {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null
            ) { path, uri ->
                // MediaStore scan completed
            }
        } catch (_: Exception) {}
    }

    private fun updateTask(taskId: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.update { list ->
            list.map { if (it.id == taskId) transform(it) else it }
        }
    }
}
