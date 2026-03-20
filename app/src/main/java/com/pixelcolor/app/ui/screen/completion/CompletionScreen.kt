package com.pixelcolor.app.ui.screen.completion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelcolor.app.ui.components.ConfettiAnimation
import com.pixelcolor.app.ui.viewmodel.CompletionViewModel

@Composable
fun CompletionScreen(
    puzzleId: String,
    onBackToHome: () -> Unit,
    onNextPuzzle: (String) -> Unit,
    viewModel: CompletionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(puzzleId) {
        viewModel.loadPuzzle(puzzleId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val puzzle = uiState.puzzle
    val progress = uiState.progress

    if (puzzle == null || progress == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading completion data")
        }
        return
    }

    // Confetti overlay
    ConfettiAnimation(modifier = Modifier.fillMaxSize())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Congratulations text
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "\uD83C\uDF89",
                    fontSize = 48.sp
                )
                Text(
                    "Congratulations!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Puzzle Completed!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Completed puzzle preview
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                CompletedPuzzlePreview(
                    puzzle = puzzle,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Time",
                        value = formatTime(progress.timeSpentMs),
                        icon = "⏱️"
                    )
                    StatItem(
                        label = "Pixels",
                        value = "${progress.filledCount}",
                        icon = "🎨"
                    )
                    StatItem(
                        label = "Hints",
                        value = "${progress.hintsUsed}",
                        icon = "💡"
                    )
                    StatItem(
                        label = "Accuracy",
                        value = "${(progress.accuracyRate * 100).toInt()}%",
                        icon = "🎯"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Time-lapse preview
        if (uiState.timelapseBitmaps.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Time-lapse Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.timelapseBitmaps.take(10).forEach { bitmap ->
                            Canvas(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            ) {
                                drawImage(
                                    image = androidx.compose.ui.graphics.ImageBitmap(
                                        bitmap.width, bitmap.height
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Share image
            OutlinedButton(
                onClick = { viewModel.shareImage() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Image")
            }

            // Save time-lapse
            if (uiState.isSavingTimelapse) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving time-lapse...")
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.saveTimelapseVideo(emptyList()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Time-lapse")
                }
            }

            // Back to home
            Button(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Back to Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Text(icon, fontSize = 20.sp)
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
private fun CompletedPuzzlePreview(
    puzzle: com.pixelcolor.app.domain.model.PixelPuzzle,
    modifier: Modifier = Modifier
) {
    val palette = puzzle.palette.associateBy { it.id }

    Canvas(modifier = modifier) {
        if (puzzle.pixels.isEmpty()) return@Canvas

        val pixelW = size.width / puzzle.gridWidth
        val pixelH = size.height / puzzle.gridHeight

        // Background
        drawRect(color = Color.White, size = size)

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

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%dh %dm", hours, minutes)
    else String.format("%dm %ds", minutes, seconds)
}
