package com.example.homestorage.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "item_categories",
    indices = [Index(value = ["categoryName"], unique = true)]
)
data class ItemCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryName: String,         // 物品类别名称
    val needProductionDate: Boolean,
    val needExpirationDate: Boolean,
    val needReminder: Boolean,
    val reminderPeriodDays: Long?,     // 提前提醒的天数，可为 null
    val needQuantity: Boolean          // 新增：是否需要数量属性
)
