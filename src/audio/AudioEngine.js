import { Queue } from '../models/Queue.js';
import { SAMPLE_TRACKS } from './sampleTracks.js';

/**
 * AURA Audio Engine
 * Centralized, production-grade audio manager supporting HTML5 Audio, Web Audio synthesis fallback,
 * MediaSession lock-screen controls, seamless seek, queue orchestration, and pub/sub UI sync.
 */
export class AudioEngine {
  constructor() {
    this.queue = new Queue();
    this.audioElement = new Audio();
    this.audioElement.preload = 'auto';

    // Playback state
    this.playbackState = 'idle'; // 'idle' | 'loading' | 'playing' | 'paused' | 'error'
    this.currentTime = 0;
    this.duration = 0;
    this.volume = 1.0;
    this.isMuted = false;

    // Web Audio Synthesizer (for high-fidelity fallback when playing sample tracks without local audio files)
    this.audioCtx = null;
    this.synthInterval = null;
    this.synthOscs = [];
    this.synthGain = null;
    this.isUsingSynth = false;
    this.synthTime = 0;

    // Pub/Sub Listeners
    this.listeners = {
      statechange: new Set(),
      trackchange: new Set(),
      timeupdate: new Set(),
      modechange: new Set(),
      queuechange: new Set(),
      favoritechange: new Set(),
      error: new Set()
    };

    this._bindAudioEvents();
    this._initMediaSession();

    // Load initial queue with sample tracks
    this.setQueue(SAMPLE_TRACKS, 0);
  }

  /* ========================================================================= */
  /* Event Subscription                                                        */
  /* ========================================================================= */

  subscribe(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event].add(callback);
      // Immediately broadcast initial state
      if (event === 'statechange') callback(this.playbackState);
      if (event === 'trackchange') callback(this.currentTrack, this.queue.currentIndex, this.queue.count);
      if (event === 'modechange') callback({ isShuffle: this.queue.isShuffle, repeatMode: this.queue.repeatMode });
      if (event === 'timeupdate') callback(this.currentTime, this.duration, this.progressPercent, this.formattedRemainingTime);
      if (event === 'queuechange') callback(this.queue);
    }
    return () => this.unsubscribe(event, callback);
  }

  unsubscribe(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event].delete(callback);
    }
  }

  _emit(event, ...args) {
    if (this.listeners[event]) {
      for (const cb of this.listeners[event]) {
        try {
          cb(...args);
        } catch (e) {
          console.error(`Error in listener for ${event}:`, e);
        }
      }
    }
  }

  /* ========================================================================= */
  /* Audio Element & Media Session Bindings                                    */
  /* ========================================================================= */

  _bindAudioEvents() {
    const el = this.audioElement;

    el.addEventListener('play', () => {
      this.playbackState = 'playing';
      this._emit('statechange', 'playing');
      this._updateMediaSessionPlaybackState('playing');
    });

    el.addEventListener('pause', () => {
      this.playbackState = 'paused';
      this._emit('statechange', 'paused');
      this._updateMediaSessionPlaybackState('paused');
    });

    el.addEventListener('ended', () => {
      this._handleTrackEnded();
    });

    el.addEventListener('timeupdate', () => {
      if (!this.isUsingSynth) {
        this.currentTime = el.currentTime;
        this.duration = el.duration || (this.currentTrack ? this.currentTrack.duration : 0);
        this._emit('timeupdate', this.currentTime, this.duration, this.progressPercent, this.formattedRemainingTime);
        this._updateMediaSessionPosition();
      }
    });

    el.addEventListener('loadedmetadata', () => {
      if (!this.isUsingSynth && el.duration && !isNaN(el.duration)) {
        this.duration = el.duration;
        if (this.currentTrack) {
          this.currentTrack.duration = el.duration;
        }
        this._emit('timeupdate', this.currentTime, this.duration, this.progressPercent, this.formattedRemainingTime);
      }
    });

    el.addEventListener('error', (e) => {
      console.warn('HTML5 Audio encountered an issue, falling back to ambient audio engine:', e);
      if (this.currentTrack && !this.currentTrack.fileUri) {
        this._startSynthPlayback();
      } else {
        this.playbackState = 'error';
        this._emit('statechange', 'error');
        this._emit('error', 'Audio playback error');
      }
    });
  }

  _initMediaSession() {
    if (!('mediaSession' in navigator)) return;

    try {
      navigator.mediaSession.setActionHandler('play', () => this.play());
      navigator.mediaSession.setActionHandler('pause', () => this.pause());
      navigator.mediaSession.setActionHandler('previoustrack', () => this.previous());
      navigator.mediaSession.setActionHandler('nexttrack', () => this.next());
      navigator.mediaSession.setActionHandler('seekto', (details) => {
        if (details.seekTime !== undefined) {
          this.seek(details.seekTime);
        }
      });
      navigator.mediaSession.setActionHandler('seekbackward', (details) => {
        this.seek(Math.max(0, this.currentTime - (details.seekOffset || 10)));
      });
      navigator.mediaSession.setActionHandler('seekforward', (details) => {
        this.seek(Math.min(this.duration, this.currentTime + (details.seekOffset || 10)));
      });
    } catch (e) {
      console.warn('Could not register some MediaSession handlers:', e);
    }
  }

  _syncMediaMetadata() {
    if (!('mediaSession' in navigator) || !this.currentTrack) return;

    const track = this.currentTrack;
    navigator.mediaSession.metadata = new MediaMetadata({
      title: track.title,
      artist: track.artist,
      album: track.album,
      artwork: [
        { src: track.artwork, sizes: '512x512', type: 'image/png' }
      ]
    });
  }

  _updateMediaSessionPlaybackState(state) {
    if ('mediaSession' in navigator) {
      navigator.mediaSession.playbackState = state;
    }
  }

  _updateMediaSessionPosition() {
    if ('mediaSession' in navigator && 'setPositionState' in navigator.mediaSession) {
      if (this.duration > 0 && !isNaN(this.duration)) {
        try {
          navigator.mediaSession.setPositionState({
            duration: this.duration,
            playbackRate: 1.0,
            position: Math.min(this.currentTime, this.duration)
          });
        } catch (_) {}
      }
    }
  }

  /* ========================================================================= */
  /* High-Fidelity Generative Ambient Sound Generator (Web Audio API)          */
  /* ========================================================================= */

  _initAudioContext() {
    if (!this.audioCtx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      this.audioCtx = new AudioCtx();
    }
    if (this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
  }

  _startSynthPlayback() {
    this._initAudioContext();
    this.isUsingSynth = true;
    this._stopSynth();

    // Create rich ambient drone matching track vibes
    const ctx = this.audioCtx;
    const masterGain = ctx.createGain();
    masterGain.gain.setValueAtTime(0.01, ctx.currentTime);
    masterGain.gain.exponentialRampToValueAtTime(0.25 * this.volume, ctx.currentTime + 1.2);
    masterGain.connect(ctx.destination);
    this.synthGain = masterGain;

    // Harmonic frequency palette: AURA Cinematic 432Hz ambient chord roots
    const rootFreqs = [216, 270, 324, 432]; // warm chord
    this.synthOscs = rootFreqs.map((freq, i) => {
      const osc = ctx.createOscillator();
      const filter = ctx.createBiquadFilter();
      const panner = ctx.createStereoPanner ? ctx.createStereoPanner() : null;

      osc.type = i % 2 === 0 ? 'sine' : 'triangle';
      osc.frequency.setValueAtTime(freq + (i * 0.4), ctx.currentTime);

      filter.type = 'lowpass';
      filter.frequency.setValueAtTime(450 + (i * 120), ctx.currentTime);

      osc.connect(filter);
      if (panner) {
        panner.pan.setValueAtTime((i % 2 === 0 ? -0.4 : 0.4), ctx.currentTime);
        filter.connect(panner);
        panner.connect(masterGain);
      } else {
        filter.connect(masterGain);
      }

      osc.start();
      return osc;
    });

    this.playbackState = 'playing';
    this._emit('statechange', 'playing');
    this._updateMediaSessionPlaybackState('playing');

    // Smooth second ticker for virtual time
    this.synthInterval = setInterval(() => {
      if (this.playbackState === 'playing') {
        this.currentTime += 0.25;
        if (this.currentTime >= this.duration) {
          this._handleTrackEnded();
        } else {
          this._emit('timeupdate', this.currentTime, this.duration, this.progressPercent, this.formattedRemainingTime);
          this._updateMediaSessionPosition();
        }
      }
    }, 250);
  }

  _stopSynth() {
    if (this.synthInterval) {
      clearInterval(this.synthInterval);
      this.synthInterval = null;
    }
    if (this.synthGain && this.audioCtx) {
      try {
        this.synthGain.gain.setValueAtTime(this.synthGain.gain.value, this.audioCtx.currentTime);
        this.synthGain.gain.linearRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.1);
      } catch (_) {}
    }
    if (this.synthOscs.length > 0) {
      this.synthOscs.forEach(osc => {
        try { osc.stop(); osc.disconnect(); } catch (_) {}
      });
      this.synthOscs = [];
    }
  }

  /* ========================================================================= */
  /* Primary Playback Controls                                                 */
  /* ========================================================================= */

  get currentTrack() {
    return this.queue.currentTrack;
  }

  get progressPercent() {
    if (!this.duration || this.duration <= 0) return 0;
    return Math.min(100, Math.max(0, (this.currentTime / this.duration) * 100));
  }

  get formattedCurrentTime() {
    return this._formatSeconds(this.currentTime);
  }

  get formattedRemainingTime() {
    const remaining = Math.max(0, this.duration - this.currentTime);
    return `-${this._formatSeconds(remaining)}`;
  }

  get formattedDuration() {
    return this._formatSeconds(this.duration);
  }

  _formatSeconds(sec) {
    const s = Math.floor(sec || 0);
    const m = Math.floor(s / 60);
    const rem = s % 60;
    return `${m}:${rem < 10 ? '0' : ''}${rem}`;
  }

  async play(track = null) {
    if (track) {
      const idx = this.queue.tracks.indexOf(track);
      if (idx !== -1) {
        this.queue.currentIndex = idx;
      } else {
        this.queue.addTrack(track);
        this.queue.currentIndex = this.queue.tracks.length - 1;
      }
    }

    const current = this.currentTrack;
    if (!current) return;

    this._syncMediaMetadata();
    this._emit('trackchange', current, this.queue.currentIndex, this.queue.count);

    // If real file URI is available (e.g. from local device or remote audio)
    if (current.fileUri) {
      this._stopSynth();
      this.isUsingSynth = false;
      if (this.audioElement.src !== current.fileUri) {
        this.audioElement.src = current.fileUri;
        this.currentTime = 0;
      }
      try {
        await this.audioElement.play();
      } catch (err) {
        console.warn('Audio play request failed or was interrupted:', err);
      }
    } else {
      // Use AURA generative spatial ambient engine
      this.duration = current.duration || 268;
      this._startSynthPlayback();
    }
  }

  pause() {
    if (this.isUsingSynth) {
      this.playbackState = 'paused';
      this._stopSynth();
      this._emit('statechange', 'paused');
      this._updateMediaSessionPlaybackState('paused');
    } else {
      this.audioElement.pause();
    }
  }

  togglePlayPause() {
    if (this.playbackState === 'playing') {
      this.pause();
    } else {
      this.play();
    }
  }

  next() {
    const nextTrack = this.queue.advanceNext();
    if (nextTrack) {
      this.currentTime = 0;
      this.play();
    } else {
      this.pause();
      this.currentTime = 0;
      this._emit('timeupdate', 0, this.duration, 0, this.formattedRemainingTime);
    }
  }

  previous() {
    // If playing more than 3 seconds in, restart track first
    if (this.currentTime > 3) {
      this.seek(0);
      return;
    }
    const prevTrack = this.queue.advancePrevious();
    if (prevTrack) {
      this.currentTime = 0;
      this.play();
    }
  }

  seek(seconds) {
    const target = Math.max(0, Math.min(seconds, this.duration));
    this.currentTime = target;
    if (this.isUsingSynth) {
      this._emit('timeupdate', this.currentTime, this.duration, this.progressPercent, this.formattedRemainingTime);
      this._updateMediaSessionPosition();
    } else {
      this.audioElement.currentTime = target;
    }
  }

  seekPercent(percent) {
    if (this.duration > 0) {
      const targetSec = (Math.max(0, Math.min(100, percent)) / 100) * this.duration;
      this.seek(targetSec);
    }
  }

  setVolume(val) {
    this.volume = Math.max(0, Math.min(1, val));
    this.audioElement.volume = this.volume;
    if (this.synthGain && this.audioCtx) {
      this.synthGain.gain.setValueAtTime(0.25 * this.volume, this.audioCtx.currentTime);
    }
  }

  toggleShuffle() {
    const active = this.queue.toggleShuffle();
    this._emit('modechange', { isShuffle: active, repeatMode: this.queue.repeatMode });
    return active;
  }

  cycleRepeat() {
    const mode = this.queue.cycleRepeat();
    this._emit('modechange', { isShuffle: this.queue.isShuffle, repeatMode: mode });
    return mode;
  }

  toggleFavorite(track = null) {
    const target = track || this.currentTrack;
    if (target) {
      target.isFavorite = !target.isFavorite;
      this._emit('favoritechange', target.id, target.isFavorite);
    }
  }

  setQueue(tracks, startIndex = 0) {
    this.queue.setTracks(tracks, startIndex);
    this.currentTime = 0;
    this.duration = this.currentTrack ? this.currentTrack.duration : 0;
    this._emit('queuechange', this.queue);
    this._emit('trackchange', this.currentTrack, this.queue.currentIndex, this.queue.count);
    this._emit('timeupdate', 0, this.duration, 0, this.formattedRemainingTime);
  }

  addToQueue(track) {
    this.queue.addTrack(track);
    this._emit('queuechange', this.queue);
  }

  _handleTrackEnded() {
    if (this.queue.repeatMode === 'one') {
      this.seek(0);
      this.play();
    } else {
      this.next();
    }
  }
}

// Global Singleton Instance
export const audioEngine = new AudioEngine();
