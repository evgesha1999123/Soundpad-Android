package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.player.MediaPlayer
import com.simplemobiletools.voicerecorder.recorder.Mp3Recorder
import recorder.TimerViewModel

@Composable
fun MyAppNavGraph(mp3Recorder: Mp3Recorder, fileRepo: FileRepo, mediaPlayer: MediaPlayer, timerViewModel: TimerViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(mp3Recorder = mp3Recorder, fileRepo = fileRepo, mediaPlayer = mediaPlayer, timerViewModel = timerViewModel, navController = navController)
        }
        composable("menu") { MenuScreen(navController) }
    }
}