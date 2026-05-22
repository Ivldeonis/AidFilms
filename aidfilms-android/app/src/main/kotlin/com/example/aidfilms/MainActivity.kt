package com.example.aidfilms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.aidfilms.ui.screen.PlayerScreen
import com.example.aidfilms.ui.screen.RoomScreen
import com.example.aidfilms.ui.theme.AidFilmsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AidFilmsTheme {
                var currentRoomId by remember { mutableStateOf<String?>(null) }
                var currentUrl by remember { mutableStateOf<String?>(null) }
                var isHost by remember { mutableStateOf(false) }

                if (currentRoomId == null) {
                    RoomScreen { roomId, url, host ->
                        currentRoomId = roomId
                        currentUrl = url
                        isHost = host
                    }
                } else {
                    PlayerScreen(
                        roomId = currentRoomId!!,
                        initialUrl = currentUrl ?: "",
                        isHost = isHost,
                        onBack = {
                            currentRoomId = null
                            currentUrl = null
                        }
                    )
                }
            }
        }
    }
}
