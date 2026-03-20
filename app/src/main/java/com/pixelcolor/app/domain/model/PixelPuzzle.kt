package com.pixelcolor.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PixelPuzzle(
    val id: String,
    val title: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val palette: List<PaletteColor>,
    val pixels: List<Pixel>,
    val difficulty: Difficulty,
    val category: Category,
    val isDailyPuzzle: Boolean = false,
    val thumbnailRes: String = ""
) {
    val totalPixels: Int get() = pixels.size

    fun getPixelAt(x: Int, y: Int): Pixel? = pixels.find { it.x == x && it.y == y }

    fun getPixelsByColorId(colorId: Int): List<Pixel> = pixels.filter { it.colorId == colorId }

    fun getColorCount(colorId: Int): Int = pixels.count { it.colorId == colorId }

    fun getPaletteColor(id: Int): PaletteColor? = palette.find { it.id == id }
}
