package com.example.aura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.aura.domain.model.Song

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["sourceUri"], unique = true)
    ]
)
data class SongEntity(
    @PrimaryKey val id: String,
    val sourceUri: String = "",
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String?,
    val trackNumber: Int?,
    val genre: String?,
    val year: Int? = null,
    val dateAdded: Long,
    val filePath: String? = null,
    val mimeType: String? = null,
    val fileSize: Long = 0L,
    val codec: String? = null,
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val bitDepth: Int = 0,
    val channelCount: Int = 2,
    val isLossless: Boolean = false
) {
    fun toDomain(isFavorite: Boolean = false, rating: Int = 0): Song {
        return Song(
            id = id,
            sourceUri = sourceUri,
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            durationMs = durationMs,
            mediaUri = mediaUri,
            artworkUri = artworkUri,
            trackNumber = trackNumber,
            genre = genre,
            year = year,
            dateAdded = dateAdded,
            filePath = filePath,
            mimeType = mimeType,
            fileSize = fileSize,
            codec = codec,
            bitrate = bitrate,
            sampleRate = sampleRate,
            bitDepth = bitDepth,
            channelCount = channelCount,
            isLossless = isLossless,
            isFavorite = isFavorite,
            rating = rating
        )
    }

    companion object {
        fun fromDomain(song: Song): SongEntity {
            return SongEntity(
                id = song.id,
                sourceUri = song.sourceUri,
                title = song.title,
                artist = song.artist,
                album = song.album,
                albumArtist = song.albumArtist,
                durationMs = song.durationMs,
                mediaUri = song.mediaUri,
                artworkUri = song.artworkUri,
                trackNumber = song.trackNumber,
                genre = song.genre,
                year = song.year,
                dateAdded = song.dateAdded,
                filePath = song.filePath,
                mimeType = song.mimeType,
                fileSize = song.fileSize,
                codec = song.codec,
                bitrate = song.bitrate,
                sampleRate = song.sampleRate,
                bitDepth = song.bitDepth,
                channelCount = song.channelCount,
                isLossless = song.isLossless
            )
        }
    }
}

@Entity(tableName = "music_folders")
data class MusicFolderEntity(
    @PrimaryKey val uriString: String,
    val displayName: String,
    val trackCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 1,
    val skipCount: Int = 0
)

@Entity(tableName = "queue")
data class QueueEntity(
    @PrimaryKey val songId: String,
    val orderIndex: Int,
    val isCurrent: Boolean = false
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val artworkUri: String? = null
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "song_ratings")
data class SongRatingEntity(
    @PrimaryKey val songId: String,
    val rating: Int, // 1 to 5 stars
    val ratedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lyrics_cache")
data class LyricsCacheEntity(
    @PrimaryKey val songId: String,
    val rawLyrics: String,
    val isSynchronized: Boolean,
    val source: String = "local", // "embedded", "lrc_file", "lrclib", "manual"
    val offsetMs: Long = 0L,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "listening_history")
data class ListeningHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val durationListenedMs: Long = 0L
)

