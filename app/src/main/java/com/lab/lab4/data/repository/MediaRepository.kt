package com.lab.lab4.data.repository

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.lab.lab4.data.local.FileStorageManager
import com.lab.lab4.data.local.dao.MediaDao
import com.lab.lab4.data.local.entity.MediaEntity
import com.lab.lab4.data.local.entity.MediaType
import java.io.File

class MediaRepository(
    private val mediaDao: MediaDao,
    private val fileStorage: FileStorageManager
) {
    val allMedia = mediaDao.observeAll()
    val photoCount = mediaDao.observePhotoCount()
    val videoCount = mediaDao.observeVideoCount()

    fun observeByType(type: MediaType) = mediaDao.observeByType(type.name)

    fun newPhotoFile(): File = fileStorage.newPhotoFile()

    fun newVideoFile(): File = fileStorage.newVideoFile()

    suspend fun registerPhoto(file: File) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        mediaDao.insert(
            MediaEntity(
                filePath = file.absolutePath,
                type = MediaType.PHOTO.name,
                sizeBytes = fileStorage.fileSize(file.absolutePath),
                durationMs = null,
                widthPx = options.outWidth.takeIf { it > 0 },
                heightPx = options.outHeight.takeIf { it > 0 },
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun registerVideo(file: File) {
        val metadata = readVideoMetadata(file)
        mediaDao.insert(
            MediaEntity(
                filePath = file.absolutePath,
                type = MediaType.VIDEO.name,
                sizeBytes = fileStorage.fileSize(file.absolutePath),
                durationMs = metadata.durationMs,
                widthPx = metadata.widthPx,
                heightPx = metadata.heightPx,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun delete(item: MediaEntity) {
        mediaDao.delete(item)
        fileStorage.deleteFile(item.filePath)
    }

    fun deletePending(file: File?) {
        file?.absolutePath?.let { fileStorage.deleteFile(it) }
    }

    private fun readVideoMetadata(file: File): VideoMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            VideoMetadata(
                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull(),
                widthPx = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull(),
                heightPx = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            )
        } catch (_: RuntimeException) {
            VideoMetadata()
        } finally {
            retriever.release()
        }
    }

    private data class VideoMetadata(
        val durationMs: Long? = null,
        val widthPx: Int? = null,
        val heightPx: Int? = null
    )
}
