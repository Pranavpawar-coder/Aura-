# Graph Report - Aura  (2026-09-29)

## Corpus Check
- 109 files · ~239,692 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 47 file(s) not represented in the graph (top: .xml 38, (none) 3, .properties 2)

## Summary
- 1813 nodes · 4829 edges · 97 communities (69 shown, 28 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 71 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- audioEngine
- spatialaudiooff
- documentscontract
- Components.kt
- Playlist
- LibraryScreen.kt
- NowPlayingScreen.kt
- SongSortOrder
- QueueState
- AuraDatabase.kt
- EqualizerPreset
- AudioEffectsScreen.kt
- Song
- AuraPlayerManager
- MusicRepositoryTest.kt
- PlayerViewModel
- CenterDisplayMode
- MusicImportAndLyricsTest.kt
- MusicRepositoryImpl
- SortCriterion
- AuraAudioService.kt
- .withPlayer
- AudioMetadataHelper.kt
- .normalizeSourceString
- Aura Cinematic Sound — Design System
- SongEntity
- PlaylistRepository
- SmartScanSettings
- LibraryViewModel
- MusicRepositoryImpl.kt
- Listener
- arrowforward
- aurasecondarycontainer
- build_app.py
- AuraWidgetManager.kt
- MainActivity.kt
- QueueDao
- EqualizerSettings
- Ponytail
- AuraScreen
- gradlew
- SpatialPositionRadar.kt
- rules/graphify.md
- workflows/graphify.md
- app/build.gradle.kts
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
- BiquadFilter.kt
- UserPreferences
- NowPlayingScreen
- ParametricBand
- AudioEffectsState
- LyricsManager
- FilterType
- PlaylistDao
- SpatialPreset
- LyricsState
- SmartPlaylistType
- SongSortTest
- StatisticsScreen.kt
- ponytail-audit/SKILL.md
- ReverbPreset
- SettingsCategory
- RecentlyPlayedDao
- Ponytail Gain
- PlaybackStatus
- ponytail-review/SKILL.md
- GestureAxis
- Spatial4DSettings
- PlayerState
- ponytail-debt/SKILL.md
- AuraAudioService
- AuraFallbackArtwork
- AudioDeviceType
- LibraryViewModel.kt
- SleepTimerSettings
- ContextMenuItem
- SettingsScreen.kt
- .cleanupExistingDuplicates
- ponytail.md
- folderopen
- defaultrenderersfactory
- graphicslayer
- view

## God Nodes (most connected - your core abstractions)
1. `Song` - 128 edges
2. `PlayerViewModel` - 82 edges
3. `AuraPlayerManager` - 79 edges
4. `MusicRepositoryImpl` - 72 edges
5. `UserPreferences` - 67 edges
6. `MusicRepository` - 65 edges
7. `LibraryViewModel` - 59 edges
8. `audioEngine` - 35 edges
9. `SongEntity` - 34 edges
10. `AudioEffectsState` - 34 edges

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

## Communities (97 total, 28 thin omitted)

### Community 0 - "audioEngine"
Cohesion: 0.05
Nodes (6): audioEngine, SAMPLE_TRACKS, Queue, Track, AudioSync, NavigationController

### Community 3 - "Components.kt"
Cohesion: 0.08
Nodes (32): alpha, AnimatedEqualizerBars(), AuraHeader(), AuraScrubber(), Color, Modifier, animatefloat, auraglassborder (+24 more)

### Community 4 - "Playlist"
Cohesion: 0.15
Nodes (18): Album, Artist, FolderGroup, Genre, Playlist, PlaylistSelectionDialog(), SongContextMenuBottomSheet(), AlbumDetailScreen() (+10 more)

### Community 5 - "LibraryScreen.kt"
Cohesion: 0.10
Nodes (47): alignment, Color, Dp, Modifier, StarRatingBar(), asyncimage, auraprimary, auraprimarycontainer (+39 more)

### Community 6 - "NowPlayingScreen.kt"
Cohesion: 0.06
Nodes (57): activityresultcontracts, auraPressable(), auraShimmer(), Color, Modifier, animatable, animatecolorasstate, animatedcontent (+49 more)

### Community 7 - "SongSortOrder"
Cohesion: 0.11
Nodes (16): SongSortOrder, ALBUM_AZ, ALBUM_ZA, ARTIST_AZ, ARTIST_ZA, DATE_ADDED, DATE_ADDED_OLD, DURATION_ASC (+8 more)

### Community 9 - "AuraDatabase.kt"
Cohesion: 0.05
Nodes (19): FavoriteDao, com, ListeningHistoryDao, LyricsCacheDao, SongRatingDao, AuraDatabase, Context, FavoriteEntity (+11 more)

### Community 10 - "EqualizerPreset"
Cohesion: 0.12
Nodes (15): EqualizerPreset, ACOUSTIC, BASS, BASS_BOOST, CLASSICAL, CUSTOM, DANCE, DEEP (+7 more)

### Community 11 - "AudioEffectsScreen.kt"
Cohesion: 0.09
Nodes (27): SleepTimerBottomSheet(), SleepTimerOptionRow(), auraonprimary, bedtime, card, carddefaults, contentdescription, dropdownmenu (+19 more)

### Community 12 - "Song"
Cohesion: 0.05
Nodes (7): Song, com, Flow, Uri, MusicRepository, MetadataEditorDialog(), EntityMappingTest

### Community 14 - "MusicRepositoryTest.kt"
Cohesion: 0.15
Nodes (5): FakeFavoriteDao, FakeRecentlyPlayedDao, Flow, MusicRepositoryTest, runtest

### Community 15 - "PlayerViewModel"
Cohesion: 0.04
Nodes (3): android, FloatArray, PlayerViewModel

### Community 16 - "CenterDisplayMode"
Cohesion: 0.50
Nodes (4): CenterDisplayMode, ARTWORK, LYRICS, VISUALIZER

### Community 17 - "MusicImportAndLyricsTest.kt"
Cohesion: 0.17
Nodes (13): assertequals, assertfalse, assertnotequals, assertnotnull, assertnull, asserttrue, before, junit4 (+5 more)

### Community 18 - "MusicRepositoryImpl"
Cohesion: 0.07
Nodes (5): DatabaseIntegrityReport, DiscoveredTrack, Flow, Uri, MusicRepositoryImpl

### Community 19 - "SortCriterion"
Cohesion: 0.12
Nodes (16): SortCriterion, ALBUM, ALBUM_ARTIST, ARTIST, DATE_ADDED, DURATION, FILE_SIZE, GENRE (+8 more)

### Community 20 - "AuraAudioService.kt"
Cohesion: 0.12
Nodes (16): DefaultRenderersFactory, Context, audioattributes, AudioSink, bundle, commandbutton, defaultaudiosink, defaultextractorsfactory (+8 more)

### Community 22 - "AudioMetadataHelper.kt"
Cohesion: 0.19
Nodes (8): AudioMetadataHelper, AudioTechnicalDetails, Context, Uri, AudioFormatSupportTest, mediaextractor, mediaformat, mediametadataretriever

### Community 23 - ".normalizeSourceString"
Cohesion: 0.18
Nodes (5): AudioSourceNormalizer, Context, Uri, NormalizedAudioSource, MusicImportAndLyricsTest

### Community 24 - "Aura Cinematic Sound — Design System"
Cohesion: 0.17
Nodes (11): 1. Brand & Style, 2. Color Palette & Surface Tokens, 3. Typography (Plus Jakarta Sans), 4. Spacing & Border Radii, 5. Screen Manifest, Accents & Spectrum, Aura Cinematic Sound — Design System, Border Radii (+3 more)

### Community 25 - "SongEntity"
Cohesion: 0.11
Nodes (3): SongDao, SongEntity, FakeSongDao

### Community 27 - "SmartScanSettings"
Cohesion: 0.07
Nodes (21): Uri, LibraryExclusionFilter, ExcludedFolderRule, FilenamePatternRule, HiddenTrackItem, SmartScanSettings, EmptyCardMessage(), ExcludedItemsScreen() (+13 more)

### Community 29 - "MusicRepositoryImpl.kt"
Cohesion: 0.12
Nodes (19): MetadataEditor, StatisticsEngine, ListeningStatistics, build, combine, contenturis, context, dispatchers (+11 more)

### Community 30 - "Listener"
Cohesion: 0.20
Nodes (3): Listener, Listener, Player

### Community 33 - "build_app.py"
Cohesion: 0.25
Nodes (4): json, os, re, urllib_request

### Community 34 - "AuraWidgetManager.kt"
Cohesion: 0.06
Nodes (43): AuraPlayerSingleton, PlaybackState, ArtworkColorExtractor, ArtworkPalette, Context, AuraWidgetManager, AppWidgetManager, Bundle (+35 more)

### Community 35 - "MainActivity.kt"
Cohesion: 0.06
Nodes (36): Flow, PlaylistRepositoryImpl, AppearanceSettings, android, Bundle, MainActivity, Factory, Factory (+28 more)

### Community 36 - "QueueDao"
Cohesion: 0.18
Nodes (3): QueueDao, QueueEntity, FakeQueueDao

### Community 37 - "EqualizerSettings"
Cohesion: 0.19
Nodes (6): AudioOutputInfo, BassTrebleSettings, EqualizerBand, EqualizerSettings, ParametricSettings, AudioEffectsStateTest

### Community 38 - "Ponytail"
Cohesion: 0.22
Nodes (8): Boundaries, Intensity, Output, Persistence, Ponytail, Rules, The ladder, When NOT to be lazy

### Community 39 - "AuraScreen"
Cohesion: 0.29
Nodes (7): AuraScreen, Favorites, Home, Library, Playlists, Search, Settings

### Community 40 - "gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 41 - "SpatialPositionRadar.kt"
Cohesion: 0.18
Nodes (18): Color, Modifier, SpatialPositionRadar(), aspectratio, aurasurfacecontainerhigh, box, brush, canvas (+10 more)

### Community 50 - "MusicFolderEntity"
Cohesion: 0.12
Nodes (7): MusicFolderDao, ListeningHistoryEntity, MusicFolderEntity, entity, foreignkey, index, primarykey

### Community 53 - "AuraPlayerManager.kt"
Cohesion: 0.13
Nodes (15): Context, Job, ListenableFuture, StateFlow, AuraSleepTimer, Job, StateFlow, asstateflow (+7 more)

### Community 54 - "PlayerViewModel.kt"
Cohesion: 0.08
Nodes (20): AudioProfile, BalanceSettings, CompressorSettings, LimiterSettings, LoudnessSettings, PlaybackSettings, ReplayGainSettings, ReverbSettings (+12 more)

### Community 55 - "UserPreferences.kt"
Cohesion: 0.08
Nodes (29): Flow, AnimationSettings, BluetoothGestureSettings, GesturesSettings, HapticSettings, LibrarySettings, LyricsDisplaySettings, NotificationSettings (+21 more)

### Community 56 - "Ponytail Help"
Cohesion: 0.25
Nodes (7): Configure Default Mode, Deactivate, Levels, More, Ponytail Help, Skills, Update

### Community 57 - "AuraAudioProcessor"
Cohesion: 0.13
Nodes (13): AuraAudioProcessor, FloatArray, ReplayGainMode, ALBUM, OFF, TRACK, AudioProcessor, BaseAudioProcessor (+5 more)

### Community 58 - "BiquadFilter"
Cohesion: 0.12
Nodes (5): BiquadFilter, Coefficients, DynamicCompressor, PeakLimiter, DspEngineTest

### Community 59 - "VisualizerStyle"
Cohesion: 0.20
Nodes (10): VisualizerStyle, BARS, CIRCULAR, MINIMAL, SPECTRUM, WAVEFORM, AuraVisualizerView(), Color (+2 more)

### Community 60 - "BiquadFilter.kt"
Cohesion: 0.18
Nodes (13): abs, atan2, cos, exp, ln, log10, max, pi (+5 more)

### Community 62 - "NowPlayingScreen"
Cohesion: 0.10
Nodes (22): RepeatMode, ALL, OFF, ONE, AuraMotion, InfoRow(), InfoSectionHeader(), TrackInfoDialog() (+14 more)

### Community 63 - "ParametricBand"
Cohesion: 0.14
Nodes (15): ParametricBand, AudioEffectsScreen(), DynamicsTab(), EffectSectionCard(), EqAndToneTab(), com, FloatArray, Modifier (+7 more)

### Community 64 - "AudioEffectsState"
Cohesion: 0.17
Nodes (10): AuraHardwareEffectsManager, OnDataCaptureListener, AudioEffectsState, BassBoost, ByteArray, Equalizer, LoudnessEnhancer, PresetReverb (+2 more)

### Community 65 - "LyricsManager"
Cohesion: 0.24
Nodes (3): LyricsCacheEntity, Uri, LyricsManager

### Community 66 - "FilterType"
Cohesion: 0.33
Nodes (6): FilterType, HIGH_PASS, HIGH_SHELF, LOW_PASS, LOW_SHELF, PEAKING

### Community 67 - "PlaylistDao"
Cohesion: 0.08
Nodes (5): PlaylistDao, PlaylistEntity, PlaylistSongCrossRef, FakePlaylistDao, Flow

### Community 68 - "SpatialPreset"
Cohesion: 0.18
Nodes (10): SpatialPreset, CINEMA, CONCERT, FOUR_D, FOUR_D_FAST, FOUR_D_SLOW, NORMAL, STUDIO (+2 more)

### Community 69 - "LyricsState"
Cohesion: 0.11
Nodes (14): LrcLibLyricsProvider, Loading, LyricLine, LyricsParser, LyricsState, Success, Unavailable, LyricsSyncTest (+6 more)

### Community 70 - "SmartPlaylistType"
Cohesion: 0.20
Nodes (9): SmartPlaylist, SmartPlaylistType, FAVORITES, HIGH_QUALITY, LONG_TRACKS, MOST_PLAYED, NEVER_PLAYED, RECENTLY_ADDED (+1 more)

### Community 72 - "StatisticsScreen.kt"
Cohesion: 0.15
Nodes (17): album, alertdialog, ImageVector, Modifier, StatisticsScreen(), StatMetricCard(), TopRankItem(), arrowback (+9 more)

### Community 73 - "ponytail-audit/SKILL.md"
Cohesion: 0.40
Nodes (4): Boundaries, Hunt, Output, Tags

### Community 74 - "ReverbPreset"
Cohesion: 0.22
Nodes (9): ReverbPreset, CONCERT, HALL, LARGE_HALL, LARGE_ROOM, OFF, ROOM, SMALL_ROOM (+1 more)

### Community 75 - "SettingsCategory"
Cohesion: 0.12
Nodes (16): SettingsCategory, ABOUT, ACCESSIBILITY, ADVANCED, APPEARANCE, AUDIO, BACKUP, BLUETOOTH (+8 more)

### Community 76 - "RecentlyPlayedDao"
Cohesion: 0.21
Nodes (3): Flow, RecentlyPlayedDao, RecentlyPlayedEntity

### Community 77 - "Ponytail Gain"
Cohesion: 0.40
Nodes (4): Boundaries, Honesty boundary, Ponytail Gain, Scoreboard

### Community 78 - "PlaybackStatus"
Cohesion: 0.17
Nodes (8): androidx, PlaybackStatus, BUFFERING, ENDED, ERROR, IDLE, PAUSED, PLAYING

### Community 79 - "ponytail-review/SKILL.md"
Cohesion: 0.40
Nodes (4): Boundaries, Examples, Format, Scoring

### Community 80 - "GestureAxis"
Cohesion: 0.50
Nodes (4): GestureAxis, HORIZONTAL, UNDECIDED, VERTICAL

### Community 83 - "ponytail-debt/SKILL.md"
Cohesion: 0.50
Nodes (3): Boundaries, Output, Scan

### Community 84 - "AuraAudioService"
Cohesion: 0.15
Nodes (9): AuraAudioService, Callback, Listener, Intent, ListenableFuture, Listener, ExoPlayer, MediaSession (+1 more)

### Community 85 - "AuraFallbackArtwork"
Cohesion: 0.16
Nodes (19): AuraAnimatedFavoriteButton(), AuraAnimatedPlayPauseButton(), AuraFallbackArtwork(), Color, Dp, Modifier, MiniPlayer(), HomeScreen() (+11 more)

### Community 86 - "AudioDeviceType"
Cohesion: 0.14
Nodes (12): AudioOutputManager, AudioDeviceCallback, Quad, AudioDeviceType, BLUETOOTH_HEADPHONES, BLUETOOTH_SPEAKER, OTHER, PHONE_SPEAKER (+4 more)

### Community 87 - "LibraryViewModel.kt"
Cohesion: 0.20
Nodes (7): SearchResults, StateFlow, ViewModel, flatmaplatest, flowof, sharingstarted, viewmodelscope

### Community 88 - "SleepTimerSettings"
Cohesion: 0.20
Nodes (3): SleepTimerSettings, SleepTimerTest, Phase4FeaturesTest

### Community 89 - "ContextMenuItem"
Cohesion: 0.40
Nodes (5): ContextMenuItem(), HideFromLibraryDialog(), HideOptionCard(), androidx, ImageVector

### Community 90 - "SettingsScreen.kt"
Cohesion: 0.08
Nodes (56): add, arrangement, auraonsurface, auraonsurfacevariant, auraoutline, auraoutlinevariant, aurasecondary, aurasurfacecontainer (+48 more)

### Community 91 - ".cleanupExistingDuplicates"
Cohesion: 0.25
Nodes (3): AuraBackupManager, Uri, SongRatingEntity

## Knowledge Gaps
- **167 isolated node(s):** `Loading`, `Unavailable`, `RECENTLY_ADDED`, `RECENTLY_PLAYED`, `MOST_PLAYED` (+162 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 523 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **28 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Song` connect `Song` to `Playlist`, `LibraryScreen.kt`, `NowPlayingScreen.kt`, `QueueState`, `AuraPlayerManager`, `PlayerViewModel`, `MusicImportAndLyricsTest.kt`, `MusicRepositoryImpl`, `.withPlayer`, `.normalizeSourceString`, `SongEntity`, `PlaylistRepository`, `SmartScanSettings`, `LibraryViewModel`, `MusicRepositoryImpl.kt`, `AuraWidgetManager.kt`, `MainActivity.kt`, `QueueDao`, `MusicFolderEntity`, `AuraPlayerManager.kt`, `PlayerViewModel.kt`, `UserPreferences.kt`, `UserPreferences`, `NowPlayingScreen`, `LyricsManager`, `LyricsState`, `SongSortTest`, `StatisticsScreen.kt`, `PlayerState`, `AuraFallbackArtwork`, `AudioDeviceType`, `LibraryViewModel.kt`, `SleepTimerSettings`, `ContextMenuItem`, `SettingsScreen.kt`, `.cleanupExistingDuplicates`?**
  _High betweenness centrality (0.172) - this node is a cross-community bridge._
- **Why does `UserPreferences` connect `UserPreferences` to `AudioEffectsState`, `MainActivity.kt`, `EqualizerSettings`, `SongSortOrder`, `AuraPlayerManager`, `Spatial4DSettings`, `AuraPlayerManager.kt`, `PlayerViewModel.kt`, `UserPreferences.kt`, `LibraryViewModel.kt`, `SettingsScreen.kt`, `SmartScanSettings`, `MusicRepositoryImpl.kt`, `ParametricBand`?**
  _High betweenness centrality (0.091) - this node is a cross-community bridge._
- **Why does `AuraPlayerManager` connect `AuraPlayerManager` to `AudioEffectsState`, `AuraWidgetManager.kt`, `EqualizerSettings`, `QueueState`, `Song`, `PlaybackStatus`, `.withPlayer`, `AuraPlayerManager.kt`, `AudioDeviceType`, `PlayerViewModel.kt`, `UserPreferences`, `Listener`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `Song` (e.g. with `.testSongFormattedDuration()` and `.testMinimizeTransitionState()`) actually correct?**
  _`Song` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `AuraPlayerManager` (e.g. with `AudioOutputManager` and `AuraSleepTimer`) actually correct?**
  _`AuraPlayerManager` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Loading`, `Unavailable`, `RECENTLY_ADDED` to the rest of the system?**
  _167 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `audioEngine` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._