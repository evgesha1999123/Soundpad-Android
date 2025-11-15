package com.example.myapplication.fileRepo

import android.content.Context
import android.util.Log
import com.example.myapplication.playlist_repository.FileSchema
import com.example.myapplication.playlist_repository.PlaylistRepository
import com.example.myapplication.playlist_repository.PlaylistSchema
import com.example.myapplication.playlist_repository.SchemaUtils
import java.io.File
import androidx.core.net.toUri

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

    fun listFileSchemas(playlistName: String = "records"): MutableList<FileSchema> {
        Log.d(this::class.simpleName, "Listing file schemas from playlist: '$playlistName'")
        val files = mutableListOf<FileSchema>()
        val playlistSchema = playlistRepository.getPlaylistByName(playlistName)
        Log.d(this::class.simpleName, "listFileSchemasGetSchema: $playlistSchema")
        if (playlistSchema != null) {
            Log.d(this::class.simpleName, "playlistSchema content: $playlistSchema")
            return playlistSchema.content
        }
        return files
    }

    fun getFileSchema(index: Int, playlistName: String = "records"): FileSchema {
        val fileSchemas: MutableList<FileSchema> = listFileSchemas(playlistName)
        if (fileSchemas.isNotEmpty()) {
            val fileSchema: FileSchema = fileSchemas[index]
            return fileSchema
        }
        return FileSchema(
            uid = schemaUtils.generateUid(),
            fileName = "empty",
            absolutePath = "empty",
            uri = null,
            isUserRecord = false,
            isDeletable = false,
            created = schemaUtils.getCurrentDateTime()
        )
    }


    fun deleteFile(fileSchema: FileSchema): Boolean {
        Log.d(this::class.simpleName, "Deleting file '${fileSchema.fileName}'")
        Log.d(this::class.simpleName, "Deletable? '${fileSchema.isDeletable}'")
        val currentPlaylistName = currentDirectory.name
        Log.d(this::class.simpleName, "file schema: $fileSchema")
        Log.d(this::class.simpleName, "playlist schema not null")
        val trackUid = fileSchema.uid
        val result = playlistRepository.deleteTrackFromPlaylist(currentPlaylistName, trackUid = trackUid)
        Log.d(this::class.simpleName, "result of playlist repos: $result")
        try {
            if (fileSchema.isDeletable) {
                Log.i(this::class.simpleName, "deletable file")
                Log.i(this::class.simpleName, "Удаление файла созданного пользователем: ${File(fileSchema.absolutePath)}")
                return (File(fileSchema.absolutePath).delete()) && (result.isSuccess)
            }
            Log.i(this::class.simpleName, "Удаление стороннего файла из json: ${fileSchema.fileName}")
            return result.isSuccess
        } catch (err: Exception) {
            Log.e(this::class.simpleName, "Удаление провалено для ${fileSchema.fileName}")
            Log.e(this::class.simpleName, err.toString())
            return false
        }
    }

    fun purgeDirectory(playlistName: String = currentDirectory.name.toString()): Boolean {
        Log.d(this::class.simpleName, "Purging directory '$playlistName'")
        if (File(baseDirectory, playlistName).exists()) {
            val trackUids = mutableListOf<String>()
            val playlistSchema = playlistRepository.getPlaylistByName(playlistName)
            Log.i(this::class.simpleName, "path: ${File(baseDirectory, playlistName)}")
            if (playlistSchema != null) {
                if (playlistSchema.content.isNotEmpty()) {
                    for (file in playlistSchema.content) {
                        Log.i(this::class.simpleName, file.toString())
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

    fun createPlaylist(name: String): Result<PlaylistSchema> {
        setDirectory(name)
        val playlistSchema = PlaylistSchema(
            uid = schemaUtils.generateUid(),
            name = name,
            absolutePath = currentDirectory.toString(),
            created = schemaUtils.getCurrentDateTime()
        )
        val result = playlistRepository.createPlaylist(playlistSchema)
        Log.i(this::class.simpleName, "Успешно создан? ${result.exceptionOrNull()}")
        return result
    }

    fun deletePlaylist(name: String): Boolean {
        if (name != "records") {
            val directory = File(baseDirectory, name)
            val successDeletedDirectory = directory.deleteRecursively()
            val result = playlistRepository.deletePlaylistByName(name)
            return result.isSuccess && successDeletedDirectory
        }
        return purgeDirectory(name)
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
                    fileName = if (!isOuterFile) File(file).name else schemaUtils.getFileNameFromUri(context, file.toUri()),
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