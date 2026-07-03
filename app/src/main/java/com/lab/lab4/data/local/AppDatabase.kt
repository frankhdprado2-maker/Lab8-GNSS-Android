package com.lab.lab4.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lab.lab4.data.local.dao.AudioDao
import com.lab.lab4.data.local.dao.GpsGoogleDao
import com.lab.lab4.data.local.dao.GpsSensorsDao
import com.lab.lab4.data.local.dao.MediaDao
import com.lab.lab4.data.local.entity.AudioEntity
import com.lab.lab4.data.local.entity.GpsGoogleEntity
import com.lab.lab4.data.local.entity.GpsSensorsEntity
import com.lab.lab4.data.local.entity.MediaEntity

@Database(
    entities = [
        GpsGoogleEntity::class,
        GpsSensorsEntity::class,
        MediaEntity::class,
        AudioEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsGoogleDao(): GpsGoogleDao
    abstract fun gpsSensorsDao(): GpsSensorsDao
    abstract fun mediaDao(): MediaDao
    abstract fun audioDao(): AudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lab4_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
