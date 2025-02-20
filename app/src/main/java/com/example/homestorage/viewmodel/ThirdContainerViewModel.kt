package com.example.homestorage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.ThirdContainerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ThirdContainerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val thirdContainerDao = db.thirdContainerDao()

    fun getThirdContainers(
        room: String,
        container: String,
        subContainer: String
    ): Flow<List<ThirdContainerEntity>> {
        return thirdContainerDao.getThirdContainersByRoomAndSubContainer(
            room, container, subContainer
        )
    }

    fun insertOrUpdateThirdContainers(
        room: String,
        container: String,
        subContainer: String,
        thirdContainers: List<String>
    ) = viewModelScope.launch {
        // 删除当前二级容器下所有三级容器记录，再插入新的
        thirdContainerDao.deleteThirdContainersBySubContainer(room, container, subContainer)
        val newList = thirdContainers.filter { it.isNotBlank() }.map { thirdName ->
            ThirdContainerEntity(
                room = room,
                containerName = container,
                subContainerName = subContainer,
                thirdContainerName = thirdName
            )
        }
        if (newList.isNotEmpty()) {
            thirdContainerDao.insertThirdContainers(newList)
        }
    }

    fun deleteThirdContainer(thirdContainer: ThirdContainerEntity) = viewModelScope.launch {
        thirdContainerDao.deleteThirdContainer(thirdContainer)
    }
}
