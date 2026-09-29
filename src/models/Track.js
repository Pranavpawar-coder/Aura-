/**
 * AURA Normalized Track Model
 * Conforming to Phase 1 Audio Engine & Phase 2 Library specifications.
 */
export class Track {
  constructor({
    id = '',
    title = 'Untitled Track',
    artist = 'Unknown Artist',
    album = 'Unknown Album',
    duration = 0,
    fileUri = '',
    artwork = 'assets/album_resonance.png',
    genre = 'Ambient',
    quality = '24-BIT / 96kHz LOSSLESS',
    isFavorite = false,
    audioBuffer = null,
    lyrics = []
  } = {}) {
    this.id = id || `track_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    this.title = title;
    this.artist = artist;
    this.album = album;
    this.duration = duration;
    this.fileUri = fileUri;
    this.artwork = artwork;
    this.genre = genre;
    this.quality = quality;
    this.isFavorite = isFavorite;
    this.audioBuffer = audioBuffer;
    this.lyrics = lyrics;
  }

  static fromFile(file, objectUrl, metadata = {}) {
    return new Track({
      id: `file_${file.name}_${file.size}_${file.lastModified}`,
      title: metadata.title || file.name.replace(/\.[^/.]+$/, ''),
      artist: metadata.artist || 'Local Artist',
      album: metadata.album || 'Local Device Audio',
      duration: metadata.duration || 0,
      fileUri: objectUrl,
      artwork: metadata.artwork || 'assets/album_resonance.png',
      genre: metadata.genre || 'Local Audio',
      quality: metadata.quality || (file.name.endsWith('.flac') ? '24-BIT / 96kHz LOSSLESS' : '320kbps MP3'),
      isFavorite: false
    });
  }
}
