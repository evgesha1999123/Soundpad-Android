package com.example.audiorecorder

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.myapplication.audioRecorder.AudioRecorder


class MainActivity : ComponentActivity() {

    private lateinit var outputFile: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputFile = "${externalCacheDir?.absolutePath}/compose_record.3gp"

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        200
                    )
                    MicrophoneController()
                }
            }
        }
    }
}

// 👇 Вынесено за пределы класса
@Composable
fun MicrophoneController() {
    var recording by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(20.dp)
            .size(100.dp)
    ){
        Button(
            onClick = {
                if (!recording) {
                    recording = true
                    println("recording -> $recording")
                    AudioRecorder().startRecording(
                        audioDir =
                            getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                            )
                    )
                }
                else {
                    recording = false
                    println("recording -> $recording")
                    AudioRecorder().stopRecording()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(100.dp) // квадратный размер
                .aspectRatio(1f), // гарантирует квадрат
            shape = RoundedCornerShape(8.dp) // или RectangleShape
        ) {
            Text(if (recording) "🛑 Остановить" else "🎙 Начать запись")
        }
    }
}