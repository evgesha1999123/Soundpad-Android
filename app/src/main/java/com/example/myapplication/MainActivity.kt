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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.SupervisorJob
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
                    var refreshTrigger by remember { mutableIntStateOf(0) }
                    var deleteFilesTrigger by remember { mutableIntStateOf(0) }

                    PlayButtons(
                        AudioPlayer(_audioConfig = audioConfig),
                        appendFileTrigger = refreshTrigger,
                        deleteFilesTrigger = deleteFilesTrigger,
                        fileRepo = fileRepo
                    )
                    MicrophoneControls(dictaphone, fileRepo, { refreshTrigger++ })
                    FileControls(fileRepo, { deleteFilesTrigger++ })
                }
            }
        }
    }

    @Composable
    fun PlayButtons(
        audioPlayer: AudioPlayer,
        fileRepo: FileRepo,
        appendFileTrigger: Int,
        deleteFilesTrigger: Int
    ) {
        var files = remember { mutableStateListOf<File>() }
        val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        LaunchedEffect(appendFileTrigger, deleteFilesTrigger) {
            files.clear()
            files.addAll(fileRepo.listFiles())
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
                            coroutineScope.launch {
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

    @Composable
    fun FileControls(fileRepo: FileRepo, onPurgeFiles: () -> Unit){
        Box(
            modifier = Modifier
                .padding(20.dp)
                .size(75.dp)
        ){
            Button(
                onClick = {
                    fileRepo.purgeDirectory()
                    onPurgeFiles()
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .size(100.dp)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(8.dp)
            ) { Text("\uD83D\uDDD1\uFE0F", fontSize = 35.sp) }
        }
    }
}