import { Track } from '../models/Track.js';

export const SAMPLE_TRACKS = [
  new Track({
    id: 'track_resonance',
    title: 'Resonance',
    artist: 'Kaelen Vance',
    album: 'Resonance',
    duration: 268, // 4:28
    artwork: 'assets/album_resonance.png',
    genre: 'Cinematic Ambient',
    quality: '24-BIT / 96kHZ LOSSLESS',
    isFavorite: true,
    fileUri: '',
    lyrics: [
      { time: 0, text: "Fractured starlight pulls the gravity away" },
      { time: 14, text: "Before the silence broke, you spoke in oscillations" },
      { time: 28, text: "Where celestial oceans fold into the twilight" },
      { time: 42, text: "We found the frequency where memories ignite" },
      { time: 56, text: "A resonant drift across the velvet boundary" },
      { time: 70, text: "Echoes of starlight suspended in the air" },
      { time: 84, text: "Through holographic rings and deep purple stardust" },
      { time: 105, text: "We remain in harmony, weightless in sound" }
    ]
  }),
  new Track({
    id: 'track_obsidian_mist',
    title: 'Obsidian Mist',
    artist: 'Nocturne Echo',
    album: 'Velvet Eclipse',
    duration: 235, // 3:55
    artwork: 'assets/album_velvet_eclipse.png',
    genre: 'Nocturne Electronic',
    quality: 'HI-RES FLAC',
    isFavorite: false,
    fileUri: '',
    lyrics: [
      { time: 0, text: "Indigo currents swirling in the dark" },
      { time: 16, text: "Liquid obsidian reflecting silver sparks" },
      { time: 32, text: "A silent whisper through the midnight haze" }
    ]
  }),
  new Track({
    id: 'track_corona_radiance',
    title: 'Corona Radiance',
    artist: 'Lyra Bloom',
    album: 'Solar Flare',
    duration: 364, // 6:04
    artwork: 'assets/album_solar_flare.png',
    genre: 'Warm Acoustic Drift',
    quality: 'FLAC 24-BIT',
    isFavorite: false,
    fileUri: '',
    lyrics: [
      { time: 0, text: "Radiant warmth descending from the golden sphere" },
      { time: 20, text: "Solar winds breathing life into the atmosphere" }
    ]
  }),
  new Track({
    id: 'track_neon_horizons',
    title: 'Neon Horizons',
    artist: 'Cyberpulse',
    album: 'Neon Horizons',
    duration: 298, // 4:58
    artwork: 'assets/album_neon_horizons.png',
    genre: 'Synthwave & Cyberpunk',
    quality: 'HI-RES MASTER',
    isFavorite: true,
    fileUri: '',
    lyrics: [
      { time: 0, text: "Dusk over high towers, amber neon glows" },
      { time: 18, text: "Synth lines running through the city street flows" }
    ]
  })
];
