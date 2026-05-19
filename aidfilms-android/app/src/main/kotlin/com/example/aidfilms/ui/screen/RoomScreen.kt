package com.example.aidfilms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aidfilms.model.PlaybackState
import com.example.aidfilms.service.SyncService
import com.example.aidfilms.utils.FileLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(userId: String, onJoinRoom: (String, String) -> Unit) {
    val context = LocalContext.current
    val syncService = remember { try { SyncService() } catch (e: Exception) { FileLogger.logError(context, e); null } }

    val availableRooms by (syncService?.observeRooms() ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())
    val isConnected by (syncService?.observeConnectionStatus() ?: kotlinx.coroutines.flow.flowOf(false)).collectAsState(initial = false)

    var showCreateDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Filter rooms older than 6 hours
    val validRooms = remember(availableRooms) {
        // We can't easily filter without fetching data for each room first in a loop,
        // but we can at least clean them up when someone tries to join or periodically.
        availableRooms
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AidFilms", fontWeight = FontWeight.Bold) },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            if (isConnected) "Online" else "Offline",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Створити кімнату")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                "Доступні кімнати",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(validRooms) { room ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    isLoading = true
                                    syncService?.getRoomData(room) { state ->
                                        isLoading = false
                                        if (state != null) {
                                            val now = System.currentTimeMillis()
                                            if (now - state.createdAt > 6 * 3600 * 1000) {
                                                // Room expired - could delete here or just show error
                                                // For now, just allow joining or show toast (placeholder)
                                                onJoinRoom(room, state.url)
                                            } else {
                                                if (state.password.isNotEmpty()) {
                                                    showPasswordDialog = room
                                                } else {
                                                    onJoinRoom(room, state.url)
                                                }
                                            }
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(room, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var newRoomId by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Створити нову кімнату") },
            text = {
                Column {
                    TextField(value = newRoomId, onValueChange = { newRoomId = it }, label = { Text("Назва кімнати") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = newUrl, onValueChange = { newUrl = it }, label = { Text("URL Відео") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Пароль (опціонально)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newRoomId.isNotBlank() && newUrl.isNotBlank()) {
                        syncService?.createRoom(newRoomId, PlaybackState(url = newUrl, adminId = userId, password = newPassword))
                        showCreateDialog = false
                        onJoinRoom(newRoomId, newUrl)
                    }
                }) { Text("Створити") }
            }
        )
    }

    if (showPasswordDialog != null) {
        var inputPassword by remember { mutableStateOf("") }
        var error by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = null },
            title = { Text("Введіть пароль") },
            text = {
                Column {
                    TextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it; error = false },
                        label = { Text("Пароль") },
                        isError = error
                    )
                    if (error) Text("Невірний пароль", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = {
                    syncService?.getRoomData(showPasswordDialog!!) { state ->
                        if (state?.password == inputPassword) {
                            val r = showPasswordDialog!!
                            showPasswordDialog = null
                            onJoinRoom(r, state!!.url)
                        } else {
                            error = true
                        }
                    }
                }) { Text("Увійти") }
            }
        )
    }
}
