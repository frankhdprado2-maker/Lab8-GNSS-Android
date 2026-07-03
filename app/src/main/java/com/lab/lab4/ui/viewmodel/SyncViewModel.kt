package com.lab.lab4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lab.lab4.data.repository.AudioRepository
import com.lab.lab4.data.repository.GpsRepository
import com.lab.lab4.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SyncCounts(
    val gpsGoogle: Int = 0,
    val gpsSensors: Int = 0,
    val photos: Int = 0,
    val videos: Int = 0,
    val audios: Int = 0
) {
    val total: Int get() = gpsGoogle + gpsSensors + photos + videos + audios
}

class SyncViewModel(
    gpsRepository: GpsRepository,
    mediaRepository: MediaRepository,
    audioRepository: AudioRepository
) : ViewModel() {
    val counts = combine(
        gpsRepository.googleCount,
        gpsRepository.sensorsCount,
        mediaRepository.photoCount,
        mediaRepository.videoCount,
        audioRepository.count
    ) { gpsGoogle, gpsSensors, photos, videos, audios ->
        SyncCounts(
            gpsGoogle = gpsGoogle,
            gpsSensors = gpsSensors,
            photos = photos,
            videos = videos,
            audios = audios
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SyncCounts()
    )

    class Factory(
        private val gpsRepository: GpsRepository,
        private val mediaRepository: MediaRepository,
        private val audioRepository: AudioRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SyncViewModel(gpsRepository, mediaRepository, audioRepository) as T
        }
    }
}
