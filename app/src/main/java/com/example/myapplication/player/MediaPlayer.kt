package com.example.myapplication.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var _playing = MutableStateFlow(false)
    val playing = _playing.asStateFlow()
    var playOnPreparation = true

    public fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
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

    fun playFile(file: File) {
        stopOldPlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    start()
                    _playing.value = true
                    Log.d("AudioPlayer", "Playing: ${file.name}")
                }
                setOnCompletionListener {
                    stopOldPlayer()
                    Log.d("AudioPlayer", "Playback completed: ${file.name}")
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