package com.example.myapplication

import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.File

data class deletedPlayButtonModel(
    val deleteSingleFile: Boolean,
    val index: Int,
    val file: File,
    val fileElements: SnapshotStateList<File>
)