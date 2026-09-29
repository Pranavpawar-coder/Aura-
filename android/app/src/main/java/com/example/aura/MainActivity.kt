package com.example.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.aura.data.audio.AuraPlayerSingleton
import com.example.aura.data.local.UserPreferences
import com.example.aura.data.local.database.AuraDatabase
import com.example.aura.data.repository.MusicRepositoryImpl
import com.example.aura.data.repository.PlaylistRepositoryImpl
import com.example.aura.domain.model.settings.AppearanceSettings
import com.example.aura.theme.AuraTheme
import com.example.aura.ui.navigation.AuraApp
import com.example.aura.ui.splash.AuraSplashScreen
import com.example.aura.ui.viewmodel.LibraryViewModel
import com.example.aura.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    private var activePlayerViewModel: PlayerViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure high-performance Coil cache to eliminate scroll jank
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_artwork_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        val database = AuraDatabase.getInstance(applicationContext)

        val musicRepository = MusicRepositoryImpl(
            context = applicationContext,
            songDao = database.songDao(),
            favoriteDao = database.favoriteDao(),
            recentlyPlayedDao = database.recentlyPlayedDao(),
            musicFolderDao = database.musicFolderDao(),
            queueDao = database.queueDao(),
            playlistDao = database.playlistDao(),
            songRatingDao = database.songRatingDao(),
            lyricsCacheDao = database.lyricsCacheDao(),
            listeningHistoryDao = database.listeningHistoryDao()
        )

        val playlistRepository = PlaylistRepositoryImpl(
            playlistDao = database.playlistDao(),
            favoriteDao = database.favoriteDao()
        )

        val playerManager = AuraPlayerSingleton.getPlayerManager(applicationContext, musicRepository)
        val userPreferences = UserPreferences(applicationContext)

        val playerViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayerViewModel(playerManager, musicRepository) as T
                }
            }
        )[PlayerViewModel::class.java]
        this.activePlayerViewModel = playerViewModel
        handleWidgetIntent(intent)

        val libraryViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(musicRepository, playlistRepository, userPreferences) as T
                }
            }
        )[LibraryViewModel::class.java]

        setContent {
            val appearance by userPreferences.appearanceFlow.collectAsState(initial = AppearanceSettings())
            AuraTheme(appearance = appearance) {
                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(1100L)
                    showSplash = false
                }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith
                            fadeOut(animationSpec = tween(400))
                    },
                    label = "splash_transition"
                ) { isSplash ->
                    if (isSplash) {
                        AuraSplashScreen()
                    } else {
                        AuraApp(
                            playerViewModel = playerViewModel,
                            libraryViewModel = libraryViewModel,
                            userPreferences = userPreferences
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: android.content.Intent?) {
        if (intent == null) return
        when {
            intent.getBooleanExtra(com.example.aura.widget.AuraWidgetManager.EXTRA_OPEN_AUDIO_EFFECTS, false) -> {
                activePlayerViewModel?.openAudioEffects()
            }
            intent.getBooleanExtra(com.example.aura.widget.AuraWidgetManager.EXTRA_OPEN_QUEUE, false) -> {
                activePlayerViewModel?.openQueue()
            }
            intent.getBooleanExtra(com.example.aura.widget.AuraWidgetManager.EXTRA_OPEN_LYRICS, false) ||
            intent.getBooleanExtra(com.example.aura.widget.AuraWidgetManager.EXTRA_OPEN_FULL_PLAYER, false) -> {
                activePlayerViewModel?.openFullPlayer()
            }
        }
    }
}
