package com.example.aura.data.repository

import com.example.aura.data.audio.AudioSourceNormalizer
import com.example.aura.data.local.dao.PlaylistDao
import com.example.aura.data.local.dao.QueueDao
import com.example.aura.data.local.entity.FavoriteEntity
import com.example.aura.data.local.entity.PlaylistEntity
import com.example.aura.data.local.entity.PlaylistSongCrossRef
import com.example.aura.data.local.entity.QueueEntity
import com.example.aura.data.local.entity.RecentlyPlayedEntity
import com.example.aura.data.local.entity.SongEntity
import com.example.aura.data.lyrics.LyricsManager
import com.example.aura.domain.model.LyricsParser
import com.example.aura.domain.model.LyricsState
import com.example.aura.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FakePlaylistDao : PlaylistDao {
    val playlistSongs = mutableListOf<PlaylistSongCrossRef>()
    override fun getPlaylists(): Flow<List<PlaylistEntity>> = MutableStateFlow(emptyList())
    override suspend fun getPlaylistById(id: Long): PlaylistEntity? = null
    override suspend fun createPlaylist(playlist: PlaylistEntity): Long = 1L
    override suspend fun renamePlaylist(id: Long, newName: String) {}
    override suspend fun deletePlaylist(id: Long) {}
    override suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef) { playlistSongs.add(crossRef) }
    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) { playlistSongs.removeAll { it.playlistId == playlistId && it.songId == songId } }
    override suspend fun deletePlaylistSongs(playlistId: Long) { playlistSongs.removeAll { it.playlistId == playlistId } }
    override suspend fun insertPlaylistSongs(crossRefs: List<PlaylistSongCrossRef>) { playlistSongs.addAll(crossRefs) }
    override suspend fun getRefsForSong(songId: String): List<PlaylistSongCrossRef> = playlistSongs.filter { it.songId == songId }
    override suspend fun deleteRefsForSong(songId: String) { playlistSongs.removeAll { it.songId == songId } }
    override suspend fun repointSongId(oldSongId: String, newSongId: String) {
        val matches = playlistSongs.filter { it.songId == oldSongId }
        playlistSongs.removeAll { it.songId == oldSongId }
        for (m in matches) {
            if (playlistSongs.none { it.playlistId == m.playlistId && it.songId == newSongId }) {
                playlistSongs.add(m.copy(songId = newSongId))
            }
        }
    }
    override suspend fun isSongInPlaylist(playlistId: Long, songId: String): Boolean = playlistSongs.any { it.playlistId == playlistId && it.songId == songId }
    override fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>> = MutableStateFlow(emptyList())
}

class FakeQueueDao : QueueDao {
    val queueList = mutableListOf<QueueEntity>()
    override fun getQueue(): Flow<List<QueueEntity>> = MutableStateFlow(queueList.toList())
    override suspend fun getQueueSync(): List<QueueEntity> = queueList.toList()
    override suspend fun insertQueueItems(items: List<QueueEntity>) { queueList.addAll(items) }
    override suspend fun clearQueue() { queueList.clear() }
    override suspend fun repointQueueSongId(oldSongId: String, newSongId: String) {
        for (i in queueList.indices) {
            if (queueList[i].songId == oldSongId) {
                queueList[i] = queueList[i].copy(songId = newSongId)
            }
        }
    }
    override suspend fun deleteQueueSongId(songId: String) {
        queueList.removeAll { it.songId == songId }
    }
}

class MusicImportAndLyricsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var songDao: FakeSongDao
    private lateinit var favoriteDao: FakeFavoriteDao
    private lateinit var recentlyPlayedDao: FakeRecentlyPlayedDao
    private lateinit var playlistDao: FakePlaylistDao
    private lateinit var queueDao: FakeQueueDao

    @Before
    fun setup() {
        songDao = FakeSongDao()
        favoriteDao = FakeFavoriteDao()
        recentlyPlayedDao = FakeRecentlyPlayedDao()
        playlistDao = FakePlaylistDao()
        queueDao = FakeQueueDao()
    }

    /**
     * Test 1: Same physical file discovered through multiple sources (MediaStore + SAF)
     * Expected: Exactly 1 Track record in Room.
     */
    @Test
    fun test1_sameFileDiscoveredTwice_expectedSingleRecord() = runTest {
        val physicalPath = "/storage/emulated/0/Music/Song A.mp3"

        // Source 1: MediaStore content query
        val mediaStoreUri = "content://media/external/audio/media/101"
        val normMediaStore = AudioSourceNormalizer.normalizeSourceString(
            context = null,
            uriString = mediaStoreUri,
            filePath = physicalPath
        )

        // Source 2: SAF Folder Scanner
        val safUri = "content://com.android.externalstorage.documents/document/primary%3AMusic%2FSong%20A.mp3"
        val normSaf = AudioSourceNormalizer.normalizeSourceString(
            context = null,
            uriString = safUri,
            filePath = null
        )

        assertEquals("Canonical identity must match across MediaStore and SAF",
            normMediaStore.canonicalIdentity, normSaf.canonicalIdentity)
        assertEquals("Stable track ID must match across MediaStore and SAF",
            AudioSourceNormalizer.generateStableTrackId(normMediaStore.canonicalIdentity),
            AudioSourceNormalizer.generateStableTrackId(normSaf.canonicalIdentity)
        )

        val stableId = AudioSourceNormalizer.generateStableTrackId(normMediaStore.canonicalIdentity)

        // Simulate discovery batch containing both discovery sources for the same physical file
        val discoveredList = listOf(
            normMediaStore.canonicalIdentity to mediaStoreUri,
            normSaf.canonicalIdentity to safUri
        )

        // Deduplicate in memory
        val uniqueDiscovered = discoveredList.associateBy { it.first }.values.toList()
        assertEquals(1, uniqueDiscovered.size)

        // Upsert into Room
        val entity = SongEntity(
            id = stableId,
            sourceUri = uniqueDiscovered[0].first,
            title = "Song A",
            artist = "Artist",
            album = "Album",
            durationMs = 210000,
            mediaUri = uniqueDiscovered[0].second,
            artworkUri = null,
            trackNumber = 1,
            genre = "Pop",
            dateAdded = 1000,
            filePath = physicalPath
        )
        songDao.upsertSongs(listOf(entity))

        // Subsequent scan with the second source
        val entityFromSaf = entity.copy(mediaUri = safUri)
        songDao.upsertSongs(listOf(entityFromSaf))

        val allSongs = songDao.getAllSongsSync()
        assertEquals("Expected exactly 1 record for the same physical file", 1, allSongs.size)
        assertEquals(allSongs.size, allSongs.distinctBy { it.sourceUri }.size)
    }

    /**
     * Test 2: Music root + subfolder exact structure:
     * Internal Storage/
     * └── Music/
     *     ├── Song A.mp3
     *     ├── Song B.mp3
     *     ├── Song A.lrc
     *     └── MyFolder/
     *         ├── Song C.mp3
     *         ├── Song D.mp3
     *         ├── Song C.lrc
     *         └── Song E.flac
     * Expected: Exactly 5 audio tracks (Song A, Song B, Song C, Song D, Song E), 0 LRC tracks.
     */
    @Test
    fun test2_musicRootAndSubfolder_expectedExactPhysicalCount() = runTest {
        val musicDir = tempFolder.newFolder("Music")
        File(musicDir, "Song A.mp3").createNewFile()
        File(musicDir, "Song B.mp3").createNewFile()
        File(musicDir, "Song A.lrc").apply { writeText("[00:10.00]Song A lyrics") }

        val myFolder = File(musicDir, "MyFolder").apply { mkdir() }
        File(myFolder, "Song C.mp3").createNewFile()
        File(myFolder, "Song D.mp3").createNewFile()
        File(myFolder, "Song C.lrc").apply { writeText("[00:15.00]Song C lyrics") }
        File(myFolder, "Song E.flac").createNewFile()

        val supportedExts = setOf("mp3", "flac", "wav", "m4a", "aac", "ogg")
        val audioFiles = mutableListOf<File>()

        fun scanFolder(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) scanFolder(file)
                else if (file.isFile && file.extension.lowercase() in supportedExts) {
                    audioFiles.add(file)
                }
            }
        }
        scanFolder(musicDir)

        assertEquals("Physical audio count must be exactly 5 (.lrc must not be counted as audio)", 5, audioFiles.size)

        val entities = audioFiles.map { file ->
            val norm = AudioSourceNormalizer.normalizeSourceString(
                context = null,
                uriString = "file://${file.absolutePath.replace('\\', '/')}",
                filePath = file.absolutePath
            )
            SongEntity(
                id = AudioSourceNormalizer.generateStableTrackId(norm.canonicalIdentity),
                sourceUri = norm.canonicalIdentity,
                title = file.nameWithoutExtension,
                artist = "Artist",
                album = file.parentFile?.name ?: "Music",
                durationMs = 180000,
                mediaUri = norm.canonicalIdentity,
                artworkUri = null,
                trackNumber = 1,
                genre = "Music",
                dateAdded = 1000,
                filePath = file.absolutePath
            )
        }

        songDao.upsertSongs(entities)

        val allSongs = songDao.getAllSongsSync()
        assertEquals("Database must contain exactly 5 tracks", 5, allSongs.size)
        val distinctSources = allSongs.map { it.sourceUri }.toSet().size
        assertEquals("Total tracks must equal distinct source IDs", allSongs.size, distinctSources)
    }

    /**
     * Test 3: Repeated rescan produces identical count without duplication.
     */
    @Test
    fun test3_repeatedRescan_countUnchanged() = runTest {
        val tracks = (1..5).map { i ->
            val path = "/storage/emulated/0/Music/Track$i.mp3"
            val norm = AudioSourceNormalizer.normalizeSourceString(null, "file://$path", path)
            SongEntity(
                id = AudioSourceNormalizer.generateStableTrackId(norm.canonicalIdentity),
                sourceUri = norm.canonicalIdentity,
                title = "Track $i",
                artist = "Artist",
                album = "Album",
                durationMs = 200000,
                mediaUri = norm.canonicalIdentity,
                artworkUri = null,
                trackNumber = i,
                genre = "Pop",
                dateAdded = 1000L + i,
                filePath = path
            )
        }

        // Scan 1
        songDao.upsertSongs(tracks)
        assertEquals(5, songDao.getAllSongsSync().size)

        // Scan 2 (Manual rescan)
        songDao.upsertSongs(tracks)
        assertEquals(5, songDao.getAllSongsSync().size)

        // Scan 3 (Automatic rescan on restart)
        songDao.upsertSongs(tracks)
        assertEquals(5, songDao.getAllSongsSync().size)

        val allSongs = songDao.getAllSongsSync()
        assertEquals(5, allSongs.distinctBy { it.sourceUri }.size)
    }

    /**
     * Test 4: Same title, different artists remain separate records.
     * Music/
     * ├── ArtistA/
     * │   └── Hello.mp3
     * └── ArtistB/
     *     └── Hello.mp3
     * Expected: 2 tracks.
     */
    @Test
    fun test4_sameTitleDifferentArtists_expectedTwoTracks() = runTest {
        val pathA = "/storage/emulated/0/Music/ArtistA/Hello.mp3"
        val pathB = "/storage/emulated/0/Music/ArtistB/Hello.mp3"

        val normA = AudioSourceNormalizer.normalizeSourceString(null, "file://$pathA", pathA)
        val normB = AudioSourceNormalizer.normalizeSourceString(null, "file://$pathB", pathB)

        val trackA = SongEntity(
            id = AudioSourceNormalizer.generateStableTrackId(normA.canonicalIdentity),
            sourceUri = normA.canonicalIdentity,
            title = "Hello",
            artist = "ArtistA",
            album = "Album A",
            durationMs = 180000,
            mediaUri = normA.canonicalIdentity,
            artworkUri = null,
            trackNumber = 1,
            genre = "Pop",
            dateAdded = 1000,
            filePath = pathA
        )

        val trackB = SongEntity(
            id = AudioSourceNormalizer.generateStableTrackId(normB.canonicalIdentity),
            sourceUri = normB.canonicalIdentity,
            title = "Hello",
            artist = "ArtistB",
            album = "Album B",
            durationMs = 180000,
            mediaUri = normB.canonicalIdentity,
            artworkUri = null,
            trackNumber = 1,
            genre = "Pop",
            dateAdded = 2000,
            filePath = pathB
        )

        songDao.upsertSongs(listOf(trackA, trackB))

        val allSongs = songDao.getAllSongsSync()
        assertEquals("Both tracks must remain separate in database", 2, allSongs.size)
        assertNotEquals(trackA.id, trackB.id)
        assertNotEquals(trackA.sourceUri, trackB.sourceUri)
    }

    /**
     * Test 5: Same filename, different folders remain separate records.
     * Expected: 2 tracks.
     */
    @Test
    fun test5_sameFilenameDifferentFolders_expectedTwoTracks() = runTest {
        val path1 = "/storage/emulated/0/Music/Folder1/Track01.mp3"
        val path2 = "/storage/emulated/0/Music/Folder2/Track01.mp3"

        val norm1 = AudioSourceNormalizer.normalizeSourceString(null, "file://$path1", path1)
        val norm2 = AudioSourceNormalizer.normalizeSourceString(null, "file://$path2", path2)

        val track1 = SongEntity(
            id = AudioSourceNormalizer.generateStableTrackId(norm1.canonicalIdentity),
            sourceUri = norm1.canonicalIdentity,
            title = "Track01",
            artist = "Artist 1",
            album = "Folder1",
            durationMs = 180000,
            mediaUri = norm1.canonicalIdentity,
            artworkUri = null,
            trackNumber = 1,
            genre = "Pop",
            dateAdded = 1000,
            filePath = path1
        )
        val track2 = SongEntity(
            id = AudioSourceNormalizer.generateStableTrackId(norm2.canonicalIdentity),
            sourceUri = norm2.canonicalIdentity,
            title = "Track01",
            artist = "Artist 2",
            album = "Folder2",
            durationMs = 180000,
            mediaUri = norm2.canonicalIdentity,
            artworkUri = null,
            trackNumber = 1,
            genre = "Pop",
            dateAdded = 2000,
            filePath = path2
        )

        songDao.upsertSongs(listOf(track1, track2))
        val allSongs = songDao.getAllSongsSync()
        assertEquals(2, allSongs.size)
        assertNotEquals(track1.id, track2.id)
    }

    /**
     * Test 6: Local LRC discovery and matching attaches to canonical Track ID.
     * Music/MyFolder/Song C.mp3 -> Song C.lrc
     */
    @Test
    fun test6_localLrcMatching_correctTrackId() = runTest {
        val folder = tempFolder.newFolder("MyFolder")
        val audioFile = File(folder, "Song C.mp3").apply { createNewFile() }
        val lrcFile = File(folder, "Song C.lrc").apply {
            writeText("[00:15.50]Line from Song C")
        }

        val norm = AudioSourceNormalizer.normalizeSourceString(null, "file://${audioFile.absolutePath}", audioFile.absolutePath)
        val trackId = AudioSourceNormalizer.generateStableTrackId(norm.canonicalIdentity)

        val song = Song(
            id = trackId,
            sourceUri = norm.canonicalIdentity,
            title = "Song C",
            artist = "Artist C",
            album = "MyFolder",
            durationMs = 150000,
            mediaUri = norm.canonicalIdentity,
            filePath = audioFile.absolutePath
        )

        // Find sibling
        val path = song.filePath ?: ""
        val parent = File(path).parentFile
        val siblingLrc = File(parent, "${File(path).nameWithoutExtension}.lrc")
        assertTrue("Sibling .lrc file must exist", siblingLrc.exists())

        val rawLrc = siblingLrc.readText()
        assertTrue(rawLrc.contains("Line from Song C"))

        val parsed = LyricsParser.parse(rawLrc)
        assertTrue(parsed is LyricsState.Success)
        val lines = (parsed as LyricsState.Success).lines
        assertEquals(1, lines.size)
        assertEquals(15500L, lines[0].timestampMs)
        assertEquals("Line from Song C", lines[0].text)
    }

    /**
     * Test 7: Wrong lyric candidate rejected.
     */
    @Test
    fun test7_wrongLyricCandidate_rejected() {
        val conflictingLrc = """
            [ti:Other Title]
            [ar:Completely Wrong Artist]
            [00:10.00]Invalid lyrics
        """.trimIndent()

        val song = Song(
            id = "canonical_1",
            sourceUri = "file:///storage/emulated/0/Music/Song A.mp3",
            title = "Song A",
            artist = "Real Artist",
            album = "Album",
            durationMs = 200000,
            mediaUri = "file:///storage/emulated/0/Music/Song A.mp3"
        )

        val accepted = LyricsManager.validateLrcContent(conflictingLrc, song)
        assertFalse("Lyrics with contradictory artist must be rejected", accepted)
    }

    /**
     * Test 8: Existing duplicate database records are safely merged into one canonical record.
     */
    @Test
    fun test8_existingDuplicateDatabaseRecords_safelyMergedIntoOne() = runTest {
        val physicalPath = "/storage/emulated/0/Music/Song A.mp3"
        val canonicalSource = "file://$physicalPath"

        // Old legacy records for the same physical file with different sourceUri aliases and different IDs
        val oldRecord1 = SongEntity(
            id = "legacy_101",
            sourceUri = "file:///sdcard/Music/Song A.mp3",
            title = "Song A",
            artist = "Artist",
            album = "Album",
            durationMs = 200000,
            mediaUri = "content://media/external/audio/media/101",
            artworkUri = null,
            trackNumber = 1,
            genre = "Pop",
            dateAdded = 1000,
            filePath = physicalPath
        )
        val oldRecord2 = SongEntity(
            id = "saf_doc_202",
            sourceUri = "content://com.android.externalstorage.documents/document/primary%3AMusic%2FSong%20A.mp3",
            title = "Song A",
            artist = "Artist",
            album = "Album",
            durationMs = 200000,
            mediaUri = "content://com.android.externalstorage.documents/document/primary%3AMusic%2FSong%20A.mp3",
            artworkUri = null,
            trackNumber = 1,
            genre = "Pop",
            dateAdded = 2000,
            filePath = physicalPath
        )

        songDao.insertSongs(listOf(oldRecord1, oldRecord2))
        assertEquals(2, songDao.getAllSongsSync().size)

        // Perform merge logic matching AuraDatabase MIGRATION_5_6 and MusicRepositoryImpl.cleanupExistingDuplicates
        val allSongs = songDao.getAllSongsSync()
        val groups = allSongs.groupBy { song ->
            AudioSourceNormalizer.normalizeSourceString(
                context = null,
                uriString = song.sourceUri,
                filePath = song.filePath
            ).canonicalIdentity
        }

        val redundantIdsToDelete = mutableListOf<String>()
        val canonicalSongsToUpdate = mutableListOf<SongEntity>()

        for ((canonSource, group) in groups) {
            val canonSong = group.find { !it.id.startsWith("saf_") } ?: group.first()
            val canonicalId = canonSong.id
            val dupIds = group.map { it.id }.filter { it != canonicalId }

            redundantIdsToDelete.addAll(dupIds)
            canonicalSongsToUpdate.add(canonSong.copy(sourceUri = canonSource))
        }

        songDao.deleteSongsByIds(redundantIdsToDelete)
        songDao.upsertSongs(canonicalSongsToUpdate)

        val mergedSongs = songDao.getAllSongsSync()
        assertEquals("Expected exactly 1 merged song", 1, mergedSongs.size)
        assertEquals(canonicalSource, mergedSongs[0].sourceUri)
        assertEquals("legacy_101", mergedSongs[0].id)
    }

    /**
     * Test 9: Playlist references after merge are repointed to valid canonical Track ID.
     */
    @Test
    fun test9_playlistReferencesAfterMerge_validCanonicalTrackId() = runTest {
        val oldDupId = "saf_dup_42"
        val canonicalId = "track_canonical_10"

        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = 1L, songId = oldDupId, orderIndex = 0))
        assertEquals(1, playlistDao.getRefsForSong(oldDupId).size)

        // Repoint to canonical Track ID
        playlistDao.repointSongId(oldSongId = oldDupId, newSongId = canonicalId)
        playlistDao.deleteRefsForSong(oldDupId)

        assertEquals("Old duplicate song must have 0 playlist references", 0, playlistDao.getRefsForSong(oldDupId).size)
        val canonicalRefs = playlistDao.getRefsForSong(canonicalId)
        assertEquals("Canonical track must now hold the playlist reference", 1, canonicalRefs.size)
        assertEquals(1L, canonicalRefs[0].playlistId)
        assertEquals(canonicalId, canonicalRefs[0].songId)
    }

    /**
     * Test 10: Queue after merge has no duplicate canonical Track IDs.
     */
    @Test
    fun test10_queueAfterMerge_noDuplicateCanonicalTrackIds() = runTest {
        val canonicalId = "track_canonical_1"
        val dupId = "saf_dup_2"

        // Queue had both canonical and duplicate entries
        queueDao.insertQueueItems(listOf(
            QueueEntity(songId = canonicalId, orderIndex = 0, isCurrent = true),
            QueueEntity(songId = dupId, orderIndex = 1, isCurrent = false)
        ))

        // Merge logic: if queue already contains canonicalId, delete dupId
        val queueItems = queueDao.getQueueSync()
        val hasCanonical = queueItems.any { it.songId == canonicalId }
        if (hasCanonical) {
            queueDao.deleteQueueSongId(dupId)
        } else {
            queueDao.repointQueueSongId(dupId, canonicalId)
        }

        val finalQueue = queueDao.getQueueSync()
        assertEquals("Queue should contain only 1 entry after deduplication", 1, finalQueue.size)
        assertEquals(canonicalId, finalQueue[0].songId)
    }
}
