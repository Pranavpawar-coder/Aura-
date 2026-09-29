# Graph Report - Aura  (2026-09-28)

## Corpus Check
- 105 files · ~231,158 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 38 file(s) not represented in the graph (top: .xml 29, (none) 3, .properties 2)

## Summary
- 1673 nodes · 4400 edges · 103 communities (67 shown, 36 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 57 edges (avg confidence: 0.85)
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
- ListeningHistoryDao
- EqualizerPreset
- SettingsScreen.kt
- Song
- AuraPlayerManager
- RecentlyPlayedDao
- PlayerViewModel
- CenterDisplayMode
- assertequals
- MusicRepositoryImpl
- AudioEffectsScreen.kt
- AuraAudioService.kt
- .normalizeSourceString
- AudioMetadataHelper.kt
- PlaylistRepositoryImpl.kt
- Aura Cinematic Sound — Design System
- SongEntity
- PlaylistRepository
- Daos.kt
- LibraryViewModel
- MusicRepositoryImpl.kt
- PlaylistDao
- arrowforward
- aurasecondarycontainer
- build_app.py
- AuraWidgetManager.kt
- MainActivity.kt
- QueueDao
- AudioEffectsState.kt
- Ponytail
- AuraScreen
- gradlew
- Flow
- rules/graphify.md
- workflows/graphify.md
- app/build.gradle.kts
- repeatmode
- allinclusive
- history
- AuraDatabase.kt
- spatialtracking
- nightlight
- SleepTimerSettings
- AuraPlayerManager.kt
- UserPreferences.kt
- Ponytail Help
- AuraAudioProcessor
- BiquadFilter
- AuraVisualizerView.kt
- BiquadFilter.kt
- .importDiscoveredTracks
- NowPlayingScreen
- AudioEffectsScreen
- AudioEffectsState
- LyricsManager
- Listener
- FakePlaylistDao
- .cleanupExistingDuplicates
- LyricsState
- SmartPlaylistType
- SongSortTest
- ParametricBand
- ponytail-audit/SKILL.md
- ReverbPreset
- SettingsCategory
- abs
- Ponytail Gain
- OnDataCaptureListener
- ponytail-review/SKILL.md
- GestureAxis
- FakeFavoriteDao
- ponytail-debt/SKILL.md
- AuraAudioService
- AuraFallbackArtwork
- AudioDeviceType
- Entities.kt
- MusicImportAndLyricsTest.kt
- FilterType
- StatisticsScreen.kt
- PlaybackStatus
- SpatialMovementMode
- .fromDomain
- .attachManualLyrics
- ponytail.md
- folderopen
- defaultrenderersfactory
- graphicslayer

## God Nodes (most connected - your core abstractions)
1. `Song` - 119 edges
2. `PlayerViewModel` - 80 edges
3. `AuraPlayerManager` - 76 edges
4. `MusicRepositoryImpl` - 55 edges
5. `UserPreferences` - 48 edges
6. `MusicRepository` - 48 edges
7. `LibraryViewModel` - 44 edges
8. `audioEngine` - 35 edges
9. `AudioEffectsState` - 34 edges
10. `SongEntity` - 30 edges

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

## Communities (103 total, 36 thin omitted)

### Community 0 - "audioEngine"
Cohesion: 0.05
Nodes (6): audioEngine, SAMPLE_TRACKS, Queue, Track, AudioSync, NavigationController

### Community 3 - "Components.kt"
Cohesion: 0.10
Nodes (36): alpha, Color, Modifier, SpatialPositionRadar(), auraglassborder, auraglasssurface, auraoutline, aurasurface (+28 more)

### Community 4 - "Playlist"
Cohesion: 0.15
Nodes (20): Album, Artist, FolderGroup, Genre, Playlist, PlaylistSelectionDialog(), ContextMenuItem(), androidx (+12 more)

### Community 5 - "LibraryScreen.kt"
Cohesion: 0.12
Nodes (29): add, album, arrowback, aspectratio, auraprimarycontainer, aurasurfacecontainerhigh, aurasurfacecontainerlow, aurasurfacecontainerlowest (+21 more)

### Community 6 - "NowPlayingScreen.kt"
Cohesion: 0.05
Nodes (61): activityresultcontracts, AuraMotion, auraPressable(), auraShimmer(), Color, Modifier, animatable, animatecolorasstate (+53 more)

### Community 7 - "SongSortOrder"
Cohesion: 0.06
Nodes (32): SongSortOrder, ALBUM_AZ, ALBUM_ZA, ARTIST_AZ, ARTIST_ZA, DATE_ADDED, DATE_ADDED_OLD, DURATION_ASC (+24 more)

### Community 9 - "ListeningHistoryDao"
Cohesion: 0.13
Nodes (3): com, ListeningHistoryDao, LyricsCacheDao

### Community 10 - "EqualizerPreset"
Cohesion: 0.12
Nodes (15): EqualizerPreset, ACOUSTIC, BASS, BASS_BOOST, CLASSICAL, CUSTOM, DANCE, DEEP (+7 more)

### Community 11 - "SettingsScreen.kt"
Cohesion: 0.06
Nodes (48): alignment, AnimatedEqualizerBars(), Color, Dp, Modifier, StarRatingBar(), aurasurfacecontainerhighest, barchart (+40 more)

### Community 12 - "Song"
Cohesion: 0.08
Nodes (6): Song, com, Flow, Uri, MusicRepository, MetadataEditorDialog()

### Community 13 - "AuraPlayerManager"
Cohesion: 0.10
Nodes (3): AuraPlayerManager, FloatArray, MediaItem

### Community 14 - "RecentlyPlayedDao"
Cohesion: 0.15
Nodes (4): RecentlyPlayedDao, RecentlyPlayedEntity, FakeRecentlyPlayedDao, Flow

### Community 16 - "CenterDisplayMode"
Cohesion: 0.50
Nodes (4): CenterDisplayMode, ARTWORK, LYRICS, VISUALIZER

### Community 17 - "assertequals"
Cohesion: 0.23
Nodes (9): assertequals, assertfalse, assertnotnull, assertnull, asserttrue, junit4, runwith, test (+1 more)

### Community 19 - "AudioEffectsScreen.kt"
Cohesion: 0.10
Nodes (19): animatedvisibility, buttondefaults, card, carddefaults, dropdownmenu, dropdownmenuitem, filterchip, filterchipdefaults (+11 more)

### Community 20 - "AuraAudioService.kt"
Cohesion: 0.12
Nodes (16): DefaultRenderersFactory, Context, audioattributes, AudioSink, bundle, commandbutton, defaultaudiosink, defaultextractorsfactory (+8 more)

### Community 21 - ".normalizeSourceString"
Cohesion: 0.18
Nodes (5): AudioSourceNormalizer, Context, Uri, NormalizedAudioSource, MusicImportAndLyricsTest

### Community 22 - "AudioMetadataHelper.kt"
Cohesion: 0.18
Nodes (8): AudioMetadataHelper, AudioTechnicalDetails, Context, Uri, AudioFormatSupportTest, mediaextractor, mediaformat, mediametadataretriever

### Community 23 - "PlaylistRepositoryImpl.kt"
Cohesion: 0.20
Nodes (4): Flow, PlaylistRepositoryImpl, combine, map

### Community 24 - "Aura Cinematic Sound — Design System"
Cohesion: 0.17
Nodes (11): 1. Brand & Style, 2. Color Palette & Surface Tokens, 3. Typography (Plus Jakarta Sans), 4. Spacing & Border Radii, 5. Screen Manifest, Accents & Spectrum, Aura Cinematic Sound — Design System, Border Radii (+3 more)

### Community 25 - "SongEntity"
Cohesion: 0.11
Nodes (3): SongDao, SongEntity, FakeSongDao

### Community 27 - "Daos.kt"
Cohesion: 0.14
Nodes (8): FavoriteDao, FavoriteEntity, dao, insert, onconflictstrategy, query, transaction, upsert

### Community 28 - "LibraryViewModel"
Cohesion: 0.06
Nodes (12): StatisticsEngine, ListeningStatistics, SearchResults, StateFlow, Uri, ViewModel, LibraryViewModel, flatmaplatest (+4 more)

### Community 29 - "MusicRepositoryImpl.kt"
Cohesion: 0.10
Nodes (25): Uri, MetadataEditor, DatabaseIntegrityReport, audiomanager, bufferedreader, build, contenturis, context (+17 more)

### Community 33 - "build_app.py"
Cohesion: 0.25
Nodes (4): json, os, re, urllib_request

### Community 34 - "AuraWidgetManager.kt"
Cohesion: 0.05
Nodes (45): AuraPlayerSingleton, Context, PlaybackState, ArtworkColorExtractor, ArtworkPalette, Context, AuraWidgetManager, AppWidgetManager (+37 more)

### Community 35 - "MainActivity.kt"
Cohesion: 0.07
Nodes (35): AppearanceSettings, android, Bundle, MainActivity, Factory, Factory, AuraColorScheme, auraDarkColorScheme() (+27 more)

### Community 36 - "QueueDao"
Cohesion: 0.16
Nodes (3): QueueDao, QueueEntity, FakeQueueDao

### Community 37 - "AudioEffectsState.kt"
Cohesion: 0.08
Nodes (17): Spatial4DEngine, AudioOutputInfo, AudioProfile, BalanceSettings, BassTrebleSettings, CompressorSettings, EqualizerBand, EqualizerSettings (+9 more)

### Community 38 - "Ponytail"
Cohesion: 0.22
Nodes (8): Boundaries, Intensity, Output, Persistence, Ponytail, Rules, The ladder, When NOT to be lazy

### Community 39 - "AuraScreen"
Cohesion: 0.29
Nodes (7): AuraScreen, Favorites, Home, Library, Playlists, Search, Settings

### Community 40 - "gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 50 - "AuraDatabase.kt"
Cohesion: 0.13
Nodes (9): MusicFolderDao, AuraDatabase, Context, MusicFolderEntity, database, migration, room, RoomDatabase (+1 more)

### Community 53 - "SleepTimerSettings"
Cohesion: 0.11
Nodes (10): AuraSleepTimer, Job, StateFlow, SleepTimerSettings, SleepTimerBottomSheet(), SleepTimerOptionRow(), SleepTimerTest, Phase4FeaturesTest (+2 more)

### Community 54 - "AuraPlayerManager.kt"
Cohesion: 0.07
Nodes (25): Job, ListenableFuture, StateFlow, LimiterSettings, LoudnessSettings, PlaybackSettings, ReplayGainSettings, ReverbSettings (+17 more)

### Community 55 - "UserPreferences.kt"
Cohesion: 0.09
Nodes (27): Flow, UserPreferences, AnimationSettings, BluetoothGestureSettings, GesturesSettings, HapticSettings, LibrarySettings, LyricsDisplaySettings (+19 more)

### Community 56 - "Ponytail Help"
Cohesion: 0.25
Nodes (7): Configure Default Mode, Deactivate, Levels, More, Ponytail Help, Skills, Update

### Community 57 - "AuraAudioProcessor"
Cohesion: 0.18
Nodes (9): AuraAudioProcessor, FloatArray, AudioProcessor, BaseAudioProcessor, ByteBuffer, byteorder, c, optin (+1 more)

### Community 58 - "BiquadFilter"
Cohesion: 0.14
Nodes (6): BiquadFilter, DynamicCompressor, Color, Modifier, ParametricEqCurve(), DspEngineTest

### Community 59 - "AuraVisualizerView.kt"
Cohesion: 0.18
Nodes (13): VisualizerStyle, BARS, CIRCULAR, MINIMAL, SPECTRUM, WAVEFORM, AuraVisualizerView(), Color (+5 more)

### Community 60 - "BiquadFilter.kt"
Cohesion: 0.31
Nodes (7): atan2, cos, ln, pi, sin, sinh, sqrt

### Community 62 - "NowPlayingScreen"
Cohesion: 0.13
Nodes (17): PlayerState, InfoRow(), InfoSectionHeader(), TrackInfoDialog(), ArtworkDisplay(), Color, com, FloatArray (+9 more)

### Community 63 - "AudioEffectsScreen"
Cohesion: 0.25
Nodes (11): AudioEffectsScreen(), DynamicsTab(), EffectSectionCard(), EqAndToneTab(), com, FloatArray, Modifier, OutputProfileTab() (+3 more)

### Community 64 - "AudioEffectsState"
Cohesion: 0.29
Nodes (7): AuraHardwareEffectsManager, AudioEffectsState, BassBoost, Equalizer, LoudnessEnhancer, PresetReverb, Virtualizer

### Community 66 - "Listener"
Cohesion: 0.15
Nodes (4): Listener, androidx, Listener, Player

### Community 67 - "FakePlaylistDao"
Cohesion: 0.13
Nodes (3): PlaylistSongCrossRef, FakePlaylistDao, Flow

### Community 68 - ".cleanupExistingDuplicates"
Cohesion: 0.29
Nodes (3): AuraBackupManager, Uri, SongRatingEntity

### Community 69 - "LyricsState"
Cohesion: 0.16
Nodes (9): LrcLibLyricsProvider, Loading, LyricLine, LyricsParser, LyricsState, Success, Unavailable, LyricsSyncTest (+1 more)

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
Cohesion: 0.11
Nodes (19): SettingsCategory, ABOUT, ACCESSIBILITY, ADVANCED, APPEARANCE, AUDIO, BACKUP, BLUETOOTH (+11 more)

### Community 76 - "abs"
Cohesion: 0.19
Nodes (7): abs, PeakLimiter, exp, log10, max, pow, tanh

### Community 77 - "Ponytail Gain"
Cohesion: 0.40
Nodes (4): Boundaries, Honesty boundary, Ponytail Gain, Scoreboard

### Community 78 - "OnDataCaptureListener"
Cohesion: 0.60
Nodes (3): OnDataCaptureListener, ByteArray, Visualizer

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
Cohesion: 0.13
Nodes (23): AuraAnimatedFavoriteButton(), AuraAnimatedPlayPauseButton(), AuraFallbackArtwork(), Color, Dp, Modifier, AuraHeader(), AuraScrubber() (+15 more)

### Community 86 - "AudioDeviceType"
Cohesion: 0.14
Nodes (11): AudioOutputManager, AudioDeviceCallback, Quad, AudioDeviceType, BLUETOOTH_HEADPHONES, BLUETOOTH_SPEAKER, OTHER, PHONE_SPEAKER (+3 more)

### Community 87 - "Entities.kt"
Cohesion: 0.25
Nodes (6): ListeningHistoryEntity, LyricsCacheEntity, entity, foreignkey, index, primarykey

### Community 88 - "MusicImportAndLyricsTest.kt"
Cohesion: 0.32
Nodes (6): assertnotequals, before, mutablestateflow, rule, runtest, temporaryfolder

### Community 89 - "FilterType"
Cohesion: 0.29
Nodes (6): FilterType, HIGH_PASS, HIGH_SHELF, LOW_PASS, LOW_SHELF, PEAKING

### Community 90 - "StatisticsScreen.kt"
Cohesion: 0.10
Nodes (43): alertdialog, ImageVector, Modifier, StatisticsScreen(), StatMetricCard(), TopRankItem(), arrangement, asyncimage (+35 more)

### Community 91 - "PlaybackStatus"
Cohesion: 0.17
Nodes (12): PlaybackStatus, BUFFERING, ENDED, ERROR, IDLE, PAUSED, PLAYING, RepeatMode (+4 more)

### Community 92 - "SpatialMovementMode"
Cohesion: 0.33
Nodes (6): SpatialMovementMode, CUSTOM, FAST_ORBIT, MEDIUM_ORBIT, SLOW_ORBIT, STATIC

## Knowledge Gaps
- **167 isolated node(s):** `Loading`, `Unavailable`, `RECENTLY_ADDED`, `RECENTLY_PLAYED`, `MOST_PLAYED` (+162 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 503 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **36 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Song` connect `Song` to `Playlist`, `LibraryScreen.kt`, `NowPlayingScreen.kt`, `QueueState`, `SettingsScreen.kt`, `AuraPlayerManager`, `PlayerViewModel`, `assertequals`, `MusicRepositoryImpl`, `.normalizeSourceString`, `AudioMetadataHelper.kt`, `PlaylistRepositoryImpl.kt`, `SongEntity`, `PlaylistRepository`, `LibraryViewModel`, `MusicRepositoryImpl.kt`, `AuraWidgetManager.kt`, `QueueDao`, `SleepTimerSettings`, `AuraPlayerManager.kt`, `NowPlayingScreen`, `LyricsManager`, `LyricsState`, `SongSortTest`, `.clearLyricsCache`, `AuraFallbackArtwork`, `AudioDeviceType`, `Entities.kt`, `MusicImportAndLyricsTest.kt`, `StatisticsScreen.kt`, `.fromDomain`, `.addToQueue`, `.toggleFavorite`?**
  _High betweenness centrality (0.169) - this node is a cross-community bridge._
- **Why does `PlayerViewModel` connect `PlayerViewModel` to `NowPlayingScreen.kt`, `QueueState`, `EqualizerPreset`, `AudioEffectsScreen.kt`, `AuraWidgetManager.kt`, `MainActivity.kt`, `AudioEffectsState.kt`, `SleepTimerSettings`, `AuraPlayerManager.kt`, `NowPlayingScreen`, `AudioEffectsScreen`, `AudioEffectsState`, `LyricsState`, `ParametricBand`, `.clearLyricsCache`, `AuraFallbackArtwork`, `AudioDeviceType`, `.addToQueue`, `.attachManualLyrics`, `.removeFromQueue`, `.seekTo`, `.toggleFavorite`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **Why does `AudioEffectsState` connect `AudioEffectsState` to `AudioEffectsState.kt`, `NowPlayingScreen.kt`, `AuraPlayerManager`, `PlayerViewModel`, `AudioEffectsScreen.kt`, `AuraAudioService`, `AuraPlayerManager.kt`, `UserPreferences.kt`, `AuraAudioProcessor`, `NowPlayingScreen`, `AudioEffectsScreen`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `Song` (e.g. with `.testSongFormattedDuration()` and `.testMinimizeTransitionState()`) actually correct?**
  _`Song` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `AuraPlayerManager` (e.g. with `AudioOutputManager` and `AuraSleepTimer`) actually correct?**
  _`AuraPlayerManager` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Loading`, `Unavailable`, `RECENTLY_ADDED` to the rest of the system?**
  _167 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `audioEngine` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._