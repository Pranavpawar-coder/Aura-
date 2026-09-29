package com.example.aura.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.util.SizeF
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.aura.MainActivity
import com.example.aura.R
import com.example.aura.data.audio.AuraPlayerSingleton
import com.example.aura.domain.model.PlaybackState
import com.example.aura.domain.model.PlaybackStatus
import com.example.aura.domain.model.RepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Authoritative manager for rendering and synchronizing AURA home-screen widgets.
 * Handles artwork caching, memory-conscious downsampling, responsive sizing,
 * and MediaSession action routing matching the reference design.
 */
object AuraWidgetManager {

    private const val TAG = "AuraWidgetManager"

    const val ACTION_PLAY_PAUSE = "com.example.aura.widget.ACTION_PLAY_PAUSE"
    const val ACTION_NEXT = "com.example.aura.widget.ACTION_NEXT"
    const val ACTION_PREVIOUS = "com.example.aura.widget.ACTION_PREVIOUS"
    const val ACTION_TOGGLE_SHUFFLE = "com.example.aura.widget.ACTION_TOGGLE_SHUFFLE"
    const val ACTION_CYCLE_REPEAT = "com.example.aura.widget.ACTION_CYCLE_REPEAT"
    const val ACTION_TOGGLE_FAVORITE = "com.example.aura.widget.ACTION_TOGGLE_FAVORITE"
    const val ACTION_OPEN_FULL_PLAYER = "com.example.aura.widget.ACTION_OPEN_FULL_PLAYER"
    const val ACTION_OPEN_LYRICS = "com.example.aura.widget.ACTION_OPEN_LYRICS"
    const val ACTION_OPEN_AUDIO_EFFECTS = "com.example.aura.widget.ACTION_OPEN_AUDIO_EFFECTS"
    const val ACTION_OPEN_QUEUE = "com.example.aura.widget.ACTION_OPEN_QUEUE"

    const val EXTRA_OPEN_FULL_PLAYER = "extra_open_full_player"
    const val EXTRA_OPEN_LYRICS = "extra_open_lyrics"
    const val EXTRA_OPEN_AUDIO_EFFECTS = "extra_open_audio_effects"
    const val EXTRA_OPEN_QUEUE = "extra_open_queue"

    private const val REQUEST_CODE_PLAY_PAUSE = 101
    private const val REQUEST_CODE_NEXT = 102
    private const val REQUEST_CODE_PREVIOUS = 103
    private const val REQUEST_CODE_OPEN_PLAYER = 104
    private const val REQUEST_CODE_SHUFFLE = 105
    private const val REQUEST_CODE_REPEAT = 106
    private const val REQUEST_CODE_FAVORITE = 107
    private const val REQUEST_CODE_LYRICS = 108
    private const val REQUEST_CODE_AUDIO_EFFECTS = 109
    private const val REQUEST_CODE_QUEUE = 110

    private val widgetScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Memory-conscious LRU bitmap cache for downsampled widget artwork (max 10 items)
    private val artworkCache = object : LruCache<String, Bitmap>(10) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    // Cached fallback artwork
    private var cachedFallbackArtwork: Bitmap? = null

    /**
     * Updates all active AURA widget instances across all sizes.
     */
    fun updateAllWidgets(context: Context, state: PlaybackState? = null) {
        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext) ?: return

        val smallIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, AuraSmallWidgetProvider::class.java)
        )
        val mediumIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, AuraMediumWidgetProvider::class.java)
        )
        val largeIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, AuraLargeWidgetProvider::class.java)
        )
        val responsiveIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, AuraResponsiveWidgetProvider::class.java)
        )

        val totalCount = smallIds.size + mediumIds.size + largeIds.size + responsiveIds.size
        if (totalCount == 0) return

        val activeState = state ?: try {
            AuraPlayerSingleton.getPlayerManager(context).playbackState.value
        } catch (_: Exception) {
            PlaybackState()
        }

        widgetScope.launch {
            val artworkBitmap = loadArtworkBitmap(context, activeState.currentSong?.artworkUri)

            // 1. Update Small Widgets (2x2)
            for (id in smallIds) {
                val views = buildSmallWidget(context, activeState, artworkBitmap)
                appWidgetManager.updateAppWidget(id, views)
            }

            // 2. Update Medium Widgets (4x2)
            for (id in mediumIds) {
                val views = buildMediumWidget(context, activeState, artworkBitmap)
                appWidgetManager.updateAppWidget(id, views)
            }

            // 3. Update Large Widgets (4x3 / 4x4)
            for (id in largeIds) {
                val views = buildLargeWidget(context, activeState, artworkBitmap)
                appWidgetManager.updateAppWidget(id, views)
            }

            // 4. Update Responsive Widgets
            for (id in responsiveIds) {
                val options = appWidgetManager.getAppWidgetOptions(id)
                val views = buildResponsiveWidget(context, id, options, activeState, artworkBitmap)
                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }

    /**
     * Updates a single widget instance with specific options.
     */
    fun updateWidgetInstance(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        providerClass: Class<*>,
        options: Bundle? = null
    ) {
        val activeState = try {
            AuraPlayerSingleton.getPlayerManager(context).playbackState.value
        } catch (_: Exception) {
            PlaybackState()
        }

        widgetScope.launch {
            val artworkBitmap = loadArtworkBitmap(context, activeState.currentSong?.artworkUri)
            val views = when (providerClass) {
                AuraSmallWidgetProvider::class.java -> buildSmallWidget(context, activeState, artworkBitmap)
                AuraMediumWidgetProvider::class.java -> buildMediumWidget(context, activeState, artworkBitmap)
                AuraLargeWidgetProvider::class.java -> buildLargeWidget(context, activeState, artworkBitmap)
                else -> {
                    val opts = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
                    buildResponsiveWidget(context, appWidgetId, opts, activeState, artworkBitmap)
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Builds the Small (2x2) widget layout.
     */
    fun buildSmallWidget(
        context: Context,
        state: PlaybackState,
        artwork: Bitmap
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_small)
        val song = state.currentSong
        val isPlaying = state.status == PlaybackStatus.PLAYING

        // Metadata
        val title = song?.title?.ifBlank { context.getString(R.string.widget_default_title) }
            ?: context.getString(R.string.widget_idle_title)
        val artist = song?.artist?.ifBlank { context.getString(R.string.widget_default_artist) }
            ?: context.getString(R.string.widget_idle_artist)

        views.setTextViewText(R.id.widget_track_title, title)
        views.setTextViewText(R.id.widget_track_artist, artist)
        views.setImageViewBitmap(R.id.widget_artwork, artwork)

        // Quality Badge
        val qualityText = when {
            song?.isLossless == true -> "LOSSLESS"
            song?.codec != null -> song.codec
            else -> "AURA"
        }
        views.setTextViewText(R.id.widget_quality_badge, qualityText)

        // Favorite Icon
        val favIcon = if (song?.isFavorite == true) R.drawable.ic_widget_heart_filled else R.drawable.ic_widget_heart
        views.setImageViewResource(R.id.widget_btn_favorite, favIcon)

        // Play / Pause Icon
        val playPauseIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
        views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

        // Pending Intents
        setupWidgetIntents(context, views)
        return views
    }

    /**
     * Builds the Medium (4x2) widget layout.
     */
    fun buildMediumWidget(
        context: Context,
        state: PlaybackState,
        artwork: Bitmap
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_medium)
        val song = state.currentSong
        val isPlaying = state.status == PlaybackStatus.PLAYING

        // Metadata
        val title = song?.title?.ifBlank { context.getString(R.string.widget_default_title) }
            ?: context.getString(R.string.widget_idle_title)
        val artist = song?.artist?.ifBlank { context.getString(R.string.widget_default_artist) }
            ?: context.getString(R.string.widget_idle_artist)

        views.setTextViewText(R.id.widget_track_title, title)
        views.setTextViewText(R.id.widget_track_artist, artist)
        views.setImageViewBitmap(R.id.widget_artwork, artwork)

        // Overlays
        val qualityText = when {
            song?.isLossless == true -> "LOSSLESS"
            song?.codec != null -> song.codec
            else -> "LOSSLESS"
        }
        views.setTextViewText(R.id.widget_quality_badge, qualityText)
        views.setTextViewText(R.id.widget_badge_spatial, "SPATIAL")

        // Favorite
        val favIcon = if (song?.isFavorite == true) R.drawable.ic_widget_heart_filled else R.drawable.ic_widget_heart
        views.setImageViewResource(R.id.widget_btn_favorite, favIcon)

        // Progress
        val durationMs = state.durationMs.coerceAtLeast(0L)
        val currentMs = state.currentPositionMs.coerceIn(0L, if (durationMs > 0) durationMs else Long.MAX_VALUE)
        val progressInt = if (durationMs > 0) ((currentMs.toFloat() / durationMs.toFloat()) * 1000).toInt() else 0

        views.setProgressBar(R.id.widget_progress_bar, 1000, progressInt, false)
        views.setTextViewText(R.id.widget_time_current, state.formattedCurrentPosition)
        views.setTextViewText(R.id.widget_time_total, state.formattedRemainingTime)

        // Play / Pause Icon
        val playPauseIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
        views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

        // Repeat Icon
        val repeatIcon = if (state.repeatMode == RepeatMode.ONE) R.drawable.ic_widget_repeat_one else R.drawable.ic_widget_repeat
        views.setImageViewResource(R.id.widget_btn_repeat, repeatIcon)

        // Pending Intents
        setupWidgetIntents(context, views)
        return views
    }

    /**
     * Builds the Large (4x3 / 4x4) widget layout matching the uploaded reference design.
     */
    fun buildLargeWidget(
        context: Context,
        state: PlaybackState,
        artwork: Bitmap
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_large)
        val song = state.currentSong
        val isPlaying = state.status == PlaybackStatus.PLAYING

        // Metadata
        val title = song?.title?.ifBlank { context.getString(R.string.widget_default_title) }
            ?: context.getString(R.string.widget_idle_title)
        val artist = song?.artist?.ifBlank { context.getString(R.string.widget_default_artist) }
            ?: context.getString(R.string.widget_idle_artist)

        views.setTextViewText(R.id.widget_track_title, title)
        views.setTextViewText(R.id.widget_track_artist, artist)
        views.setImageViewBitmap(R.id.widget_artwork, artwork)

        // Overlays on Artwork matching the screenshot:
        // Top-left: ● 24-BIT / 96kHz LOSSLESS
        val qualityText = when {
            song?.isLossless == true -> {
                if (song.bitDepth > 0 && song.sampleRate > 0) {
                    "${song.bitDepth}-BIT / ${song.sampleRate / 1000}kHz LOSSLESS"
                } else {
                    "LOSSLESS AUDIO"
                }
            }
            song?.codec != null -> "${song.codec} • ${song.bitrate / 1000}kbps"
            else -> "24-BIT / 96kHz LOSSLESS"
        }
        views.setTextViewText(R.id.widget_quality_badge, qualityText)
        views.setTextViewText(R.id.widget_badge_spatial, "SPATIAL AUDIO")

        // Favorite Icon
        val favIcon = if (song?.isFavorite == true) R.drawable.ic_widget_heart_filled else R.drawable.ic_widget_heart
        views.setImageViewResource(R.id.widget_btn_favorite, favIcon)

        // Progress
        val durationMs = state.durationMs.coerceAtLeast(0L)
        val currentMs = state.currentPositionMs.coerceIn(0L, if (durationMs > 0) durationMs else Long.MAX_VALUE)
        val progressInt = if (durationMs > 0) ((currentMs.toFloat() / durationMs.toFloat()) * 1000).toInt() else 0

        views.setProgressBar(R.id.widget_progress_bar, 1000, progressInt, false)
        views.setTextViewText(R.id.widget_time_current, state.formattedCurrentPosition)
        views.setTextViewText(R.id.widget_time_total, state.formattedRemainingTime)

        // Play / Pause Icon
        val playPauseIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
        views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

        // Repeat Icon
        val repeatIcon = if (state.repeatMode == RepeatMode.ONE) R.drawable.ic_widget_repeat_one else R.drawable.ic_widget_repeat
        views.setImageViewResource(R.id.widget_btn_repeat, repeatIcon)

        // Bottom Card 1: DEVICE OUTPUT
        views.setTextViewText(R.id.widget_output_name, "AURA SoundStage Pro • Bit-Perfect")

        // Bottom Card 2: UP NEXT
        val nextSong = try {
            AuraPlayerSingleton.getPlayerManager(context).queueState.value.nextSong
        } catch (_: Exception) {
            null
        }
        val upNextText = if (nextSong != null) {
            "UP NEXT: ${nextSong.title} • ${nextSong.artist}"
        } else {
            "UP NEXT: Tap to view queue"
        }
        views.setTextViewText(R.id.widget_up_next_text, upNextText)

        // Pending Intents
        setupWidgetIntents(context, views)
        return views
    }

    /**
     * Builds responsive widget for Android 12+ or calculates optimal size for current bounds.
     */
    private fun buildResponsiveWidget(
        context: Context,
        appWidgetId: Int,
        options: Bundle?,
        state: PlaybackState,
        artwork: Bitmap
    ): RemoteViews {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val smallViews = buildSmallWidget(context, state, artwork)
            val mediumViews = buildMediumWidget(context, state, artwork)
            val largeViews = buildLargeWidget(context, state, artwork)

            val viewMapping = mapOf(
                SizeF(120f, 120f) to smallViews,
                SizeF(240f, 100f) to mediumViews,
                SizeF(240f, 180f) to largeViews
            )
            return RemoteViews(viewMapping)
        }

        // On pre-Android 12, select based on min width & height
        val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 260
        val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 110

        return when {
            minWidth < 200 || minHeight < 100 -> buildSmallWidget(context, state, artwork)
            minHeight >= 160 -> buildLargeWidget(context, state, artwork)
            else -> buildMediumWidget(context, state, artwork)
        }
    }

    /**
     * Wires all PendingIntents to widget buttons and containers.
     */
    private fun setupWidgetIntents(context: Context, views: RemoteViews) {
        // 1. Play / Pause Action
        val playPauseIntent = Intent(context, AuraWidgetActionReceiver::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PLAY_PAUSE,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPausePending)

        // 2. Next Track Action
        val nextIntent = Intent(context, AuraWidgetActionReceiver::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_NEXT,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

        // 3. Previous Track Action
        val prevIntent = Intent(context, AuraWidgetActionReceiver::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val prevPending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PREVIOUS,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_previous, prevPending)

        // 4. Shuffle Action
        val shuffleIntent = Intent(context, AuraWidgetActionReceiver::class.java).apply {
            action = ACTION_TOGGLE_SHUFFLE
        }
        val shufflePending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SHUFFLE,
            shuffleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_shuffle, shufflePending)

        // 5. Repeat Action
        val repeatIntent = Intent(context, AuraWidgetActionReceiver::class.java).apply {
            action = ACTION_CYCLE_REPEAT
        }
        val repeatPending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REPEAT,
            repeatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_repeat, repeatPending)

        // 6. Favorite Action
        val favoriteIntent = Intent(context, AuraWidgetActionReceiver::class.java).apply {
            action = ACTION_TOGGLE_FAVORITE
        }
        val favoritePending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_FAVORITE,
            favoriteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_favorite, favoritePending)

        // 7. Open Lyrics Action
        val openLyricsIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_LYRICS
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_LYRICS, true)
        }
        val openLyricsPending = PendingIntent.getActivity(
            context,
            REQUEST_CODE_LYRICS,
            openLyricsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_lyrics, openLyricsPending)

        // 8. Open Audio Effects / Device Output Action
        val openEffectsIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_AUDIO_EFFECTS
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_AUDIO_EFFECTS, true)
        }
        val openEffectsPending = PendingIntent.getActivity(
            context,
            REQUEST_CODE_AUDIO_EFFECTS,
            openEffectsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_card_device_output, openEffectsPending)

        // 9. Open Queue Action
        val openQueueIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_QUEUE
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_QUEUE, true)
        }
        val openQueuePending = PendingIntent.getActivity(
            context,
            REQUEST_CODE_QUEUE,
            openQueueIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_card_up_next, openQueuePending)

        // 10. Open Full Player on Main Artwork / Background Tap
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_FULL_PLAYER
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_FULL_PLAYER, true)
        }
        val openAppPending = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_PLAYER,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_root, openAppPending)
        views.setOnClickPendingIntent(R.id.widget_artwork, openAppPending)
        views.setOnClickPendingIntent(R.id.widget_artwork_container, openAppPending)
        views.setOnClickPendingIntent(R.id.widget_text_container, openAppPending)
    }

    /**
     * Loads, downsamples, and rounds artwork bitmap safely for RemoteViews.
     */
    private suspend fun loadArtworkBitmap(context: Context, artworkUri: String?): Bitmap = withContext(Dispatchers.IO) {
        if (artworkUri.isNullOrBlank()) {
            return@withContext getOrCreateFallbackArtwork(context)
        }

        artworkCache.get(artworkUri)?.let { return@withContext it }

        var inputStream: InputStream? = null
        try {
            val uri = Uri.parse(artworkUri)
            inputStream = if (uri.scheme == "file") {
                java.io.File(uri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            }

            if (inputStream != null) {
                // Downsample to max ~320x320 to fit comfortably inside RemoteViews transaction buffer
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val rawBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                if (rawBitmap != null) {
                    val targetSize = 320
                    val scaled = Bitmap.createScaledBitmap(rawBitmap, targetSize, targetSize, true)
                    val rounded = createRoundedBitmap(scaled, 28f)
                    artworkCache.put(artworkUri, rounded)
                    return@withContext rounded
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode widget artwork for $artworkUri: ${e.message}")
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
        }

        return@withContext getOrCreateFallbackArtwork(context)
    }

    /**
     * Generates a sleek AURA cosmic gradient fallback artwork with rounded corners.
     */
    private fun getOrCreateFallbackArtwork(context: Context): Bitmap {
        cachedFallbackArtwork?.let { return it }

        val size = 320
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Cosmic background gradient
        val gradient = LinearGradient(
            0f, 0f, size.toFloat(), size.toFloat(),
            intArrayOf(0xFF141822.toInt(), 0xFF0E1118.toInt(), 0xFF090C12.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
        }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawRoundRect(rect, 28f, 28f, paint)

        // Draw glowing inner accent circle
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33CABEFF
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size * 0.32f, glowPaint)

        // Draw AURA logo in center
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_widget_aura_logo)
            ?: ContextCompat.getDrawable(context, R.drawable.ic_widget_music_note)

        drawable?.let {
            val iconSize = (size * 0.44f).toInt()
            val left = (size - iconSize) / 2
            val top = (size - iconSize) / 2
            it.setBounds(left, top, left + iconSize, top + iconSize)
            it.setTint(0xFFCABEFF.toInt())
            it.draw(canvas)
        }

        cachedFallbackArtwork = bitmap
        return bitmap
    }

    /**
     * Renders a Bitmap with anti-aliased rounded corners using BitmapShader.
     */
    private fun createRoundedBitmap(src: Bitmap, cornerRadiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        val rect = RectF(0f, 0f, src.width.toFloat(), src.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        return output
    }
}
