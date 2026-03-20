package com.pixelcolor.app.data.repository

import com.pixelcolor.app.data.database.PixelColorDatabase
import com.pixelcolor.app.domain.model.PixelPuzzle
import com.pixelcolor.app.domain.model.Difficulty
import com.pixelcolor.app.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.LocalDate

class DailyPuzzleManager(
    private val database: PixelColorDatabase
) {
    private val puzzleDao = database.puzzleDao()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getTodayPuzzle(): PixelPuzzle? {
        val allList = puzzleDao.getAllPuzzles().first()

        if (allList.isEmpty()) return null

        val today = LocalDate.now()
        val seed = today.toEpochDay()
        val index = ((seed % allList.size) + allList.size) % allList.size
        val entity = allList[index.toInt()]

        return PixelPuzzle(
            id = entity.id,
            title = entity.title,
            gridWidth = entity.gridWidth,
            gridHeight = entity.gridHeight,
            difficulty = Difficulty.valueOf(entity.difficulty),
            category = Category.valueOf(entity.category),
            isDailyPuzzle = true,
            thumbnailRes = entity.thumbnailRes,
            palette = json.decodeFromString(entity.paletteJson),
            pixels = json.decodeFromString(entity.pixelsJson)
        )
    }

    fun getNextPuzzleTime(): Long {
        val tomorrow = LocalDate.now().plusDays(1)
        val midnight = java.time.LocalDateTime.of(tomorrow, java.time.LocalTime.MIDNIGHT)
        return midnight.atZone(java.time.ZoneId.systemDefault()).toEpochMilli()
    }
}
