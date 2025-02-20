package com.example.homestorage.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.homestorage.data.Item

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemRow(
    item: com.example.homestorage.data.Item,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // 控制是否显示确认删除弹窗
    var showConfirmDialog by remember { mutableStateOf(false) }

    // 使用 rememberDismissState 管理滑动状态
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            // 当从右向左滑动至删除状态且未显示弹窗时，显示确认弹窗并不直接完成删除
            if (dismissValue == DismissValue.DismissedToStart && !showConfirmDialog) {
                showConfirmDialog = true
                false
            } else {
                // 始终返回 false，等待外部操作处理删除或恢复
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            // 根据滑动进度设置背景色：
            // 默认状态使用 surfaceVariant，达到删除状态时使用 errorContainer
            val color = if (dismissState.targetValue == DismissValue.Default) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium) // 圆角背景
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                // 通过动画调整删除图标的缩放
                val scale by animateFloatAsState(
                    targetValue = if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.scale(scale)
                )
            }
        },
        dismissContent = {
            // 内容区域可点击，点击后进入编辑页面等
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    AsyncImage(
                        model = item.photoUri,
                        contentDescription = item.name,
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "房间: ${item.room}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "容器: ${item.container}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "类别: ${item.category}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )

    // 显示删除确认弹窗
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("确认删除") },
            text = { Text("是否真的要删除此物品？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
