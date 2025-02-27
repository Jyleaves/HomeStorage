// ItemDao.kt
package com.example.homestorage.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE expirationDate IS NOT NULL AND expirationDate <= :time")
    suspend fun getItemsExpiringBefore(time: Long): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Transaction // 确保原子操作
    @Query("DELETE FROM items WHERE id IN (:itemIds)")
    suspend fun deleteBatch(itemIds: List<Int>)

    @Query("DELETE FROM items")
    suspend fun deleteAll()

    @Query("""
        UPDATE items
        SET container = :newContainerName
        WHERE room = :room AND container = :oldContainerName
    """)
    suspend fun updateContainerForRoom(
        room: String,
        oldContainerName: String,
        newContainerName: String
    )

    // 新增：更新 items 表中 room 字段（级联更新房间名称）
    @Query("""
        UPDATE items
        SET room = :newRoom
        WHERE room = :oldRoom
    """)
    suspend fun updateItemsRoom(oldRoom: String, newRoom: String)

    // 删除指定房间下的所有物品
    @Query("DELETE FROM items WHERE room = :roomName")
    suspend fun deleteItemsByRoom(roomName: String)

    // 根据类别查所有物品
    @Query("SELECT * FROM items WHERE category = :categoryName")
    suspend fun getItemsByCategory(categoryName: String): List<Item>

    // 批量更新物品
    @Update
    suspend fun updateItems(items: List<Item>)

    // 删除该类别的所有物品
    @Query("DELETE FROM items WHERE category = :categoryName")
    suspend fun deleteItemsByCategory(categoryName: String)

    @Query("""
        UPDATE items
        SET subContainer = :newSubContainer
        WHERE room = :room AND container = :container AND subContainer = :oldSubContainer
    """)
    suspend fun updateSubContainerNameForItems(
        room: String,
        container: String,
        oldSubContainer: String,
        newSubContainer: String
    )

    // 新增：删除指定 room、container 下属于某个 subContainer 的所有物品
    // （根据业务需求，你也可以选择更新该字段为空字符串或 NULL）
    @Query("""
        DELETE FROM items 
        WHERE room = :room AND container = :container AND subContainer = :subContainer
    """)
    suspend fun deleteItemsBySubContainer(
        room: String,
        container: String,
        subContainer: String
    )
}

