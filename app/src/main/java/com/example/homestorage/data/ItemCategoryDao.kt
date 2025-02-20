package com.example.homestorage.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCategoryDao {
    @Query("SELECT * FROM item_categories")
    fun getAllCategories(): Flow<List<ItemCategory>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: ItemCategory)

    @Delete
    suspend fun delete(category: ItemCategory)

    @Update
    suspend fun update(category: ItemCategory)
}

