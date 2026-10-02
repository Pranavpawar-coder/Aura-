package com.example.aura.domain.model.settings

data class PlaybackBehaviorSettings(
    val autoPlayOnLaunch: Boolean = false,
    val resumeLastPosition: Boolean = true,
    val pauseOnDisconnect: Boolean = true,
    val resumeOnConnect: Boolean = false,
    val intelligentShuffle: Boolean = true,
    val avoidRecentlyPlayedShuffle: Boolean = true
)

data class BluetoothGestureSettings(
    val singleTapAction: String = "Play / Pause",
    val doubleTapLeftAction: String = "Previous Track",
    val doubleTapRightAction: String = "Next Track",
    val tripleTapAction: String = "Toggle 4D Spatial Audio",
    val longPressAction: String = "Toggle Favorite",
    val autoLoadDeviceProfile: Boolean = true
) {
    companion object {
        val AVAILABLE_ACTIONS = listOf(
            "Play / Pause",
            "Next Track",
            "Previous Track",
            "Volume Up",
            "Volume Down",
            "Toggle Favorite",
            "Toggle 4D Spatial Audio",
            "Toggle Equalizer"
        )
    }
}

data class LyricsDisplaySettings(
    val lyricsSource: String = "Automatic (All Sources)",
    val autoSearchOnline: Boolean = true,
    val onlineLyricsEnabled: Boolean = true,
    val wifiOnly: Boolean = false,
    val autoDownloadLyrics: Boolean = true,
    val localLyricsSupport: Boolean = true,
    val embeddedLyricsSupport: Boolean = true,
    val localTxtSupport: Boolean = true,
    val cachedLyricsSupport: Boolean = true,
    val fontSizeSp: Float = 18f,
    val lineSpacingSp: Float = 10f,
    val textAlignment: String = "Center",
    val currentLineHighlight: Boolean = true,
    val autoScroll: Boolean = true,
    val lyricsAnimation: Boolean = true,
    val reduceMotion: Boolean = false,
    val globalOffsetMs: Long = 0L,
    val tapToSeek: Boolean = true,
    val fullScreenLyrics: Boolean = false
)

data class AppearanceSettings(
    val theme: String = "Dark", // "System", "Light", "Dark"
    val accentColorHex: String = "#7C5CFC",
    val dynamicColors: Boolean = true,
    val playerArtworkCornerRadiusDp: Int = 24,
    val artworkStyle: String = "Rounded", // "Rounded", "Squircle", "Square"
    val backgroundStyle: String = "Adaptive Gradient", // "Adaptive Gradient", "Minimal Dark", "Deep Black"
    val miniPlayerStyle: String = "Standard"
)

data class AnimationSettings(
    val animationsEnabled: Boolean = true,
    val animationScale: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val visualizerAnimationEnabled: Boolean = true
)

data class GesturesSettings(
    val swipeLeftPlayer: String = "Next Track",
    val swipeRightPlayer: String = "Previous Track",
    val swipeDownPlayer: String = "Minimize Player",
    val swipeUpPlayer: String = "Open Queue",
    val artworkTap: String = "Toggle Lyrics",
    val artworkDoubleTap: String = "Toggle Favorite"
)

data class NotificationSettings(
    val showControls: Boolean = true,
    val showProgress: Boolean = true,
    val showFavorite: Boolean = true,
    val compactNotification: Boolean = false,
    val persistentNotification: Boolean = true
)

data class LibrarySettings(
    val scanOnStartup: Boolean = false,
    val ignoreTracksShorterThanSec: Int = 30,
    val excludedFolders: Set<String> = emptySet(),
    val defaultSongSort: String = "Title",
    val defaultAlbumSort: String = "Album",
    val defaultArtistSort: String = "Artist"
)

data class PrivacyAndDataSettings(
    val offlineOnlyMode: Boolean = false,
    val historyTrackingEnabled: Boolean = true,
    val searchHistoryEnabled: Boolean = true
)

data class HapticSettings(
    val hapticEnabled: Boolean = true,
    val intensity: String = "Medium" // "Low", "Medium", "High"
)

enum class SettingsCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String
) {
    AUDIO("audio", "Audio", "EQ, bass, treble, balance, 4D, reverb, compressor, limiter, ReplayGain", "GraphicEq"),
    PLAYBACK("playback", "Playback", "Auto-play, resume, queue, shuffle, repeat, crossfade, gapless", "PlayCircle"),
    LYRICS("lyrics", "Lyrics", "Sources, sync, typography, caching, manual import", "Mic"),
    BLUETOOTH("bluetooth", "Bluetooth & Devices", "Headphones, Bluetooth controls, device profiles, output behavior", "Headset"),
    APPEARANCE("appearance", "Appearance", "Theme, accent, player appearance, animations", "Palette"),
    GESTURES("gestures", "Gestures & Controls", "Swipe controls, artwork gestures, mini-player gestures, button alternatives", "TouchApp"),
    LIBRARY("library", "Library", "Scanning, folders, sorting, categories", "LibraryMusic"),
    NOTIFICATIONS("notifications", "Notifications / Media", "Notification behavior & lock screen media", "Notifications"),
    STORAGE("storage", "Storage & Data", "Cache, database, statistics, backup & restore", "Storage"),
    PRIVACY("privacy", "Privacy", "History, online lyrics, data controls", "Security"),
    BACKUP("backup", "Backup & Restore", "Backups and restoration", "Backup"),
    ACCESSIBILITY("accessibility", "Accessibility", "Text size, contrast, reduced motion, larger controls, screen reader labels", "Accessibility"),
    PERFORMANCE("performance", "Performance", "Artwork quality, animations, visualizer, battery-related processing", "Speed"),
    ADVANCED("advanced", "Advanced", "Diagnostics and developer-level controls", "Tune"),
    ABOUT("about", "About", "Version, libraries, licenses, credits, privacy information", "Info")
}

data class SettingsSearchResult(
    val title: String,
    val subtitle: String,
    val category: SettingsCategory,
    val actionKey: String
)
