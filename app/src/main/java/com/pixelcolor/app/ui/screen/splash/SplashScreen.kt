package com.pixelcolor.app.ui.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedAlpha = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )

    val pixelAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationPlayed = true
        pixelAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
        )
        delay(500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Pixel art logo using Canvas
            Canvas(modifier = Modifier.size(120.dp)) {
                drawPixelLogo(pixelAnim.value)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PixelColor",
                color = Color.White.copy(alpha = animatedAlpha.value),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Color by Numbers",
                color = Color.White.copy(alpha = animatedAlpha.value * 0.7f),
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun DrawScope.drawPixelLogo(progress: Float) {
    val pixelSize = size.width / 8f
    val colors = listOf(
        Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFF4ECDC4),
        Color(0xFFFF8C42), Color(0xFF95E1D3), Color(0xFFF38181),
        Color(0xFFAA96DA), Color(0xFFFCBAD3)
    )

    // Pixel grid pattern
    val grid = arrayOf(
        intArrayOf(0, 0, 1, 1, 1, 1, 0, 0),
        intArrayOf(0, 1, 2, 2, 2, 2, 1, 0),
        intArrayOf(1, 2, 3, 3, 3, 3, 2, 1),
        intArrayOf(1, 2, 3, 4, 4, 3, 2, 1),
        intArrayOf(1, 2, 3, 4, 4, 3, 2, 1),
        intArrayOf(1, 2, 3, 3, 3, 3, 2, 1),
        intArrayOf(0, 1, 2, 2, 2, 2, 1, 0),
        intArrayOf(0, 0, 1, 1, 1, 1, 0, 0)
    )

    val totalPixels = grid.sumOf { row -> row.count { it > 0 } }
    val pixelsToShow = (totalPixels * progress).toInt()
    var count = 0

    for (row in grid.indices) {
        for (col in grid[row].indices) {
            if (grid[row][col] > 0) {
                count++
                if (count <= pixelsToShow) {
                    val colorIdx = grid[row][col] % colors.size
                    drawRect(
                        color = colors[colorIdx],
                        topLeft = Offset(col * pixelSize, row * pixelSize),
                        size = androidx.compose.ui.geometry.Size(pixelSize * 0.9f, pixelSize * 0.9f)
                    )
                }
            }
        }
    }
}
