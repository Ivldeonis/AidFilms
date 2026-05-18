package com.example.aidfilms.service

import com.example.aidfilms.model.PlaybackState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SyncService(private val roomId: String = "") {
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")
    private val currentRoomRef = if (roomId.isNotEmpty()) roomsRef.child(roomId) else null

    fun observePlaybackState(): Flow<PlaybackState?> = callbackFlow {
        if (currentRoomRef == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.getValue(PlaybackState::class.java)
                trySend(state)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        currentRoomRef.addValueEventListener(listener)
        awaitClose { currentRoomRef.removeEventListener(listener) }
    }

    fun observeRooms(): Flow<List<String>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { it.key }
                trySend(rooms)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        roomsRef.addValueEventListener(listener)
        awaitClose { roomsRef.removeEventListener(listener) }
    }

    fun observeConnectionStatus(): Flow<Boolean> = callbackFlow {
        val connectedRef = database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                trySend(connected)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        connectedRef.addValueEventListener(listener)
        awaitClose { connectedRef.removeEventListener(listener) }
    }

    fun updatePlaybackState(state: PlaybackState) {
        currentRoomRef?.setValue(state)
    }
}
