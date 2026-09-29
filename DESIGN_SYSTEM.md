# Aura Cinematic Sound — Design System

> Imported directly from Google Stitch project: **AURA Music Player Prototype** (`projects/6839351440726336669`)

---

## 1. Brand & Style
The design system establishes a high-fidelity, sensory-driven mobile music experience rooted in modern dark-mode aesthetics, cinematic lighting, and precision-engineered glassmorphism. Designed for discerning audiophiles, nocturnal listeners, and curators who view audio as an immersive art form, the interface dissolves structural boundaries, letting visual artwork and ambient light lead the hierarchy.

The visual ethos blends **Cosmic Minimalism** with **Atmospheric Glassmorphism**:
- **Atmospheric Void:** The base environment is an impenetrable, deep cosmic canvas (`#080A0F` / `#10131A`) that minimizes visual noise, prevents OLED battery drain, and isolates luminous media assets.
- **Dynamic Spectral Glow:** Rather than static brand accents, secondary surfaces react dynamically to track artwork, casting ultra-diffused auroras and soft spectral shadows across floating frosted panels.
- **Precision Glass:** Key interactive surfaces leverage translucent, high-blur glass materials accented with hairline inner borders, communicating tangible depth, tactile richness, and optical precision.
- **Kinetic Fluidity:** Interactive elements exhibit smooth, spring-calibrated micro-responses, reinforcing an experience that feels alive, weighted, and responsive.

---

## 2. Color Palette & Surface Tokens

### Surface Tiers
- **Void Canvas / Background:** `#10131a`
- **Surface Container Lowest:** `#0b0e15`
- **Surface Container Low:** `#191b23`
- **Surface Container:** `#1d2027`
- **Surface Container High:** `#272a31`
- **Surface Container Highest:** `#32353c`
- **Surface Bright:** `#363941`
- **Glass Surface:** `rgba(22, 27, 38, 0.65)` with `backdrop-filter: blur(24px)`
- **Glass Hairline Edge:** `rgba(255, 255, 255, 0.08)` (1px stroke catching rim lighting)

### Accents & Spectrum
- **Primary Accent (Electric Violet):** `#cabeff` / container `#947dff` / CTA `#7c5cfc`
- **Secondary Accent (Radiant Cyan / Blue):** `#aac7ff` / container `#0068d0`
- **Tertiary Accent (Radiant Orchid / Magenta):** `#f6adff` / container `#dc52f9`
- **Error:** `#ffb4ab` / container `#93000a`

### Typography & Icon Contrast
- **Text Primary (`on-surface`):** `#e0e2ec`
- **Text Secondary (`on-surface-variant`):** `#c9c4d8`
- **Text Outline / Muted:** `#938ea1`
- **Outline Variant:** `#484555`

---

## 3. Typography (Plus Jakarta Sans)

| Role | Size | Line Height | Weight | Letter Spacing |
| :--- | :--- | :--- | :--- | :--- |
| **Headline XL** | 36px | 44px | 700 (Bold) | -0.03em |
| **Headline XL Mobile** | 28px | 34px | 700 (Bold) | -0.025em |
| **Headline LG** | 24px | 30px | 600 (SemiBold) | -0.02em |
| **Headline MD** | 20px | 26px | 600 (SemiBold) | -0.015em |
| **Title MD** | 17px | 22px | 600 (SemiBold) | -0.01em |
| **Body LG** | 16px | 24px | 400 (Regular) | -0.005em |
| **Body MD** | 14px | 20px | 400 (Regular) | 0em |
| **Body SM** | 13px | 18px | 500 (Medium) | +0.01em |
| **Label MD** | 12px | 16px | 600 (SemiBold) | +0.04em |
| **Label SM** | 11px | 14px | 700 (Bold) | +0.08em |
| **Code SM** | 12px | 16px | 500 (Medium) | +0.02em |

---

## 4. Spacing & Border Radii

### Border Radii
- **DEFAULT**: `1rem` (16px)
- **sm**: `0.5rem` (8px)
- **md**: `1.5rem` (24px)
- **lg**: `2rem` (32px)
- **xl**: `3rem` (48px)
- **full**: `9999px` (Pill)

### Spacing Scale
- `space-xs`: `0.25rem` (4px)
- `space-sm`: `0.5rem` (8px)
- `space-md`: `1rem` (16px)
- `space-lg`: `1.5rem` (24px)
- `space-xl`: `2.25rem` (36px)
- `gutter`: `1rem` (16px)
- `margin`: `1.25rem` (20px)

---

## 5. Screen Manifest

| Screen File | Stitch Screen Title | Type |
| :--- | :--- | :--- |
| `screens/home.html` | AURA - Home | Full Mobile Screen |
| `screens/now_playing.html` | AURA - Now Playing | Full Player Interface |
| `screens/lyrics.html` | AURA - Live Lyrics | Realtime Synchronized Lyrics |
| `screens/search.html` | AURA - Search | Discovery & Mood Filter View |
| `screens/library.html` | AURA - Library | Playlists, Artists & Collections |
| `screens/settings.html` | AURA - Settings | Audiophile & DAC Stream Config |
| `screens/prototype_overview.html` | AURA Audiophile Music Player | Master Interactive Layout |
