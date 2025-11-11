package com.example.myapplication.playlist_repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchemaUtils {
    fun generateUid(length: Int = 16): String {
        if (length > 0) {
            return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, length)
        }
        throw IllegalArgumentException("Количество символов uid должно быть больше нуля!").also {
            Log.e(this::class.simpleName, "Количество символов uid должно быть больше нуля!")
        }
    }
    fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            // Способ 1: Через OpenableColumns (предпочтительный)
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1 && cursor.moveToFirst()) {
                    val name = cursor.getString(displayNameIndex)
                    if (!name.isNullOrEmpty()) return name
                }
            }

            // Способ 2: Из последнего сегмента пути
            uri.lastPathSegment?.let { lastSegment ->
                val decoded = URLDecoder.decode(lastSegment, "UTF-8")
                if (decoded.contains('.')) { // Проверяем что это похоже на файл
                    return decoded
                }
            }

            // Способ 3: Из всего пути
            uri.path?.let { path ->
                val fileName = path.substringAfterLast('/')
                if (fileName.isNotEmpty()) {
                    return URLDecoder.decode(fileName, "UTF-8")
                }
            }

            // Способ 4: Генерируем имя если все остальное не сработало
            "audio_${System.currentTimeMillis()}.mp3"

        } catch (e: Exception) {
            Log.e("FileName", "Error getting readable name: ${e.message}")
            "unknown_audio_${System.currentTimeMillis()}.mp3"
        }
    }
}