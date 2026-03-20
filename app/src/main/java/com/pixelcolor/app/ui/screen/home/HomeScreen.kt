package com.pixelcolor.app.ui.screen.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelcolor.app.domain.model.Category
import com.pixelcolor.app.domain.model.Difficulty
import com.pixelcolor.app.ui.viewmodel.HomeViewModel
import com.pixelcolor.app.ui.viewmodel.PuzzleWithProgress
import com.pixelcolor.app.ui.viewmodel.SortOrder
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPuzzleClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onDailyPuzzleClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var countdown by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            countdown = viewModel.getTimeUntilNextDaily()
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PixelColor",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Daily puzzle banner
            DailyPuzzleBanner(
                countdown = countdown,
                streak = uiState.dailyStreak,
                onClick = onDailyPuzzleClick
            )

            // Category filter chips
            CategoryFilterRow(
                selected = uiState.selectedCategory,
                onSelect = viewModel::setCategory
            )

            // Sort chips
            SortRow(
                selected = uiState.sortOrder,
                onSelect = viewModel::setSortOrder
            )

            // Puzzle grid
            if (uiState.puzzles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No puzzles found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.puzzles, key = { it.puzzle.id }) { item ->
                        PuzzleCard(
                            item = item,
                            onClick = { onPuzzleClick(item.puzzle.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPuzzleBanner(
    countdown: String,
    streak: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pixel art icon
            Canvas(modifier = Modifier.size(48.dp)) {
                val s = size.width / 6f
                val colors = listOf(
                    Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFF4ECDC4),
                    Color(0xFFFF8C42), Color(0xFF95E1D3)
                )
                for (i in 0..2) {
                    for (j in 0..2) {
                        drawRect(
                            color = colors[(i + j) % colors.size],
                            topLeft = Offset((i * 2 + 0.5f) * s, (j * 2 + 0.5f) * s),
                            size = Size(s * 1.5f, s * 1.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Daily Puzzle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Next in $countdown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            if (streak > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$streak",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "day streak",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selected: Category?,
    onSelect: (Category?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("ALL") }
        )
        Category.entries.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat.displayName) }
            )
        }
    }
}

@Composable
private fun SortRow(
    selected: SortOrder,
    onSelect: (SortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SortOrder.entries.forEach { order ->
            val label = when (order) {
                SortOrder.NEW -> "NEW"
                SortOrder.EASIEST -> "EASIEST"
                SortOrder.HARDEST -> "HARDEST"
                SortOrder.IN_PROGRESS -> "IN PROGRESS"
            }
            AssistChip(
                onClick = { onSelect(order) },
                label = {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = if (selected == order) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == order)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun PuzzleCard(
    item: PuzzleWithProgress,
    onClick: () -> Unit
) {
    val puzzle = item.puzzle
    val progress = item.progress
    val completionPercent = progress?.completionPercent ?: 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Pixel art preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                PixelThumbnail(
                    puzzle = puzzle,
                    modifier = Modifier.fillMaxSize()
                )

                // Difficulty badge
                Surface(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(4.dp),
                    color = when (puzzle.difficulty) {
                        Difficulty.EASY -> Color(0xFF4CAF50)
                        Difficulty.MEDIUM -> Color(0xFFFF9800)
                        Difficulty.HARD -> Color(0xFFF44336)
                    }
                ) {
                    Text(
                        puzzle.difficulty.displayName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info section
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    puzzle.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        puzzle.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "${(completionPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (completionPercent >= 1f) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.primary
                    )
                }

                // Progress bar
                if (completionPercent > 0f) {
                    LinearProgressIndicator(
                        progress = { completionPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (completionPercent >= 1f) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PixelThumbnail(
    puzzle: com.pixelcolor.app.domain.model.PixelPuzzle,
    modifier: Modifier = Modifier
) {
    val pixels = puzzle.pixels
    val palette = puzzle.palette.associateBy { it.id }

    Canvas(modifier = modifier) {
        if (pixels.isEmpty()) return@Canvas

        val pixelW = size.width / puzzle.gridWidth
        val pixelH = size.height / puzzle.gridHeight

        for (pixel in pixels) {
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
