// ItemViewModel.kt
package com.example.homestorage.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.homestorage.util.deletePhotoFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val itemDao = AppDatabase.getDatabase(application).itemDao()
    private val appContext = application.applicationContext

    // 将 Flow 转为 StateFlow 以便在 Compose 中观察
    val allItems = itemDao.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun getItemById(itemId: Int): Item? {
        return itemDao.getItemById(itemId)
    }

    fun insert(item: Item) {
        viewModelScope.launch {
            val newId = itemDao.insert(item) // 如果 insert 返回 Long，则记录它
            Log.d("ItemViewModel", "Inserted new item with id: $newId")
        }
    }

    fun delete(item: Item) {
        viewModelScope.launch {
            // 先删除照片文件
            deletePhotoFile(appContext, item.photoUri)
            // 再删除数据库记录
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

    fun updateItems(items: List<Item>) {
        viewModelScope.launch {
            itemDao.updateItems(items)
        }
    }

    fun deleteItems(items: List<Item>) {
        viewModelScope.launch(Dispatchers.IO) {
            // 获取要删除的ID列表
            val itemIds = items.map { it.id }
            val photoUris = items.map { it.photoUri }

            // 批量删除数据库记录
            itemDao.deleteBatch(itemIds)

            // 批量删除照片文件
            photoUris.forEach { uri ->
                deletePhotoFile(appContext, uri)
            }

            // 在主线程显示结果
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    appContext,
                    "已删除 ${items.size} 件物品",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

