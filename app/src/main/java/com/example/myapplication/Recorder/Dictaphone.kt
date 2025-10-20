package com.example.myapplication.Recorder

import android.media.AudioRecord
import android.util.Log
import com.example.myapplication.models.AudioConfigModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class Dictaphone(_audioConfig: AudioConfigModel) {
    var audioConfig = _audioConfig
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var isPaused = false
    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recordingJob: Job? = null


    fun startRecording(
        fileName: String = System.currentTimeMillis().toString(),
        recordDir: File
    ): String? {
        if (isRecording) return null

        try {
            val outputFile = File(recordDir, "$fileName.pcm")
            audioRecord = AudioRecord(
                audioConfig.microphoneAudioSource,
                audioConfig.sampleRate,
                audioConfig.channelConfig,
                audioConfig.audioFormat,
                audioConfig.bufferedSize
            )
            audioRecord?.startRecording()
            isRecording = true

            // Запускаем запись в отдельном потоке
            recordingJob = coroutineScope.launch {
                recordAudioToFile(outputFile)
            }

            return outputFile.absolutePath

        } catch (e: SecurityException) {
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun stopRecording() {
        isRecording = false
        recordingJob?.cancel()

        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        Log.d("Dictaphone", "Stopped!")
    }

    private fun recordAudioToFile(outputFile: File) {
        val buffer = ByteArray(audioConfig.bufferedSize)
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(outputFile)

            while (isRecording) {
                if (!isPaused) {
                    val bytesRead = audioRecord?.read(buffer, 0, audioConfig.bufferedSize) ?: 0
                    if (bytesRead > 0) {
                        Log.d("Dictaphone", "Recording to file -> $outputFile")
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.flush()
            outputStream?.close()
        }
    }

    public fun pauseRecording(){
        if (!isRecording || isPaused) return
        isPaused = true
        Log.d("Dictaphone", "Paused...")
    }

    public fun resumeRecording(){
        if (!isRecording || !isPaused) return
        isPaused = false
        Log.d("Dictaphone", "Resuming")
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    fun isPaused(): Boolean {
        return isPaused
    }

}