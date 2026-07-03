package com.lab.lab4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lab.lab4.data.local.entity.MediaEntity
import com.lab.lab4.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MediaViewModel(private val mediaRepository: MediaRepository) : ViewModel() {
    val media = mediaRepository.allMedia.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun newPhotoFile(): File = mediaRepository.newPhotoFile()

    fun newVideoFile(): File = mediaRepository.newVideoFile()

    fun registerPhoto(file: File) {
        viewModelScope.launch {
            mediaRepository.registerPhoto(file)
        }
    }

    fun registerVideo(file: File) {
        viewModelScope.launch {
            mediaRepository.registerVideo(file)
        }
    }

    fun delete(item: MediaEntity) {
        viewModelScope.launch {
            mediaRepository.delete(item)
        }
    }

    fun deletePending(file: File?) {
        mediaRepository.deletePending(file)
    }

    class Factory(private val mediaRepository: MediaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MediaViewModel(mediaRepository) as T
        }
    }
}
