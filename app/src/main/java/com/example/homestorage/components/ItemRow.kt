// ItemRow.kt
package com.example.homestorage.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
//noinspection UsingMaterialAndMaterial3Libraries
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ItemRow(
    item: com.example.homestorage.data.Item,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
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

    val pointerModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { onLongClick() },
            onTap = { onClick() }
        )
    }

    if (isSelectionMode) {
        // 将复选框放置在左侧，并且不与物品的背景混合
        Row(
            modifier = Modifier.fillMaxWidth().then(pointerModifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp) // 给复选框一点内边距
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 物品内容
            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.EndToStart),
                background = {
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
                            .padding(end = 60.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = item.photoUri,
                                    contentDescription = item.name,
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1
                                    )
                                    val location = listOf(
                                        item.room,
                                        item.container,
                                        item.subContainer,
                                        item.thirdContainer
                                    ).filter { !it.isNullOrEmpty() }.joinToString(" - ")
                                    Text(
                                        text = "位置: $location",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    } else {
        // 物品内容直接显示
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart),
            background = {
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
                        .padding(end = 60.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
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
                Row(
                    modifier = Modifier.fillMaxWidth().then(pointerModifier)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            AsyncImage(
                                model = item.photoUri,
                                contentDescription = item.name,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                val location = listOf(
                                    item.room,
                                    item.container,
                                    item.subContainer,
                                    item.thirdContainer
                                ).filter { !it.isNullOrEmpty() }.joinToString(" - ")
                                Text(
                                    text = "位置: $location",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        )
    }

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
