package com.example.aura.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.aura.data.audio.AuraPlayerSingleton

/**
 * Base AppWidgetProvider managing lifecycle, updates, and options changes for AURA widgets.
 */
abstract class AuraBaseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            AuraWidgetManager.updateWidgetInstance(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                providerClass = this::class.java
            )
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        AuraWidgetManager.updateWidgetInstance(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            providerClass = this::class.java,
            options = newOptions
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            AuraWidgetManager.updateAllWidgets(context)
        }
    }
}

/**
 * AURA Compact 2x2 Widget
 */
class AuraSmallWidgetProvider : AuraBaseWidgetProvider()

/**
 * AURA Standard Player 4x2 Widget
 */
class AuraMediumWidgetProvider : AuraBaseWidgetProvider()

/**
 * AURA SoundStage 4x3 Widget
 */
class AuraLargeWidgetProvider : AuraBaseWidgetProvider()

/**
 * AURA Dynamic Responsive Widget (auto-adapts between 2x2, 4x2, and 4x3)
 */
class AuraResponsiveWidgetProvider : AuraBaseWidgetProvider()

/**
 * Dedicated BroadcastReceiver routing widget actions directly to authoritative AuraPlayerManager.
 */
class AuraWidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val appContext = context.applicationContext
        val playerManager = AuraPlayerSingleton.getPlayerManager(appContext)

        when (action) {
            AuraWidgetManager.ACTION_PLAY_PAUSE -> {
                playerManager.togglePlayPause()
                AuraWidgetManager.updateAllWidgets(appContext)
            }
            AuraWidgetManager.ACTION_NEXT -> {
                playerManager.next()
                AuraWidgetManager.updateAllWidgets(appContext)
            }
            AuraWidgetManager.ACTION_PREVIOUS -> {
                playerManager.previous()
                AuraWidgetManager.updateAllWidgets(appContext)
            }
            AuraWidgetManager.ACTION_TOGGLE_SHUFFLE -> {
                playerManager.toggleShuffle()
                AuraWidgetManager.updateAllWidgets(appContext)
            }
            AuraWidgetManager.ACTION_CYCLE_REPEAT -> {
                playerManager.cycleRepeat()
                AuraWidgetManager.updateAllWidgets(appContext)
            }
            AuraWidgetManager.ACTION_TOGGLE_FAVORITE -> {
                playerManager.toggleFavorite()
                AuraWidgetManager.updateAllWidgets(appContext)
            }
        }
    }
}
