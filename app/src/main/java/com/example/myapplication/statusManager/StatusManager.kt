package com.example.myapplication.statusManager

import com.example.myapplication.navigation.Status
import com.example.myapplication.player.MediaPlayer
import com.example.myapplication.recorder.RecorderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StatusManager(val recorderService: RecorderService, val player: MediaPlayer, scope: CoroutineScope) {
    private val _status = MutableStateFlow(Status.IDLE)
    val status = _status.asStateFlow()

    init {
        scope.launch {
            combine(
                recorderService.recorder.recording,
                player.playing
            ) { recording, playing ->
                when {
                    recording -> Status.RECORDING
                    playing -> Status.PLAYING
                    else -> Status.IDLE
                }
            }.collect { newStatus ->
                _status.value = newStatus
            }
        }
    }
    fun getStatus(): Status {
        if (recorderService.recorder.recording.value) {
            return Status.RECORDING
        }
        return if (player.playing.value) {
            Status.PLAYING
        } else {
            Status.IDLE
        }
    }
}