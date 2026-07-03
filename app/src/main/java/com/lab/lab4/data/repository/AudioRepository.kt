package com.lab.lab4.data.repository

import com.lab.lab4.data.local.FileStorageManager
import com.lab.lab4.data.local.dao.AudioDao
import com.lab.lab4.data.local.entity.AudioEntity
import java.io.File

class AudioRepository(
    private val audioDao: AudioDao,
    private val fileStorage: FileStorageManager
) {
    val allAudios = audioDao.observeAll()
    val count = audioDao.observeCount()

    fun newAudioFile(extension: String = "m4a"): File = fileStorage.newAudioFile(extension)

    suspend fun registerAudio(file: File, durationMs: Long, format: String = "m4a") {
        audioDao.insert(
            AudioEntity(
                filePath = file.absolutePath,
                durationMs = durationMs,
                sizeBytes = fileStorage.fileSize(file.absolutePath),
                format = format,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun delete(item: AudioEntity) {
        audioDao.delete(item)
        fileStorage.deleteFile(item.filePath)
    }

    fun deletePending(file: File?) {
        file?.absolutePath?.let { fileStorage.deleteFile(it) }
    }
}
