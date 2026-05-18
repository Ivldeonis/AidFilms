package com.example.aidfilms.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PlaybackState(
    val url: String = "",
    val position: Long = 0L,
    val isPlaying: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
