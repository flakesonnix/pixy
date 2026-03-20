package com.pixelcolor.app.data.database.dao

import androidx.room.*
import com.pixelcolor.app.data.database.entity.PuzzleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzles")
    fun getAllPuzzles(): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM puzzles WHERE id = :id")
    suspend fun getPuzzleById(id: String): PuzzleEntity?

    @Query("SELECT * FROM puzzles WHERE category = :category")
    fun getPuzzlesByCategory(category: String): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM puzzles WHERE isDailyPuzzle = 1")
    suspend fun getDailyPuzzle(): PuzzleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: PuzzleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzles(puzzles: List<PuzzleEntity>)

    @Delete
    suspend fun deletePuzzle(puzzle: PuzzleEntity)

    @Query("DELETE FROM puzzles")
    suspend fun deleteAllPuzzles()
}
