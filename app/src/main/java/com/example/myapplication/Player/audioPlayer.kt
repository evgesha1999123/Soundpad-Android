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
        // Остановим старый плеер и освободим ресурсы
        stopOldPlayer()

        player = MediaPlayer().apply {
            setDataSource(context, file.toUri())
            setOnCompletionListener {
                stop()
                _playing.value = false
            }
            setOnErrorListener { _, _, _ ->
                stop()
                true
            }
            prepare()  // prepare синхронно, безопаснее чем create()
            start()
            _playing.value = true
            Log.d(this::class.simpleName, "Start playing audiofile: $file")
        }
    }

    private fun stopOldPlayer() {
        player?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: IllegalStateException) {
                Log.w(this::class.simpleName, "Stop called in invalid state")
            } finally {
                release()
            }
        }
        player = null
        _playing.value = false
        Log.d(this::class.simpleName, "Stopped playing old player")
    }

    fun stop() {
        stopOldPlayer()
    }

    fun isPlaying(): Boolean = _playing.value
}