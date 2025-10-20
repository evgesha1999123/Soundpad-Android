package com.example.myapplication.models

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

data class AudioConfigModel(
    val microphoneAudioSource: Int = MediaRecorder.AudioSource.MIC, // Источник - микрофон
    val sampleRate: Int = 44100,    // Частота
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,   // Моно/стерео
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,  // Качество
    val bufferedSize: Int = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)    // Размер буфера
    )