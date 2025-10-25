package com.example.myapplication.Recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.myapplication.models.AudioConfigModel
import java.io.File
import java.io.FileOutputStream


class Dictaphone(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var currentFileStream: FileOutputStream? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    fun startRecording(outputFile: File) {
        stopRecording() // остановим предыдущий, если он был

        currentFileStream = FileOutputStream(outputFile)
        recorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(currentFileStream!!.fd)

            prepare()
            start()
            recording = true
            Log.d(this::class.simpleName, "Start recording: $outputFile")
        }
    }

    fun stopRecording() {
        recorder?.let {
            try {
                it.stop()
            } catch (e: Exception) {
                Log.e("Dictaphone", "Error stopping recorder: ${e.message}")
            } finally {
                it.reset()
                it.release()
            }
        }
        recorder = null

        currentFileStream?.close()
        currentFileStream = null

        recording = false
        Log.d("Dictaphone", "Recording stopped")
    }

    fun isRecording(): Boolean = recording
}