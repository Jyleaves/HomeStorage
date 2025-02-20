package com.example.homestorage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Query("SELECT * FROM containers ORDER BY id ASC")
    fun getAllContainers(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE room = :room ORDER BY id ASC")
    fun getContainersByRoom(room: String): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE room = :room AND name = :containerName LIMIT 1")
    fun getContainerByRoomAndName(room: String, containerName: String): Flow<ContainerEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(container: ContainerEntity)

    @Query("DELETE FROM containers WHERE room = :room AND name = :containerName")
    suspend fun deleteContainer(room: String, containerName: String)

    @Query("""
        UPDATE containers
        SET name = :newName, hasSubContainer = :newHasSubContainer
        WHERE room = :room AND name = :oldName
    """)
    suspend fun updateContainer(
        room: String,
        oldName: String,
        newName: String,
        newHasSubContainer: Boolean
    )

    @Query("DELETE FROM containers WHERE room = :room")
    suspend fun deleteContainersByRoom(room: String)

    @Query("UPDATE containers SET room = :newRoom WHERE room = :oldRoom")
    suspend fun updateContainersRoom(oldRoom: String, newRoom: String)
}
