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
    var recording = false
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            MediaRecorder(context)
        }
        else{
            MediaRecorder()
        }
    }

    fun startRecording(outputFile: File){
        createRecorder().apply{
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            Log.d(this::class.simpleName, "Start recording: $outputFile")
            start()
            recording = true
            recorder = this
        }
    }

    fun stopRecording(){
        Log.d(this::class.simpleName, "Recording stop")
        recorder?.stop()
        recorder?.reset()
        recording = false

    }

    fun isRecording(): Boolean {
        return recording
    }
}