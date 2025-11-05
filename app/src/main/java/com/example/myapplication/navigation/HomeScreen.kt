package com.example.myapplication.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.player.MediaPlayer
import com.simplemobiletools.voicerecorder.recorder.Mp3Recorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import recorder.TimerViewModel
import java.io.File

@Composable
fun HomeScreen(
    mp3Recorder: Mp3Recorder,
    fileRepo: FileRepo,
    mediaPlayer: MediaPlayer,
    timerViewModel: TimerViewModel,
    navController: NavController
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var deleteFilesTrigger by remember { mutableIntStateOf(0) }
    var deleting by remember { mutableStateOf(false) }

    PlayButtons(
        mediaPlayer,
        appendFileTrigger = refreshTrigger,
        deleteFilesTrigger = deleteFilesTrigger,
        deleteSingleFile = deleting,
        fileRepo = fileRepo
    )
    MicrophoneControls(mp3Recorder, fileRepo, { refreshTrigger++ })
    FileControls(fileRepo, { deleteFilesTrigger++ }, onDeletingChange = { newValue -> deleting = newValue })
    StopPlayAudioControl(mediaPlayer)
    Timer(mp3Recorder, timerViewModel)
    MenuButton(navController)
}

@Composable
fun PlayButtons(
    audioPlayer: com.example.myapplication.player.MediaPlayer,
    fileRepo: FileRepo,
    appendFileTrigger: Int,
    deleteFilesTrigger: Int,
    deleteSingleFile: Boolean
) {
    val files = remember { mutableStateListOf<File>() }
    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val deletedIndex = remember { mutableIntStateOf(-1) }
    val deletedFile = remember { mutableStateOf(File("")) }
    val playing = audioPlayer.playing.collectAsState()
    var currentPlayingIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(appendFileTrigger, deleteFilesTrigger) {
        files.clear()
        files.addAll(fileRepo.listFiles())
    }

    LaunchedEffect(deletedIndex.intValue) {
        if (deleteSingleFile && deletedIndex.intValue >= 0 && deletedIndex.intValue < files.size) {
            val fileToRemove = fileRepo.getFile(deletedIndex.intValue)
            // Сначала удаляем из репозитория
            val success = fileRepo.deleteFile(fileToRemove)
            if (success) {
                // Затем удаляем из локального списка
                files.removeAt(deletedIndex.intValue)
            }
            deletedIndex.intValue = -1
        }
    }
    Card(
        modifier = Modifier
            .offset(y = -(50.dp))
            .wrapContentSize()
            .padding(10.dp)
            .height(590.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 3 колонки
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(files.size) { index ->
                val file = files[index]
                Button(
                    onClick = {
                        if (!deleteSingleFile) {
                            coroutineScope.launch {
                                currentPlayingIndex = index
                                audioPlayer.playFile(
                                    File(
                                        fileRepo.getFile(index).toString()
                                    )
                                )
                            }
                        } else {
                            deletedIndex.intValue = index
                            deletedFile.value = File(fileRepo.getFile(index).toString())
                        }
                    },
                    modifier = Modifier
                        .height(100.dp)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        if (deleteSingleFile) {
                            Color.Red
                        } else if (currentPlayingIndex == index && playing.value) {
                            Color.Green
                        } else {
                            Color.Unspecified
                        }
                    )
                ) {
                    Text(
                        text = ""
                    )
                }
            }
        }
    }
}

@Composable
fun FileControls(fileRepo: FileRepo, onPurgeFiles: () -> Unit, onDeletingChange: (Boolean) -> Unit) {
    var deleting by remember { mutableStateOf(false) }
    var showPurgeFilesDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(20.dp)
            .size(75.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart), // ← Выравниваем столбец в левом нижнем углу
            horizontalAlignment = Alignment.Start // ← Выравниваем кнопки по левому краю внутри столбца
        ) {
            // Удаление выбранного файла
            Button(
                onClick = {
                    if (!deleting) {
                        deleting = true
                    }
                    else {
                        deleting = false
                    }
                    Log.d("Deleting", "Deleting: $deleting")
                    onDeletingChange(deleting)
                },
                modifier = Modifier
                    .size(50.dp)
                    .aspectRatio(1f)
                    .offset(x = 20.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (deleting) "-" else "♫",
                    color = if (deleting) Color.Red else Color.Black,
                    fontSize = if (deleting) 45.sp else 25.sp)
            }

            // Небольшой отступ между кнопками
            Spacer(modifier = Modifier.height(8.dp))

            // Удаление всех файлов из плейлиста
            Button(
                onClick = {
                    showPurgeFilesDialog = true
                },
                modifier = Modifier
                    .size(50.dp)
                    .aspectRatio(1f)
                    .offset(x = 20.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("\uD83D\uDDD1\uFE0F", fontSize = 20.sp)
                }
            }
        }
    }
    if (showPurgeFilesDialog) {
        Box() {
            AlertDialog(
                onDismissRequest = { showPurgeFilesDialog = false },
                title = { Text("Удаление файлов") },
                text = {Text("Вы действительно хотите удалить ВСЕ файлы из текущего плейлиста?")},
                confirmButton = {
                    Button(
                        onClick = {
                            fileRepo.purgeDirectory()
                            onPurgeFiles()
                            showPurgeFilesDialog = false
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                    ) {
                        Text("Да")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPurgeFilesDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun MicrophoneControls(
    recorder: Mp3Recorder,
    fileRepo: FileRepo,
    onRecordStopped: () -> Unit = {}
) {
    var recording by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(20.dp)
            .size(75.dp)
    ) {
        Button(
            onClick = {
                if (!recording) {
                    recording = true
                    recorder.prepare()
                    recorder.setOutputFile(
                        File(
                            fileRepo.getCurrentDirectory().toString(),
                            "${System.currentTimeMillis()}.mp3"
                        ).toString()
                    )
                    recorder.start()
                } else {
                    try {
                        recording = false
                        recorder.stop()
                        recorder.release()
                        onRecordStopped()
                    } catch (e: Exception) {
                        Log.e("Error closing recording stream:", e.toString())
                    }

                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .size(100.dp)
                .aspectRatio(1f)
                .offset(x = (-1).dp),
            shape = RoundedCornerShape(8.dp) // или RectangleShape
        ) {
            Text(if (recording) "✋" else "\uD83D\uDD34", fontSize = 35.sp)
        }
    }
}
@Composable
fun StopPlayAudioControl(audioPlayer: com.example.myapplication.player.MediaPlayer){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 29.dp)
            .offset(x = (-39).dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = {
                if (audioPlayer.playing.value) {
                    audioPlayer.stop()
                }
            },
            modifier = Modifier
                .size(100.dp)
                .aspectRatio(1f),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Stop,
                contentDescription = "Стоп",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun Timer(mp3Recorder: Mp3Recorder, timerViewModel: TimerViewModel) {
    val recording by mp3Recorder.recording.collectAsState()
    val timeInMillis by timerViewModel.timeInMillis.collectAsState()
    val timerLabel = formatTime(timeInMillis)

    LaunchedEffect(recording) {
        if (recording) {
            timerViewModel.startTimer()
        }
        else {
            timerViewModel.stopTimer()
            timerViewModel.resetTimer()
        }
    }

    Box(
        modifier = Modifier
            .padding(20.dp)
            .size(120.dp)
    ) {
        Text(
            text = timerLabel,
            modifier = Modifier
                .size(width = 200.dp, height = 75.dp)
                .align(alignment = Alignment.BottomCenter)
                .offset(x = 60.dp, y = -(85.dp)),
            fontSize = 28.sp
        )
    }
}

@Composable
fun MenuButton(navController: NavController) {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .size(30.dp)
    ) {
        Button(
            onClick = {
                Log.i("Menu button", "Open Menu...")
                navController.navigate("menu")
            },
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .size(50.dp)
                .aspectRatio(1f)
                .offset(x = (20).dp, y = (15).dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("☰", fontSize = 28.sp)
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}