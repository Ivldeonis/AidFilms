package com.example.aidfilms

import com.example.aidfilms.model.PlaybackState
import org.junit.Test
import org.junit.Assert.*

class PlaybackStateTest {
    @Test
    fun testPlaybackStateCreation() {
        val state = PlaybackState(url = "http://test.com/video.mp4", position = 1000L, isPlaying = true)
        assertEquals("http://test.com/video.mp4", state.url)
        assertEquals(1000L, state.position)
        assertTrue(state.isPlaying)
    }
}
