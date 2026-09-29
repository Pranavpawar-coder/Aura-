package com.example.aura.domain.model

sealed class LyricsState {
    object Loading : LyricsState()
    data class Success(
        val lines: List<LyricLine>,
        val isSynchronized: Boolean,
        val offsetMs: Long = 0L
    ) : LyricsState() {
        fun getActiveLineIndex(positionMs: Long): Int {
            if (!isSynchronized || lines.isEmpty()) return -1
            val effectiveTime = positionMs + offsetMs
            var activeIdx = -1
            for (i in lines.indices) {
                if (effectiveTime >= lines[i].timestampMs) {
                    activeIdx = i
                } else {
                    break
                }
            }
            return activeIdx
        }
    }
    object Unavailable : LyricsState()
}

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

object LyricsParser {
    private val timeTagRegex = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{2,3}))?]""")

    fun parse(rawText: String?, offsetMs: Long = 0L): LyricsState {
        if (rawText.isNullOrBlank()) return LyricsState.Unavailable

        val lines = rawText.lines()
        val parsedLines = mutableListOf<LyricLine>()
        var hasTimeTags = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Check if line contains one or more timestamp tags, e.g. [01:23.45]
            val matches = timeTagRegex.findAll(trimmed).toList()
            if (matches.isNotEmpty()) {
                hasTimeTags = true
                val textOnly = trimmed.replace(timeTagRegex, "").trim()
                for (match in matches) {
                    val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                    val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                    val fractionStr = match.groupValues[3]
                    val millis = when (fractionStr.length) {
                        2 -> (fractionStr.toLongOrNull() ?: 0L) * 10
                        3 -> fractionStr.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    val totalMs = minutes * 60_000L + seconds * 1_000L + millis
                    if (textOnly.isNotEmpty()) {
                        parsedLines.add(LyricLine(timestampMs = totalMs, text = textOnly))
                    }
                }
            } else if (!trimmed.startsWith("[ti:") && !trimmed.startsWith("[ar:") && !trimmed.startsWith("[al:")) {
                // Regular lyric line without timestamp
                parsedLines.add(LyricLine(timestampMs = -1L, text = trimmed))
            }
        }

        if (parsedLines.isEmpty()) return LyricsState.Unavailable

        return if (hasTimeTags) {
            val sorted = parsedLines.filter { it.timestampMs >= 0L }.sortedBy { it.timestampMs }
            if (sorted.isNotEmpty()) {
                LyricsState.Success(lines = sorted, isSynchronized = true, offsetMs = offsetMs)
            } else {
                LyricsState.Success(lines = parsedLines, isSynchronized = false, offsetMs = offsetMs)
            }
        } else {
            LyricsState.Success(lines = parsedLines, isSynchronized = false, offsetMs = offsetMs)
        }
    }
}
