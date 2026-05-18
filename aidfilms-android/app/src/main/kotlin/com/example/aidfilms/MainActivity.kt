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

                if (currentRoomId == null || currentUrl == null) {
                    RoomScreen { roomId, url ->
                        currentRoomId = roomId
                        currentUrl = url
                    }
                } else {
                    PlayerScreen(currentRoomId!!, currentUrl!!)
                }
            }
        }
    }
}
