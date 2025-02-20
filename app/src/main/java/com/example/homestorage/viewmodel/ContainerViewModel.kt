package com.example.homestorage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.ContainerEntity
import kotlinx.coroutines.launch

class ContainerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val containerDao = db.containerDao()

    fun getContainersByRoom(room: String) = containerDao.getContainersByRoom(room)

    fun insertContainer(room: String, containerName: String, hasSubContainer: Boolean = false) {
        viewModelScope.launch {
            containerDao.insert(ContainerEntity(room = room, name = containerName, hasSubContainer = hasSubContainer))
        }
    }

    fun getContainerByRoomAndName(room: String, containerName: String) =
        containerDao.getContainerByRoomAndName(room, containerName)

    fun deleteContainer(room: String, containerName: String) {
        viewModelScope.launch {
            // 级联删除：删除该容器下所有二级容器和三级容器
            db.subContainerDao().deleteSubContainersByContainer(room, containerName)
            db.thirdContainerDao().deleteThirdContainersByContainer(room, containerName)
            containerDao.deleteContainer(room, containerName)
        }
    }

    fun updateContainer(
        room: String,
        oldName: String,
        newName: String,
        newHasSubContainer: Boolean
    ) {
        viewModelScope.launch {
            // 更新容器记录
            containerDao.updateContainer(room, oldName, newName, newHasSubContainer)
            // 级联更新：更新二级容器和三级容器中 containerName 字段
            db.subContainerDao().updateSubContainerContainerName(room, oldName, newName)
            db.thirdContainerDao().updateThirdContainerContainerName(room, oldName, newName)
        }
    }
}
