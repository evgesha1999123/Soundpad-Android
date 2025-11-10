package com.example.myapplication.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import com.example.myapplication.playlist_repository.FileSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import androidx.core.net.toUri

class MediaPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var _playing = MutableStateFlow(false)
    val playing = _playing.asStateFlow()
    var playOnPreparation = true

    fun initMediaPlayer() {
        this.mediaPlayer?.apply {
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)

            setOnPreparedListener {
                if (playOnPreparation) {
                    mediaPlayer?.start()
                }

                playOnPreparation = true
            }
        }
    }

    fun playFile(fileSchema: FileSchema) {
        stopOldPlayer()
        this.initMediaPlayer()
        try {
            mediaPlayer = MediaPlayer().apply {
                if (fileSchema.uri == null) {
                    Log.d(this::class.simpleName, "Аудио файл будет открыт как путь")
                    setDataSource(fileSchema.absolutePath)
                }
                else {
                    Log.d(this::class.simpleName, "Аудио файл будет открыт как URI")
                    setDataSource(context, fileSchema.uri.toUri())
                }
                setOnPreparedListener {
                    start()
                    _playing.value = true
                    Log.d("AudioPlayer", "Playing: ${fileSchema.fileName}")
                }
                setOnCompletionListener {
                    stopOldPlayer()
                    Log.d("AudioPlayer", "Playback completed: ${fileSchema.fileName}")
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AudioPlayer", "Failed to play MP3: ${e.message}")
        }
    }

    private fun stopOldPlayer() {
        mediaPlayer?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.w("AudioPlayer", "Stop called in invalid state")
            } finally {
                release()
            }
        }
        mediaPlayer = null
        _playing.value = false
        Log.d("AudioPlayer", "Stopped player")
    }

    fun stop() {
        stopOldPlayer()
    }

    fun isPlaying(): Boolean = _playing.value
}