package com.example.myapplication.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.core.net.toUri
import com.example.myapplication.playlist_repository.FileSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var _playing = MutableStateFlow(false)
    val playing = _playing.asStateFlow()
    private var _currentFileName = MutableStateFlow<String>("")
    val currentFile = _currentFileName.asStateFlow()
    private var _trackDuration: MutableStateFlow<Int> = MutableStateFlow<Int>(0)
    val trackDuration = _trackDuration.asStateFlow()
    private var _playEvent: MutableStateFlow<Int> = MutableStateFlow<Int>(0)
    var playEvent = _playEvent.asStateFlow()
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

        _currentFileName.value = fileSchema.fileName
        _playEvent.value++

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
                    _trackDuration.value = duration
                    if (fileSchema.uri != null) {
                        Log.d("MediaPlayer", "Playing: ${fileSchema.uri}; duration: $duration")
                    }
                    else {
                        Log.d("MediaPlayer", "Playing: ${fileSchema.absolutePath} duration: $duration")
                    }
                }
                setOnCompletionListener {
                    stopOldPlayer(false)
                    if (fileSchema.uri != null) {
                        Log.d("MediaPlayer", "Playing: ${fileSchema.uri}")
                    }
                    else {
                        Log.d("MediaPlayer", "Playback completed: ${fileSchema.absolutePath}")
                    }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaPlayer", "Failed to play MP3: ${e.message}")
        }
    }

    private fun stopOldPlayer(isRestart: Boolean) {
        mediaPlayer?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.w("MediaPlayer", "Stop called in invalid state")
            } finally {
                _currentFileName.value = ""
                _trackDuration.value = 0
                release()
            }
        }
        mediaPlayer = null
        _playing.value = isRestart
        Log.d("MediaPlayer", "Stopped player")
    }

    fun stop() {
        stopOldPlayer(false)
    }
}