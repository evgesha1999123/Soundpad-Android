package com.example.audiorecorder

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.helpers.AudioTuner
import com.example.myapplication.navigation.HomeScreen
import com.example.myapplication.navigation.MenuScreen
import com.example.myapplication.navigation.Screen
import com.example.myapplication.player.MediaPlayer
import com.example.myapplication.recorder.Mp3Recorder
import com.example.myapplication.recorder.TimerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mp3Recorder = Mp3Recorder(context = this, audioConfig = AudioTuner(this).detectOptimalConfig())
        val fileRepo = FileRepo("records", this)
        val mediaPlayer = MediaPlayer(context = this)
        val timerViewModel = TimerViewModel()

        mediaPlayer.initMediaPlayer()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            200
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.HOME.route) {
                        composable(Screen.HOME.route) {
                            HomeScreen(
                                mp3Recorder = mp3Recorder,
                                fileRepo = fileRepo,
                                mediaPlayer = mediaPlayer,
                                timerViewModel = timerViewModel,
                                navController = navController
                            )
                        }
                        composable(Screen.MENU.route) {
                            MenuScreen( navController , fileRepo)
                        }
                    }
                }
            }
        }
    }
}