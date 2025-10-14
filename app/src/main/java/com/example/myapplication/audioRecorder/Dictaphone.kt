package com.example.myapplication.audioRecorder

import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioFormat
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class Dictaphone(_recordsDir: File) {
    private val recordsDir = _recordsDir
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var isPaused = false
    private var recordingJob: Job? = null

    // Audio configuration
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private fun ensureDirCreated(): File? {
        Log.d("AppPath", "Путь ${recordsDir.absolutePath} существует -> ${recordsDir.exists()}")
        if (!recordsDir.exists()) {
            val created = recordsDir.mkdir()
            Log.d("AppPath", "Папка создана: $created, путь: ${recordsDir.absolutePath}")
        }
        return if (recordsDir.exists()) recordsDir else null
    }

    fun startRecording(
        fileName: String = System.currentTimeMillis().toString()
    ): String? {
        if (isRecording) return null

        try {
            // Создаем директорию если не существует
            val recordDir: File? = ensureDirCreated()

            val outputFile = File(recordDir, "$fileName.pcm")

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
        isRecording = false
        recordingJob?.cancel()

        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        println("--->> КОНЕЦ записи")
    }

    private fun recordAudioToFile(outputFile: File) {
        val buffer = ByteArray(bufferSize)
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(outputFile)

            while (isRecording) {
                if (!isPaused) {
                    val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (bytesRead > 0) {
                        println("$isRecording -->> НАЧАЛО записи")
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    fun isPaused(): Boolean {
        return isPaused
    }

    public fun pauseRecording(){
        if (!isRecording || isPaused) return
        isPaused = true
        println("--->> ПАУЗА записи")
    }

    public fun resumeRecording(){
        if (!isRecording || !isPaused) return
        isPaused = false
        println("--->> ВОЗОБНОВЛЕНИЕ записи")
    }

}