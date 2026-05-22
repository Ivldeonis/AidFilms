package com.example.aidfilms.model
import kotlinx.serialization.Serializable
@Serializable
data class SocketMessage(
    val type: String,
    val url: String? = null,
    val position: Double? = null,
    val playing: Boolean? = null,
    val speed: Float? = null,
    val timestamp: Long? = null
)
