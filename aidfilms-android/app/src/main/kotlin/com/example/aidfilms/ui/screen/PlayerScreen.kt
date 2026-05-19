package com.example.aidfilms.ui.screen

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.aidfilms.model.PlaybackState
import com.example.aidfilms.service.SyncService
import com.example.aidfilms.utils.FileLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(roomId: String, initialUrl: String, userId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val window = (context as? android.app.Activity)?.window

    // Immersive Mode
    DisposableEffect(Unit) {
        window?.let {
            val controller = WindowCompat.getInsetsController(it, it.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            window?.let {
                val controller = WindowCompat.getInsetsController(it, it.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler { onBack() }

    val syncService = remember {
        try {
            SyncService(roomId).apply { joinPresence(userId) }
        } catch (e: Exception) {
            FileLogger.logError(context, e)
            null
        }
    }

    val isConnected by (syncService?.observeConnectionStatus() ?: flowOf(false)).collectAsState(initial = false)
    val participantCount by (syncService?.observeParticipantCount() ?: flowOf(1)).collectAsState(initial = 1)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(initialUrl)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var remoteState by remember { mutableStateOf<PlaybackState?>(null) }
    var showUrlDialog by remember { mutableStateOf(false) }

    // Listen to remote changes
    LaunchedEffect(roomId) {
        syncService?.observePlaybackState()?.collectLatest { state ->
            remoteState = state
            state?.let {
                if (it.url != initialUrl && it.url.isNotEmpty()) {
                    val mediaItem = MediaItem.fromUri(it.url)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                }

                if (state.adminId != userId) {
                    if (Math.abs(exoPlayer.currentPosition - it.position) > 2000) {
                        exoPlayer.seekTo(it.position)
                    }
                    if (it.isPlaying != exoPlayer.isPlaying) {
                        if (it.isPlaying) exoPlayer.play() else exoPlayer.pause()
                    }
                }
            }
        }
    }

    // Sync local state to remote ONLY IF ADMIN
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (remoteState?.adminId == userId) {
                    syncService?.updatePlaybackState(
                        PlaybackState(
                            url = exoPlayer.currentMediaItem?.localConfiguration?.uri.toString(),
                            position = exoPlayer.currentPosition,
                            isPlaying = isPlaying,
                            adminId = userId,
                            password = remoteState?.password ?: ""
                        )
                    )
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK && remoteState?.adminId == userId) {
                    syncService?.updatePlaybackState(
                        PlaybackState(
                            url = exoPlayer.currentMediaItem?.localConfiguration?.uri.toString(),
                            position = newPosition.contentPositionMs,
                            isPlaying = exoPlayer.isPlaying,
                            adminId = userId,
                            password = remoteState?.password ?: ""
                        )
                    )
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = {
                it.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (isConnected) Color.Green else Color.Red,
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sync: ${if (isConnected) "On" else "Off"} | 👥 $participantCount",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                 IconButton(onClick = {
                    remoteState?.let {
                         exoPlayer.seekTo(it.position)
                         if (it.isPlaying) exoPlayer.play() else exoPlayer.pause()
                    }
                }) {
                    Icon(Icons.Default.Refresh, "Sync", tint = Color.White)
                }

                if (remoteState?.adminId == userId) {
                    IconButton(onClick = { showUrlDialog = true }) {
                        Icon(Icons.Default.Edit, "Change Stream", tint = Color.White)
                    }
                }

                Button(
                    onClick = {
                        resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                            AspectRatioFrameLayout.RESIZE_MODE_FILL
                        } else {
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).align(Alignment.CenterVertically)
                ) {
                    Text(
                        if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) "Розтягнути" else "Оригінал",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (remoteState?.adminId == userId) {
                Text("Ви Адмін", color = Color.Yellow, style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showUrlDialog) {
        var newUrl by remember { mutableStateOf(remoteState?.url ?: "") }
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Змінити потік") },
            text = {
                TextField(value = newUrl, onValueChange = { newUrl = it }, label = { Text("Новий URL") })
            },
            confirmButton = {
                Button(onClick = {
                    syncService?.updateStreamUrl(newUrl)
                    showUrlDialog = false
                }) { Text("Оновити") }
            }
        )
    }
}
