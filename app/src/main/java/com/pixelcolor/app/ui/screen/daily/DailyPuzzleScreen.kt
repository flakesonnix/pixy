package com.pixelcolor.app.ui.screen.daily

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelcolor.app.domain.model.Difficulty
import com.pixelcolor.app.ui.viewmodel.DailyPuzzleViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPuzzleScreen(
    onPlayClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: DailyPuzzleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var countdown by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            countdown = viewModel.getTimeUntilNext()
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Puzzle") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val puzzle = uiState.puzzle
        if (puzzle == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No daily puzzle available")
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
            // Puzzle preview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pixel art preview
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                    ) {
                        PixelPreview(
                            puzzle = puzzle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        puzzle.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Difficulty badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (puzzle.difficulty) {
                                Difficulty.EASY -> Color(0xFF4CAF50)
                                Difficulty.MEDIUM -> Color(0xFFFF9800)
                                Difficulty.HARD -> Color(0xFFF44336)
                            }
                        ) {
                            Text(
                                puzzle.difficulty.displayName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Grid size
                        Text(
                            "${puzzle.gridWidth}x${puzzle.gridHeight}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        // Palette colors count
                        Text(
                            "${puzzle.palette.size} colors",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Progress if started
                    uiState.progress?.let { progress ->
                        if (progress.completionPercent > 0f) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress.completionPercent },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                            Text(
                                "${(progress.completionPercent * 100).toInt()}% complete",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Play button
            Button(
                onClick = { onPlayClick(puzzle.id) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (uiState.progress?.completionPercent ?: 0f > 0f) "Resume" else "Play",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Countdown
            Text(
                "Next puzzle in",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                countdown,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Last 7 days calendar strip
            Text(
                "This Week",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                uiState.lastSevenDays.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val date = java.time.LocalDate.parse(day.date)
                        val dayLabel = date.dayOfWeek.name.take(3)

                        Text(
                            dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (day.completed) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (day.completed) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Not completed",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            "${date.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PixelPreview(
    puzzle: com.pixelcolor.app.domain.model.PixelPuzzle,
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
