package com.example.myapplication.utils

import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.navigation.Status
import com.example.myapplication.playlist_repository.FileSchema
import java.io.File

class TextUtils {
    private fun isPlaylistProtected(playlistName: String): Boolean {
        return playlistName == "records"
    }
    fun getDeletedPlaylistFilesDescription(fileRepo: FileRepo, playlistName: String): String {
        val allFiles = fileRepo.listFileSchemas(playlistName)
        val userFiles = allFiles.filter { it.isUserRecord }.map { it.fileName }
        val isProtected = isPlaylistProtected(playlistName)
        val isEmpty = allFiles.isEmpty()

        return when {
            isProtected && isEmpty -> "Этот плейлист пуст, удалять нечего."

            isProtected && !isEmpty -> buildString {
                append("Плейлист '$playlistName' защищен от удаления, так как используется для записи новых аудиофайлов приложением.\n")
                append("При подтверждении содержимое плейлиста будет очищено.\n")
                if (userFiles.isNotEmpty()) {
                    append("\nСледующие созданные вами записи будут удалены:\n")
                    append(userFiles.joinToString("\n") { "${userFiles.indexOf(it) + 1}. $it" })
                }
                else {
                    append("Импортированные внешние аудиофайлы не удалятся с диска.")
                }
            }

            !isProtected && isEmpty -> "Хотите удалить пустой плейлист '$playlistName'?"

            else -> buildString {
                append("Плейлист \"$playlistName\" будет удален при подтверждении.\n")
                if (userFiles.isNotEmpty()) {
                    append("\nБудут удалены следующие созданные вами записи:\n")
                    append(userFiles.joinToString("\n") { "${userFiles.indexOf(it) + 1}. $it" })
                }
                append("\nИмпортированные аудиофайлы из внешних папок удалены не будут.")
            }
        }
    }

    private fun getFileExtension(fileSchema: FileSchema): String {
        return File(fileSchema.fileName).extension
    }

    fun defineIconSize(fileSchema: FileSchema): Int {
        if (fileSchema.uri == null) {
            return 11
        }
        return 16
    }

    fun getPlayButtonIcon(fileSchema: FileSchema?): String {
        if (fileSchema != null) {
            if (fileSchema.uri == null) {
                return "•၊၊||၊|။၊၊||၊|။"
            }
            return "\uD83D\uDCC4\n.${getFileExtension(fileSchema)}"
        }
        return ""
    }

    fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getStatus(status: Status): String {
        return when (status){
            Status.PLAYING -> "Играю"
            Status.RECORDING -> "Записываю"
            Status.IDLE -> ""
        }
    }

    fun getTrackTimeLabel(currentTimeMillis: Long, trackDuration: Int): String {
        val currentTimeFormatted = formatTime(currentTimeMillis)
        val trackDurationFormatted = formatTime(trackDuration.toLong())
        return "$currentTimeFormatted / $trackDurationFormatted"
    }
}