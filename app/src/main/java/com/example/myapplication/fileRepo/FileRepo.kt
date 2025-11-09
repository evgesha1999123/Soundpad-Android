package com.example.myapplication.fileRepo

import android.content.Context
import android.util.Log
import com.example.myapplication.playlist_repository.PlaylistRepository
import com.example.myapplication.playlist_repository.PlaylistSchema
import com.example.myapplication.playlist_repository.SchemaUtils
import java.io.File

class FileRepo(directoryName: String, private val context: Context) {
    private val baseDirectory = File(context.filesDir.absolutePath)
    private var currentDirectory = File(baseDirectory, directoryName)
    private val playlistRepository = PlaylistRepository(context)

    private val schemaUtils = SchemaUtils()

    init {
        ensureDirCreatedAtStart()
    }

    private fun ensureDirCreatedAtStart() {
        if (!currentDirectory.exists()) {
            currentDirectory.mkdirs()
            Log.d(this::class.simpleName, "Directory '$currentDirectory' created")
        }
    }

    private fun createDirectory(name: String): Boolean {
        val newDirectory = File(baseDirectory, name)
        return newDirectory.mkdir()
    }

    fun getCurrentDirectory(): File = currentDirectory

    fun setDirectory(newDirectory: String) {
        currentDirectory = File(baseDirectory, newDirectory)
        Log.d(this::class.simpleName, "Directory '$currentDirectory' was set as current")
        if (!currentDirectory.exists()) {
            createDirectory(newDirectory)
        }
    }

    fun listFiles(): MutableList<File> {
        Log.d(this::class.simpleName, "Getting files from '$currentDirectory'")
        return currentDirectory.listFiles()?.toMutableList() ?: mutableListOf()
    }

    fun getFile(index: Int): File {
        val file = listFiles()[index]
        Log.d(this::class.simpleName, "Getting file '$file'")
        return file
    }


    fun deleteFile(file: File): Boolean {
        Log.d(this::class.simpleName, "Deleting file '$file'")
        return file.delete()
    }

    fun purgeCurrentDirectory(): Boolean {
        Log.d(this::class.simpleName, "Purging directory '$currentDirectory'")
        if (currentDirectory.exists()){
            for (file in this.listFiles()){
                this.deleteFile(file)
            }
            return this.listFiles().isEmpty()
        }
        else{
            return false
        }
    }

    // TODO: Нужно синхронизировать операции с PlaylistRepository
    fun getAllPlaylists(): MutableList<String> {
        val schemas: List<PlaylistSchema> = playlistRepository.getAllPlaylists()
        val playlists = mutableListOf<String>()
        for (schema in schemas) {
            playlists.add(schema.name)
        }
        return playlists
    }

    fun createPlaylist(name: String): Boolean {
        setDirectory(name)
        val playlistSchema = PlaylistSchema(
            uid = schemaUtils.generateUid(),
            name = name,
            absolutePath = currentDirectory.toString(),
            created = schemaUtils.getCurrentDateTime()
        )
        val result = playlistRepository.createPlaylist(playlistSchema)
        return result.isSuccess
    }

    fun getCurrentPlaylistName(): String {
        return currentDirectory.name
    }
}