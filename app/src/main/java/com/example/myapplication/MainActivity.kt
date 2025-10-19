package com.example.audiorecorder

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.myapplication.Player.AudioPlayer
import com.example.myapplication.Recorder.Dictaphone
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.models.AudioConfigModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var audioConfig: AudioConfigModel = AudioConfigModel()
        val dictaphone = Dictaphone(audioConfig)
        val fileRepo = FileRepo(File(filesDir.absolutePath, "records"))
        println("Список файлов >>>>>>>>>> ${fileRepo.listFiles()}")
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
                    var refreshTrigger by remember { mutableStateOf(0) }

                    AudioButton(
                        AudioPlayer(_audioConfig = audioConfig),
                        refreshTrigger = refreshTrigger,
                        fileRepo = fileRepo
                    )
                    MicrophoneControls(dictaphone, fileRepo, { refreshTrigger++ })
                }
            }
        }
    }

    @Composable
    fun AudioButton(audioPlayer: AudioPlayer, fileRepo: FileRepo, refreshTrigger: Int) {
        var files by remember { mutableStateOf(fileRepo.listFiles()) }

        LaunchedEffect(refreshTrigger) {
            files = fileRepo.listFiles()
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
                            CoroutineScope(Dispatchers.IO).launch {
                                audioPlayer.playPcm(fileRepo.getFile(index).toString())
                            }
                        },
                        modifier = Modifier
                            .height(100.dp)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(8.dp)
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
                        val outFile =
                            dictaphone.startRecording(recordDir = fileRepo.getCurrentDirectory())
                        Log.d("MainActivity", "Getting file: out <<- ${outFile.toString()}")
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
}