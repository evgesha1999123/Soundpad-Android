package com.example.myapplication.fileRepo

data class FileSchema(
    val uid: String,
    val fileName: String,
    val absolutePath: String,
    val isUserRecord: Boolean,
    val isDeletable: Boolean,
    val created: String
)