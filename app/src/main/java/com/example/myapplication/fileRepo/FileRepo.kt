package com.example.myapplication.fileRepo

import android.content.Context
import android.util.Log
import com.example.myapplication.playlist_repository.FileSchema
import com.example.myapplication.playlist_repository.PlaylistRepository
import com.example.myapplication.playlist_repository.PlaylistSchema
import com.example.myapplication.playlist_repository.SchemaUtils
import java.io.File

// TODO: Нужно синхронизировать операции с PlaylistRepository
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

    fun listFiles(): MutableList<FileSchema> {
        Log.d(this::class.simpleName, "Getting files from '$currentDirectory'")
        val files = mutableListOf<FileSchema>()
        val playlistSchema = playlistRepository.getPlaylistByName(currentDirectory.name)
        if (playlistSchema != null) {
            return playlistSchema.content
        }
        return files
    }

    fun getFile(index: Int): FileSchema {
        val fileSchema: FileSchema = listFiles()[index]
        Log.d(this::class.simpleName, "Getting file '${fileSchema.fileName}'")
        return fileSchema
    }


    fun deleteFile(fileSchema: FileSchema): Boolean {
        Log.d(this::class.simpleName, "Deleting file '${fileSchema.fileName}'")
        val currentPlaylistName = currentDirectory.toString()
        val playlistSchema = playlistRepository.getPlaylistByName(currentPlaylistName)
        if (playlistSchema != null) {
            for (track in playlistSchema.content) {
                if (track.absolutePath == fileSchema.absolutePath) {
                    val trackUid = track.uid
                    val result = playlistRepository.deleteTrackFromPlaylist(currentPlaylistName, trackUid = trackUid)
                    if (track.isDeletable) {
                        Log.i(this::class.simpleName, "Удаление файла созданного пользователем: ${fileSchema.fileName}")
                        return (File(fileSchema.absolutePath).delete()) && (result.isSuccess)
                    }
                    Log.i(this::class.simpleName, "Удаление стороннего файла из json: ${fileSchema.fileName}")
                    return result.isSuccess
                }
            }
        }
        Log.e(this::class.simpleName, "Удаление провалено для ${fileSchema.fileName}")
        return false
    }

    fun purgeDirectory(playlistName: String = currentDirectory.name.toString()): Boolean {
        Log.d(this::class.simpleName, "Purging directory '$playlistName'")
        if (File(baseDirectory, playlistName).exists()) {
            val trackUids = mutableListOf<String>()
            val playlistSchema = playlistRepository.getPlaylistByName(playlistName)

            if (playlistSchema != null) {
                if (playlistSchema.content.isNotEmpty()) {
                    for (file in playlistSchema.content) {
                        trackUids.add(file.uid)
                        if (file.isDeletable) {
                            // Сначала удаляю те, которые были созданы пользователем
                            // Остальные просто убираются из json
                            this.deleteFile(file)
                        }
                    }
                }
            }
            val result = playlistRepository.bulkDeleteTracksFromPlaylist(playlistName, trackUids)
            return result.isSuccess
        }
        return false
    }

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

    fun deletePlaylist(name: String): Boolean {
        if (name != "records") {
            this.purgeDirectory(name)
            val result = playlistRepository.deletePlaylistByName(name)
            return result.isSuccess
        }
        return false
    }

    fun getCurrentPlaylistName(): String {
        return currentDirectory.name
    }

    fun addTracksToPlaylist(filePaths: List<String>): Boolean {
        val fileSchemas = mutableListOf<FileSchema>()
        for (file in filePaths) {
            val isOuterFile: Boolean = file.contains("content://")
            fileSchemas.add(
                FileSchema(
                    uid = schemaUtils.generateUid(),
                    fileName = File(file).name,
                    absolutePath = if (!isOuterFile) file else "",
                    uri = if (isOuterFile) file else null,
                    isUserRecord = !isOuterFile,
                    isDeletable = !isOuterFile,
                    created = schemaUtils.getCurrentDateTime()
                )
            )
        }
        Log.w(this::class.simpleName, "File repo add schemas: $fileSchemas")
        val result =  playlistRepository.bulkCreateTracksInPlaylist(currentDirectory.name, fileSchemas)
        return result.isSuccess
    }
}