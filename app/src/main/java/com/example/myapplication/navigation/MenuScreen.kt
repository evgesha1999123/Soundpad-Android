package com.example.myapplication.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.fileRepo.FileRepo
import com.example.myapplication.playlistCreator.PlaylistCreatorButton
import com.example.myapplication.utils.TextUtils

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MenuScreen(navController: NavController, fileRepo: FileRepo) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var playlistNames by remember { mutableStateOf(fileRepo.getAllPlaylists()) }

    LaunchedEffect(refreshTrigger) {
        playlistNames = fileRepo.getAllPlaylists()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 20.dp)
        ) {
            BackButton(navController)

            PlaylistCreatorButton(
                fileRepo = fileRepo,
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .size(50.dp)
                    .aspectRatio(1f)
                    .offset(x = 300.dp),
                onRefreshTrigger = { refreshTrigger++ }
            )
        }

        PlaylistMenu(
            playlistNames = playlistNames,
            modifier = Modifier.weight(1f),
            fileRepo,
            navController,
            onPlaylistDeleted = { refreshTrigger++ }
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
    navController: NavController,
    onPlaylistDeleted: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(playlistNames) { playlistName ->
            PlaylistItem(
                playlistName = playlistName,
                fileRepo,
                navController,
                onPlaylistDeleted = onPlaylistDeleted
            )
        }
    }
}

@Composable
fun PlaylistItem(
    playlistName: String,
    fileRepo: FileRepo,
    navController: NavController,
    onPlaylistDeleted: () -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            fileRepo.setDirectory(playlistName)
            fileRepo.listFileSchemas(playlistName)
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
                modifier = Modifier.padding(start = 12.dp).widthIn(max = 190.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Log.i("Удалить плейлист", "Удалить этот плейлист")
                            showDialog.value = true
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
    if (showDialog.value) {
        PlaylistDeleterDialog(
            playlistName = playlistName,
            fileRepo = fileRepo,
            onConfirm = {
                fileRepo.deletePlaylist(playlistName)
                onPlaylistDeleted()
                showDialog.value = false
            },
            onDismiss = { showDialog.value = false }
        )
    }
}

@Composable
fun PlaylistDeleterDialog(
    playlistName: String,
    fileRepo: FileRepo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val description = remember(playlistName) {
        TextUtils().getDeletedPlaylistFilesDescription(fileRepo, playlistName)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удаление плейлиста") },
        text = { Text(description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Подтвердить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}