package com.example.myapplication.playlist_repository

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSchema(
    val uid: String,
    val name: String,
    val absolutePath: String,
    val created: String,
    val content: MutableList<FileSchema> = mutableListOf()
)

@Serializable
data class FileSchema(
    val uid: String,
    val fileName: String,
    val absolutePath: String,
    val isUserRecord: Boolean,
    val isDeletable: Boolean,
    val created: String
)