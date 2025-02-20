package com.example.homestorage.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "containers",
    indices = [Index(value = ["room", "name"], unique = true)]
)
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val room: String,               // 所属房间
    val name: String,               // 容器名称
    val hasSubContainer: Boolean = false,  // 是否有二级容器
)

