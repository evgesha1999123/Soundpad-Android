package com.example.myapplication.Player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.myapplication.models.AudioConfigModel
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
        val pcmData = readFileBytes(filePath)
        val player = AudioTrack.Builder().setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        ).setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(audioConfig.audioFormat)
                .setSampleRate(audioConfig.sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        ).setBufferSizeInBytes(audioConfig.bufferedSize).build()

        player.apply{
            play()
            if (pcmData != null){
                write(pcmData, 0, pcmData.size)
            }
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