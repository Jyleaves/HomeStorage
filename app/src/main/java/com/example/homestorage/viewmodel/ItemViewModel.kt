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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*

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
            // 遍历删除所有照片文件
            item.photoUris.forEach { uri ->
                deletePhotoFile(appContext, uri)
            }
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
            // 展开所有照片URI
            val photoUris = items.flatMap { it.photoUris }

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
                    "已删除 ${items.size} 件物品（含${photoUris.size}张照片）",
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

    // 获取指定物品的所有照片URI
    suspend fun getItemPhotoUris(itemId: Int): List<String> {
        return itemDao.getItemById(itemId)?.photoUris ?: emptyList()
    }

    // 在 ItemViewModel 中
    fun getFilteredItems(selectedRoom: String, searchQuery: String): Flow<List<Item>> {
        // 当搜索关键词为"到期"时，创建一个每秒发射当前时间的 flow
        val timeFlow = if (searchQuery.trim() == "到期") {
            flow {
                while (true) {
                    emit(System.currentTimeMillis())
                    delay(1000L)
                }
            }
        } else {
            // 非“到期”情况，只需要发射一次无关时间的值
            flowOf(0L)
        }
        return combine(allItems, timeFlow) { items, currentTime ->
            val baseList = if (selectedRoom == "全部") items else items.filter { it.room == selectedRoom }
            if (searchQuery.trim() == "到期") {
                baseList.filter { item ->
                    item.expirationDate != null && item.reminderDays != null &&
                            item.expirationDate > currentTime &&
                            (item.expirationDate - currentTime) <= item.reminderDays * 24 * 60 * 60 * 1000
                }
            } else {
                baseList.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }.flowOn(Dispatchers.Default)
    }

    // 在 ItemViewModel.kt 中新增该函数
    fun getFilteredItemsForContainer(
        room: String,
        container: String,
        filterMode: String, // "category" 或 "subcontainer"
        selectedCategory: String,
        selectedSubContainer: String,
        selectedThirdContainer: String,
        searchQuery: String = ""
    ): Flow<List<Item>> {
        // 如果搜索关键词为"到期"，每秒发射一次当前时间；否则只发射一次
        val timeFlow = if (searchQuery.trim() == "到期") {
            flow {
                while (true) {
                    emit(System.currentTimeMillis())
                    delay(1000L)
                }
            }
        } else {
            flowOf(0L)
        }
        return combine(allItems, timeFlow) { items, currentTime ->
            // 先按房间和容器过滤，但当 room 为 "全部" 或 container 为空时不做过滤
            var filtered = items
            if (room != "全部") {
                filtered = filtered.filter { it.room == room }
            }
            if (container.isNotBlank()) {
                filtered = filtered.filter { it.container == container }
            }

            // 搜索关键词过滤（如"到期"或名称搜索）
            if (searchQuery.trim() == "到期") {
                filtered = filtered.filter { item ->
                    item.expirationDate != null && item.reminderDays != null &&
                            item.expirationDate > currentTime &&
                            (item.expirationDate - currentTime) <= item.reminderDays * 24 * 60 * 60 * 1000
                }
            } else if (searchQuery.isNotBlank()) {
                filtered = filtered.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            // 再根据筛选模式进行细化过滤
            when (filterMode) {
                "category" -> {
                    if (selectedCategory != "全部" && selectedCategory.isNotBlank()) {
                        filtered = filtered.filter { it.category == selectedCategory }
                    }
                }
                "subcontainer" -> {
                    filtered = if (selectedSubContainer == "全部") filtered
                    else filtered.filter { it.subContainer == selectedSubContainer }
                    // 如果已选二级容器且三级容器不为"全部"，进一步过滤
                    if (selectedSubContainer != "全部" && selectedThirdContainer != "全部") {
                        filtered = filtered.filter { it.thirdContainer == selectedThirdContainer }
                    }
                }
            }
            filtered
        }.flowOn(Dispatchers.Default)
    }
}

