package com.example.aura.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.aura.data.audio.AudioMetadataHelper
import com.example.aura.data.audio.AudioSourceNormalizer
import com.example.aura.data.backup.AuraBackupManager
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
import com.example.aura.data.local.entity.MusicFolderEntity
import com.example.aura.data.local.entity.QueueEntity
import com.example.aura.data.local.entity.RecentlyPlayedEntity
import com.example.aura.data.local.entity.SongEntity
import com.example.aura.data.local.entity.SongRatingEntity
import com.example.aura.data.lyrics.LyricsManager
import com.example.aura.data.metadata.MetadataEditor
import com.example.aura.data.stats.StatisticsEngine
import com.example.aura.domain.model.Album
import com.example.aura.domain.model.Artist
import com.example.aura.domain.model.FolderGroup
import com.example.aura.domain.model.Genre
import com.example.aura.domain.model.ListeningStatistics
import com.example.aura.domain.model.LyricsParser
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.SearchResults
import com.example.aura.domain.model.SmartPlaylist
import com.example.aura.domain.model.SmartPlaylistType
import com.example.aura.domain.model.Song
import com.example.aura.domain.repository.MusicRepository
import com.example.aura.data.exclusion.LibraryExclusionFilter
import com.example.aura.data.local.UserPreferences
import com.example.aura.domain.model.exclusion.SmartScanSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class DiscoveredTrack(
    val uri: Uri,
    val filePath: String? = null,
    val mediaStoreId: Long? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val durationMs: Long = 0L,
    val albumId: Long? = null,
    val trackNumber: Int? = null,
    val genre: String? = null,
    val year: Int? = null,
    val dateAdded: Long = 0L,
    val mimeType: String? = null,
    val fileSize: Long = 0L,
    val fallbackAlbum: String? = null
)

data class DatabaseIntegrityReport(
    val totalTracks: Int,
    val distinctSourceIds: Int,
    val duplicateSourceIds: Int
)

class MusicRepositoryImpl(
    private val context: Context,
    private val songDao: SongDao,
    private val favoriteDao: FavoriteDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val musicFolderDao: MusicFolderDao? = null,
    private val queueDao: QueueDao? = null,
    private val playlistDao: PlaylistDao? = null,
    private val songRatingDao: SongRatingDao? = null,
    private val lyricsCacheDao: LyricsCacheDao? = null,
    private val listeningHistoryDao: ListeningHistoryDao? = null,
    private val userPreferences: UserPreferences = UserPreferences(context)
) : MusicRepository {

    private val artworkCacheDir by lazy {
        File(context.cacheDir, "artwork_cache").apply { if (!exists()) mkdirs() }
    }

    private val lyricsManager by lazy {
        lyricsCacheDao?.let { LyricsManager(context, it) }
    }

    private val statisticsEngine by lazy {
        if (listeningHistoryDao != null) {
            StatisticsEngine(songDao, recentlyPlayedDao, listeningHistoryDao)
        } else null
    }

    private val backupManager by lazy {
        if (playlistDao != null && songRatingDao != null) {
            AuraBackupManager(context, playlistDao, favoriteDao, songRatingDao)
        } else null
    }

    private val metadataEditor by lazy {
        MetadataEditor(context, songDao)
    }

    override fun getAllSongs(): Flow<List<Song>> {
        val ratingsFlow = songRatingDao?.getAllRatings() ?: flowOf(emptyList())
        val settingsFlow = userPreferences.smartScanSettingsFlow
        return combine(songDao.getAllSongs(), favoriteDao.getFavoriteSongIds(), ratingsFlow, settingsFlow) { entities, favIds, ratings, settings ->
            val favSet = favIds.toSet()
            val ratingMap = ratings.associate { it.songId to it.rating }
            val filter = LibraryExclusionFilter(settings)
            entities
                .filterNot { filter.shouldExcludeSongEntity(it) }
                .map {
                    it.toDomain(
                        isFavorite = favSet.contains(it.id),
                        rating = ratingMap[it.id] ?: 0
                    )
                }
        }
    }

    override fun getArtists(): Flow<List<Artist>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
                Artist(
                    id = artistName,
                    name = artistName,
                    trackCount = artistSongs.size,
                    artworkUri = artistSongs.firstOrNull { it.artworkUri != null }?.artworkUri
                )
            }.sortedBy { it.name.lowercase() }
        }
    }

    override fun getAlbums(): Flow<List<Album>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.album to it.artist }.map { (key, albumSongs) ->
                val (albumName, artistName) = key
                Album(
                    id = "$albumName-$artistName",
                    title = albumName,
                    artist = artistName,
                    artworkUri = albumSongs.firstOrNull { it.artworkUri != null }?.artworkUri,
                    year = albumSongs.firstNotNullOfOrNull { it.year },
                    trackCount = albumSongs.size
                )
            }.sortedBy { it.title.lowercase() }
        }
    }

    override fun getGenres(): Flow<List<Genre>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.genre?.takeIf { g -> g.isNotBlank() } ?: "Other" }.map { (genreName, genreSongs) ->
                Genre(
                    name = genreName,
                    trackCount = genreSongs.size,
                    artworkUri = genreSongs.firstOrNull { it.artworkUri != null }?.artworkUri
                )
            }.sortedBy { it.name.lowercase() }
        }
    }

    override fun getFolders(): Flow<List<FolderGroup>> {
        return getAllSongs().map { songs ->
            songs.groupBy { song ->
                song.filePath?.let { File(it).parent } ?: "Imported Folder"
            }.map { (folderPath, folderSongs) ->
                val name = if (folderPath.startsWith("content://") || folderPath == "Imported Folder") {
                    "Imported Music"
                } else {
                    File(folderPath).name.ifBlank { folderPath }
                }
                FolderGroup(
                    path = folderPath,
                    displayName = name,
                    trackCount = folderSongs.size
                )
            }.sortedBy { it.displayName.lowercase() }
        }
    }

    override fun getFavorites(): Flow<List<Song>> {
        return getAllSongs().map { songs ->
            songs.filter { it.isFavorite }
        }
    }

    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return combine(getAllSongs(), recentlyPlayedDao.getRecentlyPlayed()) { songs, recentEntries ->
            val songMap = songs.associateBy { it.id }
            recentEntries.mapNotNull { entry ->
                songMap[entry.songId]
            }
        }
    }

    override fun getMostPlayed(): Flow<List<Song>> {
        return combine(getAllSongs(), recentlyPlayedDao.getMostPlayed()) { songs, mostPlayedEntries ->
            val songMap = songs.associateBy { it.id }
            mostPlayedEntries.mapNotNull { entry ->
                songMap[entry.songId]
            }
        }
    }

    override fun getRecentlyAdded(): Flow<List<Song>> {
        return getAllSongs().map { songs ->
            songs.sortedByDescending { it.dateAdded }
        }
    }

    override fun getFavoriteSongIds(): Flow<List<String>> {
        return favoriteDao.getFavoriteSongIds()
    }

    override fun getSongsByAlbum(albumTitle: String): Flow<List<Song>> {
        return getAllSongs().map { songs ->
            songs.filter { it.album.equals(albumTitle, ignoreCase = true) }
                .sortedWith(compareBy<Song> { it.trackNumber ?: Int.MAX_VALUE }.thenBy { it.title.lowercase() })
        }
    }

    override fun getSongsByArtist(artistName: String): Flow<List<Song>> {
        return getAllSongs().map { songs ->
            songs.filter { it.artist.equals(artistName, ignoreCase = true) }
                .sortedBy { it.title.lowercase() }
        }
    }

    override fun getSongsByGenre(genreName: String): Flow<List<Song>> {
        return getAllSongs().map { songs ->
            songs.filter { it.genre.equals(genreName, ignoreCase = true) }
                .sortedBy { it.title.lowercase() }
        }
    }

    override fun getSongsByFolder(folderPath: String): Flow<List<Song>> {
        return getAllSongs().map { songs ->
            songs.filter { song ->
                song.filePath?.let { File(it).parent } == folderPath ||
                        (folderPath.startsWith("content://") && song.mediaUri.startsWith(folderPath))
            }.sortedBy { it.title.lowercase() }
        }
    }

    override fun search(query: String): Flow<List<Song>> {
        return combine(songDao.searchSongs(query), favoriteDao.getFavoriteSongIds(), userPreferences.smartScanSettingsFlow) { entities, favIds, settings ->
            val favSet = favIds.toSet()
            val filter = LibraryExclusionFilter(settings)
            entities
                .filterNot { filter.shouldExcludeSongEntity(it) }
                .map { it.toDomain(isFavorite = favSet.contains(it.id)) }
        }
    }

    override fun searchAll(query: String): Flow<SearchResults> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(SearchResults())
        }
        val queryTokens = trimmed.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }

        return combine(
            getAllSongs(),
            getAlbums(),
            getArtists(),
            getGenres(),
            getFolders()
        ) { allSongs, allAlbums, allArtists, allGenres, allFolders ->
            fun matches(vararg fields: String?): Boolean {
                val combined = fields.filterNotNull().joinToString(" ").lowercase()
                return queryTokens.all { combined.contains(it) }
            }

            val matchingSongs = allSongs.filter {
                matches(it.title, it.artist, it.album, it.albumArtist, it.genre)
            }
            val matchingAlbums = allAlbums.filter {
                matches(it.title, it.artist)
            }
            val matchingArtists = allArtists.filter {
                matches(it.name)
            }
            val matchingGenres = allGenres.filter {
                matches(it.name)
            }
            val matchingFolders = allFolders.filter {
                matches(it.displayName, it.path)
            }

            SearchResults(
                query = query,
                songs = matchingSongs,
                albums = matchingAlbums,
                artists = matchingArtists,
                genres = matchingGenres,
                folders = matchingFolders
            )
        }
    }

    /**
     * Authoritative discovery from Android MediaStore.
     * MediaStore returns files recursively from Music/ and its subfolders.
     * @param allDeviceTrackIds optional collector for ALL valid MediaStore IDs found on device,
     * so that physically existing tracks that are merely excluded won't be deleted from Room DB.
     */
    suspend fun discoverMediaStoreTracks(allDeviceTrackIds: MutableList<String>? = null): List<DiscoveredTrack> {
        val discovered = mutableListOf<DiscoveredTrack>()
        val smartSettings = userPreferences.getSmartScanSettingsSync()
        val filter = LibraryExclusionFilter(smartSettings)

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.GENRE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.Audio.Media.ALBUM_ARTIST)
            }
        }.toTypedArray()

        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%') AND ${MediaStore.Audio.Media.DURATION} >= 1000"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
            val genreCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getColumnIndex(MediaStore.Audio.Media.GENRE) else -1
            val albumArtistCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST) else -1

            while (cursor.moveToNext()) {
                val idLong = cursor.getLong(idCol)
                val rawMediaUri = ContentUris.withAppendedId(collection, idLong)
                val filePath = if (dataCol != -1) cursor.getString(dataCol) else null
                val title = cursor.getString(titleCol)
                val duration = cursor.getLong(durationCol)

                // Track physical existence so deleteRemovedMediaStoreSongs won't purge excluded tracks from DB
                val stableId = AudioSourceNormalizer.generateStableTrackId(
                    AudioSourceNormalizer.normalizeSource(context, rawMediaUri, filePath).canonicalIdentity
                )
                allDeviceTrackIds?.add(stableId)

                // Early multi-stage exclusion check
                val fileName = if (!filePath.isNullOrBlank()) File(filePath).name else title
                if (filter.shouldExclude(
                    filePath = filePath,
                    uriString = rawMediaUri.toString(),
                    fileName = fileName,
                    durationMs = duration
                )) {
                    continue
                }

                val artist = cursor.getString(artistCol)?.takeIf { it != "<unknown>" }
                val album = cursor.getString(albumCol)?.takeIf { it != "<unknown>" }
                val albumId = cursor.getLong(albumIdCol)
                val trackNum = cursor.getInt(trackCol).takeIf { it > 0 }
                val dateAdded = cursor.getLong(dateAddedCol)
                val mimeType = cursor.getString(mimeCol)
                val fileSize = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                val mediaStoreGenre = if (genreCol != -1) cursor.getString(genreCol) else null
                val mediaStoreAlbumArtist = if (albumArtistCol != -1) cursor.getString(albumArtistCol) else null

                discovered.add(
                    DiscoveredTrack(
                        uri = rawMediaUri,
                        filePath = filePath,
                        mediaStoreId = idLong,
                        title = title,
                        artist = artist,
                        album = album,
                        albumArtist = mediaStoreAlbumArtist,
                        durationMs = duration,
                        albumId = albumId,
                        trackNumber = trackNum,
                        genre = mediaStoreGenre,
                        dateAdded = dateAdded,
                        mimeType = mimeType,
                        fileSize = fileSize
                    )
                )
            }
        }
        return discovered
    }

    /**
     * Authoritative discovery from SAF or local filesystem folders.
     */
    suspend fun discoverFolderTracks(uriString: String): List<DiscoveredTrack> {
        val discovered = mutableListOf<DiscoveredTrack>()
        val smartSettings = userPreferences.getSmartScanSettingsSync()
        val filter = LibraryExclusionFilter(smartSettings)

        val supportedExtensions = setOf(
            "mp3", "flac", "wav", "m4a", "aac", "ogg", "opus", "alac",
            "aiff", "aif", "wma", "ape", "wv", "dsf", "dff", "mka"
        )

        if (uriString.startsWith("file://") || uriString.startsWith("/")) {
            val rootPath = if (uriString.startsWith("file://")) uriString.removePrefix("file://") else uriString
            val rootDir = File(rootPath)
            if (rootDir.exists() && rootDir.isDirectory) {
                if (filter.isFolderExcluded(rootDir.absolutePath)) return discovered

                rootDir.walkTopDown()
                    .onEnter { dir -> !filter.isFolderExcluded(dir.absolutePath) }
                    .forEach { file ->
                        val ext = file.extension.lowercase()
                        if (file.isFile && ext in supportedExtensions && !filter.isExtensionExcluded(ext)) {
                            if (!filter.shouldExclude(filePath = file.absolutePath, fileName = file.name, durationMs = 0L)) {
                                discovered.add(
                                    DiscoveredTrack(
                                        uri = Uri.fromFile(file),
                                        filePath = file.absolutePath,
                                        mimeType = null,
                                        fileSize = file.length(),
                                        fallbackAlbum = rootDir.name.ifBlank { "Imported Folder" }
                                    )
                                )
                            }
                        }
                    }
            }
            return discovered
        }

        val treeUri = Uri.parse(uriString)
        val rootDoc = try {
            DocumentFile.fromTreeUri(context, treeUri)
        } catch (_: Exception) {
            null
        } ?: return discovered

        fun scanDoc(doc: DocumentFile) {
            if (doc.isDirectory) {
                val folderName = doc.name.orEmpty()
                val folderUriPath = doc.uri.path.orEmpty()
                if (filter.isFolderExcluded(folderName) || filter.isFolderExcluded(folderUriPath)) {
                    return
                }
                doc.listFiles().forEach { child -> scanDoc(child) }
            } else if (doc.isFile) {
                val name = doc.name ?: return
                val ext = name.substringAfterLast('.', "").lowercase()
                if (filter.isExtensionExcluded(ext)) return
                if (filter.shouldExclude(uriString = doc.uri.toString(), fileName = name, durationMs = 0L)) return
                if (ext in supportedExtensions) {
                    discovered.add(
                        DiscoveredTrack(
                            uri = doc.uri,
                            filePath = null,
                            mimeType = doc.type,
                            fileSize = doc.length(),
                            fallbackAlbum = rootDoc.name ?: "Imported Folder"
                        )
                    )
                }
            }
        }

        scanDoc(rootDoc)
        return discovered
    }

    /**
     * THE SINGLE AUTHORITATIVE IMPORT PIPELINE.
     * All scanners (MediaStore, SAF, Folder) MUST feed into this function.
     * Performs canonicalization, in-memory deduplication, user state preservation,
     * metadata merging, and Room @Upsert.
     */
    suspend fun importDiscoveredTracks(discovered: List<DiscoveredTrack>): Int = withContext(Dispatchers.IO) {
        if (discovered.isEmpty()) return@withContext 0

        val smartSettings = userPreferences.getSmartScanSettingsSync()
        val filter = LibraryExclusionFilter(smartSettings)

        cleanupExistingDuplicates()

        // 1. In-memory deduplication by canonical identity
        val canonicalMap = mutableMapOf<String, DiscoveredTrack>()
        val canonicalSourceMap = mutableMapOf<String, com.example.aura.data.audio.NormalizedAudioSource>()

        for (item in discovered) {
            val normalized = AudioSourceNormalizer.normalizeSource(
                context = context,
                uri = item.uri,
                filePath = item.filePath
            )
            val canonicalId = normalized.canonicalIdentity
            canonicalSourceMap[canonicalId] = normalized

            val existing = canonicalMap[canonicalId]
            if (existing == null) {
                canonicalMap[canonicalId] = item
            } else {
                // If discovered multiple times across scanners, prefer the one with richer metadata
                if (item.mediaStoreId != null && existing.mediaStoreId == null) {
                    canonicalMap[canonicalId] = item
                }
            }
        }

        val existingSongs = songDao.getAllSongsSync()
        val existingBySourceUri = existingSongs.associateBy { it.sourceUri }.toMutableMap()
        val existingById = existingSongs.associateBy { it.id }.toMutableMap()

        val entitiesToUpsert = mutableListOf<SongEntity>()

        for ((canonicalSource, track) in canonicalMap) {
            val normalized = canonicalSourceMap[canonicalSource] ?: AudioSourceNormalizer.normalizeSource(
                context = context,
                uri = track.uri,
                filePath = track.filePath
            )

            val stableId = AudioSourceNormalizer.generateStableTrackId(canonicalSource)
            val existing = existingBySourceUri[canonicalSource] ?: existingById[stableId]
            val finalId = existing?.id ?: stableId

            val rawMediaUri = track.uri.toString()
            val resolvedPath = normalized.resolvedFilePath ?: track.filePath

            val techDetails = AudioMetadataHelper.extractTechnicalDetails(
                context = context,
                uri = track.uri,
                filePath = resolvedPath,
                mimeTypeHint = track.mimeType
            )

            var resolvedArtworkUri = existing?.artworkUri
            if (resolvedArtworkUri == null && track.albumId != null && track.albumId > 0) {
                resolvedArtworkUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    track.albumId
                ).toString()
            }

            techDetails.embeddedArtworkBytes?.let { bytes ->
                val artFile = File(artworkCacheDir, "art_$finalId.jpg")
                if (!artFile.exists() || artFile.length() == 0L) {
                    try {
                        FileOutputStream(artFile).use { fos -> fos.write(bytes) }
                        resolvedArtworkUri = Uri.fromFile(artFile).toString()
                    } catch (_: Exception) {}
                } else {
                    resolvedArtworkUri = Uri.fromFile(artFile).toString()
                }
            }

            val nameFromUri = try { track.uri.lastPathSegment?.substringBeforeLast('.') } catch (_: Exception) { null }
            val finalTitle = existing?.title?.takeIf { it.isNotBlank() }
                ?: techDetails.title?.takeIf { it.isNotBlank() }
                ?: track.title?.takeIf { it.isNotBlank() }
                ?: (nameFromUri ?: "Unknown Track")

            val finalArtist = existing?.artist?.takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: techDetails.artist?.takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: track.artist?.takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: "Unknown Artist"

            val finalAlbum = existing?.album?.takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: techDetails.album?.takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: track.album?.takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: track.fallbackAlbum
                ?: "Unknown Album"

            val finalAlbumArtist = existing?.albumArtist?.takeIf { it.isNotBlank() }
                ?: track.albumArtist?.takeIf { it.isNotBlank() }
                ?: techDetails.albumArtist

            val finalGenre = existing?.genre?.takeIf { it.isNotBlank() }
                ?: track.genre?.takeIf { it.isNotBlank() }
                ?: techDetails.genre
                ?: techDetails.codec

            val finalDuration = if (existing != null && existing.durationMs > 0) {
                existing.durationMs
            } else if (track.durationMs > 0) {
                track.durationMs
            } else {
                techDetails.durationMs
            }

            if (filter.isDurationExcluded(finalDuration)) {
                continue
            }

            val finalDateAdded = existing?.dateAdded?.takeIf { it > 0 }
                ?: track.dateAdded.takeIf { it > 0 }
                ?: System.currentTimeMillis()

            val finalFileSize = if (track.fileSize > 0) {
                track.fileSize
            } else if (existing != null && existing.fileSize > 0) {
                existing.fileSize
            } else {
                resolvedPath?.let { File(it).length() } ?: 0L
            }

            val entity = SongEntity(
                id = finalId,
                sourceUri = canonicalSource,
                title = finalTitle,
                artist = finalArtist,
                album = finalAlbum,
                albumArtist = finalAlbumArtist,
                durationMs = finalDuration,
                mediaUri = rawMediaUri,
                artworkUri = resolvedArtworkUri,
                trackNumber = existing?.trackNumber ?: techDetails.trackNumber ?: track.trackNumber,
                genre = finalGenre,
                year = existing?.year ?: techDetails.year ?: track.year,
                dateAdded = finalDateAdded,
                filePath = resolvedPath,
                mimeType = track.mimeType ?: existing?.mimeType ?: techDetails.mimeType,
                fileSize = finalFileSize,
                codec = techDetails.codec ?: existing?.codec,
                bitrate = if (techDetails.bitrate > 0) techDetails.bitrate else existing?.bitrate ?: 0,
                sampleRate = if (techDetails.sampleRate > 0) techDetails.sampleRate else existing?.sampleRate ?: 0,
                bitDepth = if (techDetails.bitDepth > 0) techDetails.bitDepth else existing?.bitDepth ?: 0,
                channelCount = if (techDetails.channelCount > 0) techDetails.channelCount else existing?.channelCount ?: 2,
                isLossless = techDetails.isLossless || (existing?.isLossless ?: false)
            )

            entitiesToUpsert.add(entity)
            existingBySourceUri[canonicalSource] = entity
            existingById[finalId] = entity
        }

        if (entitiesToUpsert.isNotEmpty()) {
            songDao.upsertSongs(entitiesToUpsert)
        }

        val allSongsAfter = songDao.getAllSongsSync()
        val totalCount = allSongsAfter.size
        val distinctCount = allSongsAfter.map { it.sourceUri }.toSet().size
        Log.i("AURA_INTEGRITY", "CANONICAL IMPORT CHECK -> TOTAL TRACKS: $totalCount, DISTINCT SOURCES: $distinctCount, DUPLICATES: ${totalCount - distinctCount}")

        return@withContext entitiesToUpsert.size
    }

    override suspend fun scanDeviceMusic(): Int = withContext(Dispatchers.IO) {
        val allDeviceMediaIds = mutableListOf<String>()
        val discovered = discoverMediaStoreTracks(allDeviceMediaIds)
        val count = importDiscoveredTracks(discovered)

        // Clean up MediaStore tracks that no longer exist on storage.
        // We pass ALL valid MediaStore IDs found on the device (including excluded ones),
        // so that files merely excluded by rules are NOT deleted from the database!
        if (allDeviceMediaIds.isNotEmpty()) {
            songDao.deleteRemovedMediaStoreSongs(allDeviceMediaIds)
        }

        count
    }

    suspend fun verifyDatabaseIntegrity(): DatabaseIntegrityReport = withContext(Dispatchers.IO) {
        val allSongs = songDao.getAllSongsSync()
        val totalCount = allSongs.size
        val distinctSources = allSongs.map { it.sourceUri }.toSet().size
        val duplicateCount = totalCount - distinctSources
        Log.i("AURA_INTEGRITY", "DATABASE INTEGRITY: TOTAL: $totalCount, DISTINCT: $distinctSources, DUPLICATES: $duplicateCount")
        check(totalCount == distinctSources) {
            "Database integrity violation! Total ($totalCount) != Distinct ($distinctSources)"
        }
        DatabaseIntegrityReport(totalCount, distinctSources, duplicateCount)
    }

    private suspend fun cleanupExistingDuplicates() {
        val allSongs = songDao.getAllSongsSync()
        if (allSongs.isEmpty()) return

        val groups = allSongs.groupBy { song ->
            if (song.sourceUri.isNotBlank()) {
                AudioSourceNormalizer.normalizeSourceString(
                    context = context,
                    uriString = song.sourceUri,
                    filePath = song.filePath
                ).canonicalIdentity
            } else {
                AudioSourceNormalizer.normalizeSource(
                    context = context,
                    uri = Uri.parse(song.mediaUri),
                    filePath = song.filePath
                ).canonicalIdentity
            }
        }

        val redundantIdsToDelete = mutableListOf<String>()
        val canonicalSongsToUpdate = mutableListOf<SongEntity>()

        for ((canonicalSource, group) in groups) {
            if (group.size <= 1) {
                val single = group.first()
                if (single.sourceUri.isBlank() || single.sourceUri != canonicalSource) {
                    canonicalSongsToUpdate.add(single.copy(sourceUri = canonicalSource))
                }
                continue
            }

            val canonicalSong = group.find { !it.id.startsWith("saf_") } ?: group.minByOrNull { it.dateAdded } ?: group.first()
            val canonicalId = canonicalSong.id
            val duplicateIds = group.map { it.id }.filter { it != canonicalId }

            Log.d("AURA_SCAN", "SCAN: duplicate detected for $canonicalSource - canonicalId: $canonicalId, duplicates: $duplicateIds")

            for (dupId in duplicateIds) {
                // Merge Favorites
                if (favoriteDao.isFavorite(dupId)) {
                    if (!favoriteDao.isFavorite(canonicalId)) {
                        favoriteDao.addFavorite(FavoriteEntity(songId = canonicalId))
                    }
                    favoriteDao.removeFavorite(dupId)
                }

                // Merge Ratings
                songRatingDao?.let { dao ->
                    val dupRating = dao.getRatingSync(dupId)
                    if (dupRating != null) {
                        val canonicalRating = dao.getRatingSync(canonicalId)
                        if (canonicalRating == null) {
                            dao.setRating(SongRatingEntity(songId = canonicalId, rating = dupRating))
                        }
                        dao.removeRating(dupId)
                    }
                }

                // Merge Playlists
                playlistDao?.let { pDao ->
                    pDao.repointSongId(oldSongId = dupId, newSongId = canonicalId)
                    pDao.deleteRefsForSong(dupId)
                }

                // Merge Queue
                queueDao?.let { qDao ->
                    val queueItems = qDao.getQueueSync()
                    val hasCanonical = queueItems.any { it.songId == canonicalId }
                    if (hasCanonical) {
                        qDao.deleteQueueSongId(dupId)
                    } else {
                        qDao.repointQueueSongId(oldSongId = dupId, newSongId = canonicalId)
                    }
                }

                // Merge Lyrics Cache
                lyricsCacheDao?.let { dao ->
                    val dupLyrics = dao.getLyrics(dupId)
                    if (dupLyrics != null) {
                        val canonicalLyrics = dao.getLyrics(canonicalId)
                        if (canonicalLyrics == null) {
                            dao.saveLyrics(dupLyrics.copy(songId = canonicalId))
                        }
                        dao.deleteLyrics(dupId)
                    }
                }

                // Merge Listening History
                listeningHistoryDao?.repointHistory(oldSongId = dupId, newSongId = canonicalId)

                // Merge Recently Played
                recentlyPlayedDao.repointRecentlyPlayed(oldSongId = dupId, newSongId = canonicalId)
                recentlyPlayedDao.deleteRecentlyPlayed(dupId)

                redundantIdsToDelete.add(dupId)
            }

            canonicalSongsToUpdate.add(canonicalSong.copy(sourceUri = canonicalSource))
        }

        if (redundantIdsToDelete.isNotEmpty()) {
            Log.d("AURA_SCAN", "SCAN: removing ${redundantIdsToDelete.size} duplicate records from database")
            songDao.deleteSongsByIds(redundantIdsToDelete)
        }

        if (canonicalSongsToUpdate.isNotEmpty()) {
            songDao.upsertSongs(canonicalSongsToUpdate)
        }
    }

    override suspend fun toggleFavorite(songId: String): Boolean = withContext(Dispatchers.IO) {
        val exists = favoriteDao.isFavorite(songId)
        if (exists) {
            favoriteDao.removeFavorite(songId)
            false
        } else {
            favoriteDao.addFavorite(FavoriteEntity(songId = songId))
            true
        }
    }

    override suspend fun recordRecentlyPlayed(songId: String) = withContext(Dispatchers.IO) {
        recentlyPlayedDao.recordPlay(songId)
    }

    override suspend fun recordSkip(songId: String) = withContext(Dispatchers.IO) {
        recentlyPlayedDao.incrementSkipCount(songId)
    }

    override fun getMusicFolders(): Flow<List<MusicFolderEntity>> {
        return musicFolderDao?.getAllFolders() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override suspend fun addMusicFolder(uri: Uri, displayName: String) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val folderEntity = MusicFolderEntity(
                uriString = uri.toString(),
                displayName = displayName,
                trackCount = 0,
                dateAdded = System.currentTimeMillis()
            )
            musicFolderDao?.insertFolder(folderEntity)
            val discovered = discoverFolderTracks(uri.toString())
            importDiscoveredTracks(discovered)
            musicFolderDao?.updateTrackCount(uri.toString(), discovered.size)
        }
    }

    override suspend fun removeMusicFolder(uriString: String) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.releasePersistableUriPermission(
                Uri.parse(uriString),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}
        musicFolderDao?.deleteFolder(uriString)
        songDao.deleteSongsByUriPrefix(uriString)
    }

    override suspend fun rescanFolder(uriString: String): Int = withContext(Dispatchers.IO) {
        val discovered = discoverFolderTracks(uriString)
        val count = importDiscoveredTracks(discovered)
        musicFolderDao?.updateTrackCount(uriString, discovered.size)
        count
    }

    override suspend fun saveQueue(songs: List<Song>, currentIndex: Int) {
        if (queueDao == null) return
        withContext(Dispatchers.IO) {
            val entities = songs.mapIndexed { index, song ->
                QueueEntity(
                    songId = song.id,
                    orderIndex = index,
                    isCurrent = index == currentIndex
                )
            }
            queueDao.replaceQueue(entities)
        }
    }

    override suspend fun getSavedQueue(): Pair<List<Song>, Int>? = withContext(Dispatchers.IO) {
        if (queueDao == null) return@withContext null
        val queueItems = queueDao.getQueueSync()
        if (queueItems.isEmpty()) return@withContext null

        val songIds = queueItems.map { it.songId }
        val songEntities = songDao.getSongsByIds(songIds).associateBy { it.id }

        val orderedSongs = queueItems.mapNotNull { q ->
            songEntities[q.songId]?.toDomain()
        }

        val currentIndex = queueItems.indexOfFirst { it.isCurrent }.let { if (it == -1) 0 else it }
        if (orderedSongs.isNotEmpty()) Pair(orderedSongs, currentIndex) else null
    }

    override suspend fun getLyrics(
        song: Song,
        allowOnline: Boolean,
        forceRefresh: Boolean
    ): LyricsState = withContext(Dispatchers.IO) {
        lyricsManager?.getLyrics(song, allowOnline, forceRefresh) ?: run {
            val rawLyrics = AudioMetadataHelper.extractLyrics(
                context = context,
                uri = Uri.parse(song.mediaUri),
                filePath = song.filePath
            )
            LyricsParser.parse(rawLyrics)
        }
    }

    override suspend fun attachManualLyrics(songId: String, uri: Uri): LyricsState {
        return lyricsManager?.attachManualLyricsFile(songId, uri) ?: LyricsState.Unavailable
    }

    override suspend fun clearLyricsCache(songId: String?) {
        if (songId != null) {
            lyricsManager?.clearCacheForSong(songId)
        } else {
            lyricsManager?.clearAllCache()
        }
    }

    override suspend fun saveLyricsOffset(songId: String, offsetMs: Long) {
        lyricsManager?.savePerSongOffset(songId, offsetMs)
    }

    override fun getRatings(): Flow<Map<String, Int>> {
        return songRatingDao?.getAllRatings()?.map { list ->
            list.associate { it.songId to it.rating }
        } ?: flowOf(emptyMap())
    }

    override suspend fun setRating(songId: String, rating: Int): Unit = withContext(Dispatchers.IO) {
        songRatingDao?.setRating(SongRatingEntity(songId = songId, rating = rating.coerceIn(1, 5)))
        Unit
    }

    override suspend fun removeRating(songId: String): Unit = withContext(Dispatchers.IO) {
        songRatingDao?.removeRating(songId)
        Unit
    }

    override fun getSmartPlaylists(): Flow<List<SmartPlaylist>> {
        val recentFlow = recentlyPlayedDao.getRecentlyPlayed()
        val mostFlow = recentlyPlayedDao.getMostPlayed()
        return combine(getAllSongs(), recentFlow, mostFlow) { songs, recent, most ->
            val songMap = songs.associateBy { it.id }
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

            val recentlyAdded = songs.filter { it.dateAdded >= thirtyDaysAgo }.sortedByDescending { it.dateAdded }
            val recentlyPlayedSongs = recent.mapNotNull { songMap[it.songId] }
            val mostPlayedSongs = most.mapNotNull { songMap[it.songId] }
            val playedIds = (recent.map { it.songId } + most.map { it.songId }).toSet()
            val neverPlayed = songs.filterNot { playedIds.contains(it.id) }
            val favorites = songs.filter { it.isFavorite }
            val longTracks = songs.filter { it.durationMs > 300_000L } // > 5 minutes
            val highQuality = songs.filter { it.isLossless || (it.sampleRate >= 88200 || it.bitDepth >= 24) }

            listOf(
                SmartPlaylist(SmartPlaylistType.RECENTLY_ADDED, recentlyAdded),
                SmartPlaylist(SmartPlaylistType.RECENTLY_PLAYED, recentlyPlayedSongs),
                SmartPlaylist(SmartPlaylistType.MOST_PLAYED, mostPlayedSongs),
                SmartPlaylist(SmartPlaylistType.NEVER_PLAYED, neverPlayed),
                SmartPlaylist(SmartPlaylistType.FAVORITES, favorites),
                SmartPlaylist(SmartPlaylistType.LONG_TRACKS, longTracks),
                SmartPlaylist(SmartPlaylistType.HIGH_QUALITY, highQuality)
            )
        }
    }

    override suspend fun getStatistics(): ListeningStatistics = withContext(Dispatchers.IO) {
        val allSongs = getAllSongs().first()
        statisticsEngine?.computeStatistics(allSongs) ?: ListeningStatistics()
    }

    override suspend fun recordListeningSession(songId: String, durationMs: Long): Unit = withContext(Dispatchers.IO) {
        statisticsEngine?.recordListeningSession(songId, durationMs)
        Unit
    }

    override suspend fun updateSongMetadata(
        songId: String,
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int?,
        trackNumber: Int?,
        artworkUri: String?
    ): Boolean = withContext(Dispatchers.IO) {
        metadataEditor.updateSongMetadata(songId, title, artist, album, genre, year, trackNumber, artworkUri)
    }

    override suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        backupManager?.createBackupJson() ?: "{}"
    }

    override suspend fun restoreBackupJson(json: String): Boolean = withContext(Dispatchers.IO) {
        backupManager?.restoreBackup(json) ?: false
    }

    override suspend fun exportBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        backupManager?.exportToFile(uri) ?: false
    }

    override suspend fun importBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        backupManager?.importFromFile(uri) ?: false
    }

    // Smart Library Exclusions & Filtering
    override fun getSmartScanSettings(): Flow<SmartScanSettings> = userPreferences.smartScanSettingsFlow

    override suspend fun getSmartScanSettingsSync(): SmartScanSettings = userPreferences.getSmartScanSettingsSync()

    override suspend fun updateSmartScanSettings(settings: SmartScanSettings) {
        userPreferences.updateSmartScanSettings(settings)
    }

    override suspend fun addExcludedFolder(path: String, displayName: String) {
        userPreferences.addExcludedFolder(path, displayName)
    }

    override suspend fun removeExcludedFolder(path: String) {
        userPreferences.removeExcludedFolder(path)
    }

    override suspend fun toggleExcludedFolder(path: String, isEnabled: Boolean) {
        userPreferences.toggleExcludedFolder(path, isEnabled)
    }

    override suspend fun toggleExcludedExtension(extension: String, isExcluded: Boolean) {
        userPreferences.toggleExcludedExtension(extension, isExcluded)
    }

    override suspend fun setMinDurationSeconds(seconds: Int) {
        userPreferences.setMinDurationSeconds(seconds)
    }

    override suspend fun addFilenamePattern(pattern: String) {
        userPreferences.addFilenamePattern(pattern)
    }

    override suspend fun removeFilenamePattern(pattern: String) {
        userPreferences.removeFilenamePattern(pattern)
    }

    override suspend fun toggleFilenamePattern(pattern: String, isEnabled: Boolean) {
        userPreferences.toggleFilenamePattern(pattern, isEnabled)
    }

    override suspend fun hideSong(song: Song) {
        userPreferences.hideTrack(song)
    }

    override suspend fun hideFolder(folderPath: String, displayName: String?) {
        userPreferences.hideFolder(folderPath, displayName)
    }

    override suspend fun hideSimilar(pattern: String) {
        userPreferences.hideSimilar(pattern)
    }

    override suspend fun unhideTrack(sourceUriOrId: String) {
        userPreferences.unhideTrack(sourceUriOrId)
    }

    override suspend fun restoreAllHiddenTracks() {
        userPreferences.restoreAllHiddenTracks()
    }

    override suspend fun applyCleanMusicLibraryPreset() {
        userPreferences.applyCleanMusicLibraryPreset()
    }
}

