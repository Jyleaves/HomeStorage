// ThirdContainerEntity.kt
package com.example.homestorage.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "third_containers",
    indices = [Index(value = ["room", "containerName", "subContainerName", "thirdContainerName"], unique = true)]
)
data class ThirdContainerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val room: String,              // 所属房间
    val containerName: String,     // 对应主容器名称
    val subContainerName: String,  // 对应二级容器名称
    val thirdContainerName: String // 三级容器名称
)
