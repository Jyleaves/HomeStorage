package com.example.homestorage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.SubContainerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SubContainerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val subContainerDao = db.subContainerDao()

    fun getSubContainers(room: String, container: String): Flow<List<SubContainerEntity>> {
        return subContainerDao.getSubContainersByRoomAndContainer(room, container)
    }

    fun insertOrUpdateSubContainers(
        room: String,
        container: String,
        subContainers: List<String>
    ) = viewModelScope.launch {
        // 删除该容器下所有二级容器数据
        subContainerDao.deleteSubContainersByContainer(room, container)
        // 同时删除该容器下所有三级容器数据
        db.thirdContainerDao().deleteThirdContainersByContainer(room, container)

        val newList = subContainers.filter { it.isNotBlank() }.map { subName ->
            SubContainerEntity(
                room = room,
                containerName = container,
                subContainerName = subName
            )
        }
        if (newList.isNotEmpty()) {
            subContainerDao.insertSubContainers(newList)
        }
    }

    // 如果在编辑模式下容器名称发生了变化，需要先删除旧记录，再插入新记录
    fun updateSubContainers(
        room: String,
        oldContainerName: String,
        newContainerName: String,
        subContainers: List<String>
    ) = viewModelScope.launch {
        if (oldContainerName != newContainerName) {
            // 删除旧容器名下的二级容器记录
            subContainerDao.deleteSubContainersByContainer(room, oldContainerName)
        }
        // 删除新容器名下已有的二级容器记录
        subContainerDao.deleteSubContainersByContainer(room, newContainerName)
        // 同时删除新容器名下所有三级容器记录
        db.thirdContainerDao().deleteThirdContainersByContainer(room, newContainerName)

        // 插入新的二级容器数据
        val newList = subContainers.filter { it.isNotBlank() }.map { subName ->
            SubContainerEntity(
                room = room,
                containerName = newContainerName,
                subContainerName = subName
            )
        }
        if (newList.isNotEmpty()) {
            subContainerDao.insertSubContainers(newList)
        }
    }

    fun updateSubContainer(
        room: String,
        container: String,
        oldSubContainer: String,
        newSubContainer: String
    ) {
        viewModelScope.launch {
            // 更新二级容器记录
            subContainerDao.updateSubContainerName(room, container, oldSubContainer, newSubContainer)
            // 级联更新：更新三级容器中 subContainerName 字段
            db.thirdContainerDao().updateThirdContainerSubContainerName(
                room, container, oldSubContainer, newSubContainer
            )
        }
    }

    fun updateHasThirdContainer(
        room: String,
        containerName: String,
        subContainerName: String,
        hasThirdContainer: Boolean
    ) = viewModelScope.launch {
        subContainerDao.updateHasThirdContainer(room, containerName, subContainerName, hasThirdContainer)
    }

    fun deleteSubContainer(
        room: String,
        container: String,
        subContainer: String
    ) {
        viewModelScope.launch {
            // 级联删除：删除该二级容器下所有三级容器
            db.thirdContainerDao().deleteThirdContainersBySubContainer(room, container, subContainer)
            // 删除该二级容器记录
            subContainerDao.deleteSubContainerByName(room, container, subContainer)
        }
    }

    // 新增：删除指定房间下所有二级容器记录
    fun deleteSubContainersByRoom(room: String) {
        viewModelScope.launch {
            subContainerDao.deleteSubContainersByRoom(room)
        }
    }

    // 新增：删除指定二级容器记录（仅调用 DAO 方法，不执行级联删除操作）
    fun deleteSubContainerByName(
        room: String,
        container: String,
        subContainer: String
    ) {
        viewModelScope.launch {
            subContainerDao.deleteSubContainerByName(room, container, subContainer)
        }
    }
}
