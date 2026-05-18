package com.example.aidfilms.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoomScreen(onJoinRoom: (String, String) -> Unit) {
    var roomId by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AidFilms - Спільний перегляд", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = roomId,
            onValueChange = { roomId = it },
            label = { Text("ID Кімнати") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            label = { Text("URL Відео (m3u8, mp4, hls)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (roomId.isNotBlank() && videoUrl.isNotBlank()) onJoinRoom(roomId, videoUrl) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Приєднатися")
        }
    }
}
