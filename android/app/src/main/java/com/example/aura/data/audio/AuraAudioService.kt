package com.example.aura.data.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.aura.MainActivity
import com.example.aura.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

import android.content.Context
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.example.aura.data.audio.dsp.AuraAudioProcessor

/**
 * Dedicated AndroidX Media3 Playback Service.
 *
 * Architecture:
 * AuraAudioService : MediaSessionService
 *       ↓
 *   ExoPlayer (owned by Service)
 *       ↓
 *   AuraAudioProcessor (BaseAudioProcessor in AudioSink chain)
 *       ↓
 *   AuraHardwareEffectsManager (Native Equalizer, BassBoost, Virtualizer, Reverb, Loudness)
 *       ↓
 *  MediaSession
 *
 * Communicates with the UI exclusively through MediaController.
 * Ensures reliable background audio playback, audio focus handling,
 * headset unplug pause (becoming noisy), wake lock, notification player,
 * and lock screen controls.
 */
class AuraAudioService : MediaSessionService() {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    companion object {
        const val CHANNEL_ID = "aura_audio_channel"
        const val NOTIFICATION_ID = 2001

        @Volatile
        var activePlayer: ExoPlayer? = null
            private set

        @Volatile
        var audioProcessor: AuraAudioProcessor? = null
            private set

        @Volatile
        var hardwareEffectsManager: AuraHardwareEffectsManager? = null
            private set
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // 1. Initialize DSP AudioProcessor & Hardware Effects Manager
        val processor = AuraAudioProcessor()
        audioProcessor = processor

        val effectsManager = AuraHardwareEffectsManager(this)
        hardwareEffectsManager = effectsManager

        // 2. Initialize ExoPlayer with AudioProcessor plugged into AudioSink
        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf(processor))
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .build()
            }
        }.apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            setEnableDecoderFallback(true)
        }
        val extractorsFactory = DefaultExtractorsFactory().apply {
            setConstantBitrateSeekingEnabled(true)
        }

        val player = ExoPlayer.Builder(this, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this, extractorsFactory))
            .build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    /* handleAudioFocus= */ true
                )
                // Keep CPU awake during playback to ensure continuous playback when locked
                setWakeMode(C.WAKE_MODE_LOCAL)
                // Pause playback automatically when headphones or Bluetooth disconnect
                setHandleAudioBecomingNoisy(true)
            }

        // Attach AudioSessionId listener for native effects
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                effectsManager.attachSession(audioSessionId, com.example.aura.domain.model.audio.AudioEffectsState())
            }
        })

        this.exoPlayer = player
        activePlayer = player

        // 2. Build Activity Intent for Notification & Lock Screen Tap
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. Configure MediaSession.Callback for standard media actions & previous track behavior
        val sessionCallback = object : MediaSession.Callback {
            override fun onMediaButtonEvent(
                session: MediaSession,
                controllerInfo: MediaSession.ControllerInfo,
                intent: Intent
            ): Boolean {
                return super.onMediaButtonEvent(session, controllerInfo, intent)
            }

            override fun onPlaybackResumption(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo
            ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                val currentItem = exoPlayer?.currentMediaItem
                val currentPos = exoPlayer?.currentPosition ?: 0L
                return if (currentItem != null) {
                    Futures.immediateFuture(
                        MediaSession.MediaItemsWithStartPosition(
                            listOf(currentItem),
                            0,
                            currentPos
                        )
                    )
                } else {
                    super.onPlaybackResumption(mediaSession, controller)
                }
            }
        }

        // 4. Create MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(sessionCallback)
            .build()

        // 5. Setup Official Android Media Notification Provider
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.notification_channel_name)
            .setNotificationId(NOTIFICATION_ID)
            .build()
        setMediaNotificationProvider(notificationProvider)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        // If music is actively playing, keep background service alive
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        activePlayer = null
        hardwareEffectsManager?.release()
        hardwareEffectsManager = null
        audioProcessor = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        com.example.aura.widget.AuraWidgetManager.updateAllWidgets(this, com.example.aura.domain.model.PlaybackState())
        super.onDestroy()
    }
}
