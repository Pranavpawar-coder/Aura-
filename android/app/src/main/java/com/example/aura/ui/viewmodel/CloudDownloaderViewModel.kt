package com.example.aura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.data.cloud.SpotifyMetadataResolver
import com.example.aura.data.download.AuraDownloadEngine
import com.example.aura.domain.model.cloud.AudioQuality
import com.example.aura.domain.model.cloud.CloudCollection
import com.example.aura.domain.model.cloud.CloudTrack
import com.example.aura.domain.model.cloud.DownloadStatus
import com.example.aura.domain.model.cloud.DownloadTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudDownloaderViewModel(
    private val downloadEngine: AuraDownloadEngine,
    private val metadataResolver: SpotifyMetadataResolver = SpotifyMetadataResolver()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    private val _resolvedCollection = MutableStateFlow<CloudCollection?>(null)
    val resolvedCollection: StateFlow<CloudCollection?> = _resolvedCollection.asStateFlow()

    private val _selectedQuality = MutableStateFlow(AudioQuality.LOSSLESS_FLAC)
    val selectedQuality: StateFlow<AudioQuality> = _selectedQuality.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _customExtensionUrl = MutableStateFlow("")
    val customExtensionUrl: StateFlow<String> = _customExtensionUrl.asStateFlow()

    val allTasks: StateFlow<List<DownloadTask>> = downloadEngine.tasks

    val activeTasks: StateFlow<List<DownloadTask>> = downloadEngine.tasks.map { list ->
        list.filter { it.status != DownloadStatus.COMPLETED && it.status != DownloadStatus.CANCELLED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<DownloadTask>> = downloadEngine.tasks.map { list ->
        list.filter { it.status == DownloadStatus.COMPLETED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateQuery(query: String) {
        _searchQuery.value = query
        _errorMessage.value = null
    }

    fun setQuality(quality: AudioQuality) {
        _selectedQuality.value = quality
    }

    fun setCustomExtension(url: String) {
        _customExtensionUrl.value = url
        downloadEngine.streamResolver.setCustomExtensionUrl(url)
    }

    fun resolveInput(input: String = _searchQuery.value) {
        val q = input.trim()
        if (q.isBlank()) return

        viewModelScope.launch {
            _isResolving.value = true
            _errorMessage.value = null
            try {
                val result = metadataResolver.resolveInput(q)
                if (result.isSuccess) {
                    _resolvedCollection.value = result.getOrNull()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to resolve track information"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isResolving.value = false
            }
        }
    }

    fun downloadTrack(track: CloudTrack, quality: AudioQuality = _selectedQuality.value) {
        downloadEngine.enqueue(track, quality)
    }

    fun downloadAll(tracks: List<CloudTrack>, quality: AudioQuality = _selectedQuality.value) {
        if (tracks.isNotEmpty()) {
            downloadEngine.enqueueAll(tracks, quality)
        }
    }

    fun pauseDownload(taskId: String) {
        downloadEngine.pause(taskId)
    }

    fun resumeDownload(taskId: String) {
        downloadEngine.resume(taskId)
    }

    fun cancelDownload(taskId: String) {
        downloadEngine.cancel(taskId)
    }

    fun retryDownload(taskId: String) {
        downloadEngine.retry(taskId)
    }

    fun clearCompleted() {
        downloadEngine.clearCompleted()
    }
}
