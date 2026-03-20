package com.pixelcolor.app.ui.screen.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelcolor.app.PixelColorApplication
import com.pixelcolor.app.domain.model.Difficulty
import com.pixelcolor.app.domain.model.PixelPuzzle
import com.pixelcolor.app.domain.model.UserProgress
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleDetailScreen(
    puzzleId: String,
    onStartClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var puzzle by remember { mutableStateOf<PixelPuzzle?>(null) }
    var progress by remember { mutableStateOf<UserProgress?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val app = remember { context.applicationContext as PixelColorApplication }
    val scope = rememberCoroutineScope()

    LaunchedEffect(puzzleId) {
        puzzle = app.puzzleRepository.getPuzzleById(puzzleId)
        progress = app.puzzleRepository.getProgressSync(puzzleId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(puzzle?.title ?: "Puzzle Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val currentPuzzle = puzzle
        if (currentPuzzle == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Puzzle not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Puzzle preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    PuzzlePreview(puzzle = currentPuzzle, modifier = Modifier.fillMaxSize())

                    // Difficulty badge
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = when (currentPuzzle.difficulty) {
                            Difficulty.EASY -> Color(0xFF4CAF50)
                            Difficulty.MEDIUM -> Color(0xFFFF9800)
                            Difficulty.HARD -> Color(0xFFF44336)
                        }
                    ) {
                        Text(
                            currentPuzzle.difficulty.displayName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip("${currentPuzzle.gridWidth}x${currentPuzzle.gridHeight}", "Grid")
                InfoChip("${currentPuzzle.palette.size}", "Colors")
                InfoChip("${currentPuzzle.pixels.size}", "Pixels")
                InfoChip(currentPuzzle.category.displayName, "Category")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress if started
            progress?.let { prog ->
                if (prog.completionPercent > 0f) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Progress: ${(prog.completionPercent * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { prog.completionPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Time spent: ${formatDuration(prog.timeSpentMs)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Palette preview
            Text(
                "Palette",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currentPuzzle.palette.forEach { color ->
                    val bgColor = try {
                        Color(android.graphics.Color.parseColor(color.hexColor))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            color.label,
                            color = if (bgColor.luminance() > 0.5f) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start/Resume button
            val hasProgress = (progress?.completionPercent ?: 0f) > 0f
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (hasProgress) "Resume" else "Start Coloring",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reset button if has progress
            if (hasProgress) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    scope.launch {
                        app.puzzleRepository.resetProgress(puzzleId)
                        progress = null
                    }
                }) {
                    Text("Start Over")
                }
            }
        }
    }
}

@Composable
private fun InfoChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun PuzzlePreview(
    puzzle: PixelPuzzle,
    modifier: Modifier = Modifier
) {
    val palette = puzzle.palette.associateBy { it.id }

    Canvas(modifier = modifier) {
        if (puzzle.pixels.isEmpty()) return@Canvas

        val pixelW = size.width / puzzle.gridWidth
        val pixelH = size.height / puzzle.gridHeight

        for (pixel in puzzle.pixels) {
            val colorHex = palette[pixel.colorId]?.hexColor ?: "#CCCCCC"
            val color = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) {
                Color.Gray
            }
            drawRect(
                color = color,
                topLeft = Offset(pixel.x * pixelW, pixel.y * pixelH),
                size = Size(pixelW, pixelH)
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
