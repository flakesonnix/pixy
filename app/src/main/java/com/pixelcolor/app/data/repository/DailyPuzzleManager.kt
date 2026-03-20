package com.pixelcolor.app.data.repository

import com.pixelcolor.app.data.database.PixelColorDatabase
import com.pixelcolor.app.domain.model.PixelPuzzle
import java.time.LocalDate

class DailyPuzzleManager(
    private val database: PixelColorDatabase
) {
    private val puzzleDao = database.puzzleDao()

    suspend fun getTodayPuzzle(): PixelPuzzle? {
        val allPuzzles = puzzleDao.getAllPuzzles()
        // Use date-seeded selection
        val today = LocalDate.now()
        val seed = today.toEpochDay()
        val allList = allPuzzles.let { flow ->
            var result: List<com.pixelcolor.app.data.database.entity.PuzzleEntity>? = null
            flow.collect { result = it }
            result ?: emptyList()
        }

        if (allList.isEmpty()) return null

        val index = (seed % allList.size).toInt().let { if (it < 0) -it else it }
        val entity = allList[index]
        return entity.let { e ->
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            PixelPuzzle(
                id = e.id,
                title = e.title,
                gridWidth = e.gridWidth,
                gridHeight = e.gridHeight,
                difficulty = com.pixelcolor.app.domain.model.Difficulty.valueOf(e.difficulty),
                category = com.pixelcolor.app.domain.model.Category.valueOf(e.category),
                isDailyPuzzle = true,
                thumbnailRes = e.thumbnailRes,
                palette = json.decodeFromString(e.paletteJson),
                pixels = json.decodeFromString(e.pixelsJson)
            )
        }
    }

    fun getNextPuzzleTime(): Long {
        val tomorrow = LocalDate.now().plusDays(1)
        val midnight = java.time.LocalDateTime.of(tomorrow, java.time.LocalTime.MIDNIGHT)
        return midnight.atZone(java.time.ZoneId.systemDefault()).toEpochMilli()
    }
}
