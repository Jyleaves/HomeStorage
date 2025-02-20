package com.example.homestorage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.Item
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val itemDao = AppDatabase.getDatabase(application).itemDao()

    // 将 Flow 转为 StateFlow 以便在 Compose 中观察
    val allItems = itemDao.getAllItems().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun insert(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    fun delete(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            itemDao.deleteAll()
        }
    }

    fun updateItemsForContainerChange(
        room: String,
        oldContainerName: String,
        newContainerName: String
    ) {
        viewModelScope.launch {
            itemDao.updateContainerForRoom(room, oldContainerName, newContainerName)
        }
    }

    fun updateSubContainerName(
        room: String,
        container: String,
        oldSubContainer: String,
        newSubContainer: String
    ) {
        viewModelScope.launch {
            itemDao.updateSubContainerNameForItems(room, container, oldSubContainer, newSubContainer)
        }
    }

    // 新增：调用 DAO 更新 items 表中 room 字段的方法
    fun updateItemsRoom(oldRoom: String, newRoom: String) {
        viewModelScope.launch {
            itemDao.updateItemsRoom(oldRoom, newRoom)
        }
    }

    // 新增：调用 DAO 删除指定二级容器下所有物品的方法
    fun deleteItemsBySubContainer(room: String, container: String, subContainer: String) {
        viewModelScope.launch {
            itemDao.deleteItemsBySubContainer(room, container, subContainer)
        }
    }
}

