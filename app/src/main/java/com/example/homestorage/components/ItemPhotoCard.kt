// ItemPhotoCard.kt
package com.example.homestorage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.homestorage.data.Item
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.layout.ContentScale

@Composable
fun ItemPhotoCard(
    item: Item,
    onClick: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit
) {
    // 状态变量，用于显示确认删除弹窗
    var showConfirmDelete by remember { mutableStateOf(false) }

    // 如果处于多选模式，提高 elevation 使阴影更明显
    val cardElevation = if (isSelectionMode) 8.dp else 4.dp

    Card(
        modifier = Modifier
            .size(100.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 显示物品照片
            item.photoUris.firstOrNull()?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            } ?: run {
                // 如果没有照片，则显示名称和背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 如果进入编辑模式，在卡片上添加半透明覆盖层提示用户
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                )
            }
            // 当进入多选模式时显示复选框和删除叉
            if (isSelectionMode) {
                // 左上角显示自定义复选框（圆形边框，未选中为半透明深色，选中为绿色填充）
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    CustomRoundCheckbox(
                        checked = isSelected,
                        onCheckedChange = onSelectToggle,
                        size = 20.dp
                    )
                }
                // 右上角显示删除按钮（无背景，尺寸 24.dp）
                IconButton(
                    onClick = { showConfirmDelete = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    // 显示删除确认弹窗
    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("确认删除") },
            text = { Text("是否真的要删除此物品？") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDelete = false
                    onDelete()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("取消")
                }
            }
        )
    }
}
