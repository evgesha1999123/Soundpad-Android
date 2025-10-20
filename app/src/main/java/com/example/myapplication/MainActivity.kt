package com.example.audiorecorder

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.myapplication.Player.AudioPlayer
import com.example.myapplication.Recorder.Dictaphone
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.models.AudioConfigModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var audioConfig: AudioConfigModel = AudioConfigModel()
        val dictaphone = Dictaphone(this)
        val fileRepo = FileRepo(File(filesDir.absolutePath, "records"))
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        200
                    )
                    var refreshTrigger by remember { mutableIntStateOf(0) }
                    var deleteFilesTrigger by remember { mutableIntStateOf(0) }
                    var deleting by remember { mutableStateOf(false) }

                    PlayButtons(
                        AudioPlayer(this),
                        appendFileTrigger = refreshTrigger,
                        deleteFilesTrigger = deleteFilesTrigger,
                        deleteSingleFile = deleting,
                        fileRepo = fileRepo
                    )
                    MicrophoneControls(dictaphone, fileRepo, { refreshTrigger++ })
                    FileControls(fileRepo, { deleteFilesTrigger++ }, onDeletingChange = { newValue -> deleting = newValue })
                }
            }
        }
    }

    @Composable
    private fun DeleteChosenFileLaunchedEffect(
        fileElements: SnapshotStateList<File>,
        deletedFile: File,
        deletedIndex: MutableIntState,
        deleteSingleFile: Boolean,
        fileRepo: FileRepo
    ) {
        LaunchedEffect(deletedFile) {
            if (deleteSingleFile) {
                val deleted = fileElements.remove(fileRepo.getFile(deletedIndex.intValue))
                if (deleted) {
                    deletedIndex.intValue = -1
                }
            }
        }
    }
    @Composable
    fun PlayButtons(
        audioPlayer: AudioPlayer,
        fileRepo: FileRepo,
        appendFileTrigger: Int,
        deleteFilesTrigger: Int,
        deleteSingleFile: Boolean
    ) {
        var files = remember { mutableStateListOf<File>() }
        val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        var deletedIndex = remember { mutableIntStateOf(-1) }
        var deletedFile = remember { mutableStateOf(File("")) }

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

        Box(
            modifier = Modifier
                .padding(20.dp)
                .size(75.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 колонки
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files.size) { index ->
                    val file = files[index]
                    Button(
                        onClick = {
                            if (!deleteSingleFile){
                                coroutineScope.launch {
                                    audioPlayer.playFile(File(fileRepo.getFile(index).toString()))
                                }
                            }
                            else {
                                deletedIndex.intValue = index
                                deletedFile.value = File(fileRepo.getFile(index).toString())
                            }
                        },
                        modifier = Modifier
                            .height(100.dp)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            if (deleteSingleFile) Color.Red else Color.Unspecified
                        )
                    ) {
                        Text("PLAY $index")
                    }
                }
            }
        }
    }

    @Composable
    fun MicrophoneControls(
        dictaphone: Dictaphone,
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
                        dictaphone.startRecording(
                            File(
                                fileRepo.getCurrentDirectory().toString(),
                                "${System.currentTimeMillis()}.mp4"
                            )
                        )
                    } else {
                        recording = false
                        dictaphone.stopRecording()
                        if (!dictaphone.isRecording()) {
                            onRecordStopped()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(100.dp) // квадратный размер
                    .aspectRatio(1f), // гарантирует квадрат
                shape = RoundedCornerShape(8.dp) // или RectangleShape
            ) {
                Text(if (recording) "✋" else "\uD83D\uDD34", fontSize = 35.sp)
            }
        }
    }

    @Composable
    fun FileControls(fileRepo: FileRepo, onPurgeFiles: () -> Unit, onDeletingChange: (Boolean) -> Unit) {
        var deleting by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .padding(20.dp)
                .size(75.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart), // ← Выравниваем столбец в левом нижнем углу
                horizontalAlignment = Alignment.Start // ← Выравниваем кнопки по левому краю внутри столбца
            ) {
                // Кнопка с минусом - сверху
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
                        .aspectRatio(1f),
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

                // Кнопка с корзинкой - снизу
                Button(
                    onClick = {
                        fileRepo.purgeDirectory()
                        onPurgeFiles()
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .aspectRatio(1f),
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
    }
}