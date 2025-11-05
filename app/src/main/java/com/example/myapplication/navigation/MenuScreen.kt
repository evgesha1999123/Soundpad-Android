package com.example.myapplication.navigation

import android.util.Log
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

@Composable
fun MenuScreen(navController: NavController) {
    val playlistList = listOf(
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
        "Плейлист 1", "Плейлист 2", "Мои треки", "Подкасты",
    )

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
            playlistNames = playlistList,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BackButton(navController: NavController) {
    Button(
        onClick = {
            Log.i("navigation", "navigate back")
            navController.navigate("home")
        },
        modifier = Modifier.size(50.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text("🏠", fontSize = 24.sp)
    }
}

@Composable
fun PlaylistMenu(playlistNames: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(playlistNames) { playlistName ->
            PlaylistItem(playlistName = playlistName)
        }
    }
}

@Composable
fun PlaylistItem(playlistName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = { /* открыть плейлист */ }
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
        }
    }
}