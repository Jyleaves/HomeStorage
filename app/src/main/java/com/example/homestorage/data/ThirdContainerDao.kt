package com.example.homestorage.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ThirdContainerDao {
    @Query("""
        SELECT * FROM third_containers 
        WHERE room = :room 
          AND containerName = :container 
          AND subContainerName = :subContainer 
        ORDER BY id ASC
    """)
    fun getThirdContainersByRoomAndSubContainer(
        room: String,
        container: String,
        subContainer: String
    ): Flow<List<ThirdContainerEntity>>

    @Query("SELECT * FROM third_containers ORDER BY id ASC")
    fun getAllThirdContainers(): Flow<List<ThirdContainerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThirdContainer(thirdContainer: ThirdContainerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThirdContainers(thirdContainers: List<ThirdContainerEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(thirdContainer: ThirdContainerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(thirdContainers: List<ThirdContainerEntity>)

    @Query("""
        DELETE FROM third_containers 
        WHERE room = :room 
          AND containerName = :container 
          AND subContainerName = :subContainer
    """)
    suspend fun deleteThirdContainersBySubContainer(room: String, container: String, subContainer: String)

    @Query("DELETE FROM third_containers WHERE room = :room")
    suspend fun deleteThirdContainersByRoom(room: String)

    @Query("DELETE FROM third_containers WHERE room = :room AND containerName = :container")
    suspend fun deleteThirdContainersByContainer(room: String, container: String)

    @Delete
    suspend fun deleteThirdContainer(thirdContainer: ThirdContainerEntity)

    // 当容器名称更新时，更新第三容器表中的 containerName 字段
    @Query("UPDATE third_containers SET containerName = :newContainerName WHERE room = :room AND containerName = :oldContainerName")
    suspend fun updateThirdContainerContainerName(room: String, oldContainerName: String, newContainerName: String)

    // 当二级容器名称更新时，更新第三容器表中的 subContainerName 字段
    @Query("UPDATE third_containers SET subContainerName = :newSubContainerName WHERE room = :room AND containerName = :container AND subContainerName = :oldSubContainerName")
    suspend fun updateThirdContainerSubContainerName(
        room: String,
        container: String,
        oldSubContainerName: String,
        newSubContainerName: String
    )

    // 当房间名称更新时，更新第三容器表中的 room 字段
    @Query("UPDATE third_containers SET room = :newRoom WHERE room = :oldRoom")
    suspend fun updateThirdContainersRoom(oldRoom: String, newRoom: String)
}
