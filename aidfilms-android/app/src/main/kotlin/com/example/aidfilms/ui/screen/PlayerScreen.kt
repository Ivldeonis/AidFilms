package com.example.aidfilms.ui.screen

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
fun PlayerScreen(roomId: String, initialUrl: String, userId: String) {
    val context = LocalContext.current
    val syncService = remember {
        try {
            SyncService(roomId)
        } catch (e: Exception) {
            FileLogger.logError(context, e)
            null
        }
    }
    val isConnected by (syncService?.observeConnectionStatus() ?: flowOf(false)).collectAsState(initial = false)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(initialUrl)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var remoteState by remember { mutableStateOf<PlaybackState?>(null) }

    // Listen to remote changes
    LaunchedEffect(roomId) {
        syncService?.observePlaybackState()?.collectLatest { state ->
            remoteState = state
            state?.let {
                // If we are not admin, we follow the remote state
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
                            url = initialUrl,
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
                            url = initialUrl,
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
                    text = if (isConnected) "Sync On" else "Offline",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) "Розтягнути" else "Оригінал",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (remoteState?.adminId == userId) {
                Text("Ви Адмін", color = Color.Yellow, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
