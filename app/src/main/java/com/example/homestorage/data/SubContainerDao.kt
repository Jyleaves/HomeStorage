// SubContainerDao.kt
package com.example.homestorage.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubContainerDao {

    @Query("SELECT * FROM subcontainers WHERE room = :room AND containerName = :containerName ORDER BY id ASC")
    fun getSubContainersByRoomAndContainer(room: String, containerName: String): Flow<List<SubContainerEntity>>

    @Query("SELECT * FROM subcontainers ORDER BY id ASC")
    fun getAllSubContainers(): Flow<List<SubContainerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubContainer(subContainer: SubContainerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubContainers(subContainers: List<SubContainerEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(subContainer: SubContainerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(subContainers: List<SubContainerEntity>)

    @Query("DELETE FROM subcontainers WHERE room = :room AND containerName = :containerName")
    suspend fun deleteSubContainersByContainer(room: String, containerName: String)

    @Delete
    suspend fun deleteSubContainer(subContainer: SubContainerEntity)

    @Query("""
    UPDATE subcontainers 
    SET hasThirdContainer = :hasThirdContainer 
    WHERE room = :room 
      AND containerName = :containerName 
      AND subContainerName = :subContainerName
""")
    suspend fun updateHasThirdContainer(
        room: String,
        containerName: String,
        subContainerName: String,
        hasThirdContainer: Boolean
    )

    @Query("""
    UPDATE subcontainers 
    SET subContainerName = :newName 
    WHERE room = :room 
      AND containerName = :containerName 
      AND subContainerName = :oldName
""")
    suspend fun updateSubContainerName(
        room: String,
        containerName: String,
        oldName: String,
        newName: String
    )

    @Query("UPDATE subcontainers SET containerName = :newContainerName WHERE room = :room AND containerName = :oldContainerName")
    suspend fun updateSubContainerContainerName(room: String, oldContainerName: String, newContainerName: String)

    @Query("UPDATE subcontainers SET room = :newRoom WHERE room = :oldRoom")
    suspend fun updateSubContainersRoom(oldRoom: String, newRoom: String)

    // 删除指定房间下的所有二级容器记录
    @Query("DELETE FROM subcontainers WHERE room = :room")
    suspend fun deleteSubContainersByRoom(room: String)

    // 删除指定的二级容器（根据 room、containerName 及 subContainerName）
    @Query("""
        DELETE FROM subcontainers 
        WHERE room = :room AND containerName = :container AND subContainerName = :subContainer
    """)
    suspend fun deleteSubContainerByName(
        room: String,
        container: String,
        subContainer: String
    )
}
