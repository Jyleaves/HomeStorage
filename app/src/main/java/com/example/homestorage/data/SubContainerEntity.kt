// SubContainerEntity.kt
package com.example.homestorage.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subcontainers",
    indices = [Index(value = ["room", "containerName", "subContainerName"], unique = true)]
)
data class SubContainerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val room: String,              // 所属房间
    val containerName: String,     // 对应主容器名称
    val subContainerName: String,   // 二级容器名称
    val hasThirdContainer: Boolean = false
)
