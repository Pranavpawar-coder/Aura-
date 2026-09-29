package com.example.aura.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aura.data.local.dao.FavoriteDao
import com.example.aura.data.local.dao.ListeningHistoryDao
import com.example.aura.data.local.dao.LyricsCacheDao
import com.example.aura.data.local.dao.MusicFolderDao
import com.example.aura.data.local.dao.PlaylistDao
import com.example.aura.data.local.dao.QueueDao
import com.example.aura.data.local.dao.RecentlyPlayedDao
import com.example.aura.data.local.dao.SongDao
import com.example.aura.data.local.dao.SongRatingDao
import com.example.aura.data.local.entity.FavoriteEntity
import com.example.aura.data.local.entity.ListeningHistoryEntity
import com.example.aura.data.local.entity.LyricsCacheEntity
import com.example.aura.data.local.entity.MusicFolderEntity
import com.example.aura.data.local.entity.PlaylistEntity
import com.example.aura.data.local.entity.PlaylistSongCrossRef
import com.example.aura.data.local.entity.QueueEntity
import com.example.aura.data.local.entity.RecentlyPlayedEntity
import com.example.aura.data.local.entity.SongEntity
import com.example.aura.data.local.entity.SongRatingEntity

@Database(
    entities = [
        SongEntity::class,
        FavoriteEntity::class,
        RecentlyPlayedEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        MusicFolderEntity::class,
        QueueEntity::class,
        SongRatingEntity::class,
        LyricsCacheEntity::class,
        ListeningHistoryEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun musicFolderDao(): MusicFolderDao
    abstract fun queueDao(): QueueDao
    abstract fun songRatingDao(): SongRatingDao
    abstract fun lyricsCacheDao(): LyricsCacheDao
    abstract fun listeningHistoryDao(): ListeningHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=OFF")

                // 1. Create songs_new table with UNIQUE sourceUri column
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `songs_new` (
                        `id` TEXT NOT NULL,
                        `sourceUri` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artist` TEXT NOT NULL,
                        `album` TEXT NOT NULL,
                        `albumArtist` TEXT,
                        `durationMs` INTEGER NOT NULL,
                        `mediaUri` TEXT NOT NULL,
                        `artworkUri` TEXT,
                        `trackNumber` INTEGER,
                        `genre` TEXT,
                        `year` INTEGER,
                        `dateAdded` INTEGER NOT NULL,
                        `filePath` TEXT,
                        `mimeType` TEXT,
                        `fileSize` INTEGER NOT NULL,
                        `codec` TEXT,
                        `bitrate` INTEGER NOT NULL,
                        `sampleRate` INTEGER NOT NULL,
                        `bitDepth` INTEGER NOT NULL,
                        `channelCount` INTEGER NOT NULL,
                        `isLossless` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 2. Identify canonical track per sourceUri and merge existing duplicates
                val cursor = db.query("SELECT id, mediaUri, filePath FROM songs")
                val duplicateMap = mutableMapOf<String, MutableList<String>>()
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val mediaUri = cursor.getString(1)
                    val filePath = cursor.getString(2)
                    val sourceUri = if (!filePath.isNullOrBlank()) {
                        try {
                            "file://" + java.io.File(filePath).canonicalPath.replace('\\', '/')
                        } catch (_: Exception) {
                            "file://" + filePath.replace('\\', '/')
                        }
                    } else {
                        mediaUri
                    }
                    duplicateMap.getOrPut(sourceUri) { mutableListOf() }.add(id)
                }
                cursor.close()

                for ((sourceUri, ids) in duplicateMap) {
                    val canonicalId = ids.find { !it.startsWith("saf_") } ?: ids.first()
                    val duplicateIds = ids.filter { it != canonicalId }

                    for (dupId in duplicateIds) {
                        // Merge Favorites
                        db.execSQL("INSERT OR IGNORE INTO favorites (songId, addedAt) SELECT '$canonicalId', addedAt FROM favorites WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM favorites WHERE songId = '$dupId'")

                        // Merge Ratings
                        db.execSQL("INSERT OR IGNORE INTO song_ratings (songId, rating, ratedAt) SELECT '$canonicalId', rating, ratedAt FROM song_ratings WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM song_ratings WHERE songId = '$dupId'")

                        // Merge Recently Played
                        db.execSQL("INSERT OR IGNORE INTO recently_played (songId, playedAt, playCount, skipCount) SELECT '$canonicalId', playedAt, playCount, skipCount FROM recently_played WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM recently_played WHERE songId = '$dupId'")

                        // Merge Playlists
                        db.execSQL("INSERT OR IGNORE INTO playlist_songs (playlistId, songId, orderIndex) SELECT playlistId, '$canonicalId', orderIndex FROM playlist_songs WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM playlist_songs WHERE songId = '$dupId'")

                        // Merge Queue
                        db.execSQL("INSERT OR IGNORE INTO queue (songId, orderIndex, isCurrent) SELECT '$canonicalId', orderIndex, isCurrent FROM queue WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM queue WHERE songId = '$dupId'")

                        // Merge Lyrics Cache
                        db.execSQL("INSERT OR IGNORE INTO lyrics_cache (songId, rawLyrics, isSynchronized, source, offsetMs, cachedAt) SELECT '$canonicalId', rawLyrics, isSynchronized, source, offsetMs, cachedAt FROM lyrics_cache WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM lyrics_cache WHERE songId = '$dupId'")

                        // Merge Listening History
                        db.execSQL("UPDATE listening_history SET songId = '$canonicalId' WHERE songId = '$dupId'")
                    }

                    // Copy canonical record to songs_new
                    db.execSQL(
                        "INSERT OR REPLACE INTO songs_new SELECT id, ?, title, artist, album, albumArtist, durationMs, mediaUri, artworkUri, trackNumber, genre, year, dateAdded, filePath, mimeType, fileSize, codec, bitrate, sampleRate, bitDepth, channelCount, isLossless FROM songs WHERE id = ?",
                        arrayOf(sourceUri, canonicalId)
                    )
                }

                // 3. Replace old table and enforce UNIQUE index
                db.execSQL("DROP TABLE songs")
                db.execSQL("ALTER TABLE songs_new RENAME TO songs")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_songs_sourceUri` ON `songs` (`sourceUri`)")

                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=OFF")

                val primaryAliases = listOf(
                    "/storage/self/primary",
                    "/sdcard",
                    "/mnt/user/0/primary",
                    "/data/media/0",
                    "/mnt/shell/emulated/0",
                    "/storage/emulated/legacy"
                )

                // 1. Read all existing songs and group by canonical storage path
                val cursor = db.query("SELECT id, sourceUri, mediaUri, filePath FROM songs")
                val duplicateMap = mutableMapOf<String, MutableList<String>>()

                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val sourceUri = cursor.getString(1) ?: ""
                    val mediaUri = cursor.getString(2) ?: ""
                    val filePath = cursor.getString(3)

                    var candidate = if (!filePath.isNullOrBlank()) {
                        filePath
                    } else if (sourceUri.startsWith("file://")) {
                        sourceUri.removePrefix("file://")
                    } else if (mediaUri.startsWith("file://")) {
                        mediaUri.removePrefix("file://")
                    } else if (mediaUri.contains("/document/")) {
                        val docId = mediaUri.substringAfter("/document/")
                        val decoded = try { java.net.URLDecoder.decode(docId, "UTF-8") } catch (_: Exception) { docId }
                        if (decoded.startsWith("primary:", ignoreCase = true)) {
                            "/storage/emulated/0/" + decoded.substringAfter("primary:").trimStart('/', '\\')
                        } else decoded
                    } else {
                        mediaUri
                    }

                    candidate = candidate.replace('\\', '/')
                    while (candidate.contains("//")) {
                        candidate = candidate.replace("//", "/")
                    }
                    for (alias in primaryAliases) {
                        if (candidate.startsWith(alias, ignoreCase = true)) {
                            candidate = "/storage/emulated/0" + candidate.substring(alias.length)
                            break
                        }
                    }

                    val canonicalSource = if (candidate.startsWith("/storage/")) "file://$candidate" else candidate
                    duplicateMap.getOrPut(canonicalSource) { mutableListOf() }.add(id)
                }
                cursor.close()

                for ((canonicalSource, ids) in duplicateMap) {
                    val canonicalId = ids.find { !it.startsWith("saf_") } ?: ids.first()
                    val duplicateIds = ids.filter { it != canonicalId }

                    for (dupId in duplicateIds) {
                        db.execSQL("INSERT OR IGNORE INTO favorites (songId, addedAt) SELECT '$canonicalId', addedAt FROM favorites WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM favorites WHERE songId = '$dupId'")

                        db.execSQL("INSERT OR IGNORE INTO song_ratings (songId, rating, ratedAt) SELECT '$canonicalId', rating, ratedAt FROM song_ratings WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM song_ratings WHERE songId = '$dupId'")

                        db.execSQL("INSERT OR IGNORE INTO recently_played (songId, playedAt, playCount, skipCount) SELECT '$canonicalId', playedAt, playCount, skipCount FROM recently_played WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM recently_played WHERE songId = '$dupId'")

                        db.execSQL("INSERT OR IGNORE INTO playlist_songs (playlistId, songId, orderIndex) SELECT playlistId, '$canonicalId', orderIndex FROM playlist_songs WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM playlist_songs WHERE songId = '$dupId'")

                        db.execSQL("INSERT OR IGNORE INTO queue (songId, orderIndex, isCurrent) SELECT '$canonicalId', orderIndex, isCurrent FROM queue WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM queue WHERE songId = '$dupId'")

                        db.execSQL("INSERT OR IGNORE INTO lyrics_cache (songId, rawLyrics, isSynchronized, source, offsetMs, cachedAt) SELECT '$canonicalId', rawLyrics, isSynchronized, source, offsetMs, cachedAt FROM lyrics_cache WHERE songId = '$dupId'")
                        db.execSQL("DELETE FROM lyrics_cache WHERE songId = '$dupId'")

                        db.execSQL("UPDATE listening_history SET songId = '$canonicalId' WHERE songId = '$dupId'")

                        db.execSQL("DELETE FROM songs WHERE id = '$dupId'")
                    }

                    db.execSQL("UPDATE songs SET sourceUri = ? WHERE id = ?", arrayOf(canonicalSource, canonicalId))
                }

                db.execSQL("DROP INDEX IF EXISTS `index_songs_sourceUri`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_songs_sourceUri` ON `songs` (`sourceUri`)")

                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        fun getInstance(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "aura_music.db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    // NEVER use fallbackToDestructiveMigration — it would wipe all user data
                    // (favorites, playlists, ratings, history, queue) on any unhandled schema change.
                    // Instead: always write an explicit Migration(from, to) before bumping `version`.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
