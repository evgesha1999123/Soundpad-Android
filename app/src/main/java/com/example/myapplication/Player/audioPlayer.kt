package com.example.myapplication.Player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var _playing = MutableStateFlow(false)
    var playing = _playing.asStateFlow()

    fun playFile(file: File) {
        finalizeOldPlayer()

        player = MediaPlayer.create(context, file.toUri()).apply {
            Log.d(this::class.simpleName, "Start playing audiofile: $file")
            setOnCompletionListener {
                stop()
                _playing.value = false
            }
            setOnErrorListener { _, _, _ ->
                _playing.value = false
                false
            }
            start()
            _playing.value = true
        }
    }

    fun stop(callFromStart: Boolean = false) {
        player?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: IllegalStateException) {
                Log.w(this::class.simpleName, "Stop called in invalid state")
            }
            release()
        }
        player = null
        if (!callFromStart){
            _playing.value = false  // При рестарте не сбрасываем флаг
        }
        Log.d(this::class.simpleName, "Stopped playing")
    }

    fun isPlaying(): Boolean = _playing.value

    fun finalizeOldPlayer(){
        if (_playing.value) {           // Нужно для того чтобы убрать мерцание зеленой кнопки
            stop(callFromStart=true)    // при переключении, т.е. сохраняем текущее состояние флага воспроизведения
        }
    }
}