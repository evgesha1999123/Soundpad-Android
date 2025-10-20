package com.example.myapplication.Player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import com.example.myapplication.models.AudioConfigModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    var playing = false
        private set

    fun playFile(file: File) {
        stop() // Сначала останавливаем и освобождаем старый плеер

        player = MediaPlayer.create(context, file.toUri()).apply {
            Log.d(this::class.simpleName, "Start playing audiofile: $file")
            playing = true
            setOnCompletionListener {
                stop() // Автоочистка по завершении
            }
            start()
        }
    }

    fun stop() {
        player?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: IllegalStateException) {
                Log.w("AudioPlayer", "Stop called in invalid state")
            }
            release()
        }
        player = null
        playing = false
        Log.d("AudioPlayer", "Stopped playing")
    }

    fun isPlaying(): Boolean = playing
}