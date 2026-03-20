package com.pixelcolor.app.ui.screen.canvas

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelcolor.app.ui.viewmodel.CanvasUiState
import com.pixelcolor.app.ui.viewmodel.CanvasViewModel

@Composable
fun CanvasScreen(
    puzzleId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: CanvasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(puzzleId) {
        viewModel.loadPuzzle(puzzleId)
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    BackHandler {
        if (!uiState.isCompleted) {
            viewModel.showSaveDialog()
        } else {
            onBack()
        }
    }

    // Save dialog
    if (uiState.showSaveDialog) {
        SaveExitDialog(
            onConfirm = {
                viewModel.saveAndExit()
                onBack()
            },
            onDismiss = { viewModel.dismissSaveDialog() }
        )
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val puzzle = uiState.puzzle
    if (puzzle == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Puzzle not found")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress header
        ProgressHeader(
            title = puzzle.title,
            completionPercent = uiState.progress?.completionPercent ?: 0f,
            elapsedMs = uiState.elapsedMs,
            zoom = uiState.zoom,
            isPaused = uiState.isPaused,
            onPause = { viewModel.togglePause() },
            onBack = {
                if (!uiState.isCompleted) viewModel.showSaveDialog() else onBack()
            }
        )

        // Canvas area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF2A2A2A))
        ) {
            PixelCanvas(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Hint overlay
            if (uiState.showHint) {
                HintOverlay(
                    puzzle = puzzle,
                    selectedColorId = uiState.selectedColorId,
                    filledPixels = uiState.progress?.filledPixels ?: emptyMap(),
                    zoom = uiState.zoom,
                    panOffset = uiState.panOffset
                )
            }

            // Zoom FAB
            FloatingActionButton(
                onClick = {
                    val targetZoom = if (uiState.zoom < 2f) 3f else 1f
                    viewModel.setZoom(targetZoom)
                    viewModel.setPan(Offset.Zero)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(44.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "${uiState.zoom.roundToOne()}x",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Hint button
            if (uiState.hintsRemaining > 0) {
                FilledTonalButton(
                    onClick = { viewModel.useHint() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${uiState.hintsRemaining}", fontSize = 12.sp)
                }
            }
        }

        // Palette bar
        PaletteBar(
            palette = puzzle.palette,
            selectedColorId = uiState.selectedColorId,
            getColorRemaining = viewModel::getColorRemainingCount,
            onColorSelect = viewModel::selectColor
        )
    }
}

@Composable
private fun ProgressHeader(
    title: String,
    completionPercent: Float,
    elapsedMs: Long,
    zoom: Float,
    isPaused: Boolean,
    onPause: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back/Close
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Back", modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(8.dp))

                // Title
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Completion %
                Text(
                    "${(completionPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(12.dp))

                // Timer
                IconButton(onClick = onPause, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Close,
                        if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    formatTime(elapsedMs),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            LinearProgressIndicator(
                progress = { completionPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun PixelCanvas(
    uiState: CanvasUiState,
    viewModel: CanvasViewModel,
    modifier: Modifier = Modifier
) {
    val puzzle = uiState.puzzle ?: return
    val progress = uiState.progress
    val palette = remember(puzzle) { puzzle.palette.associateBy { it.id } }
    val filledPixels = progress?.filledPixels ?: emptyMap()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(uiState.zoom) {
        scale = uiState.zoom
    }
    LaunchedEffect(uiState.panOffset) {
        offset = uiState.panOffset
    }

    val shakeOffset = if (uiState.shakePixel != null) {
        val shakeAnim = remember { Animatable(0f) }
        LaunchedEffect(uiState.shakePixel) {
            shakeAnim.snapTo(0f)
            shakeAnim.animateTo(
                0f,
                animationSpec = keyframes {
                    durationMillis = 300
                    -4f at 50ms
                    4f at 100ms
                    -3f at 150ms
                    3f at 200ms
                    -1f at 250ms
                    0f at 300ms
                }
            )
        }
        shakeAnim.value
    } else 0f

    Canvas(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x + shakeOffset
                translationY = offset.y
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    scale = (scale * gestureZoom).coerceIn(0.5f, 8f)
                    offset += pan
                    viewModel.setZoom(scale)
                    viewModel.setPan(offset)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val adjustedX = (tapOffset.x - offset.x) / scale
                    val adjustedY = (tapOffset.y - offset.y) / scale

                    val pixelW = size.width / puzzle.gridWidth
                    val pixelH = size.height / puzzle.gridHeight

                    val gridX = (adjustedX / pixelW).toInt()
                    val gridY = (adjustedY / pixelH).toInt()

                    if (gridX in 0 until puzzle.gridWidth && gridY in 0 until puzzle.gridHeight) {
                        viewModel.tapPixel(gridX, gridY)
                    }
                }
            }
    ) {
        val pixelW = size.width / puzzle.gridWidth
        val pixelH = size.height / puzzle.gridHeight

        // Draw grid background
        drawRect(
            color = Color(0xFFE8E8E8),
            topLeft = Offset.Zero,
            size = size
        )

        // Draw grid lines
        for (x in 0..puzzle.gridWidth) {
            drawLine(
                color = Color(0xFFD0D0D0),
                start = Offset(x * pixelW, 0f),
                end = Offset(x * pixelW, size.height),
                strokeWidth = 0.5f
            )
        }
        for (y in 0..puzzle.gridHeight) {
            drawLine(
                color = Color(0xFFD0D0D0),
                start = Offset(0f, y * pixelH),
                end = Offset(size.width, y * pixelH),
                strokeWidth = 0.5f
            )
        }

        // Draw all pixels
        val textPaint = Paint().apply {
            textSize = (pixelW * 0.55f).coerceAtLeast(6f).coerceAtMost(14f)
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        for (pixel in puzzle.pixels) {
            val isFilled = filledPixels["${pixel.x},${pixel.y}"] == true
            val paletteColor = palette[pixel.colorId]
            val colorHex = paletteColor?.hexColor ?: "#CCCCCC"

            val pixelColor = if (isFilled) {
                try {
                    Color(android.graphics.Color.parseColor(colorHex))
                } catch (e: Exception) {
                    Color.Gray
                }
            } else {
                Color(0xFFF0F0F0)
            }

            drawRect(
                color = pixelColor,
                topLeft = Offset(pixel.x * pixelW, pixel.y * pixelH),
                size = Size(pixelW - 0.5f, pixelH - 0.5f)
            )

            // Draw color number on unfilled pixels when zoomed in
            if (!isFilled && scale > 1.2f) {
                val numStr = paletteColor?.label ?: ""
                if (numStr.isNotEmpty()) {
                    // Tint the unfilled pixel with a very light version of its target color
                    val targetColor = try {
                        Color(android.graphics.Color.parseColor(colorHex))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                    drawRect(
                        color = targetColor.copy(alpha = 0.12f),
                        topLeft = Offset(pixel.x * pixelW, pixel.y * pixelH),
                        size = Size(pixelW - 0.5f, pixelH - 0.5f)
                    )

                    // Draw the number
                    textPaint.color = if (targetColor.luminance() > 0.4f) {
                        android.graphics.Color.rgb(60, 60, 60)
                    } else {
                        android.graphics.Color.WHITE
                    }
                    val cx = pixel.x * pixelW + pixelW / 2f
                    val cy = pixel.y * pixelH + pixelH / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                    drawContext.canvas.nativeCanvas.drawText(numStr, cx, cy, textPaint)
                }
            }
        }
    }
}

@Composable
private fun HintOverlay(
    puzzle: com.pixelcolor.app.domain.model.PixelPuzzle,
    selectedColorId: Int,
    filledPixels: Map<String, Boolean>,
    zoom: Float,
    panOffset: Offset
) {
    val hintPixels = remember(puzzle, selectedColorId) {
        puzzle.getPixelsByColorId(selectedColorId)
            .filter { filledPixels["${it.x},${it.y}"] != true }
    }

    val pulseAnim = rememberInfiniteTransition(label = "hint_pulse")
    val alpha by pulseAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_alpha"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                translationX = panOffset.x
                translationY = panOffset.y
            }
    ) {
        val pixelW = size.width / puzzle.gridWidth
        val pixelH = size.height / puzzle.gridHeight

        val colorHex = puzzle.getPaletteColor(selectedColorId)?.hexColor ?: "#FFFF00"
        val hintColor = try {
            Color(android.graphics.Color.parseColor(colorHex)).copy(alpha = alpha)
        } catch (e: Exception) {
            Color.Yellow.copy(alpha = alpha)
        }

        for (pixel in hintPixels) {
            drawRect(
                color = hintColor,
                topLeft = Offset(pixel.x * pixelW, pixel.y * pixelH),
                size = Size(pixelW, pixelH)
            )
        }
    }
}

@Composable
private fun PaletteBar(
    palette: List<com.pixelcolor.app.domain.model.PaletteColor>,
    selectedColorId: Int,
    getColorRemaining: (Int) -> Int,
    onColorSelect: (Int) -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            palette.forEach { color ->
                val remaining = getColorRemaining(color.id)
                val isSelected = color.id == selectedColorId
                val isComplete = remaining == 0

                val bgColor = try {
                    Color(android.graphics.Color.parseColor(color.hexColor))
                } catch (e: Exception) {
                    Color.Gray
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isComplete) bgColor.copy(alpha = 0.3f) else bgColor)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelect(color.id) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isComplete) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                color.label,
                                color = if (bgColor.luminance() > 0.5f) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$remaining",
                                color = if (bgColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.7f)
                                else Color.White.copy(alpha = 0.7f),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveExitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Progress?") },
        text = { Text("Your progress will be saved. You can resume later.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Save & Exit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}

private fun Float.roundToOne(): String = String.format("%.1f", this)

private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
