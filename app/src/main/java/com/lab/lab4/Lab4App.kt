package com.lab.lab4

import android.app.Application
import com.lab.lab4.data.local.AppDatabase
import com.lab.lab4.data.local.FileStorageManager
import com.lab.lab4.data.repository.AudioRepository
import com.lab.lab4.data.repository.GpsRepository
import com.lab.lab4.data.repository.MediaRepository
import com.lab.lab4.data.session.SessionManager

class Lab4App : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val fileStorage: FileStorageManager by lazy { FileStorageManager(this) }
    val sessionManager: SessionManager by lazy { SessionManager(this) }
    val gpsRepository: GpsRepository by lazy {
        GpsRepository(database.gpsGoogleDao(), database.gpsSensorsDao())
    }
    val mediaRepository: MediaRepository by lazy {
        MediaRepository(database.mediaDao(), fileStorage)
    }
    val audioRepository: AudioRepository by lazy {
        AudioRepository(database.audioDao(), fileStorage)
    }
}
