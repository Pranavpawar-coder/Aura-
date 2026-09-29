import { audioEngine } from '../audio/AudioEngine.js';

/**
 * AURA Audio UI Synchronizer
 * Binds Stitch UI elements (Now Playing, Mini Player, Home track cards, Library, Queue)
 * to the centralized AudioEngine state.
 */
export class AudioSync {
  constructor(root = document) {
    this.root = root;
    this.isDraggingScrubber = false;
    this._initBindings();
  }

  _initBindings() {
    this._bindTransportControls();
    this._bindScrubber();
    this._bindShuffleRepeat();
    this._bindFavorite();
    this._bindTrackCards();

    // Subscribe to AudioEngine events
    audioEngine.subscribe('statechange', (state) => this._onStateChange(state));
    audioEngine.subscribe('trackchange', (track, idx, total) => this._onTrackChange(track, idx, total));
    audioEngine.subscribe('timeupdate', (cur, dur, pct, rem) => this._onTimeUpdate(cur, dur, pct, rem));
    audioEngine.subscribe('modechange', (mode) => this._onModeChange(mode));
    audioEngine.subscribe('favoritechange', (id, fav) => this._onFavoriteChange(id, fav));
  }

  /* ------------------------------------------------------------------------- */
  /* Transport Controls (Play, Pause, Next, Prev)                              */
  /* ------------------------------------------------------------------------- */

  _bindTransportControls() {
    // 1. Now Playing Master Play/Pause
    const masterPlayBtn = this.root.getElementById('masterPlayBtn');
    if (masterPlayBtn) {
      masterPlayBtn.onclick = (e) => {
        e.preventDefault();
        e.stopPropagation();
        audioEngine.togglePlayPause();
      };
    }

    // 2. Mini Player Play/Pause buttons
    const miniPlayBtns = this.root.querySelectorAll('[aria-label="Play or Pause"], .mini-play-btn');
    miniPlayBtns.forEach(btn => {
      btn.onclick = (e) => {
        e.preventDefault();
        e.stopPropagation();
        audioEngine.togglePlayPause();
      };
    });

    // 3. Next Track buttons
    const nextBtns = this.root.querySelectorAll('[aria-label="Next track"], .btn-next-track');
    nextBtns.forEach(btn => {
      btn.onclick = (e) => {
        e.preventDefault();
        e.stopPropagation();
        audioEngine.next();
      };
    });

    // 4. Previous Track buttons
    const prevBtns = this.root.querySelectorAll('[aria-label="Previous track"], .btn-prev-track');
    prevBtns.forEach(btn => {
      btn.onclick = (e) => {
        e.preventDefault();
        e.stopPropagation();
        audioEngine.previous();
      };
    });
  }

  /* ------------------------------------------------------------------------- */
  /* Interactive Scrubber & Timeline                                           */
  /* ------------------------------------------------------------------------- */

  _bindScrubber() {
    const scrubberContainer = this.root.getElementById('progressBarContainer');
    if (!scrubberContainer) return;

    const handleSeek = (e) => {
      const rect = scrubberContainer.getBoundingClientRect();
      const clientX = e.touches ? e.touches[0].clientX : e.clientX;
      const x = Math.max(0, Math.min(clientX - rect.left, rect.width));
      const percent = (x / rect.width) * 100;
      audioEngine.seekPercent(percent);
    };

    scrubberContainer.addEventListener('mousedown', (e) => {
      this.isDraggingScrubber = true;
      handleSeek(e);
      const onMouseMove = (ev) => {
        if (this.isDraggingScrubber) handleSeek(ev);
      };
      const onMouseUp = () => {
        this.isDraggingScrubber = false;
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
      };
      window.addEventListener('mousemove', onMouseMove);
      window.addEventListener('mouseup', onMouseUp);
    });

    scrubberContainer.addEventListener('touchstart', (e) => {
      this.isDraggingScrubber = true;
      handleSeek(e);
    }, { passive: true });

    scrubberContainer.addEventListener('touchmove', (e) => {
      if (this.isDraggingScrubber) handleSeek(e);
    }, { passive: true });

    scrubberContainer.addEventListener('touchend', () => {
      this.isDraggingScrubber = false;
    });
  }

  /* ------------------------------------------------------------------------- */
  /* Shuffle & Repeat                                                          */
  /* ------------------------------------------------------------------------- */

  _bindShuffleRepeat() {
    const shuffleBtn = this.root.getElementById('shuffleBtn');
    if (shuffleBtn) {
      shuffleBtn.onclick = (e) => {
        e.preventDefault();
        audioEngine.toggleShuffle();
      };
    }

    const repeatBtn = this.root.getElementById('repeatBtn');
    if (repeatBtn) {
      repeatBtn.onclick = (e) => {
        e.preventDefault();
        audioEngine.cycleRepeat();
      };
    }
  }

  /* ------------------------------------------------------------------------- */
  /* Favorite Button                                                           */
  /* ------------------------------------------------------------------------- */

  _bindFavorite() {
    const favBtns = this.root.querySelectorAll('#favoriteBtn, [aria-label="Add to favorites"], [aria-label="Like this track"]');
    favBtns.forEach(btn => {
      btn.onclick = (e) => {
        e.preventDefault();
        e.stopPropagation();
        audioEngine.toggleFavorite();
      };
    });
  }

  /* ------------------------------------------------------------------------- */
  /* Track Cards & Library Items                                               */
  /* ------------------------------------------------------------------------- */

  _bindTrackCards() {
    // Find track item rows across home / library screens
    const trackRows = this.root.querySelectorAll('.track-row, [data-track-id], .cursor-pointer');
    trackRows.forEach(row => {
      const titleEl = row.querySelector('.font-title-md, h2, h3');
      const text = titleEl ? titleEl.textContent.trim().toLowerCase() : '';
      
      // Match with sample tracks
      const matched = audioEngine.queue.tracks.find(t => 
        t.title.toLowerCase().includes(text) || text.includes(t.title.toLowerCase())
      );

      if (matched) {
        row.addEventListener('click', (e) => {
          // If clicking menu button inside row, don't trigger track change
          if (e.target.closest('button')) return;
          audioEngine.play(matched);
        });
      }
    });
  }

  /* ------------------------------------------------------------------------- */
  /* Engine Event Handlers                                                     */
  /* ------------------------------------------------------------------------- */

  _onStateChange(state) {
    const isPlaying = state === 'playing';

    // 1. Update Master Play Icon
    const masterIcon = this.root.getElementById('masterPlayIcon');
    if (masterIcon) {
      masterIcon.textContent = isPlaying ? 'pause' : 'play_arrow';
    }

    // 2. Update Mini Player Icons
    const miniIcons = this.root.querySelectorAll('[aria-label="Play or Pause"] span, .mini-play-btn span');
    miniIcons.forEach(icon => {
      icon.textContent = isPlaying ? 'pause_circle' : 'play_circle';
    });

    // 3. Update Ambient Halo & Artwork Glow
    const halo = this.root.getElementById('ambientHalo');
    const reflection = this.root.getElementById('artworkReflection');
    if (halo) {
      if (isPlaying) {
        halo.classList.remove('opacity-30', 'scale-90');
        halo.classList.add('opacity-100', 'scale-100');
      } else {
        halo.classList.add('opacity-30', 'scale-90');
        halo.classList.remove('opacity-100', 'scale-100');
      }
    }
    if (reflection) {
      if (isPlaying) {
        reflection.classList.remove('opacity-10');
        reflection.classList.add('opacity-30');
      } else {
        reflection.classList.remove('opacity-30');
        reflection.classList.add('opacity-10');
      }
    }

    // 4. Equalizer animated bars
    const eqBars = this.root.querySelectorAll('.animate-bounce, .animate-pulse');
    eqBars.forEach(bar => {
      bar.style.animationPlayState = isPlaying ? 'running' : 'paused';
    });
  }

  _onTrackChange(track, index, total) {
    if (!track) return;

    // 1. Now Playing Screen Text
    const npTitle = this.root.querySelector('#nowPlayingTitle, .font-headline-xl-mobile');
    if (npTitle && npTitle.textContent !== track.title) {
      npTitle.textContent = track.title;
    }

    const npArtist = this.root.querySelector('#nowPlayingArtist, .font-body-lg');
    if (npArtist && npArtist.textContent !== track.artist) {
      npArtist.textContent = track.artist;
    }

    const npAlbum = this.root.querySelector('#nowPlayingAlbum, [aria-label="Playing from Album"] + span');
    if (npAlbum) {
      npAlbum.textContent = track.album;
    }

    // 2. Artwork image
    const coverImg = this.root.getElementById('albumCoverImg');
    if (coverImg && track.artwork) {
      coverImg.src = track.artwork;
      coverImg.alt = `${track.title} Cover Art`;
    }

    // 3. Mini Player Text & Art
    const miniTitles = this.root.querySelectorAll('.mini-track-title, aside .font-title-md');
    miniTitles.forEach(el => el.textContent = track.title);

    const miniArtists = this.root.querySelectorAll('.mini-track-artist, aside .font-label-sm');
    miniArtists.forEach(el => el.textContent = track.artist);

    // 4. Queue / Up Next info
    const upNext = audioEngine.queue.nextTrack;
    const upNextEl = this.root.querySelector('#upNextText, [aria-label="Next In Queue"] span, .text-on-surface-variant.truncate');
    if (upNextEl && upNext) {
      upNextEl.textContent = `${upNext.title} • ${upNext.artist}`;
    }

    // 5. Update Favorite Heart
    this._onFavoriteChange(track.id, track.isFavorite);
  }

  _onTimeUpdate(currentTime, duration, percent, remainingTime) {
    if (this.isDraggingScrubber) return;

    // 1. Scrubber fill width
    const fill = this.root.getElementById('progressFill');
    if (fill) {
      fill.style.width = `${percent}%`;
    }

    // 2. Scrubber thumb position
    const thumb = this.root.getElementById('scrubberThumb');
    if (thumb) {
      thumb.style.left = `${percent}%`;
    }

    // 3. Mini Player hairline progress
    const miniBar = this.root.querySelector('aside .h-full.bg-primary, .mini-progress-fill');
    if (miniBar) {
      miniBar.style.width = `${percent}%`;
    }

    // 4. Timestamp displays
    const curTimeEl = this.root.getElementById('currentTime');
    if (curTimeEl) {
      curTimeEl.textContent = audioEngine.formattedCurrentTime;
    }

    const remTimeEl = this.root.getElementById('remainingTime');
    if (remTimeEl) {
      remTimeEl.textContent = remainingTime;
    }
  }

  _onModeChange({ isShuffle, repeatMode }) {
    // 1. Shuffle Button Style
    const shuffleBtn = this.root.getElementById('shuffleBtn');
    if (shuffleBtn) {
      const dot = shuffleBtn.querySelector('span.rounded-full');
      if (isShuffle) {
        shuffleBtn.classList.add('text-primary');
        shuffleBtn.classList.remove('text-on-surface-variant');
        if (dot) dot.classList.remove('hidden');
      } else {
        shuffleBtn.classList.remove('text-primary');
        shuffleBtn.classList.add('text-on-surface-variant');
        if (dot) dot.classList.add('hidden');
      }
    }

    // 2. Repeat Button Style
    const repeatBtn = this.root.getElementById('repeatBtn');
    if (repeatBtn) {
      const icon = repeatBtn.querySelector('.material-symbols-outlined');
      if (repeatMode === 'off') {
        repeatBtn.classList.remove('text-primary');
        repeatBtn.classList.add('text-on-surface-variant');
        if (icon) icon.textContent = 'repeat';
      } else if (repeatMode === 'all') {
        repeatBtn.classList.add('text-primary');
        repeatBtn.classList.remove('text-on-surface-variant');
        if (icon) icon.textContent = 'repeat';
      } else if (repeatMode === 'one') {
        repeatBtn.classList.add('text-primary');
        repeatBtn.classList.remove('text-on-surface-variant');
        if (icon) icon.textContent = 'repeat_one';
      }
    }
  }

  _onFavoriteChange(trackId, isFavorite) {
    const heartBtns = this.root.querySelectorAll('#favoriteBtn, [aria-label="Add to favorites"], [aria-label="Like this track"]');
    heartBtns.forEach(btn => {
      const icon = btn.querySelector('.material-symbols-outlined');
      if (isFavorite) {
        btn.classList.add('text-primary');
        btn.classList.remove('text-outline', 'text-on-surface-variant');
        if (icon) icon.style.fontVariationSettings = "'FILL' 1";
      } else {
        btn.classList.remove('text-primary');
        btn.classList.add('text-outline');
        if (icon) icon.style.fontVariationSettings = "'FILL' 0";
      }
    });
  }
}
