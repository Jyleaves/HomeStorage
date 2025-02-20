package com.example.homestorage.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY id ASC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE name = :roomName")
    suspend fun deleteRoomByName(roomName: String)

    @Query("UPDATE rooms SET name = :newRoomName WHERE name = :oldRoomName")
    suspend fun updateRoomName(oldRoomName: String, newRoomName: String)
}
