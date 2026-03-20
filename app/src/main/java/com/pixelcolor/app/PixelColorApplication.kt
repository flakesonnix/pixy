package com.pixelcolor.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.pixelcolor.app.data.database.PixelColorDatabase

class PixelColorApplication : Application(), Configuration.Provider {

    lateinit var database: PixelColorDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = PixelColorDatabase.getInstance(this)
        WorkManager.initialize(this, workManagerConfiguration)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
