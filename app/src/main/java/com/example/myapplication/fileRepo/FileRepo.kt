package com.example.myapplication.fileRepo

import android.util.Log
import java.io.File

class FileRepo(_directory: File) {
    private var directory: File = _directory
    private var files: MutableList<File> = directory.listFiles()?.toMutableList() ?: mutableListOf()

    private fun ensureDirCreated(): File? {
        Log.d("AppPath", "Путь $directory существует -> ${directory.exists()}")
        if (!directory.exists()) {
            val created = directory.mkdir()
            Log.d("AppPath", "Папка создана: $created, путь: ${directory.absolutePath}")
        }
        return if (directory.exists()) directory else null
    }

    fun getDirectory(): File{
        return directory
    }

    fun setDirectory(directory: File){
        this.directory = directory
    }

    fun getListOfFiles(): MutableList<File> {
        return files
    }

    fun getFile(index: Int): File{
        return files[index]
    }

    fun addFile(file: File): Int{
        /* Returns index of the last element */
        files.add(file)
        return files.size - 1
    }

    fun deleteFile(){

    }
}