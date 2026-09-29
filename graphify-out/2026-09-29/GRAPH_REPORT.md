# Graph Report - Aura  (2026-09-29)

## Corpus Check
- 105 files · ~232,803 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 47 file(s) not represented in the graph (top: .xml 38, (none) 3, .properties 2)

## Summary
- 1683 nodes · 4419 edges · 110 communities (67 shown, 43 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 61 edges (avg confidence: 0.85)
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
- Daos.kt
- EqualizerPreset
- SettingsScreen.kt
- Song
- AuraPlayerManager
- RecentlyPlayedDao
- PlayerViewModel
- CenterDisplayMode
- MusicImportAndLyricsTest.kt
- MusicRepositoryImpl
- SortCriterion
- AuraAudioService.kt
- .withPlayer
- AudioMetadataHelper.kt
- PlaylistRepositoryImpl.kt
- Aura Cinematic Sound — Design System
- SongEntity
- PlaylistRepository
- AuraBackupManager.kt
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
- ReplayGainMode
- rules/graphify.md
- workflows/graphify.md
- app/build.gradle.kts
- repeatmode
- allinclusive
- history
- AuraDatabase.kt
- spatialtracking
- nightlight
- AuraSleepTimer
- AuraPlayerManager.kt
- UserPreferences.kt
- Ponytail Help
- AuraAudioProcessor
- DspEngineTest
- AuraVisualizerView.kt
- SpatialMovementMode
- alignment
- NowPlayingScreen
- AudioEffectsScreen
- AudioEffectsState
- LyricsManager
- BiquadFilter
- PlaylistDao
- SpatialPreset
- LyricsState
- SmartPlaylistType
- SongSortTest
- ponytail-audit/SKILL.md
- ReverbPreset
- SettingsCategory
- abs
- Ponytail Gain
- OnDataCaptureListener
- ponytail-review/SKILL.md
- GestureAxis
- PlayerState
- ponytail-debt/SKILL.md
- AuraAudioService
- AuraFallbackArtwork
- AudioOutputManager
- LibraryViewModel.kt
- Phase4FeaturesTest
- AudioDeviceType
- SearchScreen.kt
- AuraBackupManager
- .attachManualLyrics
- ponytail.md
- folderopen
- defaultrenderersfactory
- graphicslayer
- view

## God Nodes (most connected - your core abstractions)
1. `Song` - 119 edges
2. `PlayerViewModel` - 82 edges
3. `AuraPlayerManager` - 79 edges
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

## Communities (110 total, 43 thin omitted)

### Community 0 - "audioEngine"
Cohesion: 0.05
Nodes (6): audioEngine, SAMPLE_TRACKS, Queue, Track, AudioSync, NavigationController

### Community 3 - "Components.kt"
Cohesion: 0.10
Nodes (34): AnimatedEqualizerBars(), AuraHeader(), AuraScrubber(), Color, Modifier, Color, Modifier, SpatialPositionRadar() (+26 more)

### Community 4 - "Playlist"
Cohesion: 0.15
Nodes (20): Album, Artist, FolderGroup, Genre, Playlist, PlaylistSelectionDialog(), ContextMenuItem(), androidx (+12 more)

### Community 5 - "LibraryScreen.kt"
Cohesion: 0.11
Nodes (33): arrowback, aspectratio, asyncimage, auraprimarycontainer, aurasurfacecontainerhighest, aurasurfacecontainerlow, aurasurfacecontainerlowest, autoawesome (+25 more)

### Community 6 - "NowPlayingScreen.kt"
Cohesion: 0.05
Nodes (69): activityresultcontracts, alpha, AuraMotion, auraPressable(), auraShimmer(), Color, Modifier, animatable (+61 more)

### Community 7 - "SongSortOrder"
Cohesion: 0.11
Nodes (16): SongSortOrder, ALBUM_AZ, ALBUM_ZA, ARTIST_AZ, ARTIST_ZA, DATE_ADDED, DATE_ADDED_OLD, DURATION_ASC (+8 more)

### Community 9 - "Daos.kt"
Cohesion: 0.08
Nodes (11): com, Flow, ListeningHistoryDao, LyricsCacheDao, SongRatingDao, dao, insert, onconflictstrategy (+3 more)

### Community 10 - "EqualizerPreset"
Cohesion: 0.12
Nodes (15): EqualizerPreset, ACOUSTIC, BASS, BASS_BOOST, CLASSICAL, CUSTOM, DANCE, DEEP (+7 more)

### Community 11 - "SettingsScreen.kt"
Cohesion: 0.06
Nodes (46): add, barchart, bedtime, card, carddefaults, check, chevronright, circleshape (+38 more)

### Community 12 - "Song"
Cohesion: 0.06
Nodes (7): Song, com, Flow, Uri, MusicRepository, MetadataEditorDialog(), EntityMappingTest

### Community 13 - "AuraPlayerManager"
Cohesion: 0.11
Nodes (3): AuraPlayerManager, FloatArray, ParametricBand

### Community 14 - "RecentlyPlayedDao"
Cohesion: 0.11
Nodes (6): RecentlyPlayedDao, RecentlyPlayedEntity, FakeFavoriteDao, FakeRecentlyPlayedDao, Flow, MusicRepositoryTest

### Community 15 - "PlayerViewModel"
Cohesion: 0.05
Nodes (4): FloatArray, StateFlow, ViewModel, PlayerViewModel

### Community 16 - "CenterDisplayMode"
Cohesion: 0.50
Nodes (4): CenterDisplayMode, ARTWORK, LYRICS, VISUALIZER

### Community 17 - "MusicImportAndLyricsTest.kt"
Cohesion: 0.14
Nodes (16): SleepTimerTest, assertequals, assertfalse, assertnotequals, assertnotnull, assertnull, asserttrue, before (+8 more)

### Community 18 - "MusicRepositoryImpl"
Cohesion: 0.06
Nodes (11): AudioSourceNormalizer, Context, Uri, NormalizedAudioSource, SongRatingEntity, DatabaseIntegrityReport, DiscoveredTrack, Flow (+3 more)

### Community 19 - "SortCriterion"
Cohesion: 0.12
Nodes (16): SortCriterion, ALBUM, ALBUM_ARTIST, ARTIST, DATE_ADDED, DURATION, FILE_SIZE, GENRE (+8 more)

### Community 20 - "AuraAudioService.kt"
Cohesion: 0.12
Nodes (16): DefaultRenderersFactory, Context, audioattributes, AudioSink, bundle, commandbutton, defaultaudiosink, defaultextractorsfactory (+8 more)

### Community 22 - "AudioMetadataHelper.kt"
Cohesion: 0.19
Nodes (8): AudioMetadataHelper, AudioTechnicalDetails, Context, Uri, AudioFormatSupportTest, mediaextractor, mediaformat, mediametadataretriever

### Community 23 - "PlaylistRepositoryImpl.kt"
Cohesion: 0.20
Nodes (4): Flow, PlaylistRepositoryImpl, combine, map

### Community 24 - "Aura Cinematic Sound — Design System"
Cohesion: 0.17
Nodes (11): 1. Brand & Style, 2. Color Palette & Surface Tokens, 3. Typography (Plus Jakarta Sans), 4. Spacing & Border Radii, 5. Screen Manifest, Accents & Spectrum, Aura Cinematic Sound — Design System, Border Radii (+3 more)

### Community 25 - "SongEntity"
Cohesion: 0.10
Nodes (3): SongDao, SongEntity, FakeSongDao

### Community 27 - "AuraBackupManager.kt"
Cohesion: 0.14
Nodes (13): MetadataEditor, StatisticsEngine, ListeningStatistics, bufferedreader, dispatchers, first, httpurlconnection, inputstreamreader (+5 more)

### Community 29 - "MusicRepositoryImpl.kt"
Cohesion: 0.19
Nodes (13): LyricsParser, audiomanager, build, contenturis, context, documentfile, environment, file (+5 more)

### Community 30 - "Listener"
Cohesion: 0.18
Nodes (4): Listener, androidx, Listener, Player

### Community 33 - "build_app.py"
Cohesion: 0.25
Nodes (4): json, os, re, urllib_request

### Community 34 - "AuraWidgetManager.kt"
Cohesion: 0.06
Nodes (43): Context, PlaybackState, ArtworkColorExtractor, ArtworkPalette, Context, AuraWidgetManager, AppWidgetManager, Bundle (+35 more)

### Community 35 - "MainActivity.kt"
Cohesion: 0.08
Nodes (33): android, Bundle, MainActivity, Factory, Factory, AuraColorScheme, auraDarkColorScheme(), auraLightColorScheme() (+25 more)

### Community 36 - "QueueDao"
Cohesion: 0.18
Nodes (3): QueueDao, QueueEntity, FakeQueueDao

### Community 37 - "EqualizerSettings"
Cohesion: 0.21
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

### Community 41 - "ReplayGainMode"
Cohesion: 0.50
Nodes (4): ReplayGainMode, ALBUM, OFF, TRACK

### Community 50 - "AuraDatabase.kt"
Cohesion: 0.07
Nodes (17): FavoriteDao, MusicFolderDao, AuraDatabase, Context, FavoriteEntity, ListeningHistoryEntity, LyricsCacheEntity, MusicFolderEntity (+9 more)

### Community 53 - "AuraSleepTimer"
Cohesion: 0.36
Nodes (3): AuraSleepTimer, Job, StateFlow

### Community 54 - "AuraPlayerManager.kt"
Cohesion: 0.14
Nodes (23): AuraPlayerSingleton, Job, ListenableFuture, StateFlow, AudioProfile, BalanceSettings, CompressorSettings, LimiterSettings (+15 more)

### Community 55 - "UserPreferences.kt"
Cohesion: 0.09
Nodes (29): Flow, UserPreferences, ReplayGainSettings, AnimationSettings, AppearanceSettings, BluetoothGestureSettings, GesturesSettings, HapticSettings (+21 more)

### Community 56 - "Ponytail Help"
Cohesion: 0.25
Nodes (7): Configure Default Mode, Deactivate, Levels, More, Ponytail Help, Skills, Update

### Community 57 - "AuraAudioProcessor"
Cohesion: 0.18
Nodes (9): AuraAudioProcessor, FloatArray, AudioProcessor, BaseAudioProcessor, ByteBuffer, byteorder, c, optin (+1 more)

### Community 58 - "DspEngineTest"
Cohesion: 0.11
Nodes (4): DynamicCompressor, PeakLimiter, Spatial4DEngine, DspEngineTest

### Community 59 - "AuraVisualizerView.kt"
Cohesion: 0.18
Nodes (13): VisualizerStyle, BARS, CIRCULAR, MINIMAL, SPECTRUM, WAVEFORM, AuraVisualizerView(), Color (+5 more)

### Community 60 - "SpatialMovementMode"
Cohesion: 0.16
Nodes (13): SpatialMovementMode, CUSTOM, FAST_ORBIT, MEDIUM_ORBIT, SLOW_ORBIT, STATIC, atan2, cos (+5 more)

### Community 61 - "alignment"
Cohesion: 0.24
Nodes (10): alignment, Color, Dp, Modifier, StarRatingBar(), icons, row, size (+2 more)

### Community 62 - "NowPlayingScreen"
Cohesion: 0.09
Nodes (29): PlaybackStatus, BUFFERING, ENDED, ERROR, IDLE, PAUSED, PLAYING, RepeatMode (+21 more)

### Community 63 - "AudioEffectsScreen"
Cohesion: 0.25
Nodes (11): AudioEffectsScreen(), DynamicsTab(), EffectSectionCard(), EqAndToneTab(), com, FloatArray, Modifier, OutputProfileTab() (+3 more)

### Community 64 - "AudioEffectsState"
Cohesion: 0.25
Nodes (7): AuraHardwareEffectsManager, AudioEffectsState, BassBoost, Equalizer, LoudnessEnhancer, PresetReverb, Virtualizer

### Community 66 - "BiquadFilter"
Cohesion: 0.13
Nodes (11): BiquadFilter, Coefficients, FilterType, HIGH_PASS, HIGH_SHELF, LOW_PASS, LOW_SHELF, PEAKING (+3 more)

### Community 67 - "PlaylistDao"
Cohesion: 0.08
Nodes (5): PlaylistDao, PlaylistEntity, PlaylistSongCrossRef, FakePlaylistDao, Flow

### Community 68 - "SpatialPreset"
Cohesion: 0.18
Nodes (10): SpatialPreset, CINEMA, CONCERT, FOUR_D, FOUR_D_FAST, FOUR_D_SLOW, NORMAL, STUDIO (+2 more)

### Community 69 - "LyricsState"
Cohesion: 0.16
Nodes (8): LrcLibLyricsProvider, Loading, LyricLine, LyricsState, Success, Unavailable, LyricsSyncTest, LyricsTest

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
Cohesion: 0.36
Nodes (6): abs, exp, log10, max, pow, tanh

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
Cohesion: 0.16
Nodes (19): AuraAnimatedFavoriteButton(), AuraAnimatedPlayPauseButton(), AuraFallbackArtwork(), Color, Dp, Modifier, MiniPlayer(), HomeScreen() (+11 more)

### Community 86 - "AudioOutputManager"
Cohesion: 0.24
Nodes (5): AudioOutputManager, AudioDeviceCallback, Quad, AudioOutputInfo, AudioDeviceInfo

### Community 87 - "LibraryViewModel.kt"
Cohesion: 0.18
Nodes (8): SearchResults, StateFlow, ViewModel, flatmaplatest, flowof, sharingstarted, statein, viewmodelscope

### Community 89 - "AudioDeviceType"
Cohesion: 0.22
Nodes (7): AudioDeviceType, BLUETOOTH_HEADPHONES, BLUETOOTH_SPEAKER, OTHER, PHONE_SPEAKER, USB_DAC, WIRED_HEADPHONES

### Community 90 - "SearchScreen.kt"
Cohesion: 0.09
Nodes (50): album, alertdialog, ImageVector, Modifier, StatisticsScreen(), StatMetricCard(), TopRankItem(), auraonprimary (+42 more)

## Knowledge Gaps
- **167 isolated node(s):** `Loading`, `Unavailable`, `RECENTLY_ADDED`, `RECENTLY_PLAYED`, `MOST_PLAYED` (+162 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 504 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Song` connect `Song` to `Playlist`, `LibraryScreen.kt`, `NowPlayingScreen.kt`, `QueueState`, `AuraPlayerManager`, `MusicImportAndLyricsTest.kt`, `MusicRepositoryImpl`, `.withPlayer`, `PlaylistRepositoryImpl.kt`, `SongEntity`, `PlaylistRepository`, `AuraBackupManager.kt`, `LibraryViewModel`, `MusicRepositoryImpl.kt`, `AuraWidgetManager.kt`, `QueueDao`, `AuraDatabase.kt`, `AuraPlayerManager.kt`, `NowPlayingScreen`, `LyricsManager`, `LyricsState`, `SongSortTest`, `.clearLyricsCache`, `PlayerState`, `AuraFallbackArtwork`, `AudioOutputManager`, `LibraryViewModel.kt`, `Phase4FeaturesTest`, `SearchScreen.kt`?**
  _High betweenness centrality (0.168) - this node is a cross-community bridge._
- **Why does `PlayerViewModel` connect `PlayerViewModel` to `NowPlayingScreen.kt`, `QueueState`, `EqualizerPreset`, `SettingsScreen.kt`, `Song`, `AuraWidgetManager.kt`, `MainActivity.kt`, `AuraPlayerManager.kt`, `AudioEffectsScreen`, `AudioEffectsState`, `SpatialPreset`, `LyricsState`, `.setParametricBands`, `.clearLyricsCache`, `PlayerState`, `AuraFallbackArtwork`, `AudioDeviceType`, `.removeParametricBand`, `.resetEqualizer`, `.seekPercent`, `.attachManualLyrics`, `.setAutoHeadroomEnabled`, `.setCompressor`, `.setEqualizerPreamp`, `.setReplayGain`, `.setReverb`, `.setSpatial4DPreview`, `.setVirtualizer`, `.setVisualizerSettings`, `.toggleShuffle`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Why does `AuraPlayerManager` connect `AuraPlayerManager` to `AudioEffectsState`, `AuraWidgetManager.kt`, `EqualizerSettings`, `QueueState`, `Song`, `.withPlayer`, `AudioOutputManager`, `AuraPlayerManager.kt`, `AuraSleepTimer`, `AudioDeviceType`, `UserPreferences.kt`, `Listener`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `Song` (e.g. with `.testSongFormattedDuration()` and `.testMinimizeTransitionState()`) actually correct?**
  _`Song` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `AuraPlayerManager` (e.g. with `AudioOutputManager` and `AuraSleepTimer`) actually correct?**
  _`AuraPlayerManager` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Loading`, `Unavailable`, `RECENTLY_ADDED` to the rest of the system?**
  _167 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `audioEngine` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._