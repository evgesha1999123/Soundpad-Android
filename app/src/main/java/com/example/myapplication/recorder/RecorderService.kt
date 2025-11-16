package com.example.myapplication.recorder

import android.util.Log
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.statusManager.StatusManager
import java.io.File

class RecorderService(val recorder: Mp3Recorder, val fileRepo: FileRepo) {
    fun start() {
        recorder.prepare()
        val outputFile = File(
            fileRepo.getCurrentDirectory().toString(),
            "rec_${System.currentTimeMillis()}.mp3"
        ).toString()
        recorder.setOutputFile(outputFile)
        recorder.start()
        val createdSuccess = fileRepo.addTracksToPlaylist(listOf(outputFile))
        Log.w(":: ${this::class.simpleName} :: Creating file:", createdSuccess.toString())
    }

    fun stop(onRecordStopped: () -> Unit) {
        recorder.stop()
        recorder.release()
        onRecordStopped()
    }
}