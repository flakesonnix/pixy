package com.pixelcolor.app.data.database.dao

import androidx.room.*
import com.pixelcolor.app.data.database.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE puzzleId = :puzzleId")
    suspend fun getProgress(puzzleId: String): ProgressEntity?

    @Query("SELECT * FROM progress WHERE puzzleId = :puzzleId")
    fun getProgressFlow(puzzleId: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE isCompleted = 1")
    fun getCompletedProgress(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE isCompleted = 0 AND completionPercent > 0")
    fun getInProgressPuzzles(): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Update
    suspend fun updateProgress(progress: ProgressEntity)

    @Query("DELETE FROM progress WHERE puzzleId = :puzzleId")
    suspend fun deleteProgress(puzzleId: String)

    @Query("DELETE FROM progress")
    suspend fun deleteAllProgress()
}
