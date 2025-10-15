package com.example.myapplication.fileRepo

import android.util.Log
import java.io.File

// TODO: Продумать возможность создания кастомных папок (например, для скачиваемых сэмплов)
class FileRepo(_directory: File) {
    private var directory = _directory
    init {
        ensureDirCreated()
    }

    private fun ensureDirCreated() {
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    fun getCurrentDirectory(): File = directory

    fun setDirectory(newDirectory: File) {
        directory = newDirectory
        ensureDirCreated()
    }

    fun listFiles(): List<File> =
        directory.listFiles()?.toList() ?: emptyList()

    fun getFile(index: Int): File =
        listFiles()[index]

    fun addFile(file: File): Boolean =
        file.createNewFile()

    fun deleteFile(file: File): Boolean =
        file.delete()

    fun purgeDirectory(): Boolean =
        if (directory.exists()) {
            directory.deleteRecursively()
        } else false
}