package com.lab.lab4.data.local

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileStorageManager(context: Context) {
    private val photosDir = File(context.filesDir, "photos").also { it.mkdirs() }
    private val videosDir = File(context.filesDir, "videos").also { it.mkdirs() }
    private val audiosDir = File(context.filesDir, "audios").also { it.mkdirs() }

    fun newPhotoFile(): File = File(photosDir, "photo_${timestamp()}.jpg")

    fun newVideoFile(): File = File(videosDir, "video_${timestamp()}.mp4")

    fun newAudioFile(extension: String = "m4a"): File =
        File(audiosDir, "audio_${timestamp()}.${extension.trimStart('.')}")

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return !file.exists() || file.delete()
    }

    fun fileSize(path: String): Long {
        val file = File(path)
        return if (file.exists()) file.length() else 0L
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
}
