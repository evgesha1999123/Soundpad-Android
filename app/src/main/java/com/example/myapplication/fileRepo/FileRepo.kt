package com.example.myapplication.fileRepo

import android.util.Log
import java.io.File

// TODO: Продумать возможность создания кастомных папок (например, для скачиваемых сэмплов)
// TODO: Сделать проверку на существование файлов и директории во избежание вылетов
class FileRepo(_directory: File) {
    private var directory = _directory
    init {
        ensureDirCreated()
    }

    private fun ensureDirCreated() {
        if (!directory.exists()) {
            directory.mkdirs()
            Log.d(this::class.simpleName, "Directory '$directory' created")
        }
    }

    fun getCurrentDirectory(): File = directory

    fun setDirectory(newDirectory: File) {
        directory = newDirectory
        Log.d(this::class.simpleName, "Directory '$directory' was set as main")
        ensureDirCreated()
    }

    fun listFiles(): MutableList<File> {
        Log.d(this::class.simpleName, "Getting files from '$directory'")
        return directory.listFiles()?.toMutableList() ?: mutableListOf()
    }

    fun getFile(index: Int): File {
        val file = listFiles()[index]
        Log.d(this::class.simpleName, "Getting file '$file'")
        return file
    }

    fun addFile(file: File): Boolean {
        Log.d(this::class.simpleName, "Creating new file '$file'")
        return file.createNewFile()
    }

    fun deleteFile(file: File): Boolean {
        Log.d(this::class.simpleName, "Deleting file '$file'")
        return file.delete()
    }

    fun purgeDirectory(): Boolean {
        Log.d(this::class.simpleName, "Purging directory '$directory'")
        if (directory.exists()){
            for (file in this.listFiles()){
                this.deleteFile(file)
            }
            return this.listFiles().isEmpty()
        }
        else{
            return false
        }
    }
}