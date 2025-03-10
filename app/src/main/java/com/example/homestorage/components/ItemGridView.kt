// ItemGridView.kt
package com.example.homestorage.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.homestorage.data.Item

@Composable
fun ItemGridView(
    items: List<Item>,
    // 新增参数，用于支持多选操作
    isSelectionMode: Boolean,
    selectedItems: List<Item>,
    // 正常点击回调（非多选状态下）
    onItemClick: (Item) -> Unit,
    // 切换选中状态的回调
    onSelectToggle: (Item) -> Unit,
    // 删除回调：点击右上角叉时调用，父级可以弹出删除确认提示框后再删除
    onDelete: (Item) -> Unit,
    // 长按回调，用于进入多选模式
    onLongPress: (Item) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ItemPhotoCard(
                item = item,
                onClick = { onItemClick(item) },
                isSelectionMode = isSelectionMode,
                isSelected = selectedItems.contains(item),
                onSelectToggle = { onSelectToggle(item) },
                onDelete = { onDelete(item) },
                onLongPress = { onLongPress(item) }
            )
        }
    }
}
