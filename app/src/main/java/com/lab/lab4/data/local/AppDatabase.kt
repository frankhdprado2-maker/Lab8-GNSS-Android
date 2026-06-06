package com.lab.lab4.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lab.lab4.data.local.dao.GpsGoogleDao
import com.lab.lab4.data.local.dao.GpsSensorsDao
import com.lab.lab4.data.local.entity.GpsGoogleEntity
import com.lab.lab4.data.local.entity.GpsSensorsEntity

@Database(
    entities = [GpsGoogleEntity::class, GpsSensorsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsGoogleDao(): GpsGoogleDao
    abstract fun gpsSensorsDao(): GpsSensorsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lab4_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}