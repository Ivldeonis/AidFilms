package com.example.aidfilms.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.aidfilms.player.SyncController
import com.example.aidfilms.service.SyncService
import com.example.aidfilms.webrtc.PeerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

@Composable
fun PlayerScreen(
    roomId: String,
    initialUrl: String,
    isHost: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var streamUrl by remember { mutableStateOf(initialUrl) }
    var connectionState by remember { mutableStateOf(PeerConnection.PeerConnectionState.NEW) }
    val syncService = remember { SyncService(roomId) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // Load initial URL or observe from Firebase
    LaunchedEffect(streamUrl) {
        if (streamUrl.isNotEmpty()) {
            val mediaItem = createMediaItem(streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    if (!isHost && streamUrl.isEmpty()) {
        LaunchedEffect(roomId) {
            syncService.observePlaybackState().collectLatest { state ->
                if (state != null && state.url.isNotEmpty() && streamUrl.isEmpty()) {
                    streamUrl = state.url
                }
            }
        }
    }

    val peerManager = remember {
        PeerManager(
            context = context,
            onIceCandidateGenerated = { candidate ->
                val map = mutableMapOf<String, Any>(
                    "sdpMid" to candidate.sdpMid,
                    "sdpMLineIndex" to candidate.sdpMLineIndex.toDouble(),
                    "sdp" to candidate.sdp
                )
                syncService.sendIceCandidate(map, isHost)
            },
            onConnectionStateChanged = { connectionState = it }
        )
    }

    val syncController = remember {
        SyncController(exoPlayer, isHost) { peerManager.sendMessage(it) }
    }

    LaunchedEffect(peerManager, syncController) {
        peerManager.onMessageReceived = { syncController.onMessageReceived(it) }
    }

    // Signaling Flow
    LaunchedEffect(roomId) {
        peerManager.createPeerConnection()
        if (isHost) {
            peerManager.createDataChannel()
            peerManager.createOffer { sdp ->
                syncService.sendOffer(sdp.description)
            }
            // Listen for Answer
            syncService.observeAnswer().collectLatest { sdp ->
                if (sdp != null && sdp.isNotEmpty()) {
                    peerManager.setRemoteDescription(SessionDescription(SessionDescription.Type.ANSWER, sdp))
                }
            }
        } else {
            // Guest Flow
            syncService.observeOffer().collectLatest { sdp ->
                if (sdp != null && sdp.isNotEmpty()) {
                    peerManager.setRemoteDescription(SessionDescription(SessionDescription.Type.OFFER, sdp))
                    peerManager.createAnswer { answer ->
                        syncService.sendAnswer(answer.description)
                    }
                }
            }
        }
    }

    // ICE Candidates Flow
    LaunchedEffect(roomId) {
        syncService.observeCandidates(!isHost).collectLatest { data ->
            try {
                val candidate = IceCandidate(
                    data["sdpMid"] as String,
                    (data["sdpMLineIndex"] as Double).toInt(),
                    data["sdp"] as String
                )
                peerManager.addIceCandidate(candidate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Host commands listener
    DisposableEffect(exoPlayer, isHost) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isHost) syncController.sendCommand(if (isPlaying) "play" else "pause")
            }
            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                if (isHost && reason == Player.DISCONTINUITY_REASON_SEEK) {
                    syncController.sendCommand("seek", newPosition.contentPositionMs)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(isHost, connectionState) {
        if (isHost && connectionState == PeerConnection.PeerConnectionState.CONNECTED) {
            while (true) {
                delay(2000)
                syncController.sendSync()
            }
        }
    }

    DisposableEffect(peerManager) {
        onDispose {
            peerManager.release()
            exoPlayer.release()
        }
    }

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { PlayerView(it).apply { player = exoPlayer; useController = true } },
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text("Кімната: $roomId", color = Color.White, style = MaterialTheme.typography.labelSmall)
            Text("Статус P2P: ${connectionState.name}", color = Color.White, style = MaterialTheme.typography.labelSmall)
            if (connectionState != PeerConnection.PeerConnectionState.CONNECTED) {
                LinearProgressIndicator(modifier = Modifier.width(100.dp).padding(top = 4.dp))
            }
        }
    }
}

private fun createMediaItem(url: String): MediaItem {
    val builder = MediaItem.Builder().setUri(url)
    when {
        url.contains(".m3u8") -> builder.setMimeType(MimeTypes.APPLICATION_M3U8)
        url.contains(".mpd") -> builder.setMimeType(MimeTypes.APPLICATION_MPD)
        url.contains(".mp4") -> builder.setMimeType(MimeTypes.VIDEO_MP4)
    }
    return builder.build()
}
