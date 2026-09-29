import { audioEngine } from './audio/AudioEngine.js';
import { AudioSync } from './ui/audioSync.js';
import { NavigationController } from './ui/navigation.js';
import { Track } from './models/Track.js';

/**
 * AURA Application Core
 * Phase 1 Audio Engine Initialization & Screen Synchronization
 */
document.addEventListener('DOMContentLoaded', () => {
  console.log('⚡ Initializing AURA Audiophile Engine (Phase 1)...');

  // Initialize Audio UI Sync
  const audioSync = new AudioSync(document);

  // Initialize Screen Navigation
  const nowPlayingModal = document.getElementById('nowPlayingModal');
  const lyricsModal = document.getElementById('lyricsModal');
  const screensContainer = document.getElementById('screensContainer');

  const navigation = new NavigationController({
    screensContainer,
    nowPlayingModal,
    lyricsModal,
    activeTab: 'home'
  });

  // Local Audio File Picker (Preparatory for Phase 2, functional right now)
  const localAudioInput = document.getElementById('localAudioInput');
  if (localAudioInput) {
    localAudioInput.addEventListener('change', (e) => {
      const files = Array.from(e.target.files);
      if (files.length === 0) return;

      const newTracks = files.map(file => {
        const url = URL.createObjectURL(file);
        return Track.fromFile(file, url);
      });

      console.log(`Loaded ${newTracks.length} local audio files into AURA.`);
      audioEngine.setQueue(newTracks, 0);
      audioEngine.play();
    });
  }

  // Live Lyrics Sync Handler
  audioEngine.subscribe('timeupdate', (currentTime) => {
    const curTrack = audioEngine.currentTrack;
    if (!curTrack || !curTrack.lyrics || curTrack.lyrics.length === 0) return;

    const lyricsContainer = document.getElementById('lyricsLinesContainer');
    if (!lyricsContainer) return;

    const lines = lyricsContainer.querySelectorAll('.lyric-line');
    let activeIndex = -1;

    curTrack.lyrics.forEach((line, idx) => {
      if (currentTime >= line.time) {
        activeIndex = idx;
      }
    });

    lines.forEach((el, idx) => {
      const p = el.querySelector('p');
      if (idx === activeIndex) {
        el.className = 'relative p-space-md rounded-lg bg-surface-container-low/70 backdrop-blur-2xl shadow-2xl shadow-primary-container/10 transition-all duration-500 lyric-line';
        if (p) {
          p.className = 'font-headline-md text-headline-md font-bold text-transparent bg-clip-text bg-gradient-to-r from-primary via-tertiary-fixed to-secondary leading-relaxed';
        }
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (idx < activeIndex) {
        el.className = 'flex items-start gap-space-sm opacity-30 filter blur-[0.4px] transition-all duration-700 lyric-line';
        if (p) {
          p.className = 'font-headline-md text-headline-md text-on-surface-variant leading-relaxed';
        }
      } else {
        el.className = 'flex items-start gap-space-sm opacity-50 transition-all duration-700 lyric-line';
        if (p) {
          p.className = 'font-headline-md text-headline-md text-on-surface leading-relaxed';
        }
      }
    });
  });

  // Expose global test interface for inspection
  window.aura = {
    audioEngine,
    audioSync,
    navigation
  };

  console.log('✅ AURA Engine online. Centralized player state active across all screens.');
});
