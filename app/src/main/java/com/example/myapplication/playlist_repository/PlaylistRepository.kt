package com.example.myapplication.playlist_repository

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import kotlin.io.readText

class PlaylistRepository(private val context: Context) {
    private val baseDirectory = File(context.filesDir.absolutePath)
    private val jsonFile = File(baseDirectory, "playlists.json").toString()
    private val json = Json { prettyPrint = true }
    private val schemaUtils = SchemaUtils()

    init {
        val created = ensureJsonFileCreated()
        Log.d(this::class.simpleName, "Файл создан: $created. Путь: $jsonFile")
        val isEmpty = File(jsonFile).length() == 0L
        Log.d(this::class.simpleName, "Пустой файл?: $isEmpty")
        if (isEmpty) {
            Log.d(this::class.simpleName, "Файл с плейлистами пуст, создаем изначальный плейлист 'records'")
            initRecordPlaylist()
        }
    }

    private fun ensureJsonFileCreated(skipFileChecking: Boolean = false): Boolean {
        val file = File(jsonFile)
        if (skipFileChecking) {
            return file.createNewFile()
        }
        val exists = file.exists()
        Log.d(this::class.simpleName, "Проверка существования json плейлистов: $exists")
        if (!exists) {
            try {
                Log.d(this::class.simpleName, "Создание файла")
                return file.createNewFile()
            } catch (err: IOException) {
                Log.e(this::class.simpleName, err.toString())
                return false
            }
        }
        return true
    }

    fun initRecordPlaylist() {
        val recordPlaylistSchema = PlaylistSchema(
            uid = schemaUtils.generateUid(),
            name = "records",
            absolutePath = File(baseDirectory, "records").toString(),
            created = schemaUtils.getCurrentDateTime()
        )
        createPlaylist(recordPlaylistSchema)
    }

    // Чтение всех объектов из файла
    private fun readAll(): MutableList<PlaylistSchema> {
        val file = File(jsonFile)
        if (!file.exists()) ensureJsonFileCreated(true)
        val jsonString = file.readText()
        return try {
            json.decodeFromString<List<PlaylistSchema>>(jsonString).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    // Запись всех объектов в файл
    private fun writeAll(list: List<PlaylistSchema>) {
        File(jsonFile).writeText(json.encodeToString(list))
    }

    // Поиск плейлиста по имени
    private fun findPlaylistByName(name: String): PlaylistSchema? {
        return readAll().find { it.name == name }
    }


    // ========== PLAYLIST OPERATIONS ==========
    // Создание плейлиста
    fun createPlaylist(playlistSchema: PlaylistSchema): Result<PlaylistSchema> {
        return try {
            val list = readAll()
            if (list.any { it.name == playlistSchema.name }) {
                return Result.failure(IllegalArgumentException("Плейлист с именем '${playlistSchema.name}' уже существует"))
            }
            list.add(playlistSchema)
            writeAll(list)
            Result.success(playlistSchema)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Обновление плейлиста по имени
    fun updatePlaylistByName(name: String, updatedPlaylist: PlaylistSchema): Result<PlaylistSchema> {
        return try {
            val list = readAll()
            val playlistIndex = list.indexOfFirst { it.name == name }
            if (playlistIndex == -1) {
                return Result.failure(NoSuchElementException("Плейлист с именем '$name' не найден"))
            }

            // Проверяем конфликт имен (если имя изменилось)
            if (updatedPlaylist.name != name && list.any { it.name == updatedPlaylist.name }) {
                return Result.failure(IllegalArgumentException("Плейлист с именем '${updatedPlaylist.name}' уже существует"))
            }

            list[playlistIndex] = updatedPlaylist
            writeAll(list)
            Result.success(updatedPlaylist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Удаление плейлиста по имени
    fun deletePlaylistByName(name: String): Result<Boolean> {
        return try {
            val list = readAll()
            val newList = list.filterNot { it.name == name }
            if (newList.size == list.size) {
                return Result.failure(NoSuchElementException("Плейлист с именем '$name' не найден"))
            }
            writeAll(newList)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== TRACK OPERATIONS ==========
    // Создание трека в плейлисте
    fun createTrackInPlaylist(playlistName: String, fileSchema: FileSchema): Result<FileSchema> {
        return try {
            val list = readAll()
            val playlistIndex = list.indexOfFirst { it.name == playlistName }
            if (playlistIndex == -1) {
                return Result.failure(NoSuchElementException("Плейлист с именем '$playlistName' не найден"))
            }

            val playlist = list[playlistIndex]
            if (playlist.content.any { it.uid == fileSchema.uid }) {
                return Result.failure(IllegalArgumentException("Трек с UID '${fileSchema.uid}' уже существует в плейлисте"))
            }

            playlist.content.add(fileSchema)
            writeAll(list)
            Result.success(fileSchema)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Обновление трека в плейлисте
    fun updateTrackInPlaylist(playlistName: String, updatedTrack: FileSchema): Result<FileSchema> {
        return try {
            val list = readAll()
            val playlistIndex = list.indexOfFirst { it.name == playlistName }
            if (playlistIndex == -1) {
                return Result.failure(NoSuchElementException("Плейлист с именем '$playlistName' не найден"))
            }

            val playlist = list[playlistIndex]
            val trackIndex = playlist.content.indexOfFirst { it.uid == updatedTrack.uid }
            if (trackIndex == -1) {
                return Result.failure(NoSuchElementException("Трек с UID '${updatedTrack.uid}' не найден в плейлисте"))
            }

            playlist.content[trackIndex] = updatedTrack
            writeAll(list)
            Result.success(updatedTrack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Удаление трека из плейлиста
    fun deleteTrackFromPlaylist(playlistName: String, trackUid: String): Result<Boolean> {
        return try {
            Log.i(this::class.simpleName, "Удаление файла $trackUid из плейлиста $playlistName")
            val list = readAll()
            val playlistIndex = list.indexOfFirst { it.name == playlistName }
            if (playlistIndex == -1) {
                Log.i(this::class.simpleName, "$playlistName не существует")
                return Result.failure(NoSuchElementException("Плейлист с именем '$playlistName' не найден"))
            }

            val playlist = list[playlistIndex]
            val initialSize = playlist.content.size
            playlist.content.removeAll { it.uid == trackUid }

            if (playlist.content.size == initialSize) {
                Log.i(this::class.simpleName, "Трек с UID '$trackUid' не найден в плейлисте")
                return Result.failure(NoSuchElementException("Трек с UID '$trackUid' не найден в плейлисте"))
            }

            writeAll(list)
            Result.success(true)
        } catch (e: Exception) {
            Log.e(this::class.simpleName, e.toString())
            Result.failure(e)
        }
    }

    // Массовое создание треков в плейлисте
    fun bulkCreateTracksInPlaylist(playlistName: String, tracks: List<FileSchema>): Result<List<FileSchema>> {
        return try {
            val list = readAll()
            val playlistIndex = list.indexOfFirst { it.name == playlistName }
            Log.w("list in json: ", list.toString())
            if (playlistIndex == -1) {
                Log.w("playlist name: ", playlistName)
                Log.w("playlist index == -1: ", "failure")
                return Result.failure(NoSuchElementException("Плейлист с именем '$playlistName' не найден"))
            }

            val playlist = list[playlistIndex]
            val existingUids = playlist.content.map { it.uid }.toSet()
            val duplicates = tracks.filter { it.uid in existingUids }

            if (duplicates.isNotEmpty()) {
                Log.w("duplicated founded", "failure")
                return Result.failure(IllegalArgumentException("Найдены дубликаты треков: ${duplicates.map { it.uid }}"))
            }

            playlist.content.addAll(tracks)
            writeAll(list)
            Result.success(tracks)
        } catch (e: Exception) {
            Log.e("error updating json", e.toString())
            Result.failure(e)
        }
    }

    // Массовое удаление треков из плейлиста
    fun bulkDeleteTracksFromPlaylist(playlistName: String, trackUids: List<String>): Result<Boolean> {
        return try {
            val list = readAll()
            val playlistIndex = list.indexOfFirst { it.name == playlistName }
            if (playlistIndex == -1) {
                return Result.failure(NoSuchElementException("Плейлист с именем '$playlistName' не найден"))
            }

            val playlist = list[playlistIndex]
            val initialSize = playlist.content.size
            playlist.content.removeAll { it.uid in trackUids }

            if (playlist.content.size == initialSize) {
                return Result.failure(NoSuchElementException("Ни один из треков не найден в плейлисте"))
            }

            writeAll(list)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== UTILITY METHODS ==========

    fun getAllPlaylists(): List<PlaylistSchema> = readAll()

    fun getPlaylistByName(name: String): PlaylistSchema? = findPlaylistByName(name)
}