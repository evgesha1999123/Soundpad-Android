package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.player.MediaPlayer
import com.example.myapplication.recorder.Mp3Recorder
import com.example.myapplication.recorder.TimerViewModel

@Composable
fun AppNavigationGraph(mp3Recorder: Mp3Recorder, fileRepo: FileRepo, mediaPlayer: MediaPlayer, timerViewModel: TimerViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.HOME.route) {
        composable(Screen.HOME.route) {
            HomeScreen(mp3Recorder = mp3Recorder, fileRepo = fileRepo, mediaPlayer = mediaPlayer, timerViewModel = timerViewModel, navController = navController)
        }
        composable(Screen.MENU.route) { MenuScreen(navController, fileRepo) }
    }
}