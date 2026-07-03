package com.lab.lab4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio")
data class AudioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val format: String,
    val timestamp: Long
)
