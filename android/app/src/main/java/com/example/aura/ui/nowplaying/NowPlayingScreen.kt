package com.example.aura.ui.nowplaying

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.domain.model.LyricLine
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.PlaybackState
import com.example.aura.domain.model.PlaybackStatus
import com.example.aura.domain.model.PlayerState
import com.example.aura.domain.model.QueueState
import com.example.aura.domain.model.RepeatMode
import com.example.aura.domain.model.Song
import com.example.aura.domain.model.audio.AudioEffectsState
import com.example.aura.domain.model.audio.SleepTimerSettings
import com.example.aura.theme.ArtworkColorExtractor
import com.example.aura.theme.ArtworkPalette
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraSurfaceBlack
import com.example.aura.theme.AuraMotion
import com.example.aura.theme.AuraOnPrimary
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraPrimaryAccent
import com.example.aura.theme.AuraPrimaryContainer
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerLow
import com.example.aura.theme.AuraTextPrimary
import com.example.aura.theme.AuraTextSecondary
import com.example.aura.theme.AuraTextTertiary
import com.example.aura.theme.auraPressable
import com.example.aura.ui.components.AuraAnimatedFavoriteButton
import com.example.aura.ui.components.AuraCapsulePlayPauseButton
import com.example.aura.ui.components.AuraFallbackArtwork
import com.example.aura.ui.components.AuraGlassPillButton
import com.example.aura.ui.components.AuraProgressBar
import com.example.aura.ui.components.AuraScrubber
import com.example.aura.ui.components.AuraVisualizerView
import com.example.aura.ui.components.SleepTimerBottomSheet
import com.example.aura.ui.components.TrackInfoDialog
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


private enum class GestureAxis {
    UNDECIDED,
    HORIZONTAL,
    VERTICAL
}

private enum class CenterDisplayMode {
    ARTWORK,
    LYRICS,
    VISUALIZER
}

@Composable
fun NowPlayingScreen(
    playbackState: PlaybackState,
    queueState: QueueState,
    lyricsState: LyricsState,
    playerState: PlayerState,
    effectsState: AudioEffectsState = AudioEffectsState(),
    visualizerData: FloatArray = FloatArray(0),
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekTimestamp: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onDismiss: () -> Unit,
    onOpenAudioEffects: () -> Unit = {},
    onLyricsOffsetChange: (Long) -> Unit = {},
    onPlaySong: (Song) -> Unit = {},
    onReorderQueue: (Int, Int) -> Unit = { _, _ -> },
    onRemoveFromQueue: (Int) -> Unit = {},
    onClearQueue: () -> Unit = {},
    sleepTimerSettings: SleepTimerSettings = SleepTimerSettings(),
    onSetSleepTimerMinutes: (Int, Boolean) -> Unit = { _, _ -> },
    onSetSleepTimerEndOfSong: (Boolean) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onAttachLyricsFile: (Uri) -> Unit = {},
    onRefreshLyrics: () -> Unit = {},
    onClearLyricsCache: () -> Unit = {},
    lyricsSettings: com.example.aura.domain.model.settings.LyricsDisplaySettings = com.example.aura.domain.model.settings.LyricsDisplaySettings(),
    modifier: Modifier = Modifier
) {
    val song = playbackState.currentSong ?: return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dynamic Artwork Palette
    var palette by remember { mutableStateOf(ArtworkPalette()) }
    LaunchedEffect(song.artworkUri) {
        palette = ArtworkColorExtractor.extractColors(context, song.artworkUri)
    }

    val animatedAccent by animateColorAsState(palette.accent, animationSpec = AuraMotion.colorTween(), label = "accent")
    val animatedBgStart by animateColorAsState(palette.gradientStart, animationSpec = AuraMotion.colorTween(), label = "bgStart")
    val animatedBgEnd by animateColorAsState(palette.gradientEnd, animationSpec = AuraMotion.colorTween(), label = "bgEnd")
    val animatedGlowColor by animateColorAsState(palette.glowColor, animationSpec = AuraMotion.colorTween(), label = "glowColor")

    // UI Dialog & Overlay States
    var showTrackInfo by remember { mutableStateOf(false) }
    var centerMode by remember { mutableStateOf(CenterDisplayMode.ARTWORK) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onAttachLyricsFile(uri)
        }
    }

    // Back handling within Now Playing Screen
    BackHandler(enabled = showTrackInfo) {
        showTrackInfo = false
    }
    BackHandler(enabled = showOptionsMenu && !showTrackInfo) {
        showOptionsMenu = false
    }
    BackHandler(enabled = showSleepTimerSheet && !showTrackInfo && !showOptionsMenu) {
        showSleepTimerSheet = false
    }
    BackHandler(enabled = showQueueSheet && !showTrackInfo && !showOptionsMenu && !showSleepTimerSheet) {
        showQueueSheet = false
    }
    BackHandler(enabled = centerMode != CenterDisplayMode.ARTWORK && !showTrackInfo && !showOptionsMenu && !showSleepTimerSheet && !showQueueSheet) {
        centerMode = CenterDisplayMode.ARTWORK
    }
    BackHandler(enabled = centerMode == CenterDisplayMode.ARTWORK && !showTrackInfo && !showOptionsMenu && !showSleepTimerSheet && !showQueueSheet) {
        onDismiss()
    }

    if (showTrackInfo) {
        TrackInfoDialog(song = song, onDismiss = { showTrackInfo = false })
    }

    if (showSleepTimerSheet) {
        SleepTimerBottomSheet(
            settings = sleepTimerSettings,
            onSetTimerMinutes = onSetSleepTimerMinutes,
            onSetTimerEndOfSong = onSetSleepTimerEndOfSong,
            onCancelTimer = onCancelSleepTimer,
            onDismiss = { showSleepTimerSheet = false }
        )
    }

    if (showOptionsMenu) {
        PlayerOptionsMenuSheet(
            song = song,
            onDismiss = { showOptionsMenu = false },
            onOpenTrackInfo = {
                showOptionsMenu = false
                showTrackInfo = true
            },
            onOpenAudioEffects = {
                showOptionsMenu = false
                onOpenAudioEffects()
            },
            onOpenSleepTimer = {
                showOptionsMenu = false
                showSleepTimerSheet = true
            },
            onRefreshLyrics = {
                showOptionsMenu = false
                onRefreshLyrics()
            },
            onAttachLyrics = {
                showOptionsMenu = false
                filePickerLauncher.launch("*/*")
            },
            onClearLyricsCache = {
                showOptionsMenu = false
                onClearLyricsCache()
            },
            accentColor = animatedAccent
        )
    }

    if (showQueueSheet) {
        QueueBottomSheet(
            queueState = queueState,
            isPlaying = playbackState.status == PlaybackStatus.PLAYING,
            onDismiss = { showQueueSheet = false },
            onSongClick = onPlaySong,
            onRemoveFromQueue = onRemoveFromQueue,
            onReorderQueue = onReorderQueue,
            onClearQueue = onClearQueue,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat
        )
    }

    var playerHeightPx by remember { mutableFloatStateOf(0f) }
    var playerWidthPx by remember { mutableFloatStateOf(0f) }
    val dragOffsetY = remember { Animatable(0f) }
    val dragOffsetX = remember { Animatable(0f) }
    var currentAxis by remember { mutableStateOf(GestureAxis.UNDECIDED) }
    var horizontalAccumulator by remember { mutableFloatStateOf(0f) }
    var verticalAccumulator by remember { mutableFloatStateOf(0f) }

    val currentAppearance = com.example.aura.theme.LocalAuraAppearance.current
    val currentBgStyle = currentAppearance.backgroundStyle

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                playerHeightPx = it.height.toFloat()
                playerWidthPx = it.width.toFloat()
            }
            .offset { IntOffset(0, dragOffsetY.value.roundToInt().coerceAtLeast(0)) }
            .background(Color(0xFF0C0706))
            .pointerInput(song.id) {
                detectDragGestures(
                    onDragStart = {
                        currentAxis = GestureAxis.UNDECIDED
                        horizontalAccumulator = 0f
                        verticalAccumulator = 0f
                    },
                    onDrag = { change, dragAmount ->
                        horizontalAccumulator += dragAmount.x
                        verticalAccumulator += dragAmount.y

                        if (currentAxis == GestureAxis.UNDECIDED) {
                            val dx = abs(horizontalAccumulator)
                            val dy = abs(verticalAccumulator)
                            if (dx > dy * 1.3f && dx > 25f) {
                                currentAxis = GestureAxis.HORIZONTAL
                                change.consume()
                            } else if (dy > dx * 1.3f && dy > 25f) {
                                currentAxis = GestureAxis.VERTICAL
                                change.consume()
                            }
                        } else {
                            change.consume()
                            if (currentAxis == GestureAxis.HORIZONTAL) {
                                coroutineScope.launch {
                                    dragOffsetX.snapTo(dragOffsetX.value + dragAmount.x)
                                }
                            } else if (currentAxis == GestureAxis.VERTICAL) {
                                if (dragAmount.y < -25f && !showQueueSheet) {
                                    showQueueSheet = true
                                } else {
                                    coroutineScope.launch {
                                        dragOffsetY.snapTo((dragOffsetY.value + dragAmount.y).coerceAtLeast(0f))
                                    }
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        if (currentAxis == GestureAxis.HORIZONTAL) {
                            val threshold = if (playerWidthPx > 0) playerWidthPx * 0.22f else 220f
                            val currentX = dragOffsetX.value
                            if (currentX < -threshold) {
                                // Swipe LEFT -> Next track with smooth cinematic slide
                                coroutineScope.launch {
                                    dragOffsetX.animateTo(
                                        targetValue = -playerWidthPx,
                                        animationSpec = tween(180, easing = FastOutLinearInEasing)
                                    )
                                    onNext()
                                    dragOffsetX.snapTo(playerWidthPx * 0.35f)
                                    dragOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            } else if (currentX > threshold) {
                                // Swipe RIGHT -> Previous track with smooth cinematic slide
                                coroutineScope.launch {
                                    dragOffsetX.animateTo(
                                        targetValue = playerWidthPx,
                                        animationSpec = tween(180, easing = FastOutLinearInEasing)
                                    )
                                    onPrevious()
                                    dragOffsetX.snapTo(-playerWidthPx * 0.35f)
                                    dragOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            } else {
                                // Threshold not reached: spring back smoothly to center
                                coroutineScope.launch {
                                    dragOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        } else if (currentAxis == GestureAxis.VERTICAL) {
                            val threshold = playerHeightPx * 0.25f
                            if (dragOffsetY.value > threshold) {
                                onDismiss()
                            } else {
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        }
                        currentAxis = GestureAxis.UNDECIDED
                        horizontalAccumulator = 0f
                        verticalAccumulator = 0f
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragOffsetX.animateTo(0f, spring())
                            dragOffsetY.animateTo(0f, spring())
                        }
                        currentAxis = GestureAxis.UNDECIDED
                        horizontalAccumulator = 0f
                        verticalAccumulator = 0f
                    }
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Atmospheric Background reacting to Background Style preference
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {}
        ) {
            when (currentBgStyle) {
                "Deep Black" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF000000))
                    )
                }
                "Minimal Dark" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF14161F),
                                        Color(0xFF0B0C10),
                                        Color(0xFF040507)
                                    )
                                )
                            )
                    )
                }
                else -> {
                    // "Adaptive Gradient" / Artwork Reactive
                    if (song.artworkUri != null) {
                        AsyncImage(
                            model = song.artworkUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(50.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        animatedBgStart.copy(alpha = 0.82f),
                                        animatedBgEnd.copy(alpha = 0.90f),
                                        Color(0xFF04060C)
                                    )
                                )
                            )
                    )
                    // Radial accent aura glow centered on artwork
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp)
                            .size(280.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        animatedGlowColor.copy(alpha = 0.22f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }

        // Main Player Container
        AnimatedContent(
            targetState = centerMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(AuraMotion.DurationQuick)) togetherWith
                        fadeOut(animationSpec = tween(AuraMotion.DurationQuick))
            },
            label = "player_screen_mode",
            modifier = Modifier.fillMaxSize()
        ) { mode ->
            when (mode) {
                CenterDisplayMode.ARTWORK -> {
                    NowPlayingMainStage(
                        song = song,
                        playbackState = playbackState,
                        queueState = queueState,
                        effectsState = effectsState,
                        animatedAccent = animatedAccent,
                        animatedGlowColor = animatedGlowColor,
                        dragOffsetX = dragOffsetX,
                        playerWidthPx = playerWidthPx,
                        onDismiss = onDismiss,
                        onShowQueue = { showQueueSheet = true },
                        onOpenOptionsMenu = { showOptionsMenu = true },
                        onToggleLyrics = { centerMode = CenterDisplayMode.LYRICS },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onSeek = onSeek,
                        onSeekTimestamp = onSeekTimestamp,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat,
                        onOpenAudioEffects = onOpenAudioEffects,
                        onShowSleepTimer = { showSleepTimerSheet = true },
                        sleepTimerMinutes = (sleepTimerSettings.remainingSeconds / 60).toInt()
                    )
                }
                CenterDisplayMode.LYRICS -> {
                    SyncedLyricsFullStage(
                        song = song,
                        lyricsState = lyricsState,
                        playbackState = playbackState,
                        queueState = queueState,
                        animatedAccent = animatedAccent,
                        lyricsSettings = lyricsSettings,
                        onBack = { centerMode = CenterDisplayMode.ARTWORK },
                        onOpenOptionsMenu = { showOptionsMenu = true },
                        onSeekTo = onSeekTimestamp,
                        onSeek = onSeek,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat
                    )
                }

                CenterDisplayMode.VISUALIZER -> {
                    VisualizerFullStage(
                        audioData = visualizerData,
                        style = effectsState.visualizer.style,
                        accentColor = animatedAccent,
                        onClose = { centerMode = CenterDisplayMode.ARTWORK }
                    )
                }
            }
        }
    }
}

/**
 * Now Playing Main Stage (Cinematic, artwork-driven, matching reference screen)
 */
@Composable
private fun NowPlayingMainStage(
    song: Song,
    playbackState: PlaybackState,
    queueState: QueueState,
    effectsState: AudioEffectsState,
    animatedAccent: Color,
    animatedGlowColor: Color,
    dragOffsetX: Animatable<Float, AnimationVector1D>,
    playerWidthPx: Float,
    onDismiss: () -> Unit,
    onShowQueue: () -> Unit,
    onOpenOptionsMenu: () -> Unit,
    onToggleLyrics: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekTimestamp: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenAudioEffects: () -> Unit,
    onShowSleepTimer: () -> Unit,
    sleepTimerMinutes: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Controls
        PlayerTopBar(
            song = song,
            onMinimize = onDismiss,
            onOpenOptions = onOpenOptionsMenu,
            onToggleLyrics = onToggleLyrics
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Center Artwork Showcase with interactive swipe translation, scale, and alpha
        val swipeProgress = (abs(dragOffsetX.value) / (if (playerWidthPx > 0) playerWidthPx else 1000f)).coerceIn(0f, 1f)
        val swipeAlpha = (1f - swipeProgress * 0.45f).coerceIn(0.55f, 1f)
        val swipeScale = (1f - swipeProgress * 0.10f).coerceIn(0.90f, 1f)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    alpha = swipeAlpha
                    scaleX = swipeScale
                    scaleY = swipeScale
                },
            contentAlignment = Alignment.Center
        ) {
            ArtworkDisplay(
                song = song,
                glowColor = animatedGlowColor,
                onLyricsClick = onToggleLyrics
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Track Info & Primary Actions Row (Title, Artist, Lossless Pill, Favorite, Lyrics)
        TrackInfoAndActionRow(
            song = song,
            isFavorite = song.isFavorite,
            onToggleFavorite = onToggleFavorite,
            onToggleLyrics = onToggleLyrics,
            accentColor = animatedAccent
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Fluid Wave Scrubber (Section 10)
        AuraProgressBar(
            positionMs = playbackState.currentPositionMs,
            durationMs = playbackState.durationMs,
            onSeek = onSeek,
            onSeekTimestamp = onSeekTimestamp,
            activeColor = animatedAccent,
            showWaveformTrail = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Main Transport Playback Controls (Shuffle, Prev, Glowing Capsule Play/Pause, Next, Repeat)
        PlayerControlsRow(
            playbackStatus = playbackState.status,
            isShuffle = queueState.isShuffle,
            repeatMode = queueState.repeatMode,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            accentColor = animatedAccent
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Audio Engine & Mode Utility Dock (Queue, Warm EQ, Sleep Timer, Output Target)
        PlayerDockBar(
            song = song,
            queueCount = queueState.songs.size,
            onShowQueue = onShowQueue,
            onOpenAudioEffects = onOpenAudioEffects,
            isAudioEffectsActive = effectsState.equalizer.enabled || effectsState.spatial4d.enabled || effectsState.bassTreble.bassEnabled,
            onShowSleepTimer = onShowSleepTimer,
            sleepTimerMinutes = sleepTimerMinutes,
            accentColor = animatedAccent
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}

/**
 * Header Controls Bar matching reference screen:
 * Left: Dismiss / Minimize Sheet button (glass pill)
 * Center: NOW PLAYING (OFFLINE) uppercase with emerald pulsing dot + download status
 * Right: Lyrics button (glass pill) + Options menu (glass pill)
 */
@Composable
private fun PlayerTopBar(
    song: Song,
    onMinimize: () -> Unit,
    onOpenOptions: () -> Unit,
    onToggleLyrics: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "offline_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraGlassPillButton(
            onClick = onMinimize,
            size = 42.dp
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Minimize Player",
                tint = Color.White.copy(alpha = 0.90f),
                modifier = Modifier.size(26.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34D399).copy(alpha = pulseAlpha))
                )
                Text(
                    text = "NOW PLAYING (OFFLINE)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.2.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val codecLabel = if (!song.codec.isNullOrBlank()) song.codec.uppercase() else "LOCAL FLAC"
            Text(
                text = "100% Downloaded • $codecLabel",
                color = AuraTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraGlassPillButton(
                onClick = onToggleLyrics,
                size = 42.dp
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = "Lyrics",
                    tint = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AuraGlassPillButton(
                onClick = onOpenOptions,
                size = 42.dp
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Options",
                    tint = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Center Artwork Showcase with Rounded Corners, Ambient Shadow, Gloss and Badges
 */
@Composable
private fun ArtworkDisplay(
    song: Song,
    glowColor: Color,
    onLyricsClick: () -> Unit
) {
    val cornerRadius = 32.dp

    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Deep ambient glow
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = glowColor.copy(alpha = 0.35f),
                    ambientColor = glowColor.copy(alpha = 0.20f)
                )
        )

        // Artwork Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(cornerRadius))
                .background(Color(0xFF141414))
                .auraPressable(pressedScale = 0.98f, onClick = onLyricsClick),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = song,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(AuraMotion.DurationArtworkTransition, easing = AuraMotion.CinematicEasing)) +
                            scaleIn(initialScale = 0.95f, animationSpec = tween(AuraMotion.DurationArtworkTransition, easing = AuraMotion.CinematicEasing)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(AuraMotion.DurationArtworkTransition, easing = AuraMotion.CinematicEasing)) +
                                    scaleOut(targetScale = 1.03f, animationSpec = tween(AuraMotion.DurationArtworkTransition, easing = AuraMotion.CinematicEasing))
                        )
                },
                label = "artwork_transition",
                modifier = Modifier.fillMaxSize()
            ) { currentSong ->
                if (currentSong.artworkUri != null) {
                    AsyncImage(
                        model = currentSong.artworkUri,
                        contentDescription = currentSong.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AuraFallbackArtwork(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = cornerRadius,
                        iconSize = 64.dp
                    )
                }
            }

            // Subtle top-left offline cache tag badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.50f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "OFFLINE CACHE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            // Audio Quality Pill Badge Overlay (Top-Right)
            val badgeText = when {
                song.isLossless -> "LOSSLESS"
                song.sampleRate >= 48000 -> "HI-RES"
                else -> "HQ AUDIO"
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.50f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

/**
 * Track Info & Action Row matching reference:
 * Left: Song title & artist & High-Res Fidelity Pill
 * Right: Heart favorite glass pill & Quotes lyrics glass pill
 */
@Composable
private fun TrackInfoAndActionRow(
    song: Song,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleLyrics: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                color = AuraTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            // High-Res Audio Fidelity Pill (matches reference)
            val sampleRateStr = if (song.sampleRate > 0) "${song.sampleRate / 1000}kHz" else "96kHz"
            val bitDepthStr = if (song.bitDepth > 0) "${song.bitDepth}-bit" else "24-bit"
            val codecStr = if (!song.codec.isNullOrBlank()) song.codec.uppercase() else "FLAC"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (song.isLossless) "LOSSLESS" else "HI-RES",
                    color = Color(0xFFFCD34D),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = " • ",
                    color = Color.White.copy(alpha = 0.40f),
                    fontSize = 9.sp
                )
                Text(
                    text = "$bitDepthStr / $sampleRateStr $codecStr",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuraGlassPillButton(
                onClick = onToggleFavorite,
                size = 44.dp,
                isActive = isFavorite,
                activeColor = accentColor
            ) {
                AuraAnimatedFavoriteButton(
                    isFavorite = isFavorite,
                    onClick = onToggleFavorite,
                    accentColor = accentColor,
                    size = 44.dp,
                    iconSize = 22.dp
                )
            }

            AuraGlassPillButton(
                onClick = onToggleLyrics,
                size = 44.dp
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = "Lyrics",
                    tint = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Main Transport Playback Controls:
 * Shuffle -> Previous -> Capsule Glowing Play/Pause -> Next -> Repeat
 */
@Composable
private fun PlayerControlsRow(
    playbackStatus: PlaybackStatus,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    accentColor: Color
) {
    val isPlaying = playbackStatus == PlaybackStatus.PLAYING

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onToggleShuffle,
            modifier = Modifier
                .size(44.dp)
                .auraPressable(pressedScale = 0.88f, hapticFeedback = true, onClick = onToggleShuffle)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffle) accentColor else Color.White.copy(alpha = 0.60f),
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                .auraPressable(pressedScale = 0.88f, hapticFeedback = true, onClick = onPrevious)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous Song",
                tint = Color.White.copy(alpha = 0.90f),
                modifier = Modifier.size(28.dp)
            )
        }

        // Prominent Capsule Play/Pause Button
        AuraCapsulePlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayPause,
            accentColor = accentColor
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                .auraPressable(pressedScale = 0.88f, hapticFeedback = true, onClick = onNext)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next Song",
                tint = Color.White.copy(alpha = 0.90f),
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = onCycleRepeat,
            modifier = Modifier
                .size(44.dp)
                .auraPressable(pressedScale = 0.88f, hapticFeedback = true, onClick = onCycleRepeat)
        ) {
            Icon(
                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) accentColor else Color.White.copy(alpha = 0.60f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Audio Engine & Utility Dock Bar matching reference dock:
 * - Queue [count]
 * - Warm EQ / Audio FX
 * - Sleep Timer
 * - Output Target / Audio Quality
 */
@Composable
private fun PlayerDockBar(
    song: Song,
    queueCount: Int,
    onShowQueue: () -> Unit,
    onOpenAudioEffects: () -> Unit,
    isAudioEffectsActive: Boolean,
    onShowSleepTimer: () -> Unit,
    sleepTimerMinutes: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Queue counter
        Row(
            modifier = Modifier
                .auraPressable(pressedScale = 0.94f, hapticFeedback = true, onClick = onShowQueue),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Queue",
                tint = Color.White.copy(alpha = 0.70f),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "Queue ",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$queueCount",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.15f)))

        // EQ Shortcut
        Row(
            modifier = Modifier
                .auraPressable(pressedScale = 0.94f, hapticFeedback = true, onClick = onOpenAudioEffects),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Equalizer",
                tint = if (isAudioEffectsActive) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.70f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isAudioEffectsActive) "Warm EQ" else "Audio FX",
                color = if (isAudioEffectsActive) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.15f)))

        // Sleep Timer
        Row(
            modifier = Modifier
                .auraPressable(pressedScale = 0.94f, hapticFeedback = true, onClick = onShowSleepTimer),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Sleep Timer",
                tint = if (sleepTimerMinutes > 0) accentColor else Color.White.copy(alpha = 0.70f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (sleepTimerMinutes > 0) "${sleepTimerMinutes}m" else "Timer",
                color = if (sleepTimerMinutes > 0) accentColor else Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.15f)))

        // Audio Output / Codec
        Row(
            modifier = Modifier
                .auraPressable(pressedScale = 0.94f, hapticFeedback = true, onClick = onOpenAudioEffects),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = "Audio Target",
                tint = Color(0xFF34D399),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (song.isLossless) "Lossless" else "High-Res",
                color = Color(0xFF34D399),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


/**
 * Synced Lyrics Full Stage (matching Stitch design and reference Image 1):
 * - Clean Top Bar with Back, LYRICS, song title & options
 * - WORD SYNC BetterLyrics animated indicator badge
 * - Synchronized lyrics list with luminous active line glow & progressive fading mask
 * - Fullscreen toggle button
 * - Waveform scrubber + playback transport controls at bottom
 */
@Composable
private fun SyncedLyricsFullStage(
    song: Song,
    lyricsState: LyricsState,
    playbackState: PlaybackState,
    queueState: QueueState,
    animatedAccent: Color,
    lyricsSettings: com.example.aura.domain.model.settings.LyricsDisplaySettings,
    onBack: () -> Unit,
    onOpenOptionsMenu: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit
) {
    val listState = rememberLazyListState()
    var isFullscreenLyrics by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lyrics Header
        if (!isFullscreenLyrics) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraGlassPillButton(
                    onClick = onBack,
                    size = 42.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Player",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "LYRICS",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.title} • ${song.artist}",
                        color = Color(0xFFA3A3A3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AuraGlassPillButton(
                    onClick = onOpenOptionsMenu,
                    size = 42.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sync Status Pill Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399).copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (lyricsState) {
                            is LyricsState.Success -> if (lyricsState.isSynchronized) "WORD SYNC • BetterLyrics" else "STATIC LYRICS"
                            is LyricsState.Loading -> "FETCHING LYRICS..."
                            else -> "OFFLINE • NO LYRICS"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.4.sp,
                        color = Color(0xFFE4E4E7)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Lyrics Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (lyricsState) {
                is LyricsState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = animatedAccent, modifier = Modifier.size(34.dp))
                    }
                }
                is LyricsState.Unavailable -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No lyrics found",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Configure sources in Settings or attach an .lrc file",
                            color = AuraOnSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is LyricsState.Success -> {
                    val lines = lyricsState.lines
                    if (lines.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No lyrics text available", color = AuraOnSurfaceVariant, fontSize = 15.sp)
                        }
                    } else {
                        val activeIndex = remember(lyricsState, playbackState.currentPositionMs) {
                            lyricsState.getActiveLineIndex(playbackState.currentPositionMs)
                        }

                        if (lyricsSettings.autoScroll) {
                            LaunchedEffect(activeIndex) {
                                if (activeIndex >= 0 && activeIndex < lines.size) {
                                    listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
                        ) {
                            itemsIndexed(lines) { index, line ->
                                val distance = abs(index - activeIndex)
                                val isCurrent = index == activeIndex
                                val targetAlpha = when {
                                    isCurrent -> 1.0f
                                    distance == 1 -> 0.55f
                                    distance == 2 -> 0.35f
                                    else -> 0.20f
                                }
                                val animatedAlpha by animateColorAsState(
                                    targetValue = Color.White.copy(alpha = targetAlpha),
                                    animationSpec = tween(AuraMotion.DurationLyricsScroll, easing = AuraMotion.CinematicEasing),
                                    label = "lyric_alpha_$index"
                                )

                                val lineFontSize = if (isCurrent) 30.sp else 24.sp
                                val lineFontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .auraPressable(pressedScale = 0.98f) {
                                            if (lyricsState.isSynchronized && line.timestampMs >= 0L) {
                                                onSeekTo(line.timestampMs)
                                            }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = line.text,
                                        color = if (isCurrent) Color.White else animatedAlpha,
                                        fontSize = lineFontSize,
                                        fontWeight = lineFontWeight,
                                        lineHeight = (lineFontSize.value * 1.3f).sp,
                                        style = if (isCurrent) {
                                            androidx.compose.ui.text.TextStyle(
                                                shadow = Shadow(
                                                    color = Color.White.copy(alpha = 0.45f),
                                                    blurRadius = 18f
                                                )
                                            )
                                        } else {
                                            androidx.compose.ui.text.TextStyle.Default
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fullscreen toggle in bottom right
            AuraGlassPillButton(
                onClick = { isFullscreenLyrics = !isFullscreenLyrics },
                size = 38.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = if (isFullscreenLyrics) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Toggle Fullscreen",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Embedded Scrubber & Progression
        AuraProgressBar(
            positionMs = playbackState.currentPositionMs,
            durationMs = playbackState.durationMs,
            onSeek = onSeek,
            onSeekTimestamp = onSeekTo,
            activeColor = animatedAccent,
            showWaveformTrail = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Main Transport Controls
        PlayerControlsRow(
            playbackStatus = playbackState.status,
            isShuffle = queueState.isShuffle,
            repeatMode = queueState.repeatMode,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            accentColor = animatedAccent
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}

/**
 * Visualizer Full Stage
 */
@Composable
private fun VisualizerFullStage(
    audioData: FloatArray,
    style: com.example.aura.domain.model.audio.VisualizerStyle,
    accentColor: Color,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VISUALIZER • ${style.displayName.uppercase()}",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            AuraGlassPillButton(onClick = onClose, size = 38.dp) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            AuraVisualizerView(
                audioData = audioData,
                style = style,
                accentColor = accentColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Options Menu Modal Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerOptionsMenuSheet(
    song: Song,
    onDismiss: () -> Unit,
    onOpenTrackInfo: () -> Unit,
    onOpenAudioEffects: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onRefreshLyrics: () -> Unit,
    onAttachLyrics: () -> Unit,
    onClearLyricsCache: () -> Unit,
    accentColor: Color
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AuraSurfaceBlack,
        contentColor = AuraTextPrimary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Track Info Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.artworkUri != null) {
                        AsyncImage(
                            model = song.artworkUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = accentColor)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        fontSize = 13.sp,
                        color = AuraOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Items
            OptionItemRow(icon = Icons.Default.Info, title = "Track Information", onClick = onOpenTrackInfo)
            OptionItemRow(icon = Icons.Default.Tune, title = "Audio Effects & Equalizer", onClick = onOpenAudioEffects)
            OptionItemRow(icon = Icons.Default.Schedule, title = "Sleep Timer", onClick = onOpenSleepTimer)
            OptionItemRow(icon = Icons.Default.Refresh, title = "Search & Refresh Lyrics", onClick = onRefreshLyrics)
            OptionItemRow(icon = Icons.Default.FileOpen, title = "Attach Lyrics File (.lrc / .txt)", onClick = onAttachLyrics)
            OptionItemRow(icon = Icons.Default.DeleteOutline, title = "Clear Lyrics Cache", onClick = onClearLyricsCache)
        }
    }
}

@Composable
private fun OptionItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}
