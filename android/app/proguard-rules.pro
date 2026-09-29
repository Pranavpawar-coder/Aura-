# ============================================================
# AURA Music Player — R8 / ProGuard Rules
# ============================================================

# --- Kotlin ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# --- Room Database (entities, DAOs, migrations) ---
-keep class com.example.aura.data.local.entity.** { *; }
-keep class com.example.aura.data.local.dao.** { *; }
-keep class com.example.aura.data.local.database.** { *; }
-keepclassmembers @androidx.room.Entity class * { *; }

# --- AndroidX / Lifecycle ---
-keep class androidx.lifecycle.** { *; }
-keep class androidx.datastore.** { *; }

# --- Media3 / ExoPlayer ---
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- Coil image loading ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- Kotlin Serialization ---
-keepattributes *Annotation*
-keepclasseswithmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# --- DocumentFile (SAF) ---
-keep class androidx.documentfile.** { *; }

# --- Suppress common unresolvable references ---
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
