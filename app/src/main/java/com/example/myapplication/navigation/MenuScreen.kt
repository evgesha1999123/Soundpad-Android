package com.example.myapplication.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.fileRepo.FileRepo

@Composable
fun MenuScreen(navController: NavController, fileRepo: FileRepo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(start = 20.dp)
        ) {
            BackButton(navController)
        }

        PlaylistMenu(
            playlistNames = fileRepo.getAllPlaylists(),
            modifier = Modifier.weight(1f),
            fileRepo,
            navController
        )
    }
}

@Composable
fun BackButton(navController: NavController) {
    Button(
        onClick = {
            Log.i("navigation", "navigate back")
            navController.navigate(Screen.HOME.route) {
                popUpTo(Screen.HOME.route) { inclusive = true }
            }
        },
        modifier = Modifier.size(50.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text("🏠", fontSize = 24.sp)
    }
}

@Composable
fun PlaylistMenu(
    playlistNames: MutableList<String>,
    modifier: Modifier = Modifier,
    fileRepo: FileRepo,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(playlistNames) { playlistName ->
            PlaylistItem(playlistName = playlistName, fileRepo, navController)
        }
    }
}

@Composable
fun PlaylistItem(playlistName: String, fileRepo: FileRepo, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            fileRepo.setDirectory(playlistName)
            fileRepo.listFiles()
            navController.navigate(Screen.HOME.route) {
                popUpTo(Screen.HOME.route) { inclusive = true }
            }
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = playlistName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { Log.i("Изменить", "Изменить имя плейлиста") },
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("✏\uFE0F")
                    }

                    Button(
                        onClick = {
                            Log.i("Удалить плейлист", "Удалить этот плейлист")
                            fileRepo.deletePlaylist(playlistName)
                        },
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("❌")
                    }
                }
            }
        }
    }
}