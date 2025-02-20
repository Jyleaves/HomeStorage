package com.example.homestorage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.RoomEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val roomDao = db.roomDao()

    val allRooms = roomDao.getAllRooms().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertRoom(name: String) {
        viewModelScope.launch {
            roomDao.insert(RoomEntity(name = name))
        }
    }

    fun deleteRoom(roomName: String) {
        viewModelScope.launch {
            // 级联删除：删除该房间下的所有物品、容器、二级容器、三级容器
            db.itemDao().deleteItemsByRoom(roomName)
            db.containerDao().deleteContainersByRoom(roomName)
            db.subContainerDao().deleteSubContainersByRoom(roomName)
            db.thirdContainerDao().deleteThirdContainersByRoom(roomName)
            // 删除房间记录
            roomDao.deleteRoomByName(roomName)
        }
    }

    fun updateRoom(oldRoomName: String, newRoomName: String) {
        viewModelScope.launch {
            // 更新房间记录
            roomDao.updateRoomName(oldRoomName, newRoomName)
            // 级联更新其他表中 room 字段
            db.itemDao().updateItemsRoom(oldRoomName, newRoomName)
            db.containerDao().updateContainersRoom(oldRoomName, newRoomName)
            db.subContainerDao().updateSubContainersRoom(oldRoomName, newRoomName)
            db.thirdContainerDao().updateThirdContainersRoom(oldRoomName, newRoomName)
        }
    }
}
