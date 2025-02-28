// MultiImageSelector.kt
package com.example.homestorage.components

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage

// MultiImageSelector.kt
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MultiImageSelector(
    photoUris: List<Uri>,
    isEditing: Boolean,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onPreviewImage: (Int) -> Unit,
    onMoveImage: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    maxCount: Int = 3
) {
    val remaining = maxCount - photoUris.size
    val itemSize = 120.dp
    val spacing = 8.dp

    val dragState = rememberDragState()
    var currentDragIndex by remember { mutableIntStateOf(-1) }

    BoxWithConstraints(modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(photoUris) { index, uri ->
                ImageItem(
                    uri = uri,
                    index = index,
                    isEditing = isEditing,
                    onRemove = { onRemoveImage(index) },
                    onPreview = { onPreviewImage(index) },
                    size = itemSize,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = if (currentDragIndex == index) dragState.offset.x else 0f
                            alpha = if (currentDragIndex == index) 0.8f else 1f
                            scaleX = if (currentDragIndex == index) 1.1f else 1f
                            scaleY = if (currentDragIndex == index) 1.1f else 1f
                        }
                        .pointerInput(isEditing) {
                            if (!isEditing) return@pointerInput

                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    currentDragIndex = index
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragState.onDrag(dragAmount)

                                    // 计算拖拽位置
                                    val targetIndex = index + (dragState.offset.x / itemSize.toPx()).toInt()
                                    if (targetIndex in photoUris.indices && targetIndex != index) {
                                        onMoveImage(index, targetIndex)
                                        // 交换后立即重置状态
                                        currentDragIndex = -1
                                        dragState.reset()
                                    }
                                },
                                onDragEnd = {
                                    currentDragIndex = -1
                                    dragState.reset()
                                },
                                onDragCancel = {
                                    currentDragIndex = -1
                                    dragState.reset()
                                }
                            )
                        }
                        .zIndex(if (currentDragIndex == index) 1f else 0f)
                )
            }

            if (isEditing && remaining > 0) {
                item {
                    AddImageButton(
                        remaining = remaining,
                        onAddImage = onAddImage,
                        size = itemSize
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageItem(
    uri: Uri,
    index: Int,
    isEditing: Boolean,
    onRemove: () -> Unit,
    onPreview: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(size)
            .clickable { onPreview() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            AsyncImage(
                model = uri,
                contentDescription = "Item image ${index + 1}",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // 右上角的删除按钮
            if (isEditing) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp)  // 适当减小整体按钮尺寸
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove",
                        // 让图标的颜色和背景半透明，视觉更轻盈
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddImageButton(
    remaining: Int,
    onAddImage: () -> Unit,
    size: Dp
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onAddImage),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add image",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$remaining left",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

// 新增拖拽状态类
class DragState {
    var offset by mutableStateOf(Offset.Zero)

    fun onDrag(dragAmount: Offset) {
        offset += dragAmount
    }

    fun reset() {
        offset = Offset.Zero
    }
}

@Composable
fun rememberDragState() = remember { DragState() }
