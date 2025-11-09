package com.example.myapplication.playlist_repository

import android.util.Log
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
}