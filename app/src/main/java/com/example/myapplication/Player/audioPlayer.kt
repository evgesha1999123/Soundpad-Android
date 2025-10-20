package com.example.myapplication.Player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import com.example.myapplication.models.AudioConfigModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

class AudioPlayer(_audioConfig: AudioConfigModel) {
    var audioConfig: AudioConfigModel = _audioConfig
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    private fun readFileBytes(filePath: String): ByteArray?{
        return try {
            File(filePath).readBytes()
        } catch (error: Exception) {
            error.printStackTrace()
            null
        }
    }
    fun playPcm(filePath: String){
        val bufferSize = audioConfig.bufferedSize
        val pcmData = readFileBytes(filePath)
        val pcmInput = pcmData?.inputStream()?.buffered()

        val player = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
        )
            .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(audioConfig.audioFormat)
                .setSampleRate(audioConfig.sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
            .setBufferSizeInBytes(audioConfig.bufferedSize)
            .build()

        try{
            Log.d(this::class.simpleName, "Start playing: $filePath")
            player.play()
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            while (pcmInput?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                Log.d(this::class.simpleName, "Now playing: $filePath")
                player.write(buffer, 0, bytesRead)
            }
            player.stop()

        } catch (error: Exception){
            Log.e(this::class.simpleName, "Playback error: $error")
        } finally {
            player.release()
            pcmInput?.close()
            Log.d(this::class.simpleName, "Playback finished!")
        }
    }

    fun playPcmStream() {
        stop()

        val bufferSize = AudioTrack.getMinBufferSize(
            audioConfig.sampleRate,
            audioConfig.channelConfig,
            audioConfig.audioFormat
        )

        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setEncoding(audioConfig.audioFormat)
                .setSampleRate(audioConfig.sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        audioTrack?.play()
        isPlaying = true
    }

    fun writeToStream(filePath: String) {
        val data = readFileBytes(filePath)
        if (data != null){
            if (isPlaying) {
                audioTrack?.write(data, 0, data.size)
            }
        }
    }

    fun stop() {
        isPlaying = false
        audioTrack?.apply {
            stop()
            release()
        }
        audioTrack = null
    }

    fun release() {
        stop()
    }
}