package com.pixelcolor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: Int // 0 = rect, 1 = circle
)

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    durationMs: Int = 3000
) {
    val colors = listOf(
        Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFF4ECDC4),
        Color(0xFFFF8C42), Color(0xFF95E1D3), Color(0xFFF38181),
        Color(0xFFAA96DA), Color(0xFFFCBAD3), Color(0xFF6C5CE7),
        Color(0xFF00B894)
    )

    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = -0.1f - Random.nextFloat() * 0.3f,
                color = colors[Random.nextInt(colors.size)],
                size = 4f + Random.nextFloat() * 8f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                speedY = 0.3f + Random.nextFloat() * 0.4f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f,
                shape = Random.nextInt(2)
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
        )
    }

    val progress = animProgress.value
    val fadeOut = if (progress > 0.7f) 1f - ((progress - 0.7f) / 0.3f) else 1f

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (particle in particles) {
            val t = progress * 3f // acceleration
            val px = (particle.x + particle.speedX * t) * w
            val py = (particle.y + particle.speedY * t + 0.5f * 0.3f * t * t) * h
            val rot = particle.rotation + particle.rotationSpeed * t

            // Wiggle
            val wiggleX = sin(t * 5f + particle.x * 10f) * 10f

            if (py < h + 50f && py > -50f) {
                val color = particle.color.copy(alpha = fadeOut.coerceIn(0f, 1f))

                drawRect(
                    color = color,
                    topLeft = Offset(px + wiggleX - particle.size / 2, py - particle.size / 2),
                    size = Size(particle.size, particle.size * 0.6f),
                )
            }
        }
    }
}
