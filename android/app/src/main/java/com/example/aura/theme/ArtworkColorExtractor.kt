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
    val accent: Color = Color(0xFF7C5CFC),
    val onAccent: Color = Color.White,
    val surfaceTint: Color = Color(0xFF191B23),
    val glowColor: Color = Color(0xFF7C5CFC).copy(alpha = 0.35f),
    val gradientStart: Color = Color(0xFF0B0E15),
    val gradientEnd: Color = Color(0xFF10131A)
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

            // Downsample bitmap for fast palette generation
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            bitmap = BitmapFactory.decodeStream(inputStream, null, options)

            if (bitmap != null) {
                val palette = Palette.from(bitmap)
                    .maximumColorCount(16)
                    .generate()

                val dominantSwatch = palette.vibrantSwatch
                    ?: palette.dominantSwatch
                    ?: palette.mutedSwatch
                    ?: palette.lightVibrantSwatch
                    ?: palette.darkVibrantSwatch

                val rawAccentArgb = dominantSwatch?.rgb ?: AuraPrimaryAccentDefault.toArgb()

                // Ensure accent has enough brightness for dark theme readability
                val hsl = FloatArray(3)
                ColorUtils.colorToHSL(rawAccentArgb, hsl)
                // If too dark for our dark theme, boost lightness to at least 0.55
                if (hsl[2] < 0.45f) {
                    hsl[2] = 0.55f
                }
                // Cap saturation if too oversaturated
                hsl[1] = hsl[1].coerceIn(0.4f, 0.85f)
                val adjustedAccentArgb = ColorUtils.HSLToColor(hsl)

                val accentColor = Color(adjustedAccentArgb)
                val isLight = ColorUtils.calculateLuminance(adjustedAccentArgb) > 0.45
                val onAccentColor = if (isLight) Color(0xFF10131A) else Color(0xFFFFFFFF)

                // Background gradient start tinted subtly with dominant color
                val bgStartArgb = ColorUtils.blendARGB(0xFF0B0E15.toInt(), adjustedAccentArgb, 0.18f)
                val bgEndArgb = ColorUtils.blendARGB(0xFF10131A.toInt(), adjustedAccentArgb, 0.08f)

                val result = ArtworkPalette(
                    accent = accentColor,
                    onAccent = onAccentColor,
                    surfaceTint = Color(bgEndArgb),
                    glowColor = accentColor.copy(alpha = 0.35f),
                    gradientStart = Color(bgStartArgb),
                    gradientEnd = Color(bgEndArgb)
                )

                cache[artworkUri] = result
                return@withContext result
            }
        } catch (_: Exception) {
            // Fall back gracefully to standard AURA theme
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
            bitmap?.recycle()
        }

        ArtworkPalette()
    }
}
