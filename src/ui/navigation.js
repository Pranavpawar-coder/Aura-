/**
 * AURA Screen & Stack Navigation Controller
 * Manages tab switching (Home, Search, Library, Settings) and stack overlays (Now Playing, Lyrics)
 * without re-mounting the DOM or interrupting background audio playback.
 */
export class NavigationController {
  constructor({
    screensContainer,
    nowPlayingModal,
    lyricsModal,
    activeTab = 'home'
  }) {
    this.screensContainer = screensContainer;
    this.nowPlayingModal = nowPlayingModal;
    this.lyricsModal = lyricsModal;
    this.activeTab = activeTab;

    this._bindTabLinks();
    this._bindModalTriggers();
  }

  _bindTabLinks() {
    // Bottom nav bar items
    const navItems = document.querySelectorAll('nav [data-path]');
    navItems.forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const targetPath = item.getAttribute('data-path');
        if (targetPath === 'now-playing') {
          this.openNowPlaying();
        } else {
          this.switchTab(targetPath);
        }
      });
    });
  }

  _bindModalTriggers() {
    // Mini player clicks open Now Playing
    const miniPlayerLinks = document.querySelectorAll('[data-path="now-playing"], aside a[href="#"], aside');
    miniPlayerLinks.forEach(el => {
      el.addEventListener('click', (e) => {
        // If clicking control buttons in mini player, ignore modal trigger
        if (e.target.closest('button')) return;
        e.preventDefault();
        this.openNowPlaying();
      });
    });

    // Minimize Now Playing (down arrow or back)
    const minimizeBtns = document.querySelectorAll('[aria-label="Minimize now playing"], #closeNowPlayingBtn');
    minimizeBtns.forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        this.closeNowPlaying();
      });
    });

    // Open Lyrics button in Now Playing
    const lyricsBtns = document.querySelectorAll('#lyricsBtn, [aria-label="Display lyrics"]');
    lyricsBtns.forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        this.openLyrics();
      });
    });

    // Close Lyrics button
    const closeLyricsBtns = document.querySelectorAll('#closeLyricsBtn, [aria-label="Close lyrics"]');
    closeLyricsBtns.forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        this.closeLyrics();
      });
    });
  }

  switchTab(tabName) {
    this.activeTab = tabName;

    // Toggle tab views
    const tabs = document.querySelectorAll('.tab-view');
    tabs.forEach(tab => {
      if (tab.getAttribute('data-tab') === tabName) {
        tab.classList.remove('hidden');
      } else {
        tab.classList.add('hidden');
      }
    });

    // Update bottom nav bar active styling
    const navItems = document.querySelectorAll('nav [data-path]');
    navItems.forEach(item => {
      const isCur = item.getAttribute('data-path') === tabName;
      const dot = item.querySelector('.rounded-full');
      if (isCur) {
        item.classList.add('text-primary');
        item.classList.remove('text-on-surface-variant');
        if (dot) dot.classList.remove('opacity-0');
      } else {
        item.classList.remove('text-primary');
        item.classList.add('text-on-surface-variant');
        if (dot) dot.classList.add('opacity-0');
      }
    });
  }

  openNowPlaying() {
    if (this.nowPlayingModal) {
      this.nowPlayingModal.classList.remove('translate-y-full', 'pointer-events-none');
      this.nowPlayingModal.classList.add('translate-y-0', 'pointer-events-auto');
    }
  }

  closeNowPlaying() {
    if (this.nowPlayingModal) {
      this.nowPlayingModal.classList.remove('translate-y-0', 'pointer-events-auto');
      this.nowPlayingModal.classList.add('translate-y-full', 'pointer-events-none');
    }
  }

  openLyrics() {
    if (this.lyricsModal) {
      this.lyricsModal.classList.remove('translate-y-full', 'pointer-events-none');
      this.lyricsModal.classList.add('translate-y-0', 'pointer-events-auto');
    }
  }

  closeLyrics() {
    if (this.lyricsModal) {
      this.lyricsModal.classList.remove('translate-y-0', 'pointer-events-auto');
      this.lyricsModal.classList.add('translate-y-full', 'pointer-events-none');
    }
  }
}
