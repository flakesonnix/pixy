# PixelColor - Pixel Art Coloring Game

A production-ready Android pixel art coloring game built with Kotlin and Jetpack Compose.

## Features

- **12 Built-in Puzzles** - From 16x16 smileys to 64x64 dragons
- **Image-to-Pixel Art** - Convert any image into a color-by-numbers puzzle using k-means quantization
- **Pinch-to-Zoom & Pan** - Smooth gesture handling from 0.5x to 8x zoom
- **Tap-to-Fill** - Tap pixels to fill with selected palette color; wrong color shows shake animation
- **Palette Bar** - Color selection with remaining count per color, checkmark on completion
- **Progress Tracking** - Auto-saves every 10 fills, resume anytime via Room DB
- **Hint System** - 3 hints per puzzle flash unfilled pixels of selected color
- **Daily Puzzle** - Date-seeded puzzle selection with countdown timer
- **Time-lapse Recording** - Captures progress snapshots, encodes to MP4 via MediaCodec
- **Completion Screen** - Confetti animation, stats (time, accuracy, hints used), share options
- **Settings** - Sound effects, haptic feedback, color-blind mode, theme (Light/Dark/System)

## Architecture

```
app/src/main/java/com/pixelcolor/app/
├── data/
│   ├── audio/          # SoundManager (SoundPool + Vibrator)
│   ├── database/       # Room DB, Entities, DAOs
│   ├── generator/      # ImageToPixelArt (k-means), PuzzleFactory (procedural)
│   ├── notification/   # WorkManager scheduler, BroadcastReceiver
│   ├── preferences/    # DataStore user settings
│   ├── repository/     # PuzzleRepository, DailyPuzzleManager
│   └── timelapse/      # MP4 encoder (MediaCodec + MediaMuxer)
├── domain/model/       # Pixel, PixelPuzzle, PaletteColor, UserProgress, enums
├── ui/
│   ├── components/     # ConfettiAnimation
│   ├── navigation/     # NavHost with all routes
│   ├── screen/         # Splash, Home, Detail, Canvas, Completion, Daily, Settings
│   ├── theme/          # Material 3 theme, typography
│   └── viewmodel/      # ViewModels per screen (StateFlow + collectAsState)
├── MainActivity.kt
└── PixelColorApplication.kt
```

## Tech Stack

- **Kotlin** + Jetpack Compose (BOM 2024.01.00)
- **Material 3** with dynamic color support
- **Navigation Compose** for screen routing
- **Room** for local puzzle data and progress persistence
- **DataStore** for user preferences
- **Kotlinx Serialization** for JSON pixel/palette data
- **WorkManager** for daily puzzle notification scheduling
- **MediaCodec + MediaMuxer** for time-lapse MP4 encoding
- **Coroutines + Flow** for async operations and reactive UI

## Build

```bash
# Requires Android Studio Hedgehog+ and JDK 17
./gradlew assembleDebug
```

## Puzzle Generation

Puzzles are generated procedurally using `Canvas` drawing on a `Bitmap`, then quantized through an image pipeline:

1. Draw shapes (circles, rects, paths) on a `Bitmap` at grid resolution
2. Run k-means color quantization to extract a limited palette (6-12 colors)
3. Map each pixel to its nearest palette color
4. Store as `PixelPuzzle` with `List<Pixel>` + `List<PaletteColor>`

The same pipeline works for user-imported images via `ImageToPixelArt.fromBitmap()`.

## Screenshots

| Home | Canvas | Completion |
|------|--------|------------|
| Puzzle grid with filters | Zoom + tap-to-fill | Confetti + stats |
