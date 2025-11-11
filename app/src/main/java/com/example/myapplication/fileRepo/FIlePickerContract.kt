package com.example.myapplication.fileRepo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class FilePickerContract {
    companion object {
        fun createPickMultipleFilesIntent(vararg mimeTypes: String): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                // Ключевые флаги для постоянных разрешений
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }
}