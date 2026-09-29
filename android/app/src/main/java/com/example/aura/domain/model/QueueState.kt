package com.example.aura.domain.model

data class QueueState(
    val songs: List<Song> = emptyList(),
    val originalSongs: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
) {
    val currentSong: Song?
        get() = songs.getOrNull(currentIndex)

    val nextSong: Song?
        get() {
            if (songs.isEmpty() || currentIndex == -1) return null
            if (repeatMode == RepeatMode.ONE) return currentSong
            val nextIdx = currentIndex + 1
            return if (nextIdx < songs.size) {
                songs[nextIdx]
            } else if (repeatMode == RepeatMode.ALL) {
                songs.firstOrNull()
            } else {
                null
            }
        }

    val previousSong: Song?
        get() {
            if (songs.isEmpty() || currentIndex == -1) return null
            if (repeatMode == RepeatMode.ONE) return currentSong
            val prevIdx = currentIndex - 1
            return if (prevIdx >= 0) {
                songs[prevIdx]
            } else if (repeatMode == RepeatMode.ALL) {
                songs.lastOrNull()
            } else {
                null
            }
        }

    fun withNewQueue(newSongs: List<Song>, startIndex: Int, shuffle: Boolean = isShuffle): QueueState {
        if (newSongs.isEmpty()) {
            return QueueState(
                songs = emptyList(),
                originalSongs = emptyList(),
                currentIndex = -1,
                isShuffle = shuffle,
                repeatMode = repeatMode
            )
        }
        val safeIndex = startIndex.coerceIn(0, newSongs.size - 1)
        val current = newSongs[safeIndex]

        return if (shuffle && newSongs.size > 1) {
            val others = newSongs.filterIndexed { idx, _ -> idx != safeIndex }.shuffled()
            QueueState(
                songs = listOf(current) + others,
                originalSongs = newSongs,
                currentIndex = 0,
                isShuffle = true,
                repeatMode = repeatMode
            )
        } else {
            QueueState(
                songs = newSongs,
                originalSongs = newSongs,
                currentIndex = safeIndex,
                isShuffle = shuffle,
                repeatMode = repeatMode
            )
        }
    }

    fun toggleShuffle(): QueueState {
        val nextShuffle = !isShuffle
        if (songs.isEmpty() || songs.size == 1) {
            return copy(isShuffle = nextShuffle)
        }

        return if (nextShuffle) {
            val current = currentSong
            val others = if (current != null) {
                songs.filterIndexed { idx, _ -> idx != currentIndex }.shuffled()
            } else {
                songs.shuffled()
            }
            val newSongs = if (current != null) listOf(current) + others else others
            copy(
                songs = newSongs,
                currentIndex = if (current != null) 0 else currentIndex,
                isShuffle = true
            )
        } else {
            val current = currentSong
            val restored = if (originalSongs.isNotEmpty()) originalSongs else songs
            val newIndex = if (current != null) {
                restored.indexOfFirst { it.id == current.id }.let { if (it == -1) 0 else it }
            } else {
                currentIndex.coerceIn(0, restored.size - 1)
            }
            copy(
                songs = restored,
                currentIndex = newIndex,
                isShuffle = false
            )
        }
    }

    fun reorder(fromIndex: Int, toIndex: Int): QueueState {
        if (fromIndex !in songs.indices || toIndex !in songs.indices || fromIndex == toIndex) {
            return this
        }
        val list = songs.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)

        val newCurrentIndex = when {
            currentIndex == fromIndex -> toIndex
            fromIndex < currentIndex && toIndex >= currentIndex -> currentIndex - 1
            fromIndex > currentIndex && toIndex <= currentIndex -> currentIndex + 1
            else -> currentIndex
        }

        return copy(songs = list, currentIndex = newCurrentIndex)
    }

    fun removeAt(index: Int): QueueState {
        if (index !in songs.indices) return this
        val list = songs.toMutableList()
        list.removeAt(index)

        val newCurrentIndex = when {
            list.isEmpty() -> -1
            index < currentIndex -> currentIndex - 1
            index == currentIndex -> minOf(currentIndex, list.size - 1)
            else -> currentIndex
        }

        return copy(
            songs = list,
            originalSongs = originalSongs.filterIndexed { _, song -> song.id != songs[index].id },
            currentIndex = newCurrentIndex
        )
    }

    fun addToQueue(song: Song): QueueState {
        val newSongs = songs + song
        val newOriginal = originalSongs + song
        val newIndex = if (currentIndex == -1) 0 else currentIndex
        return copy(songs = newSongs, originalSongs = newOriginal, currentIndex = newIndex)
    }

    fun playNext(song: Song): QueueState {
        val insertIndex = (currentIndex + 1).coerceAtMost(songs.size)
        val list = songs.toMutableList()
        list.add(insertIndex, song)
        return copy(songs = list)
    }

    fun clear(): QueueState {
        val current = currentSong
        return if (current != null) {
            copy(songs = listOf(current), originalSongs = listOf(current), currentIndex = 0)
        } else {
            copy(songs = emptyList(), originalSongs = emptyList(), currentIndex = -1)
        }
    }
}

