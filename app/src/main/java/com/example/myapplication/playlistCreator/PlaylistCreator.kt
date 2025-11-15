package com.example.myapplication.playlistCreator

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.fileRepo.FileRepo

// Создание нового плейлиста
@Composable
fun PlaylistCreatorButton(
    fileRepo: FileRepo,
    modifier: Modifier,
    onRefreshTrigger: () -> Unit
) {
    var showAskDialog by remember { mutableStateOf(false) }
    var showFailedState by remember { mutableStateOf(false) }

    Box() {
        Button(
            onClick = { showAskDialog = true },
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("➕", fontSize = 32.sp)
        }
    }
    if (showAskDialog) {
        PlaylistCreatorDialog(
            onDismiss = { showAskDialog = false },
            onConfirm = {
                showAskDialog = false
                onRefreshTrigger()
            },
            fileRepo,
            onFailed = { showFailedState = true }
        )
    }

    if (showFailedState) {
        PlaylistCreateFailedDialog(
            { showFailedState = false }
        )
    }
}

@Composable
fun PlaylistCreatorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    fileRepo: FileRepo,
    onFailed: () -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Создание плейлиста",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Введите название для нового плейлиста",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле ввода для названия плейлиста
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Название плейлиста") },
                    placeholder = { Text("Мой плейлист") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            Log.i("Создание плейлиста", "Создан плейлист: $playlistName")
                            val result = fileRepo.createPlaylist(playlistName)
                            if (result.exceptionOrNull().toString().contains("уже существует")) {
                                onFailed()
                            }
                            else {
                                onConfirm(playlistName)
                            }
                        },
                        enabled = playlistName.isNotBlank()
                    ) {
                        Text("Создать")
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistCreateFailedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Ошибка") },
        text = { Text("Плейлист с таким именем уже существует.") },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Понял")
            }
        }
    )
}