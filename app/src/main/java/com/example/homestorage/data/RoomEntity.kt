package com.example.homestorage.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    indices = [Index(value=["name"], unique=true)]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
