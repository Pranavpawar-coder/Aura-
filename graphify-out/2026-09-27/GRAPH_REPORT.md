# Graph Report - Aura  (2026-09-27)

## Corpus Check
- 102 files · ~226,783 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 16 file(s) not represented in the graph (top: .xml 8, (none) 3, .properties 2)

## Summary
- 1577 nodes · 4131 edges · 118 communities (63 shown, 55 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 57 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- audioEngine
- spatialaudiooff
- AudioSourceNormalizer
- SpatialPositionRadar.kt
- AudioMetadataHelper.kt
- LibraryScreen.kt
- Components.kt
- SongSortOrder
- QueueState
- AuraDatabase.kt
- SortCriterion
- SettingsScreen.kt
- MusicRepository
- AuraPlayerManager
- RecentlyPlayedDao
- PlayerViewModel
- CenterDisplayMode
- MusicRepositoryTest.kt
- MusicRepositoryImpl
- PlayerState
- AuraAudioService.kt
- Daos.kt
- .parse
- LyricsManager.kt
- Aura Cinematic Sound — Design System
- SongEntity
- PlaylistRepositoryImpl
- Song
- LibraryViewModel
- MusicRepositoryImpl.kt
- PlaylistDao
- arrowforward
- aurasecondarycontainer
- build_app.py
- ArtworkColorExtractor.kt
- dp
- QueueDao
- Spatial4DSettings
- Ponytail
- AuraScreen
- gradlew
- AuraNavigation.kt
- rules/graphify.md
- workflows/graphify.md
- repeatmode
- allinclusive
- history
- MusicFolderEntity
- spatialtracking
- nightlight
- AuraPlayerManager.kt
- PlayerViewModel.kt
- UserPreferences.kt
- Ponytail Help
- AuraAudioProcessor
- BiquadFilter
- VisualizerStyle
- SpatialMovementMode
- SpatialPreset
- NowPlayingScreen.kt
- AudioEffectsScreen
- AudioEffectsState
- MusicImportAndLyricsTest
- PlaybackStatus
- AuraBackupManager
- Entities.kt
- FilterType
- SmartPlaylistType
- SongSortTest
- Uri
- ponytail-audit/SKILL.md
- ReverbPreset
- SettingsCategory
- abs
- Ponytail Gain
- LibraryViewModel.kt
- ponytail-review/SKILL.md
- ParametricBand
- FakeFavoriteDao
- ponytail-debt/SKILL.md
- AuraAudioService
- PlaybackState
- StatisticsScreen
- .cleanupExistingDuplicates
- ReplayGainMode
- Uri
- QueueBottomSheet.kt
- SleepTimerTest
- .attachManualLyrics
- ponytail.md
- folderopen
- defaultrenderersfactory
- graphicslayer

## God Nodes (most connected - your core abstractions)
1. `Song` - 115 edges
2. `PlayerViewModel` - 80 edges
3. `AuraPlayerManager` - 75 edges
4. `MusicRepositoryImpl` - 52 edges
5. `UserPreferences` - 48 edges
6. `MusicRepository` - 48 edges
7. `LibraryViewModel` - 44 edges
8. `audioEngine` - 35 edges
9. `AudioEffectsState` - 34 edges
10. `SongEntity` - 31 edges

## Surprising Connections (you probably didn't know these)
- `AuraPlayerManager` --calls--> `AudioOutputManager`  [INFERRED]
  android/app/src/main/java/com/example/aura/data/audio/AuraPlayerManager.kt → android/app/src/main/java/com/example/aura/data/audio/AudioOutputManager.kt
- `AuraPlayerManager` --calls--> `AuraSleepTimer`  [INFERRED]
  android/app/src/main/java/com/example/aura/data/audio/AuraPlayerManager.kt → android/app/src/main/java/com/example/aura/data/audio/AuraSleepTimer.kt
- `AuraAudioProcessor` --calls--> `BiquadFilter`  [INFERRED]
  android/app/src/main/java/com/example/aura/data/audio/dsp/AuraAudioProcessor.kt → android/app/src/main/java/com/example/aura/data/audio/dsp/BiquadFilter.kt
- `AuraAudioProcessor` --calls--> `DynamicCompressor`  [INFERRED]
  android/app/src/main/java/com/example/aura/data/audio/dsp/AuraAudioProcessor.kt → android/app/src/main/java/com/example/aura/data/audio/dsp/DynamicCompressor.kt
- `AuraAudioProcessor` --calls--> `PeakLimiter`  [INFERRED]
  android/app/src/main/java/com/example/aura/data/audio/dsp/AuraAudioProcessor.kt → android/app/src/main/java/com/example/aura/data/audio/dsp/PeakLimiter.kt

## Import Cycles
- None detected.

## Communities (118 total, 55 thin omitted)

### Community 0 - "audioEngine"
Cohesion: 0.05
Nodes (6): audioEngine, SAMPLE_TRACKS, Queue, Track, AudioSync, NavigationController

### Community 2 - "AudioSourceNormalizer"
Cohesion: 0.17
Nodes (10): AudioSourceNormalizer, Context, Uri, NormalizedAudioSource, contenturis, documentscontract, environment, mediastore (+2 more)

### Community 3 - "SpatialPositionRadar.kt"
Cohesion: 0.19
Nodes (17): aspectratio, aurasurfacecontainer, background, border, box, brush, canvas, composable (+9 more)

### Community 4 - "AudioMetadataHelper.kt"
Cohesion: 0.15
Nodes (9): AudioMetadataHelper, AudioTechnicalDetails, Context, Uri, AudioFormatSupportTest, file, mediaextractor, mediaformat (+1 more)

### Community 5 - "LibraryScreen.kt"
Cohesion: 0.11
Nodes (46): add, Album, Artist, FolderGroup, Genre, Playlist, AnimatedEqualizerBars(), PlaylistSelectionDialog() (+38 more)

### Community 6 - "Components.kt"
Cohesion: 0.06
Nodes (45): alpha, RepeatMode, ALL, OFF, ONE, auraPressable(), auraShimmer(), Color (+37 more)

### Community 7 - "SongSortOrder"
Cohesion: 0.11
Nodes (16): SongSortOrder, ALBUM_AZ, ALBUM_ZA, ARTIST_AZ, ARTIST_ZA, DATE_ADDED, DATE_ADDED_OLD, DURATION_ASC (+8 more)

### Community 8 - "QueueState"
Cohesion: 0.16
Nodes (3): QueueState, QueueBottomSheet(), QueueStateTest

### Community 9 - "AuraDatabase.kt"
Cohesion: 0.07
Nodes (11): com, ListeningHistoryDao, LyricsCacheDao, SongRatingDao, AuraDatabase, Context, database, migration (+3 more)

### Community 10 - "SortCriterion"
Cohesion: 0.12
Nodes (16): SortCriterion, ALBUM, ALBUM_ARTIST, ARTIST, DATE_ADDED, DURATION, FILE_SIZE, GENRE (+8 more)

### Community 11 - "SettingsScreen.kt"
Cohesion: 0.05
Nodes (65): alignment, SleepTimerOptionRow(), ContextMenuItem(), androidx, ImageVector, Color, Dp, Modifier (+57 more)

### Community 12 - "MusicRepository"
Cohesion: 0.07
Nodes (5): SearchResults, com, Flow, Uri, MusicRepository

### Community 13 - "AuraPlayerManager"
Cohesion: 0.07
Nodes (7): AuraPlayerManager, Listener, androidx, FloatArray, Listener, MediaItem, Player

### Community 14 - "RecentlyPlayedDao"
Cohesion: 0.14
Nodes (4): RecentlyPlayedDao, RecentlyPlayedEntity, FakeRecentlyPlayedDao, MusicRepositoryTest

### Community 16 - "CenterDisplayMode"
Cohesion: 0.21
Nodes (8): CenterDisplayMode, ARTWORK, LYRICS, VISUALIZER, GestureAxis, HORIZONTAL, UNDECIDED, VERTICAL

### Community 17 - "MusicRepositoryTest.kt"
Cohesion: 0.17
Nodes (14): assertequals, assertfalse, assertnotequals, assertnotnull, assertnull, asserttrue, before, junit4 (+6 more)

### Community 20 - "AuraAudioService.kt"
Cohesion: 0.12
Nodes (16): DefaultRenderersFactory, Context, audioattributes, AudioSink, commandbutton, defaultaudiosink, defaultextractorsfactory, defaultmedianotificationprovider (+8 more)

### Community 21 - "Daos.kt"
Cohesion: 0.14
Nodes (8): FavoriteDao, FavoriteEntity, dao, insert, onconflictstrategy, query, transaction, upsert

### Community 22 - ".parse"
Cohesion: 0.17
Nodes (6): LrcLibLyricsProvider, LyricLine, LyricsParser, Success, LyricsSyncTest, LyricsTest

### Community 23 - "LyricsManager.kt"
Cohesion: 0.22
Nodes (4): LyricsCacheEntity, Uri, LyricsManager, documentfile

### Community 24 - "Aura Cinematic Sound — Design System"
Cohesion: 0.17
Nodes (11): 1. Brand & Style, 2. Color Palette & Surface Tokens, 3. Typography (Plus Jakarta Sans), 4. Spacing & Border Radii, 5. Screen Manifest, Accents & Spectrum, Aura Cinematic Sound — Design System, Border Radii (+3 more)

### Community 25 - "SongEntity"
Cohesion: 0.10
Nodes (5): Flow, SongDao, SongEntity, FakeSongDao, Flow

### Community 26 - "PlaylistRepositoryImpl"
Cohesion: 0.11
Nodes (4): Flow, PlaylistRepositoryImpl, Flow, PlaylistRepository

### Community 27 - "Song"
Cohesion: 0.12
Nodes (3): Song, EntityMappingTest, Phase4FeaturesTest

### Community 29 - "MusicRepositoryImpl.kt"
Cohesion: 0.13
Nodes (17): MetadataEditor, StatisticsEngine, ListeningStatistics, bufferedreader, combine, context, dispatchers, fileoutputstream (+9 more)

### Community 30 - "PlaylistDao"
Cohesion: 0.14
Nodes (3): PlaylistDao, PlaylistEntity, PlaylistSongCrossRef

### Community 33 - "build_app.py"
Cohesion: 0.25
Nodes (4): json, os, re, urllib_request

### Community 34 - "ArtworkColorExtractor.kt"
Cohesion: 0.20
Nodes (8): Context, bitmap, bitmapfactory, colorutils, concurrenthashmap, inputstream, palette, toargb

### Community 35 - "dp"
Cohesion: 0.10
Nodes (25): MainActivity, Factory, Factory, AuraColorScheme, auraDarkColorScheme(), auraLightColorScheme(), AuraTheme(), createAuraDarkColors() (+17 more)

### Community 37 - "Spatial4DSettings"
Cohesion: 0.15
Nodes (5): Spatial4DEngine, BassTrebleSettings, ParametricSettings, Spatial4DSettings, AudioEffectsStateTest

### Community 38 - "Ponytail"
Cohesion: 0.22
Nodes (8): Boundaries, Intensity, Output, Persistence, Ponytail, Rules, The ladder, When NOT to be lazy

### Community 39 - "AuraScreen"
Cohesion: 0.29
Nodes (7): AuraScreen, Favorites, Home, Library, Playlists, Search, Settings

### Community 40 - "gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 41 - "AuraNavigation.kt"
Cohesion: 0.07
Nodes (35): activityresultcontracts, AuraApp(), com, Modifier, animatedcontent, animatedvisibility, aurasurface, coil (+27 more)

### Community 53 - "AuraPlayerManager.kt"
Cohesion: 0.11
Nodes (19): AuraPlayerSingleton, Context, Job, ListenableFuture, StateFlow, AuraSleepTimer, Job, StateFlow (+11 more)

### Community 54 - "PlayerViewModel.kt"
Cohesion: 0.11
Nodes (14): AudioProfile, BalanceSettings, CompressorSettings, LimiterSettings, LoudnessSettings, PlaybackSettings, ReplayGainSettings, ReverbSettings (+6 more)

### Community 55 - "UserPreferences.kt"
Cohesion: 0.08
Nodes (33): Flow, UserPreferences, EqualizerBand, EqualizerSettings, AnimationSettings, AppearanceSettings, BluetoothGestureSettings, GesturesSettings (+25 more)

### Community 56 - "Ponytail Help"
Cohesion: 0.25
Nodes (7): Configure Default Mode, Deactivate, Levels, More, Ponytail Help, Skills, Update

### Community 57 - "AuraAudioProcessor"
Cohesion: 0.18
Nodes (9): AuraAudioProcessor, FloatArray, AudioProcessor, BaseAudioProcessor, ByteBuffer, byteorder, c, optin (+1 more)

### Community 58 - "BiquadFilter"
Cohesion: 0.18
Nodes (3): BiquadFilter, DynamicCompressor, DspEngineTest

### Community 59 - "VisualizerStyle"
Cohesion: 0.17
Nodes (12): VisualizerStyle, BARS, CIRCULAR, MINIMAL, SPECTRUM, WAVEFORM, FloatArray, VisualizerTab() (+4 more)

### Community 60 - "SpatialMovementMode"
Cohesion: 0.16
Nodes (13): SpatialMovementMode, CUSTOM, FAST_ORBIT, MEDIUM_ORBIT, SLOW_ORBIT, STATIC, atan2, cos (+5 more)

### Community 61 - "SpatialPreset"
Cohesion: 0.18
Nodes (10): SpatialPreset, CINEMA, CONCERT, FOUR_D, FOUR_D_FAST, FOUR_D_SLOW, NORMAL, STUDIO (+2 more)

### Community 62 - "NowPlayingScreen.kt"
Cohesion: 0.08
Nodes (45): SleepTimerSettings, Loading, LyricsState, Unavailable, ArtworkColorExtractor, ArtworkPalette, AuraMotion, AuraAnimatedFavoriteButton() (+37 more)

### Community 63 - "AudioEffectsScreen"
Cohesion: 0.23
Nodes (12): AudioEffectsScreen(), DynamicsTab(), EffectSectionCard(), EqAndToneTab(), com, Modifier, OutputProfileTab(), ReverbAndPlaybackTab() (+4 more)

### Community 64 - "AudioEffectsState"
Cohesion: 0.05
Nodes (40): AudioOutputManager, AudioDeviceCallback, Quad, AuraHardwareEffectsManager, OnDataCaptureListener, AudioDeviceType, BLUETOOTH_HEADPHONES, BLUETOOTH_SPEAKER (+32 more)

### Community 66 - "PlaybackStatus"
Cohesion: 0.29
Nodes (7): PlaybackStatus, BUFFERING, ENDED, ERROR, IDLE, PAUSED, PLAYING

### Community 68 - "Entities.kt"
Cohesion: 0.29
Nodes (5): ListeningHistoryEntity, entity, foreignkey, index, primarykey

### Community 69 - "FilterType"
Cohesion: 0.29
Nodes (6): FilterType, HIGH_PASS, HIGH_SHELF, LOW_PASS, LOW_SHELF, PEAKING

### Community 70 - "SmartPlaylistType"
Cohesion: 0.20
Nodes (9): SmartPlaylist, SmartPlaylistType, FAVORITES, HIGH_QUALITY, LONG_TRACKS, MOST_PLAYED, NEVER_PLAYED, RECENTLY_ADDED (+1 more)

### Community 73 - "ponytail-audit/SKILL.md"
Cohesion: 0.40
Nodes (4): Boundaries, Hunt, Output, Tags

### Community 74 - "ReverbPreset"
Cohesion: 0.22
Nodes (9): ReverbPreset, CONCERT, HALL, LARGE_HALL, LARGE_ROOM, OFF, ROOM, SMALL_ROOM (+1 more)

### Community 75 - "SettingsCategory"
Cohesion: 0.12
Nodes (16): SettingsCategory, ABOUT, ACCESSIBILITY, ADVANCED, APPEARANCE, AUDIO, BACKUP, BLUETOOTH (+8 more)

### Community 76 - "abs"
Cohesion: 0.19
Nodes (7): abs, PeakLimiter, exp, log10, max, pow, tanh

### Community 77 - "Ponytail Gain"
Cohesion: 0.40
Nodes (4): Boundaries, Honesty boundary, Ponytail Gain, Scoreboard

### Community 78 - "LibraryViewModel.kt"
Cohesion: 0.29
Nodes (6): StateFlow, ViewModel, flatmaplatest, flowof, sharingstarted, statein

### Community 79 - "ponytail-review/SKILL.md"
Cohesion: 0.40
Nodes (4): Boundaries, Examples, Format, Scoring

### Community 80 - "ParametricBand"
Cohesion: 0.29
Nodes (4): ParametricBand, Color, Modifier, ParametricEqCurve()

### Community 83 - "ponytail-debt/SKILL.md"
Cohesion: 0.50
Nodes (3): Boundaries, Output, Scan

### Community 84 - "AuraAudioService"
Cohesion: 0.15
Nodes (9): AuraAudioService, Callback, Listener, ListenableFuture, Listener, ExoPlayer, Intent, MediaSession (+1 more)

### Community 86 - "StatisticsScreen"
Cohesion: 0.60
Nodes (5): ImageVector, Modifier, StatisticsScreen(), StatMetricCard(), TopRankItem()

### Community 88 - "ReplayGainMode"
Cohesion: 0.50
Nodes (4): ReplayGainMode, ALBUM, OFF, TRACK

### Community 90 - "QueueBottomSheet.kt"
Cohesion: 0.07
Nodes (54): album, alertdialog, MetadataEditorDialog(), InfoRow(), InfoSectionHeader(), TrackInfoDialog(), arrowback, auraonsurface (+46 more)

## Knowledge Gaps
- **167 isolated node(s):** `Loading`, `Unavailable`, `RECENTLY_ADDED`, `RECENTLY_PLAYED`, `MOST_PLAYED` (+162 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 491 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **55 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Song` connect `Song` to `AudioMetadataHelper.kt`, `LibraryScreen.kt`, `QueueState`, `SettingsScreen.kt`, `MusicRepository`, `AuraPlayerManager`, `PlayerViewModel`, `MusicRepositoryTest.kt`, `MusicRepositoryImpl`, `PlayerState`, `.parse`, `LyricsManager.kt`, `SongEntity`, `PlaylistRepositoryImpl`, `LibraryViewModel`, `MusicRepositoryImpl.kt`, `QueueDao`, `AuraPlayerManager.kt`, `PlayerViewModel.kt`, `NowPlayingScreen.kt`, `AudioEffectsState`, `MusicImportAndLyricsTest`, `Entities.kt`, `SongSortTest`, `LibraryViewModel.kt`, `.clearLyricsCache`, `PlaybackState`, `QueueBottomSheet.kt`, `.setRating`, `.toggleFavorite`, `.addSongToPlaylist`, `.addToQueue`?**
  _High betweenness centrality (0.172) - this node is a cross-community bridge._
- **Why does `PlayerViewModel` connect `PlayerViewModel` to `QueueState`, `SettingsScreen.kt`, `PlayerState`, `dp`, `Spatial4DSettings`, `AuraNavigation.kt`, `PlayerViewModel.kt`, `VisualizerStyle`, `SpatialPreset`, `NowPlayingScreen.kt`, `AudioEffectsScreen`, `AudioEffectsState`, `ParametricBand`, `.clearLyricsCache`, `PlaybackState`, `.setEqualizerEnabled`, `.setRating`, `.toggleFavorite`, `.attachManualLyrics`, `.addToQueue`, `.clearQueue`, `.cycleRepeat`, `.onCleared`, `.pause`, `.previous`, `.removeFromQueue`, `.removeParametricBand`, `.resume`, `.setBalance`, `.setEqualizerBandGain`, `.setSpatial4DPreview`, `.togglePlayPause`, `.toggleShuffle`?**
  _High betweenness centrality (0.070) - this node is a cross-community bridge._
- **Why does `AudioEffectsState` connect `AudioEffectsState` to `Spatial4DSettings`, `SettingsScreen.kt`, `AuraPlayerManager`, `PlayerViewModel`, `AuraAudioService`, `AuraPlayerManager.kt`, `PlayerViewModel.kt`, `UserPreferences.kt`, `AuraAudioProcessor`, `VisualizerStyle`, `NowPlayingScreen.kt`, `AudioEffectsScreen`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `Song` (e.g. with `.testSongFormattedDuration()` and `.testMinimizeTransitionState()`) actually correct?**
  _`Song` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `AuraPlayerManager` (e.g. with `AudioOutputManager` and `AuraSleepTimer`) actually correct?**
  _`AuraPlayerManager` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Loading`, `Unavailable`, `RECENTLY_ADDED` to the rest of the system?**
  _167 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `audioEngine` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._