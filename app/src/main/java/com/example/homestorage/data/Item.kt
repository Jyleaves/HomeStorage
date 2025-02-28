// Item.kt
package com.example.homestorage.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    indices = [Index(value=["timestamp"], unique=true)]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val room: String,
    val container: String,
    val subContainer: String? = null,
    val thirdContainer: String? = null,
    val category: String,
    val description: String,
    val photoUris: List<String> = emptyList(),
    val productionDate: Long? = null,    // 生产日期
    val reminderDays: Long? = null,        // 提前提醒的天数
    val quantity: Int? = null,             // 数量
    val timestamp: Long = System.currentTimeMillis(),
    val expirationDate: Long? = null
) {
    fun getFullLocation(): String {
        return listOfNotNull(
            room.takeIf { it.isNotEmpty() },
            container.takeIf { it.isNotEmpty() },
            subContainer?.takeIf { it.isNotEmpty() },
            thirdContainer?.takeIf { it.isNotEmpty() }
        ).joinToString(" > ")
    }

    fun getFirstPhotoUri(): String? = photoUris.firstOrNull()
}
