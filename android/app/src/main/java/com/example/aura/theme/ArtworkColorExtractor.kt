package com.example.aura.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

data class ArtworkPalette(
    val primary: Color = Color(0xFF7C5CFC),
    val secondary: Color = Color(0xFFE48B78),
    val accent: Color = Color(0xFF7C5CFC),
    val onAccent: Color = Color.White,
    val surfaceTint: Color = Color(0xFF101010),
    val glowColor: Color = Color(0xFF7C5CFC).copy(alpha = 0.28f),
    val gradientStart: Color = Color(0xFF1A1218),
    val gradientMid: Color = Color(0xFF0C090D),
    val gradientEnd: Color = Color(0xFF050505)
)

object ArtworkColorExtractor {

    private val cache = ConcurrentHashMap<String, ArtworkPalette>()

    suspend fun extractColors(context: Context, artworkUri: String?): ArtworkPalette = withContext(Dispatchers.IO) {
        if (artworkUri.isNullOrBlank()) {
            return@withContext ArtworkPalette()
        }

        cache[artworkUri]?.let { return@withContext it }

        var bitmap: Bitmap? = null
        var inputStream: InputStream? = null
        try {
            val uri = Uri.parse(artworkUri)
            inputStream = if (uri.scheme == "file") {
                java.io.File(uri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            }

            // Downsample bitmap for fast, performant palette generation
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            bitmap = BitmapFactory.decodeStream(inputStream, null, options)

            if (bitmap != null) {
                val palette = Palette.from(bitmap)
                    .maximumColorCount(16)
                    .generate()

                val vibrant = palette.vibrantSwatch
                val muted = palette.mutedSwatch
                val dominant = palette.dominantSwatch
                val lightVibrant = palette.lightVibrantSwatch
                val darkVibrant = palette.darkVibrantSwatch

                val rawPrimary = (dominant ?: vibrant ?: muted)?.rgb ?: AuraPrimaryAccentDefault.toArgb()
                val rawAccent = (vibrant ?: lightVibrant ?: dominant)?.rgb ?: rawPrimary
                val rawSecondary = (muted ?: darkVibrant ?: dominant)?.rgb ?: rawPrimary

                // Saturation & Brightness Control (Section 4):
                // Prevent extreme saturation from creating aggressive visuals
                val hslAccent = FloatArray(3)
                ColorUtils.colorToHSL(rawAccent, hslAccent)
                hslAccent[1] = hslAccent[1].coerceIn(0.35f, 0.72f) // restrained saturation
                hslAccent[2] = hslAccent[2].coerceIn(0.48f, 0.68f) // soft, readable brightness
                val adjustedAccentArgb = ColorUtils.HSLToColor(hslAccent)

                val hslPrimary = FloatArray(3)
                ColorUtils.colorToHSL(rawPrimary, hslPrimary)
                hslPrimary[1] = hslPrimary[1].coerceIn(0.30f, 0.65f)
                hslPrimary[2] = hslPrimary[2].coerceIn(0.40f, 0.62f)
                val adjustedPrimaryArgb = ColorUtils.HSLToColor(hslPrimary)

                val hslSecondary = FloatArray(3)
                ColorUtils.colorToHSL(rawSecondary, hslSecondary)
                hslSecondary[1] = hslSecondary[1].coerceIn(0.25f, 0.60f)
                hslSecondary[2] = hslSecondary[2].coerceIn(0.50f, 0.75f)
                val adjustedSecondaryArgb = ColorUtils.HSLToColor(hslSecondary)

                val accentColor = Color(adjustedAccentArgb)
                val primaryColor = Color(adjustedPrimaryArgb)
                val secondaryColor = Color(adjustedSecondaryArgb)

                val isLight = ColorUtils.calculateLuminance(adjustedAccentArgb) > 0.45
                val onAccentColor = if (isLight) Color(0xFF090909) else Color(0xFFFFFFFF)

                // Atmospheric Background Gradients fading down to Deep Black (#050505)
                val bgStartArgb = ColorUtils.blendARGB(0xFF140E14.toInt(), adjustedPrimaryArgb, 0.22f)
                val bgMidArgb = ColorUtils.blendARGB(0xFF090909.toInt(), adjustedPrimaryArgb, 0.10f)

                val result = ArtworkPalette(
                    primary = primaryColor,
                    secondary = secondaryColor,
                    accent = accentColor,
                    onAccent = onAccentColor,
                    surfaceTint = Color(bgMidArgb),
                    glowColor = accentColor.copy(alpha = 0.28f),
                    gradientStart = Color(bgStartArgb),
                    gradientMid = Color(bgMidArgb),
                    gradientEnd = Color(0xFF050505)
                )

                cache[artworkUri] = result
                return@withContext result
            }
        } catch (_: Exception) {
            // Graceful fallback to default palette
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
            bitmap?.recycle()
        }

        ArtworkPalette()
    }
}
