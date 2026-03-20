package com.pixelcolor.app.data.generator

import com.pixelcolor.app.domain.model.Pixel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PixelArtGenerator(private val width: Int, private val height: Int) {

    private val grid = Array(height) { IntArray(width) { 0 } }

    fun fill(colorId: Int): PixelArtGenerator {
        for (y in 0 until height)
            for (x in 0 until width)
                grid[y][x] = colorId
        return this
    }

    fun rect(x1: Int, y1: Int, x2: Int, y2: Int, colorId: Int): PixelArtGenerator {
        for (y in max(0, y1)..min(height - 1, y2))
            for (x in max(0, x1)..min(width - 1, x2))
                grid[y][x] = colorId
        return this
    }

    fun circle(cx: Int, cy: Int, r: Int, colorId: Int, filled: Boolean = true): PixelArtGenerator {
        for (y in max(0, cy - r - 1)..min(height - 1, cy + r + 1)) {
            for (x in max(0, cx - r - 1)..min(width - 1, cx + r + 1)) {
                val dist = sqrt(((x - cx) * (x - cx) + (y - cy) * (y - cy)).toDouble())
                if (filled) {
                    if (dist <= r) grid[y][x] = colorId
                } else {
                    if (dist <= r && dist >= r - 1.0) grid[y][x] = colorId
                }
            }
        }
        return this
    }

    fun ellipse(cx: Int, cy: Int, rx: Int, ry: Int, colorId: Int, filled: Boolean = true): PixelArtGenerator {
        for (y in max(0, cy - ry - 1)..min(height - 1, cy + ry + 1)) {
            for (x in max(0, cx - rx - 1)..min(width - 1, cx + rx + 1)) {
                val dx = (x - cx).toDouble() / rx
                val dy = (y - cy).toDouble() / ry
                val dist = dx * dx + dy * dy
                if (filled) {
                    if (dist <= 1.0) grid[y][x] = colorId
                } else {
                    if (dist <= 1.0 && dist >= 0.7) grid[y][x] = colorId
                }
            }
        }
        return this
    }

    fun line(x1: Int, y1: Int, x2: Int, y2: Int, colorId: Int, thickness: Int = 1): PixelArtGenerator {
        val dx = abs(x2 - x1)
        val dy = -abs(y2 - y1)
        val sx = if (x1 < x2) 1 else -1
        val sy = if (y1 < y2) 1 else -1
        var err = dx + dy
        var cx = x1
        var cy = y1
        while (true) {
            for (ty in -thickness / 2..thickness / 2)
                for (tx in -thickness / 2..thickness / 2) {
                    val px = cx + tx
                    val py = cy + ty
                    if (px in 0 until width && py in 0 until height)
                        grid[py][px] = colorId
                }
            if (cx == x2 && cy == y2) break
            val e2 = 2 * err
            if (e2 >= dy) { err += dy; cx += sx }
            if (e2 <= dx) { err += dx; cy += sy }
        }
        return this
    }

    fun triangle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int, colorId: Int): PixelArtGenerator {
        val minX = max(0, minOf(x1, x2, x3))
        val maxX = min(width - 1, maxOf(x1, x2, x3))
        val minY = max(0, minOf(y1, y2, y3))
        val maxY = min(height - 1, maxOf(y1, y2, y3))

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val d1 = sign(x, y, x1, y1, x2, y2)
                val d2 = sign(x, y, x2, y2, x3, y3)
                val d3 = sign(x, y, x3, y3, x1, y1)
                val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
                val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)
                if (!(hasNeg && hasPos)) grid[y][x] = colorId
            }
        }
        return this
    }

    fun diamond(cx: Int, cy: Int, r: Int, colorId: Int): PixelArtGenerator {
        for (y in max(0, cy - r)..min(height - 1, cy + r)) {
            for (x in max(0, cx - r)..min(width - 1, cx + r)) {
                if (abs(x - cx) + abs(y - cy) <= r) grid[y][x] = colorId
            }
        }
        return this
    }

    fun star(cx: Int, cy: Int, outerR: Int, innerR: Int, points: Int, colorId: Int): PixelArtGenerator {
        val vertices = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until points * 2) {
            val angle = Math.PI * i / points - Math.PI / 2
            val r = if (i % 2 == 0) outerR else innerR
            val vx = (cx + r * kotlin.math.cos(angle)).roundToInt()
            val vy = (cy + r * kotlin.math.sin(angle)).roundToInt()
            vertices.add(vx to vy)
        }
        // Scanline fill
        for (y in max(0, cy - outerR)..min(height - 1, cy + outerR)) {
            val intersections = mutableListOf<Int>()
            for (i in vertices.indices) {
                val (x1, y1) = vertices[i]
                val (x2, y2) = vertices[(i + 1) % vertices.size]
                if ((y1 <= y && y < y2) || (y2 <= y && y < y1)) {
                    val xIntersect = x1 + (y - y1) * (x2 - x1) / (y2 - y1)
                    intersections.add(xIntersect)
                }
            }
            intersections.sort()
            for (i in intersections.indices step 2) {
                if (i + 1 < intersections.size) {
                    for (x in max(0, intersections[i])..min(width - 1, intersections[i + 1]))
                        grid[y][x] = colorId
                }
            }
        }
        return this
    }

    fun arc(cx: Int, cy: Int, r: Int, startDeg: Int, endDeg: Int, colorId: Int, thickness: Int = 2): PixelArtGenerator {
        for (deg in startDeg..endDeg step 2) {
            val rad = Math.toRadians(deg.toDouble())
            val x = (cx + r * kotlin.math.cos(rad)).roundToInt()
            val y = (cy + r * kotlin.math.sin(rad)).roundToInt()
            for (ty in -thickness / 2..thickness / 2)
                for (tx in -thickness / 2..thickness / 2) {
                    val px = x + tx
                    val py = y + ty
                    if (px in 0 until width && py in 0 until height)
                        grid[py][px] = colorId
                }
        }
        return this
    }

    fun gradientVertical(colorTop: Int, colorBottom: Int): PixelArtGenerator {
        for (y in 0 until height) {
            val t = y.toDouble() / (height - 1)
            val colorId = if (t < 0.5) colorTop else colorBottom
            for (x in 0 until width) grid[y][x] = colorId
        }
        return this
    }

    fun noise(probability: Double, colorId: Int): PixelArtGenerator {
        val rng = kotlin.random.Random(42)
        for (y in 0 until height)
            for (x in 0 until width)
                if (rng.nextDouble() < probability) grid[y][x] = colorId
        return this
    }

    fun build(): List<Pixel> {
        val pixels = mutableListOf<Pixel>()
        for (y in 0 until height)
            for (x in 0 until width)
                if (grid[y][x] != 0)
                    pixels.add(Pixel(x, y, grid[y][x]))
        return pixels
    }

    private fun sign(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int): Int {
        return (x1 - x3) * (y2 - y3) - (x2 - x3) * (y1 - y3)
    }
}
