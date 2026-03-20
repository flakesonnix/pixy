package com.pixelcolor.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val difficulty: String,
    val category: String,
    val isDailyPuzzle: Boolean,
    val thumbnailRes: String,
    val paletteJson: String,
    val pixelsJson: String
)
