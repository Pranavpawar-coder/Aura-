import os
import re

SCREENS_DIR = r"D:\Aura\screens"

def extract_main_content(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    # Extract between <main ...> and </main>
    match = re.search(r'<main[^>]*>(.*?)</main>', content, re.DOTALL)
    if match:
        return match.group(1).strip()
    return ""

def extract_head_tags(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    # Extract tailwind-config
    tw_match = re.search(r'(<script id="tailwind-config">.*?</script>)', content, re.DOTALL)
    tw = tw_match.group(1) if tw_match else ""
    return tw

home_main = extract_main_content(os.path.join(SCREENS_DIR, "home.html"))
search_main = extract_main_content(os.path.join(SCREENS_DIR, "search.html"))
library_main = extract_main_content(os.path.join(SCREENS_DIR, "library.html"))
settings_main = extract_main_content(os.path.join(SCREENS_DIR, "settings.html"))
now_playing_main = extract_main_content(os.path.join(SCREENS_DIR, "now_playing.html"))
lyrics_main = extract_main_content(os.path.join(SCREENS_DIR, "lyrics.html"))

tailwind_script = extract_head_tags(os.path.join(SCREENS_DIR, "home.html"))

template = f"""<!DOCTYPE html>
<html class="dark" lang="en">
<head>
  <meta charset="utf-8"/>
  <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover" name="viewport"/>
  <meta content="mobile_tab" name="shell-type"/>
  <title>AURA — Audiophile Music Player</title>
  
  <!-- Fonts & Material Icons (Stitch Source of Truth) -->
  <link href="https://fonts.googleapis.com" rel="preconnect"/>
  <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet"/>
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet"/>
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
  
  <!-- Tailwind CSS Engine -->
  <script src="https://cdn.tailwindcss.com"></script>
  {tailwind_script}

  <style>
    @layer base {{
      html, body {{
        width: 100%;
        margin: 0;
        padding: 0;
        background-color: #0b0e15;
        overscroll-behavior: none;
      }}
      .pb-safe {{ padding-bottom: env(safe-area-inset-bottom, 0px); }}
      .pt-safe {{ padding-top: env(safe-area-inset-top, 0px); }}
    }}
    ::-webkit-scrollbar {{ display: none; }}
    
    /* Mobile-first Phone Container Simulation for Desktop viewports */
    @media (min-width: 640px) {{
      .app-shell {{
        max-width: 430px;
        margin: 20px auto;
        min-height: 900px;
        height: calc(100vh - 40px);
        border-radius: 44px;
        box-shadow: 0 25px 70px rgba(0, 0, 0, 0.9), 0 0 35px rgba(124, 92, 252, 0.2);
        border: 8px solid #1f232e;
        overflow: hidden;
        position: relative;
      }}
    }}
    @media (max-width: 639px) {{
      .app-shell {{
        width: 100vw;
        min-height: 100vh;
        position: relative;
      }}
    }}
  </style>
</head>
<body class="bg-[#0b0e15] text-on-surface font-body-md text-body-md min-h-screen flex flex-col items-center justify-center selection:bg-primary selection:text-on-primary">

  <!-- Main Mobile App Shell -->
  <div class="app-shell bg-surface flex flex-col w-full relative overflow-hidden" id="appShell">

    <!-- Developer/Audiophile Quick Test Bar -->
    <div class="bg-surface-container-lowest/90 border-b border-surface-container-high/40 px-3 py-1.5 flex items-center justify-between text-[11px] text-on-surface-variant z-50">
      <div class="flex items-center gap-1.5">
        <span class="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
        <span class="font-semibold text-primary uppercase tracking-wider text-[10px]">AURA Engine • Phase 1 Active</span>
      </div>
      <label class="cursor-pointer hover:text-primary transition-colors flex items-center gap-1 font-semibold text-[10px] uppercase">
        <span class="material-symbols-outlined text-[14px]">file_upload</span> Load Audio
        <input type="file" id="localAudioInput" accept="audio/*" multiple class="hidden" />
      </label>
    </div>

    <!-- Persistent Global Header (Stitch Source) -->
    <header class="sticky top-0 inset-x-0 z-40 bg-surface/85 backdrop-blur-xl shadow-[0_1px_8px_rgba(0,0,0,0.25)]">
      <div class="h-14 px-gutter flex items-center justify-between">
        <div class="flex items-center gap-space-sm">
          <img alt="AURA Logo" class="h-7 w-auto object-contain" src="assets/aura_logo.png"/>
          <h1 id="screenHeaderTitle" class="font-title-md text-title-md text-on-surface tracking-tight">Home</h1>
        </div>
        <div class="flex items-center gap-space-sm">
          <button aria-label="Cast audio" class="w-9 h-9 flex items-center justify-center rounded-full text-on-surface-variant hover:text-on-surface transition-colors">
            <span class="material-symbols-outlined text-[20px]">cast</span>
          </button>
          <button aria-label="Profile" class="w-9 h-9 flex items-center justify-center rounded-full transition-transform active:scale-95 overflow-hidden">
            <img alt="Profile" class="w-7 h-7 rounded-full object-cover" src="assets/user_avatar.png"/>
          </button>
        </div>
      </div>
    </header>

    <!-- Tab View Screens Container -->
    <div class="flex-1 w-full relative overflow-y-auto pb-44" id="screensContainer">
      
      <!-- Tab 1: HOME -->
      <div id="tab-home" class="tab-view w-full" data-tab="home">
        {home_main}
      </div>

      <!-- Tab 2: SEARCH -->
      <div id="tab-search" class="tab-view w-full hidden" data-tab="search">
        {search_main}
      </div>

      <!-- Tab 3: LIBRARY -->
      <div id="tab-library" class="tab-view w-full hidden" data-tab="library">
        {library_main}
      </div>

      <!-- Tab 4: SETTINGS -->
      <div id="tab-settings" class="tab-view w-full hidden" data-tab="settings">
        {settings_main}
      </div>
    </div>

    <!-- Persistent Floating Mini-Player (Stitch Source) -->
    <aside class="fixed sm:absolute bottom-20 inset-x-0 z-40 px-gutter pointer-events-none">
      <div class="pointer-events-auto w-full h-16 rounded-xl bg-surface-container/90 backdrop-blur-xl shadow-[0_12px_32px_rgba(0,0,0,0.6)] border border-white/5 flex items-center justify-between px-space-sm relative overflow-hidden">
        <!-- Live Scrubber Hairline Under Mini-Player -->
        <div class="absolute bottom-0 left-0 right-0 h-0.5 bg-surface-container-highest">
          <div class="h-full w-0 bg-primary rounded-full transition-all duration-150 mini-progress-fill"></div>
        </div>
        <!-- Tap Area to Open Now Playing -->
        <a class="flex items-center gap-space-sm min-w-0 flex-1 h-full pr-space-xs cursor-pointer select-none" data-path="now-playing" href="#">
          <div class="relative w-11 h-11 rounded-lg overflow-hidden flex-shrink-0 bg-surface-container-highest shadow-[0_0_12px_rgba(202,190,255,0.25)]">
            <img class="w-full h-full object-cover mini-cover-art" src="assets/album_resonance.png" alt="Cover" />
          </div>
          <div class="flex flex-col min-w-0 flex-1">
            <span class="font-title-md text-title-md text-on-surface truncate leading-tight mini-track-title">Resonance</span>
            <span class="font-label-sm text-label-sm text-on-surface-variant truncate mini-track-artist">Kaelen Vance</span>
          </div>
        </a>
        <!-- Mini Player Deck Controls -->
        <div class="flex items-center gap-1 flex-shrink-0">
          <button aria-label="Play or Pause" class="mini-play-btn w-11 h-11 flex items-center justify-center rounded-full text-on-surface hover:text-primary transition-colors active:scale-90">
            <span class="material-symbols-outlined text-[28px]">play_circle</span>
          </button>
          <button aria-label="Next track" class="btn-next-track w-11 h-11 flex items-center justify-center rounded-full text-on-surface-variant hover:text-on-surface transition-colors active:scale-90">
            <span class="material-symbols-outlined text-[24px]">skip_next</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- Persistent Bottom Tab Navigation (Stitch Source) -->
    <nav class="fixed sm:absolute bottom-0 inset-x-0 z-40 pb-safe bg-surface/95 backdrop-blur-2xl shadow-[0_-4px_24px_rgba(0,0,0,0.5)] border-t border-surface-container-high/40">
      <div class="flex justify-around items-center h-20 px-gutter">
        <a class="flex flex-col items-center justify-center gap-1 w-16 h-12 transition-all relative text-primary cursor-pointer select-none" data-path="home" href="#">
          <span class="material-symbols-outlined text-[24px]">home</span>
          <span class="font-label-sm text-label-sm">Home</span>
          <div class="absolute -bottom-1 w-1.5 h-1.5 rounded-full bg-primary transition-opacity"></div>
        </a>
        <a class="flex flex-col items-center justify-center gap-1 w-16 h-12 text-on-surface-variant hover:text-on-surface transition-all relative cursor-pointer select-none" data-path="search" href="#">
          <span class="material-symbols-outlined text-[24px]">search</span>
          <span class="font-label-sm text-label-sm">Search</span>
          <div class="absolute -bottom-1 w-1.5 h-1.5 rounded-full bg-primary opacity-0 transition-opacity"></div>
        </a>
        <a class="flex flex-col items-center justify-center gap-1 w-16 h-12 text-on-surface-variant hover:text-on-surface transition-all relative cursor-pointer select-none" data-path="library" href="#">
          <span class="material-symbols-outlined text-[24px]">library_music</span>
          <span class="font-label-sm text-label-sm">Library</span>
          <div class="absolute -bottom-1 w-1.5 h-1.5 rounded-full bg-primary opacity-0 transition-opacity"></div>
        </a>
        <a class="flex flex-col items-center justify-center gap-1 w-16 h-12 text-on-surface-variant hover:text-on-surface transition-all relative cursor-pointer select-none" data-path="settings" href="#">
          <span class="material-symbols-outlined text-[24px]">tune</span>
          <span class="font-label-sm text-label-sm">Settings</span>
          <div class="absolute -bottom-1 w-1.5 h-1.5 rounded-full bg-primary opacity-0 transition-opacity"></div>
        </a>
      </div>
    </nav>

    <!-- Modal 1: NOW PLAYING FULLSCREEN OVERLAY -->
    <div id="nowPlayingModal" class="absolute inset-0 z-50 bg-surface flex flex-col overflow-y-auto transform translate-y-full pointer-events-none transition-transform duration-300 ease-out">
      <!-- Now Playing Header -->
      <div class="sticky top-0 inset-x-0 z-50 bg-surface/85 backdrop-blur-xl h-14 px-gutter flex items-center justify-between border-b border-surface-container-high/30">
        <button id="closeNowPlayingBtn" aria-label="Minimize now playing" class="w-10 h-10 -ml-2 rounded-full flex items-center justify-center text-on-surface hover:text-primary active:scale-90 transition-all">
          <span class="material-symbols-outlined text-[26px]">keyboard_arrow_down</span>
        </button>
        <span class="font-title-md text-title-md text-on-surface font-semibold tracking-tight">Now Playing</span>
        <button aria-label="Audio queue" class="w-10 h-10 -mr-2 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface active:scale-90 transition-all">
          <span class="material-symbols-outlined text-[20px]">queue_music</span>
        </button>
      </div>

      <!-- Now Playing Body (Stitch Source) -->
      <div class="flex-1 w-full pb-8">
        {now_playing_main}
      </div>
    </div>

    <!-- Modal 2: LIVE LYRICS OVERLAY -->
    <div id="lyricsModal" class="absolute inset-0 z-50 bg-surface flex flex-col overflow-y-auto transform translate-y-full pointer-events-none transition-transform duration-300 ease-out">
      <!-- Lyrics Header -->
      <div class="sticky top-0 inset-x-0 z-50 bg-surface/85 backdrop-blur-xl h-14 px-gutter flex items-center justify-between border-b border-surface-container-high/30">
        <button id="closeLyricsBtn" aria-label="Close lyrics" class="w-10 h-10 -ml-2 flex items-center justify-center rounded-full text-on-surface hover:text-primary transition-colors active:scale-95">
          <span class="material-symbols-outlined text-[24px]">arrow_back_ios_new</span>
        </button>
        <span class="font-title-md text-title-md text-on-surface font-semibold tracking-tight">Live Lyrics</span>
        <button aria-label="Share lyrics" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface active:scale-90 transition-colors">
          <span class="material-symbols-outlined text-[19px]">ios_share</span>
        </button>
      </div>

      <!-- Lyrics Body (Stitch Source) -->
      <div class="flex-1 w-full pb-12" id="lyricsLinesContainer">
        {lyrics_main}
      </div>
    </div>

  </div>

  <!-- Boot AURA Audio Engine & UI Sync -->
  <script type="module" src="./src/app.js"></script>
</body>
</html>
"""

with open(r"D:\Aura\index.html", "w", encoding="utf-8") as f:
    f.write(template)

print("Unified index.html built successfully!")
