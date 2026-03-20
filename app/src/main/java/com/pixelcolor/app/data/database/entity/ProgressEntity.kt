package com.pixelcolor.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey
    val puzzleId: String,
    val filledPixelsJson: String,
    val completionPercent: Float,
    val timeSpentMs: Long,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val hintsUsed: Int,
    val totalAttempts: Int,
    val correctAttempts: Int
)
