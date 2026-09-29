# Graph Report - Aura  (2026-09-26)

## Corpus Check
- 60 files · ~127,958 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 9, (none) 3, .properties 2)

## Summary
- 884 nodes · 2239 edges · 53 communities (31 shown, 22 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 31 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Queue
- spatialaudiooff
- PlaybackState
- audioEngine
- LyricsState
- AuraAudioService
- NowPlayingScreen.kt
- SongSortOrder
- QueueState
- SongSortTest
- PlaylistRepositoryImpl.kt
- Callback
- Song
- AuraPlayerManager
- MusicRepositoryTest.kt
- PlayerViewModel
- GestureAxis
- assertequals
- MusicRepositoryImpl
- MusicRepositoryImpl.kt
- AuraAudioService.kt
- audiomanager
- RecentlyPlayedDao
- LibraryViewModel.kt
- Aura Cinematic Sound — Design System
- Daos.kt
- PlaylistRepository
- AuraDatabase.kt
- LibraryViewModel
- MusicFolderEntity
- PlaylistDao
- arrowforward
- aurasecondarycontainer
- build_app.py
- Playlist
- MainActivity.kt
- Entities.kt
- Type.kt
- Theme.kt
- AuraScreen
- gradlew
- .onCreate
- rules/graphify.md
- workflows/graphify.md
- repeatmode
- allinclusive
- history
- mic
- spatialtracking
- nightlight

## God Nodes (most connected - your core abstractions)
1. `Song` - 90 edges
2. `AuraPlayerManager` - 40 edges
3. `LibraryViewModel` - 36 edges
4. `audioEngine` - 35 edges
5. `MusicRepository` - 34 edges
6. `PlayerViewModel` - 34 edges
7. `MusicRepositoryImpl` - 33 edges
8. `QueueState` - 26 edges
9. `SongSortOrder` - 25 edges
10. `Playlist` - 23 edges

## Surprising Connections (you probably didn't know these)
- `QueueStateTest` --calls--> `Song`  [INFERRED]
  android/app/src/test/java/com/example/aura/domain/model/QueueStateTest.kt → android/app/src/main/java/com/example/aura/domain/model/Song.kt
- `SongSortTest` --calls--> `Song`  [INFERRED]
  android/app/src/test/java/com/example/aura/domain/model/SongSortTest.kt → android/app/src/main/java/com/example/aura/domain/model/Song.kt
- `SongContextMenuBottomSheet()` --calls--> `TrackInfoDialog()`  [INFERRED]
  android/app/src/main/java/com/example/aura/ui/components/SongContextMenu.kt → android/app/src/main/java/com/example/aura/ui/components/TrackInfoDialog.kt
- `AuraPlayerManager` --references--> `QueueState`  [EXTRACTED]
  android/app/src/main/java/com/example/aura/data/audio/AuraPlayerManager.kt → android/app/src/main/java/com/example/aura/domain/model/QueueState.kt
- `AuraPlayerManager` --references--> `PlaybackState`  [EXTRACTED]
  android/app/src/main/java/com/example/aura/data/audio/AuraPlayerManager.kt → android/app/src/main/java/com/example/aura/domain/model/Song.kt

## Import Cycles
- None detected.

## Communities (53 total, 22 thin omitted)

### Community 0 - "Queue"
Cohesion: 0.07
Nodes (5): SAMPLE_TRACKS, Queue, Track, AudioSync, NavigationController

### Community 2 - "PlaybackState"
Cohesion: 0.12
Nodes (17): PlaybackState, AuraHeader(), AuraScrubber(), Color, Modifier, MiniPlayer(), HomeScreen(), HorizontalSongCarousel() (+9 more)

### Community 4 - "LyricsState"
Cohesion: 0.09
Nodes (17): AudioMetadataHelper, AudioTechnicalDetails, Context, Uri, Loading, LyricLine, LyricsParser, LyricsState (+9 more)

### Community 5 - "AuraAudioService"
Cohesion: 0.38
Nodes (4): AuraAudioService, ExoPlayer, MediaSession, MediaSessionService

### Community 6 - "NowPlayingScreen.kt"
Cohesion: 0.05
Nodes (156): abs, activityresultcontracts, add, album, alignment, AnimatedEqualizerBars(), ContextMenuItem(), androidx (+148 more)

### Community 7 - "SongSortOrder"
Cohesion: 0.05
Nodes (41): Flow, UserPreferences, SongSortOrder, ALBUM_AZ, ALBUM_ZA, ARTIST_AZ, ARTIST_ZA, DATE_ADDED (+33 more)

### Community 8 - "QueueState"
Cohesion: 0.06
Nodes (28): QueueState, RepeatMode, ALL, OFF, ONE, ArtworkColorExtractor, ArtworkPalette, Context (+20 more)

### Community 10 - "PlaylistRepositoryImpl.kt"
Cohesion: 0.16
Nodes (5): PlaylistSongCrossRef, Flow, PlaylistRepositoryImpl, dispatchers, withcontext

### Community 11 - "Callback"
Cohesion: 0.33
Nodes (3): Callback, ListenableFuture, Intent

### Community 12 - "Song"
Cohesion: 0.16
Nodes (3): Song, Flow, MusicRepository

### Community 13 - "AuraPlayerManager"
Cohesion: 0.06
Nodes (25): AuraPlayerManager, Listener, AuraPlayerSingleton, androidx, Context, ListenableFuture, StateFlow, PlaybackStatus (+17 more)

### Community 14 - "MusicRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): FakeFavoriteDao, FakeRecentlyPlayedDao, FakeSongDao, Flow, MusicRepositoryTest, before, first, runtest

### Community 15 - "PlayerViewModel"
Cohesion: 0.07
Nodes (3): PlayerState, PlayerViewModel, PlayerStateTest

### Community 16 - "GestureAxis"
Cohesion: 0.50
Nodes (4): GestureAxis, HORIZONTAL, UNDECIDED, VERTICAL

### Community 17 - "assertequals"
Cohesion: 0.18
Nodes (10): EntityMappingTest, assertequals, assertfalse, assertnotnull, assertnull, asserttrue, junit4, runwith (+2 more)

### Community 18 - "MusicRepositoryImpl"
Cohesion: 0.19
Nodes (3): Flow, Uri, MusicRepositoryImpl

### Community 19 - "MusicRepositoryImpl.kt"
Cohesion: 0.13
Nodes (9): SearchResults, Artist, FolderGroup, Genre, Uri, contenturis, documentfile, fileoutputstream (+1 more)

### Community 20 - "AuraAudioService.kt"
Cohesion: 0.12
Nodes (16): audioattributes, c, commandbutton, defaultextractorsfactory, defaultmedianotificationprovider, defaultmediasourcefactory, defaultrenderersfactory, futures (+8 more)

### Community 23 - "LibraryViewModel.kt"
Cohesion: 0.16
Nodes (14): StateFlow, Uri, ViewModel, StateFlow, ViewModel, asstateflow, combine, flatmaplatest (+6 more)

### Community 24 - "Aura Cinematic Sound — Design System"
Cohesion: 0.17
Nodes (11): 1. Brand & Style, 2. Color Palette & Surface Tokens, 3. Typography (Plus Jakarta Sans), 4. Spacing & Border Radii, 5. Screen Manifest, Accents & Spectrum, Aura Cinematic Sound — Design System, Border Radii (+3 more)

### Community 25 - "Daos.kt"
Cohesion: 0.16
Nodes (8): Flow, SongDao, SongEntity, dao, insert, onconflictstrategy, query, transaction

### Community 27 - "AuraDatabase.kt"
Cohesion: 0.13
Nodes (7): FavoriteDao, AuraDatabase, Context, FavoriteEntity, database, room, RoomDatabase

### Community 33 - "build_app.py"
Cohesion: 0.25
Nodes (4): json, os, re, urllib_request

### Community 34 - "Playlist"
Cohesion: 0.17
Nodes (19): Album, Playlist, PlaylistSelectionDialog(), SongContextMenuBottomSheet(), AlbumDetailScreen(), ArtistDetailScreen(), DetailSongRow(), FolderDetailScreen() (+11 more)

### Community 35 - "MainActivity.kt"
Cohesion: 0.20
Nodes (10): MainActivity, coil, ComponentActivity, diskcache, enableedgetoedge, imageloader, memorycache, setcontent (+2 more)

### Community 36 - "Entities.kt"
Cohesion: 0.20
Nodes (6): QueueDao, QueueEntity, entity, foreignkey, index, primarykey

### Community 37 - "Type.kt"
Cohesion: 0.50
Nodes (3): fontfamily, textstyle, typography

### Community 38 - "Theme.kt"
Cohesion: 0.50
Nodes (3): AuraTheme(), darkcolorscheme, materialtheme

### Community 39 - "AuraScreen"
Cohesion: 0.29
Nodes (7): AuraScreen, Favorites, Home, Library, Playlists, Search, Settings

### Community 40 - "gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 41 - ".onCreate"
Cohesion: 0.32
Nodes (5): Factory, Factory, Bundle, Factory, T

## Knowledge Gaps
- **59 isolated node(s):** `Loading`, `Unavailable`, `IDLE`, `BUFFERING`, `PLAYING` (+54 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 266 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Song` connect `Song` to `Playlist`, `PlaybackState`, `Entities.kt`, `LyricsState`, `NowPlayingScreen.kt`, `QueueState`, `SongSortTest`, `PlaylistRepositoryImpl.kt`, `AuraPlayerManager`, `PlayerViewModel`, `assertequals`, `MusicRepositoryImpl`, `MusicRepositoryImpl.kt`, `LibraryViewModel.kt`, `Daos.kt`, `PlaylistRepository`, `LibraryViewModel`?**
  _High betweenness centrality (0.241) - this node is a cross-community bridge._
- **Why does `SongSortOrder` connect `SongSortOrder` to `Playlist`, `LibraryViewModel`, `NowPlayingScreen.kt`, `LibraryViewModel.kt`?**
  _High betweenness centrality (0.070) - this node is a cross-community bridge._
- **Why does `LibraryViewModel` connect `LibraryViewModel` to `PlaybackState`, `MainActivity.kt`, `Playlist`, `NowPlayingScreen.kt`, `SongSortOrder`, `.onCreate`, `Song`, `MusicRepositoryImpl.kt`, `LibraryViewModel.kt`, `MusicFolderEntity`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `Song` (e.g. with `.testSongFormattedDuration()` and `.testMinimizeTransitionState()`) actually correct?**
  _`Song` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Loading`, `Unavailable`, `IDLE` to the rest of the system?**
  _59 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Queue` be split into smaller, more focused modules?**
  _Cohesion score 0.07092198581560284 - nodes in this community are weakly interconnected._
- **Should `PlaybackState` be split into smaller, more focused modules?**
  _Cohesion score 0.11578947368421053 - nodes in this community are weakly interconnected._