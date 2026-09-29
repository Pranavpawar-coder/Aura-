/**
 * AURA Audio Queue Model
 * Handles track sequencing, Fisher-Yates shuffle, and repeat modes ('off' | 'all' | 'one').
 */
export class Queue {
  constructor() {
    this.tracks = [];
    this.currentIndex = -1;
    this.isShuffle = false;
    this.repeatMode = 'off'; // 'off' | 'all' | 'one'
    this.shuffledOrder = [];
  }

  setTracks(tracks, startIndex = 0) {
    this.tracks = [...tracks];
    this.currentIndex = Math.max(0, Math.min(startIndex, this.tracks.length - 1));
    this._rebuildShuffleOrder();
    return this.currentTrack;
  }

  get currentTrack() {
    if (this.currentIndex >= 0 && this.currentIndex < this.tracks.length) {
      return this.tracks[this.currentIndex];
    }
    return null;
  }

  get count() {
    return this.tracks.length;
  }

  get nextTrack() {
    if (this.tracks.length === 0) return null;

    if (this.repeatMode === 'one') {
      return this.currentTrack;
    }

    if (this.isShuffle) {
      const pos = this.shuffledOrder.indexOf(this.currentIndex);
      if (pos !== -1 && pos + 1 < this.shuffledOrder.length) {
        return this.tracks[this.shuffledOrder[pos + 1]];
      }
      if (this.repeatMode === 'all' && this.shuffledOrder.length > 0) {
        return this.tracks[this.shuffledOrder[0]];
      }
      return null;
    }

    if (this.currentIndex + 1 < this.tracks.length) {
      return this.tracks[this.currentIndex + 1];
    }
    if (this.repeatMode === 'all') {
      return this.tracks[0];
    }
    return null;
  }

  advanceNext() {
    if (this.tracks.length === 0) return null;

    if (this.repeatMode === 'one') {
      return this.currentTrack;
    }

    if (this.isShuffle) {
      const pos = this.shuffledOrder.indexOf(this.currentIndex);
      if (pos !== -1 && pos + 1 < this.shuffledOrder.length) {
        this.currentIndex = this.shuffledOrder[pos + 1];
        return this.currentTrack;
      }
      if (this.repeatMode === 'all' && this.shuffledOrder.length > 0) {
        this.currentIndex = this.shuffledOrder[0];
        return this.currentTrack;
      }
      return null;
    }

    if (this.currentIndex + 1 < this.tracks.length) {
      this.currentIndex += 1;
      return this.currentTrack;
    }
    if (this.repeatMode === 'all') {
      this.currentIndex = 0;
      return this.currentTrack;
    }
    return null;
  }

  advancePrevious() {
    if (this.tracks.length === 0) return null;

    if (this.repeatMode === 'one') {
      return this.currentTrack;
    }

    if (this.isShuffle) {
      const pos = this.shuffledOrder.indexOf(this.currentIndex);
      if (pos > 0) {
        this.currentIndex = this.shuffledOrder[pos - 1];
        return this.currentTrack;
      }
      if (this.repeatMode === 'all' && this.shuffledOrder.length > 0) {
        this.currentIndex = this.shuffledOrder[this.shuffledOrder.length - 1];
        return this.currentTrack;
      }
      return this.currentTrack;
    }

    if (this.currentIndex > 0) {
      this.currentIndex -= 1;
      return this.currentTrack;
    }
    if (this.repeatMode === 'all') {
      this.currentIndex = this.tracks.length - 1;
      return this.currentTrack;
    }
    return this.tracks[0];
  }

  toggleShuffle() {
    this.isShuffle = !this.isShuffle;
    if (this.isShuffle) {
      this._rebuildShuffleOrder();
    }
    return this.isShuffle;
  }

  cycleRepeat() {
    const modes = ['off', 'all', 'one'];
    const nextIdx = (modes.indexOf(this.repeatMode) + 1) % modes.length;
    this.repeatMode = modes[nextIdx];
    return this.repeatMode;
  }

  addTrack(track) {
    this.tracks.push(track);
    this._rebuildShuffleOrder();
  }

  playTrackAt(index) {
    if (index >= 0 && index < this.tracks.length) {
      this.currentIndex = index;
      return this.currentTrack;
    }
    return null;
  }

  removeTrack(index) {
    if (index >= 0 && index < this.tracks.length) {
      this.tracks.splice(index, 1);
      if (this.currentIndex >= this.tracks.length) {
        this.currentIndex = this.tracks.length - 1;
      }
      this._rebuildShuffleOrder();
    }
  }

  clear() {
    this.tracks = [];
    this.currentIndex = -1;
    this.shuffledOrder = [];
  }

  _rebuildShuffleOrder() {
    const indices = this.tracks.map((_, i) => i);
    // Fisher-Yates Shuffle
    for (let i = indices.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [indices[i], indices[j]] = [indices[j], indices[i]];
    }
    // Ensure current track stays at current slot if valid
    if (this.currentIndex >= 0) {
      const curPos = indices.indexOf(this.currentIndex);
      if (curPos !== -1) {
        indices.splice(curPos, 1);
        indices.unshift(this.currentIndex);
      }
    }
    this.shuffledOrder = indices;
  }
}
