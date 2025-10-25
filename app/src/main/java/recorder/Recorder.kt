package com.simplemobiletools.voicerecorder.recorder

import java.io.FileDescriptor

interface Recorder {
    fun setOutputFile(path: String)

    fun prepare()
    fun start()
    fun stop()
    fun release()
}
