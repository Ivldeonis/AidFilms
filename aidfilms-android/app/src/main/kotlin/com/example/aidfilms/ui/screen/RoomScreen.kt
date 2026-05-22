package com.example.aidfilms.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aidfilms.service.SyncService
import com.example.aidfilms.model.PlaybackState

@Composable
fun RoomScreen(
    onStartSession: (String, String, Boolean) -> Unit
) {
    var roomId by remember { mutableStateOf("") }
    var streamUrl by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AidFilms P2P", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = roomId,
            onValueChange = { roomId = it },
            label = { Text("Room ID") },
            modifier = Modifier.fillMaxWidth()
        )

        if (isHost) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = streamUrl,
                onValueChange = { streamUrl = it },
                label = { Text("Stream URL (MP4/M3U8)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isHost, onClick = { isHost = true })
            Text("Host")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = !isHost, onClick = { isHost = false })
            Text("Guest")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (roomId.isNotEmpty()) {
                    if (isHost) {
                        val syncService = SyncService(roomId)
                        syncService.createRoom(roomId, PlaybackState(url = streamUrl, adminId = "host"))
                        onStartSession(roomId, streamUrl, true)
                    } else {
                        // For guest, we need to fetch URL from Firebase first or just pass empty and let sync handle it
                        onStartSession(roomId, "", false)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isHost) "Створити" else "Приєднатися")
        }
    }
}
