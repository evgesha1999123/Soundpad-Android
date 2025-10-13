package com.example.myapplication.audioRecorder

import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioFormat
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null

    // Audio configuration
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    fun startRecording(
        audioDir: File,
        fileName: String = System.currentTimeMillis().toString()
    ): String? {
        if (isRecording) return null

        try {
            // Создаем директорию если не существует
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val outputFile = File(audioDir, "$fileName.pcm")

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,  // Источник - микрофон
                sampleRate, // Частота
                channelConfig,  // Моно/стерео
                audioFormat,    // Качество
                bufferSize   // Размер буфера
            )

            audioRecord?.startRecording()
            isRecording = true

            // Запускаем запись в отдельном потоке
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
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
        if (!isRecording) return

        isRecording = false
        recordingJob?.cancel()

        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
    }

    private fun recordAudioToFile(outputFile: File) {
        val buffer = ByteArray(bufferSize)
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(outputFile)

            while (isRecording) {
                val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
    }

    fun isRecording(): Boolean = isRecording
}