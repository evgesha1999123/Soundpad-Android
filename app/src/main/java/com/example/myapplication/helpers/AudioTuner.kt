package com.example.myapplication.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.util.Log
import com.example.myapplication.models.AudioConfig

class AudioTuner(private val context: Context) {

    companion object {
        private const val TAG = "AudioTuner"
    }

    fun detectOptimalConfig(): AudioConfig {

        val nativeRate = try {
            AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            44100
        }

        val testRates = listOf(nativeRate, 48000, 44100, 32000, 22050)
        var bestRate = 44100
        var bestBuffer = 0

        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val channelOut = AudioFormat.CHANNEL_OUT_MONO
        val channelIn = AudioFormat.CHANNEL_IN_MONO

        for (rate in testRates) {
            val buffer = AudioTrack.getMinBufferSize(rate, channelOut, encoding)
            if (buffer > 0) {
                bestRate = rate
                bestBuffer = buffer
                break
            }
        }

        Log.i(TAG, "🎚️ Native rate: $nativeRate Hz, chosen: $bestRate Hz, buffer: $bestBuffer bytes")

        return AudioConfig(
            sampleRate = bestRate,
            channelIn = channelIn,
            channelOut = channelOut,
            encoding = encoding,
            bufferSize = bestBuffer
        )
    }
}