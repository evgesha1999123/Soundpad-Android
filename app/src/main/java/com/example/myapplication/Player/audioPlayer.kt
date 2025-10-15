package com.example.myapplication.Player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.myapplication.models.AudioConfigModel
import android.media.MediaPlayer

class AudioPlayer(_audioConfig: AudioConfigModel) {
    var audioConfig = _audioConfig

//    val player = AudioTrack.Builder().setAudioAttributes(
//        AudioAttributes.Builder()
//            .setUsage(AudioAttributes.USAGE_MEDIA)
//            .build()
//    ).setAudioFormat(
//        AudioFormat.Builder()
//            .setEncoding(audioConfig.audioFormat)
//            .setSampleRate(audioConfig.sampleRate)
//            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
//            .build()
//    ).setBufferSizeInBytes(audioConfig.bufferedSize).build()

    private var mediaPlayer: MediaPlayer? = null

    fun play(){
//        mediaPlayer?.setAudioAttributes()
    }
}