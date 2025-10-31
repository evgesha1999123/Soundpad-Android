package com.simplemobiletools.voicerecorder.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.myapplication.models.AudioConfig
import com.naman14.androidlame.AndroidLame
import com.naman14.androidlame.LameBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class Mp3Recorder(val context: Context, val audioConfig: AudioConfig) : Recorder {
    private var mp3buffer: ByteArray = ByteArray(0)

    var recording = MutableStateFlow<Boolean>(false)
    private var isPaused = AtomicBoolean(false)
    private var isStopped = AtomicBoolean(false)
    private var amplitude = AtomicInteger(0)
    private var outputPath: String? = null
    private var androidLame: AndroidLame? = null
    private var outputStream: FileOutputStream? = null
    private var recordingThread: Thread? = null
    private var audioRecord: AudioRecord? = null

    private val minBufferSize = AudioRecord.getMinBufferSize(
        audioConfig.sampleRate,
        audioConfig.channelIn,
        audioConfig.encoding
    )

    override fun setOutputFile(path: String) {
        outputPath = path
    }

    override fun prepare() {}

    @SuppressLint("MissingPermission")
    override fun start() {
        val rawData = ShortArray(minBufferSize)
        mp3buffer = ByteArray((audioConfig.bufferSize + rawData.size * 2 * 1.25).toInt())

        outputStream = FileOutputStream(File(outputPath!!))

        androidLame = LameBuilder()
            .setInSampleRate(audioConfig.sampleRate)
            .setOutBitrate(audioConfig.sampleRate)
            .setOutSampleRate(audioConfig.sampleRate)
            .setOutChannels(audioConfig.channelOut)
            .build()

        // 🎯 создаём новый AudioRecord каждый раз
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            audioConfig.sampleRate,
            audioConfig.channelIn,
            audioConfig.encoding,
            audioConfig.bufferSize
        )


        isStopped.set(false)
        isPaused.set(false)

        recordingThread = Thread {
            try {
                audioRecord?.startRecording()
                recording.value = true
                while (!isStopped.get()) {
                    if (!isPaused.get()) {
                        val count = audioRecord?.read(rawData, 0, minBufferSize) ?: 0
                        if (count > 0) {
                            val encoded = androidLame!!.encode(rawData, rawData, count, mp3buffer)
                            if (encoded > 0) {
                                outputStream!!.write(mp3buffer, 0, encoded)
                                updateAmplitude(rawData)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    val flushResult = androidLame!!.flush(mp3buffer)
                    if (flushResult > 0) {
                        outputStream!!.write(mp3buffer, 0, flushResult)
                    }
                    outputStream?.flush()
                    outputStream?.close()
                    audioRecord?.stop()
                    audioRecord?.release()
                    recording.value = false
                    Log.d("Mp3Recorder", "Recording thread finished")
                } catch (e: Exception) {
                    e.printStackTrace()
                    recording.value = false
                }
            }
        }

        recordingThread!!.start()
    }

    override fun stop() {
        isStopped.set(true)
        recording.value = false
    }

    override fun release() {
        recordingThread?.join()
        recordingThread = null
        androidLame?.close()
        androidLame = null
        audioRecord = null
    }

    private fun updateAmplitude(data: ShortArray) {
        var sum = 0L
        for (i in 0 until minBufferSize step 2) {
            sum += abs(data[i].toInt())
        }
        amplitude.set((sum / (minBufferSize / 8)).toInt())
    }
}