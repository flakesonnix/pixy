package com.pixelcolor.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val puzzleId: String,
    val filledPixels: Map<String, Boolean> = emptyMap(),
    val completionPercent: Float = 0f,
    val timeSpentMs: Long = 0L,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val hintsUsed: Int = 0,
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0
) {
    val accuracyRate: Float
        get() = if (totalAttempts > 0) correctAttempts.toFloat() / totalAttempts else 1f

    val filledCount: Int get() = filledPixels.count { it.value }
}
