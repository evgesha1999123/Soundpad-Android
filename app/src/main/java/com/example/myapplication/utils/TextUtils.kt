package com.example.myapplication.utils

import com.example.myapplication.fileRepo.FileRepo

class TextUtils {
    fun getDeletedPlaylistFilesDescription(fileRepo: FileRepo, playlistName: String): String {
        if (playlistName == "records") {
            return "Плейлист 'Records' защищен от удаления, так как используется \n" +
                    "для записи новых аудиофайлов приложением.\n При подтверждении содержимое плейлиста будет очищено."
        }

        val userRecordSchemas = mutableListOf<String>()
        val allFileNames = fileRepo.listFileSchemas(playlistName)

        for (file in allFileNames) {
            if (file.isUserRecord) {
                userRecordSchemas.add(file.fileName)
            }
        }

        if (userRecordSchemas.isNotEmpty()) {
            val fileList = userRecordSchemas
                .mapIndexed { index, fileName -> "${index + 1}. $fileName" }
                .joinToString("\n")

            return "Плейлист \"$playlistName\" будет удален при подтверждении.\n\n" +
                    "Будут удалены следующие созданные вами записи:\n" +
                    fileList
        }

        return "Плейлист \"$playlistName\" будет удален при подтверждении.\n" +
                "Импортированные аудиофайлы из внешних папок удалены не будут."
    }
}