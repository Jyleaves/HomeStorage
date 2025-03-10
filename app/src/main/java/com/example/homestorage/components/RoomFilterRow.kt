// RoomFilterRow.kt
package com.example.homestorage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homestorage.data.RoomEntity

@Composable
fun RoomFilterRow(
    rooms: List<RoomEntity>,
    selectedRoom: String,
    onRoomSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // “全部”选项
        item {
            FilterChip(
                selected = selectedRoom == "全部",
                onClick = { onRoomSelected("全部") },
                label = { Text("全部") }
            )
        }
        // 遍历房间
        items(rooms) { room ->
            FilterChip(
                selected = selectedRoom == room.name,
                onClick = { onRoomSelected(room.name) },
                label = { Text(room.name) }
            )
        }
    }
}
