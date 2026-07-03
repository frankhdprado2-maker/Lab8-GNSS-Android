package com.lab.lab4.ui.viewmodel

import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lab.lab4.data.local.entity.AudioEntity
import com.lab.lab4.data.repository.AudioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioViewModel(private val audioRepository: AudioRepository) : ViewModel() {
    val audios = audioRepository.allAudios.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var startedAt: Long = 0L
    private var timerJob: Job? = null

    fun startRecording() {
        if (_isRecording.value) return

        val file = audioRepository.newAudioFile("m4a")
        val newRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        recorder = newRecorder
        recordingFile = file
        startedAt = System.currentTimeMillis()
        _elapsedMs.value = 0L
        _isRecording.value = true

        timerJob = viewModelScope.launch {
            while (isActive && _isRecording.value) {
                _elapsedMs.value = System.currentTimeMillis() - startedAt
                delay(250L)
            }
        }
    }

    fun stopRecording() {
        val file = recordingFile
        val durationMs = System.currentTimeMillis() - startedAt
        releaseRecorder(stop = true)
        _isRecording.value = false
        _elapsedMs.value = 0L

        if (file == null || durationMs < 1_000L) {
            audioRepository.deletePending(file)
            return
        }

        viewModelScope.launch {
            audioRepository.registerAudio(file, durationMs, "m4a")
        }
    }

    fun delete(item: AudioEntity) {
        viewModelScope.launch {
            audioRepository.delete(item)
        }
    }

    override fun onCleared() {
        if (_isRecording.value) {
            releaseRecorder(stop = true)
            audioRepository.deletePending(recordingFile)
        } else {
            releaseRecorder(stop = false)
        }
        super.onCleared()
    }

    private fun releaseRecorder(stop: Boolean) {
        timerJob?.cancel()
        timerJob = null
        recorder?.runCatching {
            if (stop) stop()
            reset()
            release()
        }
        recorder = null
        recordingFile = null
        startedAt = 0L
    }

    class Factory(private val audioRepository: AudioRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioViewModel(audioRepository) as T
        }
    }
}
