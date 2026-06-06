package com.lab.lab4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_sensors")
data class GpsSensorsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double?,
    val longitude: Double?,
    val provider: String,
    val altitude: Double?,
    val timestamp: Long
)