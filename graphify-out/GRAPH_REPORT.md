# Graph Report - Aura  (2026-09-30)

## Corpus Check
- 110 files · ~250,627 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 48 file(s) not represented in the graph (top: .xml 38, (none) 3, .properties 2)

## Summary
- 1830 nodes · 4885 edges · 119 communities (72 shown, 47 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 71 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- audioEngine
- spatialaudiooff
- documentscontract
- SettingsScreen.kt
- LibraryScreen
- LibraryScreen.kt
- NowPlayingScreen.kt
- SongSortOrder
- QueueState
- AuraDatabase.kt
- EqualizerPreset
- AudioEffectsScreen.kt
- MusicRepository
- AuraPlayerManager
- FakeRecentlyPlayedDao
- PlayerViewModel
- CenterDisplayMode
- MusicImportAndLyricsTest.kt
- MusicRepositoryImpl
- SortCriterion
- AuraAudioService.kt
- AuraPlayerManager.kt
- AudioMetadataHelper.kt
- .normalizeSourceString
- Aura Cinematic Sound — Design System
- SongEntity
- PlaylistRepository
- SmartScanSettings
- LibraryViewModel
- MusicRepositoryImpl.kt
- Song
- arrowforward
- aurasecondarycontainer
- build_app.py
- AuraWidgetProvider.kt
- Theme.kt
- QueueDao
- EqualizerSettings
- Ponytail
- AuraScreen
- gradlew
- .updateAllWidgets
- rules/graphify.md
- workflows/graphify.md
- app/build.gradle.kts
- repeatmode
- allinclusive
- history
- MusicFolderEntity
- spatialtracking
- nightlight
- PlayerViewModel.kt
- AudioEffectsState.kt
- UserPreferences.kt
- Ponytail Help
- AuraAudioProcessor
- BiquadFilter
- AuraVisualizerView.kt
- DynamicCompressor
- UserPreferences
- NowPlayingScreen
- AudioEffectsScreen
- AudioEffectsState
- LyricsManager
- FilterType
- PlaylistDao
- SpatialPreset
- LyricsState
- SmartPlaylistType
- SongSortTest
- PlaybackState
- ponytail-audit/SKILL.md
- ReverbPreset
- SettingsCategory
- Daos.kt
- Ponytail Gain
- PlaybackStatus
- ponytail-review/SKILL.md
- GestureAxis
- Spatial4DSettings
- PlayerState
- ponytail-debt/SKILL.md
- Listener
- SearchScreen.kt
- AudioDeviceType
- MainActivity.kt
- SleepTimerSettings
- SongContextMenu.kt
- ExcludedItemsScreen.kt
- .cleanupExistingDuplicates
- .importDiscoveredTracks
- Entities.kt
- ArtworkColorExtractor.kt
- PlaylistRepositoryImpl
- ponytail.md
- folderopen
- .onCreate
- SpatialMovementMode
- OnDataCaptureListener
- defaultrenderersfactory
- graphicslayer
- MainActivity
- LoudnessSettings
- ReplayGainSettings
- ReverbSettings
- .attachManualLyrics
- view
- localdensity

## God Nodes (most connected - your core abstractions)
1. `Song` - 132 edges
2. `PlayerViewModel` - 82 edges
3. `AuraPlayerManager` - 81 edges
4. `MusicRepositoryImpl` - 72 edges
5. `UserPreferences` - 69 edges
6. `MusicRepository` - 65 edges
7. `LibraryViewModel` - 59 edges
8. `AudioEffectsState` - 35 edges
9. `audioEngine` - 35 edges
10. `SongEntity` - 34 edges

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

## Communities (119 total, 47 thin omitted)

### Community 0 - "audioEngine"
Cohesion: 0.05
Nodes (6): audioEngine, SAMPLE_TRACKS, Queue, Track, AudioSync, NavigationController

### Community 3 - "SettingsScreen.kt"
Cohesion: 0.08
Nodes (30): activityresultcontracts, animatedvisibility, barchart, chevronright, clear, collectasstate, directionscar, download (+22 more)

### Community 4 - "LibraryScreen"
Cohesion: 0.14
Nodes (9): SearchResults, Album, Artist, FolderGroup, Genre, ArtistDetailScreen(), LazyListState, Modifier (+1 more)

### Community 5 - "LibraryScreen.kt"
Cohesion: 0.12
Nodes (34): add, Playlist, AuraFallbackArtwork(), AnimatedEqualizerBars(), PlaylistSelectionDialog(), SongContextMenuBottomSheet(), HomeScreen(), HorizontalSongCarousel() (+26 more)

### Community 6 - "NowPlayingScreen.kt"
Cohesion: 0.06
Nodes (66): alpha, auraPressable(), auraShimmer(), Color, Modifier, animatable, animatecolorasstate, animatedcontent (+58 more)

### Community 7 - "SongSortOrder"
Cohesion: 0.11
Nodes (16): SongSortOrder, ALBUM_AZ, ALBUM_ZA, ARTIST_AZ, ARTIST_ZA, DATE_ADDED, DATE_ADDED_OLD, DURATION_ASC (+8 more)

### Community 9 - "AuraDatabase.kt"
Cohesion: 0.07
Nodes (11): com, ListeningHistoryDao, LyricsCacheDao, SongRatingDao, AuraDatabase, Context, database, migration (+3 more)

### Community 10 - "EqualizerPreset"
Cohesion: 0.12
Nodes (15): EqualizerPreset, ACOUSTIC, BASS, BASS_BOOST, CLASSICAL, CUSTOM, DANCE, DEEP (+7 more)

### Community 11 - "AudioEffectsScreen.kt"
Cohesion: 0.10
Nodes (31): Color, Modifier, SpatialPositionRadar(), auraoutline, aurasurfacecontainer, background, border, box (+23 more)

### Community 12 - "MusicRepository"
Cohesion: 0.05
Nodes (3): com, Uri, MusicRepository

### Community 13 - "AuraPlayerManager"
Cohesion: 0.06
Nodes (8): AuraPlayerManager, Listener, androidx, FloatArray, Listener, ParametricBand, MediaItem, Player

### Community 14 - "FakeRecentlyPlayedDao"
Cohesion: 0.15
Nodes (4): FakeFavoriteDao, FakeRecentlyPlayedDao, Flow, MusicRepositoryTest

### Community 16 - "CenterDisplayMode"
Cohesion: 0.50
Nodes (4): CenterDisplayMode, ARTWORK, LYRICS, VISUALIZER

### Community 17 - "MusicImportAndLyricsTest.kt"
Cohesion: 0.17
Nodes (14): assertequals, assertfalse, assertnotequals, assertnotnull, assertnull, asserttrue, before, junit4 (+6 more)

### Community 19 - "SortCriterion"
Cohesion: 0.12
Nodes (16): SortCriterion, ALBUM, ALBUM_ARTIST, ARTIST, DATE_ADDED, DURATION, FILE_SIZE, GENRE (+8 more)

### Community 20 - "AuraAudioService.kt"
Cohesion: 0.09
Nodes (23): AuraAudioService, Callback, DefaultRenderersFactory, Context, Intent, ListenableFuture, audioattributes, AudioSink (+15 more)

### Community 21 - "AuraPlayerManager.kt"
Cohesion: 0.08
Nodes (27): AuraPlayerSingleton, Context, Job, ListenableFuture, StateFlow, RepeatMode, ALL, OFF (+19 more)

### Community 22 - "AudioMetadataHelper.kt"
Cohesion: 0.16
Nodes (9): AudioMetadataHelper, AudioTechnicalDetails, Context, Uri, AudioFormatSupportTest, file, mediaextractor, mediaformat (+1 more)

### Community 23 - ".normalizeSourceString"
Cohesion: 0.13
Nodes (11): AudioSourceNormalizer, Context, Uri, NormalizedAudioSource, MusicImportAndLyricsTest, contenturis, environment, log (+3 more)

### Community 24 - "Aura Cinematic Sound — Design System"
Cohesion: 0.17
Nodes (11): 1. Brand & Style, 2. Color Palette & Surface Tokens, 3. Typography (Plus Jakarta Sans), 4. Spacing & Border Radii, 5. Screen Manifest, Accents & Spectrum, Aura Cinematic Sound — Design System, Border Radii (+3 more)

### Community 25 - "SongEntity"
Cohesion: 0.11
Nodes (3): SongDao, SongEntity, FakeSongDao

### Community 27 - "SmartScanSettings"
Cohesion: 0.08
Nodes (21): Uri, LibraryExclusionFilter, ExcludedFolderRule, FilenamePatternRule, HiddenTrackItem, SmartScanSettings, EmptyCardMessage(), ExcludedItemsScreen() (+13 more)

### Community 29 - "MusicRepositoryImpl.kt"
Cohesion: 0.12
Nodes (13): FavoriteDao, MetadataEditor, DatabaseIntegrityReport, StatisticsEngine, ListeningStatistics, combine, context, dispatchers (+5 more)

### Community 30 - "Song"
Cohesion: 0.15
Nodes (4): Song, Flow, MetadataEditorDialog(), EntityMappingTest

### Community 33 - "build_app.py"
Cohesion: 0.25
Nodes (4): json, os, re, urllib_request

### Community 34 - "AuraWidgetProvider.kt"
Cohesion: 0.22
Nodes (13): AuraBaseWidgetProvider, AuraLargeWidgetProvider, AuraMediumWidgetProvider, AuraResponsiveWidgetProvider, AuraSmallWidgetProvider, AuraWidgetActionReceiver, AppWidgetManager, Bundle (+5 more)

### Community 35 - "Theme.kt"
Cohesion: 0.25
Nodes (13): AuraColorScheme, auraDarkColorScheme(), auraLightColorScheme(), AuraTheme(), createAuraDarkColors(), createAuraLightColors(), Color, compositionlocalof (+5 more)

### Community 36 - "QueueDao"
Cohesion: 0.17
Nodes (3): QueueDao, QueueEntity, FakeQueueDao

### Community 37 - "EqualizerSettings"
Cohesion: 0.23
Nodes (5): BassTrebleSettings, EqualizerBand, EqualizerSettings, ParametricSettings, AudioEffectsStateTest

### Community 38 - "Ponytail"
Cohesion: 0.22
Nodes (8): Boundaries, Intensity, Output, Persistence, Ponytail, Rules, The ladder, When NOT to be lazy

### Community 39 - "AuraScreen"
Cohesion: 0.29
Nodes (7): AuraScreen, Favorites, Home, Library, Playlists, Search, Settings

### Community 40 - "gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 41 - ".updateAllWidgets"
Cohesion: 0.46
Nodes (5): AuraWidgetManager, Bundle, Context, Bitmap, RemoteViews

### Community 53 - "PlayerViewModel.kt"
Cohesion: 0.13
Nodes (15): AuraSleepTimer, Job, StateFlow, StateFlow, ViewModel, StateFlow, ViewModel, asstateflow (+7 more)

### Community 54 - "AudioEffectsState.kt"
Cohesion: 0.12
Nodes (12): AudioProfile, BalanceSettings, CompressorSettings, LimiterSettings, PlaybackSettings, ReplayGainMode, ALBUM, OFF (+4 more)

### Community 55 - "UserPreferences.kt"
Cohesion: 0.07
Nodes (30): Flow, AnimationSettings, AppearanceSettings, BluetoothGestureSettings, GesturesSettings, HapticSettings, LibrarySettings, LyricsDisplaySettings (+22 more)

### Community 56 - "Ponytail Help"
Cohesion: 0.25
Nodes (7): Configure Default Mode, Deactivate, Levels, More, Ponytail Help, Skills, Update

### Community 57 - "AuraAudioProcessor"
Cohesion: 0.18
Nodes (9): AuraAudioProcessor, FloatArray, AudioProcessor, BaseAudioProcessor, ByteBuffer, byteorder, c, optin (+1 more)

### Community 58 - "BiquadFilter"
Cohesion: 0.13
Nodes (7): BiquadFilter, Coefficients, PeakLimiter, Color, Modifier, ParametricEqCurve(), DspEngineTest

### Community 59 - "AuraVisualizerView.kt"
Cohesion: 0.18
Nodes (13): VisualizerStyle, BARS, CIRCULAR, MINIMAL, SPECTRUM, WAVEFORM, AuraVisualizerView(), Color (+5 more)

### Community 60 - "DynamicCompressor"
Cohesion: 0.18
Nodes (7): abs, DynamicCompressor, exp, log10, max, pow, tanh

### Community 62 - "NowPlayingScreen"
Cohesion: 0.08
Nodes (39): AuraMotion, AuraAnimatedFavoriteButton(), AuraAnimatedPlayPauseButton(), AuraCapsulePlayPauseButton(), AuraGlassPillButton(), Color, Dp, Modifier (+31 more)

### Community 63 - "AudioEffectsScreen"
Cohesion: 0.25
Nodes (11): AudioEffectsScreen(), DynamicsTab(), EffectSectionCard(), EqAndToneTab(), com, FloatArray, Modifier, OutputProfileTab() (+3 more)

### Community 64 - "AudioEffectsState"
Cohesion: 0.22
Nodes (7): AuraHardwareEffectsManager, AudioEffectsState, BassBoost, Equalizer, LoudnessEnhancer, PresetReverb, Virtualizer

### Community 66 - "FilterType"
Cohesion: 0.33
Nodes (6): FilterType, HIGH_PASS, HIGH_SHELF, LOW_PASS, LOW_SHELF, PEAKING

### Community 67 - "PlaylistDao"
Cohesion: 0.08
Nodes (5): PlaylistDao, PlaylistEntity, PlaylistSongCrossRef, FakePlaylistDao, Flow

### Community 68 - "SpatialPreset"
Cohesion: 0.12
Nodes (17): SpatialPreset, CINEMA, CONCERT, FOUR_D, FOUR_D_FAST, FOUR_D_SLOW, NORMAL, STUDIO (+9 more)

### Community 69 - "LyricsState"
Cohesion: 0.11
Nodes (14): LrcLibLyricsProvider, Loading, LyricLine, LyricsParser, LyricsState, Success, Unavailable, LyricsSyncTest (+6 more)

### Community 70 - "SmartPlaylistType"
Cohesion: 0.20
Nodes (9): SmartPlaylist, SmartPlaylistType, FAVORITES, HIGH_QUALITY, LONG_TRACKS, MOST_PLAYED, NEVER_PLAYED, RECENTLY_ADDED (+1 more)

### Community 72 - "PlaybackState"
Cohesion: 0.18
Nodes (3): PlaybackState, PlaybackStateTest, AuraWidgetIntegrationTest

### Community 73 - "ponytail-audit/SKILL.md"
Cohesion: 0.40
Nodes (4): Boundaries, Hunt, Output, Tags

### Community 74 - "ReverbPreset"
Cohesion: 0.22
Nodes (9): ReverbPreset, CONCERT, HALL, LARGE_HALL, LARGE_ROOM, OFF, ROOM, SMALL_ROOM (+1 more)

### Community 75 - "SettingsCategory"
Cohesion: 0.12
Nodes (16): SettingsCategory, ABOUT, ACCESSIBILITY, ADVANCED, APPEARANCE, AUDIO, BACKUP, BLUETOOTH (+8 more)

### Community 76 - "Daos.kt"
Cohesion: 0.14
Nodes (9): Flow, RecentlyPlayedDao, RecentlyPlayedEntity, dao, insert, onconflictstrategy, query, transaction (+1 more)

### Community 77 - "Ponytail Gain"
Cohesion: 0.40
Nodes (4): Boundaries, Honesty boundary, Ponytail Gain, Scoreboard

### Community 78 - "PlaybackStatus"
Cohesion: 0.29
Nodes (7): PlaybackStatus, BUFFERING, ENDED, ERROR, IDLE, PAUSED, PLAYING

### Community 79 - "ponytail-review/SKILL.md"
Cohesion: 0.40
Nodes (4): Boundaries, Examples, Format, Scoring

### Community 80 - "GestureAxis"
Cohesion: 0.50
Nodes (4): GestureAxis, HORIZONTAL, UNDECIDED, VERTICAL

### Community 83 - "ponytail-debt/SKILL.md"
Cohesion: 0.50
Nodes (3): Boundaries, Output, Scan

### Community 85 - "SearchScreen.kt"
Cohesion: 0.15
Nodes (18): alignment, Color, Dp, Modifier, StarRatingBar(), LazyListState, Modifier, SearchScreen() (+10 more)

### Community 86 - "AudioDeviceType"
Cohesion: 0.12
Nodes (14): AudioOutputManager, AudioDeviceCallback, Quad, AudioDeviceType, BLUETOOTH_HEADPHONES, BLUETOOTH_SPEAKER, OTHER, PHONE_SPEAKER (+6 more)

### Community 87 - "MainActivity.kt"
Cohesion: 0.15
Nodes (13): AuraSplashScreen(), Color, Modifier, SplashEqualizerBars(), coil, delay, diskcache, enableedgetoedge (+5 more)

### Community 88 - "SleepTimerSettings"
Cohesion: 0.17
Nodes (5): SleepTimerSettings, SleepTimerBottomSheet(), SleepTimerOptionRow(), SleepTimerTest, Phase4FeaturesTest

### Community 89 - "SongContextMenu.kt"
Cohesion: 0.08
Nodes (52): ContextMenuItem(), HideFromLibraryDialog(), HideOptionCard(), androidx, ImageVector, InfoRow(), InfoSectionHeader(), TrackInfoDialog() (+44 more)

### Community 90 - "ExcludedItemsScreen.kt"
Cohesion: 0.07
Nodes (40): album, alertdialog, arrowback, auraonprimary, aurasecondary, button, buttondefaults, delete (+32 more)

### Community 91 - ".cleanupExistingDuplicates"
Cohesion: 0.29
Nodes (3): AuraBackupManager, Uri, SongRatingEntity

### Community 93 - "Entities.kt"
Cohesion: 0.18
Nodes (7): FavoriteEntity, ListeningHistoryEntity, LyricsCacheEntity, entity, foreignkey, index, primarykey

### Community 94 - "ArtworkColorExtractor.kt"
Cohesion: 0.22
Nodes (9): ArtworkColorExtractor, ArtworkPalette, Context, bitmapfactory, colorutils, concurrenthashmap, inputstream, palette (+1 more)

### Community 98 - ".onCreate"
Cohesion: 0.32
Nodes (5): Bundle, Factory, Factory, Factory, T

### Community 99 - "SpatialMovementMode"
Cohesion: 0.33
Nodes (6): SpatialMovementMode, CUSTOM, FAST_ORBIT, MEDIUM_ORBIT, SLOW_ORBIT, STATIC

### Community 100 - "OnDataCaptureListener"
Cohesion: 0.60
Nodes (3): OnDataCaptureListener, ByteArray, Visualizer

### Community 103 - "MainActivity"
Cohesion: 0.60
Nodes (3): android, MainActivity, ComponentActivity

## Knowledge Gaps
- **167 isolated node(s):** `Loading`, `Unavailable`, `RECENTLY_ADDED`, `RECENTLY_PLAYED`, `MOST_PLAYED` (+162 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 532 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Song` connect `Song` to `LibraryScreen`, `LibraryScreen.kt`, `NowPlayingScreen.kt`, `QueueState`, `MusicRepository`, `AuraPlayerManager`, `PlayerViewModel`, `MusicImportAndLyricsTest.kt`, `MusicRepositoryImpl`, `AuraPlayerManager.kt`, `AudioMetadataHelper.kt`, `.normalizeSourceString`, `SongEntity`, `PlaylistRepository`, `SmartScanSettings`, `LibraryViewModel`, `MusicRepositoryImpl.kt`, `QueueDao`, `PlayerViewModel.kt`, `UserPreferences.kt`, `UserPreferences`, `NowPlayingScreen`, `LyricsManager`, `LyricsState`, `SongSortTest`, `PlaybackState`, `PlayerState`, `SearchScreen.kt`, `AudioDeviceType`, `SleepTimerSettings`, `SongContextMenu.kt`, `ExcludedItemsScreen.kt`, `Entities.kt`, `PlaylistRepositoryImpl`, `.clearLyricsCache`, `.setRating`, `.toggleFavorite`?**
  _High betweenness centrality (0.177) - this node is a cross-community bridge._
- **Why does `AuraPlayerManager` connect `AuraPlayerManager` to `AudioEffectsState`, `EqualizerSettings`, `QueueState`, `PlaybackState`, `.updateAllWidgets`, `PlayerViewModel.kt`, `AuraPlayerManager.kt`, `AudioDeviceType`, `UserPreferences`, `Song`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **Why does `MusicRepositoryImpl` connect `MusicRepositoryImpl` to `LyricsManager`, `.onCreate`, `QueueDao`, `MusicRepository`, `FakeRecentlyPlayedDao`, `MusicFolderEntity`, `Entities.kt`, `AudioMetadataHelper.kt`, `MainActivity.kt`, `.cleanupExistingDuplicates`, `.importDiscoveredTracks`, `MusicRepositoryImpl.kt`?**
  _High betweenness centrality (0.078) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `Song` (e.g. with `.testSongFormattedDuration()` and `.testMinimizeTransitionState()`) actually correct?**
  _`Song` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `AuraPlayerManager` (e.g. with `AudioOutputManager` and `AuraSleepTimer`) actually correct?**
  _`AuraPlayerManager` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Loading`, `Unavailable`, `RECENTLY_ADDED` to the rest of the system?**
  _167 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `audioEngine` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._