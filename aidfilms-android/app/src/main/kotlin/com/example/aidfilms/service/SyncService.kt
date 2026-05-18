package com.example.aidfilms.service

import com.example.aidfilms.model.PlaybackState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SyncService(private val roomId: String) {
    private val database = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)

    fun observePlaybackState(): Flow<PlaybackState?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.getValue(PlaybackState::class.java)
                trySend(state)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    fun updatePlaybackState(state: PlaybackState) {
        database.setValue(state)
    }
}
