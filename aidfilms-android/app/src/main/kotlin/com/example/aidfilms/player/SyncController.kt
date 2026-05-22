package com.example.aidfilms.player
import androidx.media3.exoplayer.ExoPlayer
import com.example.aidfilms.model.SocketMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Log
class SyncController(private val player: ExoPlayer, private val isHost: Boolean, private val sendMessage: (String) -> Unit) {
    private val json = Json { ignoreUnknownKeys = true }
    fun onMessageReceived(messageStr: String) {
        try {
            val msg = json.decodeFromString<SocketMessage>(messageStr)
            when (msg.type) {
                "play" -> { msg.position?.let { player.seekTo(it.toLong()) }; player.play() }
                "pause" -> { msg.position?.let { player.seekTo(it.toLong()) }; player.pause() }
                "seek" -> { msg.position?.let { player.seekTo(it.toLong()) } }
                "sync" -> { if (!isHost) handleSync(msg) }
            }
        } catch (e: Exception) { Log.e("SyncController", "Error parsing: $messageStr", e) }
    }
    private fun handleSync(msg: SocketMessage) {
        val remotePos = msg.position?.toLong() ?: return
        val remotePlaying = msg.playing ?: return
        val localPos = player.currentPosition
        val delta = Math.abs(localPos - remotePos)
        if (delta > 1000) player.seekTo(remotePos)
        else if (delta > 300) player.setPlaybackSpeed(if (localPos < remotePos) 1.02f else 0.98f)
        else player.setPlaybackSpeed(1.0f)
        if (remotePlaying != player.isPlaying) { if (remotePlaying) player.play() else player.pause() }
    }
    fun sendSync() {
        if (isHost) {
            val msg = SocketMessage("sync", position = player.currentPosition.toDouble(), playing = player.isPlaying, speed = player.playbackParameters.speed, timestamp = System.currentTimeMillis())
            sendMessage(json.encodeToString(msg))
        }
    }
    fun sendCommand(type: String, position: Long? = null) {
        if (isHost) {
            val msg = SocketMessage(type, position = position?.toDouble() ?: player.currentPosition.toDouble())
            sendMessage(json.encodeToString(msg))
        }
    }
}
