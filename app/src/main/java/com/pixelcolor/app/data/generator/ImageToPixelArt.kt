package com.pixelcolor.app.data.generator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import com.pixelcolor.app.domain.model.PaletteColor
import com.pixelcolor.app.domain.model.Pixel
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

data class GeneratedPuzzle(
    val pixels: List<Pixel>,
    val palette: List<PaletteColor>,
    val width: Int,
    val height: Int
)

object ImageToPixelArt {

    fun fromStream(
        input: InputStream,
        gridWidth: Int,
        gridHeight: Int,
        maxColors: Int = 12
    ): GeneratedPuzzle {
        val raw = BitmapFactory.decodeStream(input)
            ?: throw IllegalArgumentException("Cannot decode image")

        val scaled = Bitmap.createScaledBitmap(raw, gridWidth, gridHeight, true)
        if (raw !== scaled) raw.recycle()

        // Sample all pixel colors
        val colorSamples = mutableListOf<Int>()
        for (y in 0 until gridHeight) {
            for (x in 0 until gridWidth) {
                colorSamples.add(scaled.getPixel(x, y))
            }
        }
        scaled.recycle()

        // Quantize to palette using k-means
        val paletteInts = kMeansQuantize(colorSamples, maxColors)

        // Build PaletteColor list with hex + label
        val palette = paletteInts.mapIndexed { idx, argb ->
            PaletteColor(
                id = idx + 1,
                hexColor = "#%06X".format(0xFFFFFF and argb),
                label = "${idx + 1}"
            )
        }

        // Map each pixel to nearest palette color
        val pixels = mutableListOf<Pixel>()
        for (y in 0 until gridHeight) {
            for (x in 0 until gridWidth) {
                val sampleColor = colorSamples[y * gridWidth + x]
                val nearestId = nearestPaletteIndex(sampleColor, paletteInts) + 1
                pixels.add(Pixel(x, y, nearestId))
            }
        }

        return GeneratedPuzzle(pixels, palette, gridWidth, gridHeight)
    }

    fun fromBitmap(
        bitmap: Bitmap,
        gridWidth: Int,
        gridHeight: Int,
        maxColors: Int = 12
    ): GeneratedPuzzle {
        val scaled = Bitmap.createScaledBitmap(bitmap, gridWidth, gridHeight, true)

        val colorSamples = mutableListOf<Int>()
        for (y in 0 until gridHeight) {
            for (x in 0 until gridWidth) {
                colorSamples.add(scaled.getPixel(x, y))
            }
        }
        if (scaled !== bitmap) scaled.recycle()

        val paletteInts = kMeansQuantize(colorSamples, maxColors)

        val palette = paletteInts.mapIndexed { idx, argb ->
            PaletteColor(
                id = idx + 1,
                hexColor = "#%06X".format(0xFFFFFF and argb),
                label = "${idx + 1}"
            )
        }

        val pixels = mutableListOf<Pixel>()
        for (y in 0 until gridHeight) {
            for (x in 0 until gridWidth) {
                val sampleColor = colorSamples[y * gridWidth + x]
                val nearestId = nearestPaletteIndex(sampleColor, paletteInts) + 1
                pixels.add(Pixel(x, y, nearestId))
            }
        }

        return GeneratedPuzzle(pixels, palette, gridWidth, gridHeight)
    }

    // --- K-Means color quantization ---

    private fun kMeansQuantize(colors: List<Int>, k: Int, iterations: Int = 12): List<Int> {
        if (colors.isEmpty()) return listOf(AndroidColor.BLACK)
        val kAdj = min(k, colors.distinct().size)
        if (kAdj <= 1) return listOf(averageColor(colors))

        // Initialize centroids by picking evenly-spaced samples
        val step = colors.size / kAdj
        var centroids = (0 until kAdj).map { i ->
            val c = colors[(i * step).coerceIn(colors.indices)]
            Triple(AndroidColor.red(c), AndroidColor.green(c), AndroidColor.blue(c))
        }

        var assignments = IntArray(colors.size)

        repeat(iterations) {
            // Assign each color to nearest centroid
            val sums = Array(kAdj) { longArrayOf(0, 0, 0, 0) } // r, g, b, count
            for (ci in colors.indices) {
                val c = colors[ci]
                val r = AndroidColor.red(c)
                val g = AndroidColor.green(c)
                val b = AndroidColor.blue(c)
                var bestIdx = 0
                var bestDist = Int.MAX_VALUE
                for (ki in centroids.indices) {
                    val dist = colorDistance(r, g, b, centroids[ki])
                    if (dist < bestDist) {
                        bestDist = dist
                        bestIdx = ki
                    }
                }
                assignments[ci] = bestIdx
                sums[bestIdx][0] += r
                sums[bestIdx][1] += g
                sums[bestIdx][2] += b
                sums[bestIdx][3]++
            }

            // Recompute centroids
            centroids = centroids.indices.map { ki ->
                if (sums[ki][3] > 0) {
                    Triple(
                        (sums[ki][0] / sums[ki][3]).toInt(),
                        (sums[ki][1] / sums[ki][3]).toInt(),
                        (sums[ki][2] / sums[ki][3]).toInt()
                    )
                } else centroids[ki]
            }
        }

        return centroids.map { (r, g, b) -> AndroidColor.rgb(r, g, b) }
    }

    private fun colorDistance(r: Int, g: Int, b: Int, centroid: Triple<Int, Int, Int>): Int {
        val dr = r - centroid.first
        val dg = g - centroid.second
        val db = b - centroid.third
        // Weighted Euclidean (perceptual)
        return 2 * dr * dr + 4 * dg * dg + 3 * db * db
    }

    private fun nearestPaletteIndex(color: Int, palette: List<Int>): Int {
        val r = AndroidColor.red(color)
        val g = AndroidColor.green(color)
        val b = AndroidColor.blue(color)
        var best = 0
        var bestDist = Int.MAX_VALUE
        for (i in palette.indices) {
            val d = colorDistance(r, g, b, Triple(
                AndroidColor.red(palette[i]),
                AndroidColor.green(palette[i]),
                AndroidColor.blue(palette[i])
            ))
            if (d < bestDist) {
                bestDist = d
                best = i
            }
        }
        return best
    }

    private fun averageColor(colors: List<Int>): Int {
        var r = 0L; var g = 0L; var b = 0L
        for (c in colors) {
            r += AndroidColor.red(c)
            g += AndroidColor.green(c)
            b += AndroidColor.blue(c)
        }
        val n = colors.size.toLong()
        return AndroidColor.rgb((r / n).toInt(), (g / n).toInt(), (b / n).toInt())
    }
}
