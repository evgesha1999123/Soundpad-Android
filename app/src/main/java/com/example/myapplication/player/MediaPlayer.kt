package com.example.myapplication.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import androidx.core.net.toUri
import com.example.myapplication.playlist_repository.FileSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        var isRestart = false
        if (_playing.value) isRestart = true
        stopOldPlayer(isRestart)
        this.initMediaPlayer()
        try {
            mediaPlayer = MediaPlayer().apply {
                if (fileSchema.uri == null) {
                    Log.d(this::class.simpleName, "Будет считан путь к аудиофайлу")
                    setDataSource(fileSchema.absolutePath)
                }
                else {
                    Log.d(this::class.simpleName, "Будет считан URI аудиофайла")
                    setDataSource(context, fileSchema.uri.toUri())
                }
                setOnPreparedListener {
                    start()
                    _playing.value = true
                    if (fileSchema.uri != null) {
                        Log.d("AudioPlayer", "Playing: ${fileSchema.uri}")
                    }
                    else {
                        Log.d("AudioPlayer", "Playing: ${fileSchema.absolutePath}")
                    }
                }
                setOnCompletionListener {
                    stopOldPlayer(false)
                    if (fileSchema.uri != null) {
                        Log.d("AudioPlayer", "Playing: ${fileSchema.uri}")
                    }
                    else {
                        Log.d("AudioPlayer", "Playback completed: ${fileSchema.absolutePath}")
                    }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AudioPlayer", "Failed to play MP3: ${e.message}")
        }
    }

    private fun stopOldPlayer(isRestart: Boolean) {
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
        _playing.value = isRestart
        Log.d("AudioPlayer", "Stopped player")
    }

    fun stop() {
        stopOldPlayer(false)
    }
}