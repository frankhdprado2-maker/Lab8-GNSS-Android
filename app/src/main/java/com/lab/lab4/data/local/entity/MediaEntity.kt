package com.lab.lab4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType { PHOTO, VIDEO }

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val type: String,
    val sizeBytes: Long,
    val durationMs: Long?,
    val widthPx: Int?,
    val heightPx: Int?,
    val timestamp: Long
)
