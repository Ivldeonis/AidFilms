package com.example.aidfilms.ui.screen

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.aidfilms.model.PlaybackState
import com.example.aidfilms.service.SyncService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(roomId: String, initialUrl: String) {
    val context = LocalContext.current
    val syncService = remember { SyncService(roomId) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(initialUrl)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    var isLocalChange by remember { mutableStateOf(false) }

    // Listen to remote changes
    LaunchedEffect(roomId) {
        syncService.observePlaybackState().collectLatest { remoteState ->
            remoteState?.let {
                if (!isLocalChange) {
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

    // Sync local state to remote
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isLocalChange) {
                    syncService.updatePlaybackState(
                        PlaybackState(
                            url = initialUrl,
                            position = exoPlayer.currentPosition,
                            isPlaying = isPlaying
                        )
                    )
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK && !isLocalChange) {
                    syncService.updatePlaybackState(
                        PlaybackState(
                            url = initialUrl,
                            position = newPosition.contentPositionMs,
                            isPlaying = exoPlayer.isPlaying
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
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
