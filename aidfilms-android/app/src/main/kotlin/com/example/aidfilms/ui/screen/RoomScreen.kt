package com.example.aidfilms.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aidfilms.service.SyncService
import com.example.aidfilms.utils.FileLogger
import androidx.compose.ui.platform.LocalContext

@Composable
fun RoomScreen(onJoinRoom: (String, String) -> Unit) {
    val context = LocalContext.current
    var roomId by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    val syncService = remember {
        try {
            SyncService()
        } catch (e: Exception) {
            FileLogger.logError(context, e)
            null
        }
    }

    val availableRooms by (syncService?.observeRooms() ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())
    val isConnected by (syncService?.observeConnectionStatus() ?: kotlinx.coroutines.flow.flowOf(false)).collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Статус: ", style = MaterialTheme.typography.bodySmall)
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isConnected) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.size(8.dp)
            ) {}
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isConnected) "Підключено" else "Відключено", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("AidFilms - Спільний перегляд", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

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
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (roomId.isNotBlank() && videoUrl.isNotBlank()) onJoinRoom(roomId, videoUrl) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Приєднатися до нової кімнати")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Або виберіть існуючу:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(availableRooms) { room ->
                ListItem(
                    headlineContent = { Text(room) },
                    modifier = Modifier.clickable {
                        roomId = room
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
