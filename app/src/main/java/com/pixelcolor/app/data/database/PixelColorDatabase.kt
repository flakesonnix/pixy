package com.pixelcolor.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pixelcolor.app.data.database.dao.PuzzleDao
import com.pixelcolor.app.data.database.dao.ProgressDao
import com.pixelcolor.app.data.database.entity.PuzzleEntity
import com.pixelcolor.app.data.database.entity.ProgressEntity

@Database(
    entities = [PuzzleEntity::class, ProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PixelColorDatabase : RoomDatabase() {

    abstract fun puzzleDao(): PuzzleDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: PixelColorDatabase? = null

        fun getInstance(context: Context): PixelColorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PixelColorDatabase::class.java,
                    "pixel_color_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
