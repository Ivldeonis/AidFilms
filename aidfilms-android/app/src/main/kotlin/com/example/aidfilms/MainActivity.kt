package com.example.aidfilms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.aidfilms.ui.screen.PlayerScreen
import com.example.aidfilms.ui.screen.RoomScreen
import com.example.aidfilms.ui.theme.AidFilmsTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple persistent userId for this device
        val sharedPrefs = getSharedPreferences("aidfilms_prefs", MODE_PRIVATE)
        var userId = sharedPrefs.getString("user_id", null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("user_id", userId).apply()
        }

        setContent {
            AidFilmsTheme {
                var currentRoomId by remember { mutableStateOf<String?>(null) }
                var currentUrl by remember { mutableStateOf<String?>(null) }

                if (currentRoomId == null || currentUrl == null) {
                    RoomScreen(userId = userId!!) { roomId, url ->
                        currentRoomId = roomId
                        currentUrl = url
                    }
                } else {
                    PlayerScreen(currentRoomId!!, currentUrl!!, userId!!)
                }
            }
        }
    }
}
