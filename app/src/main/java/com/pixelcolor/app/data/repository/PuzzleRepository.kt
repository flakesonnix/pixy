package com.pixelcolor.app.data.repository

import android.graphics.Bitmap
import com.pixelcolor.app.data.database.PixelColorDatabase
import com.pixelcolor.app.data.database.entity.PuzzleEntity
import com.pixelcolor.app.data.database.entity.ProgressEntity
import com.pixelcolor.app.data.generator.ImageToPixelArt
import com.pixelcolor.app.data.generator.PuzzleFactory
import com.pixelcolor.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PuzzleRepository(
    private val database: PixelColorDatabase
) {
    private val puzzleDao = database.puzzleDao()
    private val progressDao = database.progressDao()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun initializePuzzles() {
        val existing = puzzleDao.getAllPuzzles().first()
        if (existing.isEmpty()) {
            val puzzles = PuzzleFactory.allBundled()
            puzzleDao.insertPuzzles(puzzles.map { it.toEntity() })
        }
    }

    fun getAllPuzzles(): Flow<List<PixelPuzzle>> =
        puzzleDao.getAllPuzzles().map { it.map { e -> e.toDomain() } }

    fun getPuzzlesByCategory(category: Category): Flow<List<PixelPuzzle>> =
        puzzleDao.getPuzzlesByCategory(category.name).map { it.map { e -> e.toDomain() } }

    suspend fun getPuzzleById(id: String): PixelPuzzle? =
        puzzleDao.getPuzzleById(id)?.toDomain()

    fun getProgress(puzzleId: String): Flow<UserProgress?> =
        progressDao.getProgressFlow(puzzleId).map { it?.toDomain() }

    suspend fun getProgressSync(puzzleId: String): UserProgress? =
        progressDao.getProgress(puzzleId)?.toDomain()

    suspend fun saveProgress(progress: UserProgress) {
        val existing = progressDao.getProgress(progress.puzzleId)
        if (existing != null) progressDao.updateProgress(progress.toEntity())
        else progressDao.insertProgress(progress.toEntity())
    }

    fun getAllProgress(): Flow<List<UserProgress>> =
        progressDao.getAllProgress().map { it.map { e -> e.toDomain() } }

    fun getCompletedPuzzles(): Flow<List<UserProgress>> =
        progressDao.getCompletedProgress().map { it.map { e -> e.toDomain() } }

    fun getInProgressPuzzles(): Flow<List<UserProgress>> =
        progressDao.getInProgressPuzzles().map { it.map { e -> e.toDomain() } }

    suspend fun resetProgress(puzzleId: String) = progressDao.deleteProgress(puzzleId)

    suspend fun createFromImage(
        bitmap: Bitmap,
        title: String,
        gridW: Int,
        gridH: Int,
        maxColors: Int,
        difficulty: Difficulty,
        category: Category
    ): PixelPuzzle {
        val result = ImageToPixelArt.fromBitmap(bitmap, gridW, gridH, maxColors)
        val puzzle = PixelPuzzle(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            gridWidth = gridW,
            gridHeight = gridH,
            palette = result.palette,
            pixels = result.pixels,
            difficulty = difficulty,
            category = category
        )
        puzzleDao.insertPuzzle(puzzle.toEntity())
        return puzzle
    }

    // --- mapping helpers ---

    private fun PixelPuzzle.toEntity() = PuzzleEntity(
        id = id, title = title, gridWidth = gridWidth, gridHeight = gridHeight,
        difficulty = difficulty.name, category = category.name,
        isDailyPuzzle = isDailyPuzzle, thumbnailRes = thumbnailRes,
        paletteJson = json.encodeToString(palette),
        pixelsJson = json.encodeToString(pixels)
    )

    private fun PuzzleEntity.toDomain() = PixelPuzzle(
        id = id, title = title, gridWidth = gridWidth, gridHeight = gridHeight,
        difficulty = Difficulty.valueOf(difficulty), category = Category.valueOf(category),
        isDailyPuzzle = isDailyPuzzle, thumbnailRes = thumbnailRes,
        palette = json.decodeFromString(paletteJson),
        pixels = json.decodeFromString(pixelsJson)
    )

    private fun UserProgress.toEntity() = ProgressEntity(
        puzzleId = puzzleId,
        filledPixelsJson = json.encodeToString(filledPixels),
        completionPercent = completionPercent, timeSpentMs = timeSpentMs,
        isCompleted = isCompleted, completedAt = completedAt,
        hintsUsed = hintsUsed, totalAttempts = totalAttempts, correctAttempts = correctAttempts
    )

    private fun ProgressEntity.toDomain() = UserProgress(
        puzzleId = puzzleId,
        filledPixels = json.decodeFromString(filledPixelsJson),
        completionPercent = completionPercent, timeSpentMs = timeSpentMs,
        isCompleted = isCompleted, completedAt = completedAt,
        hintsUsed = hintsUsed, totalAttempts = totalAttempts, correctAttempts = correctAttempts
    )
}
