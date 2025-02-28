// ZoomableImage.kt
package com.example.homestorage.components

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@SuppressLint("UnrememberedMutableState")
@Composable
fun ZoomableImage(
    uri: Uri,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    minScale: Float = 1f,
    maxScale: Float = 5f
) {
    var scale by remember(uri) { mutableFloatStateOf(1f) }
    var offset by remember(uri) { mutableStateOf(Offset.Zero) }
    var layoutSize by remember(uri) { mutableStateOf(IntSize.Zero) }

    // 计算最大偏移范围
    val maxOffsetX by derivedStateOf {
        if (scale <= 1f) 0f else layoutSize.width * (scale - 1f) / 2
    }
    val maxOffsetY by derivedStateOf {
        if (scale <= 1f) 0f else layoutSize.height * (scale - 1f) / 2
    }

    val edgeThreshold = with(LocalDensity.current) { 5.dp.toPx() } // 设定“靠近边缘”的容差

    // NestedScrollConnection：在图片未放大或达到边缘时，将剩余水平滑动传递给父组件（HorizontalPager）
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 图片未放大时，全部传递
                if (scale <= 1f) return Offset(available.x, 0f)
                // 图片已放大时，根据 offset 判断是否到达左右边缘
                var consumedX = 0f
                if (available.x > 0) { // 向右拖动：图片右移，左边缘为 -maxOffsetX
                    if (offset.x <= -maxOffsetX + 1f) {
                        consumedX = 0f // 已在左边缘
                    } else {
                        val canConsume = offset.x - (-maxOffsetX)
                        consumedX = available.x.coerceAtMost(canConsume)
                    }
                } else if (available.x < 0) { // 向左拖动
                    if (offset.x >= maxOffsetX - 1f) {
                        consumedX = 0f // 已在右边缘
                    } else {
                        val canConsume = maxOffsetX - offset.x
                        consumedX = available.x.coerceAtLeast(-canConsume)
                    }
                }
                return Offset(consumedX, 0f)
            }
        }
    }

    Box(
        modifier = modifier
            // 让 HorizontalPager 有机会先接收到单指滑动事件
            .nestedScroll(nestedScrollConnection)
            .pointerInput(uri) {
                awaitEachGesture {
                    // 等待第一个按下事件
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // 尝试等待第二个按下事件（超时 150 毫秒）
                    val pointerEvent = try {
                        withTimeout(150) { awaitPointerEvent() }
                    } catch (e: Exception) {
                        null
                    }
                    if (pointerEvent == null || pointerEvent.changes.size < 2) {
                        // 单指分支
                        // 如果图片已放大，则单指拖动用于平移；否则，不消费，交由 HorizontalPager 处理翻页
                        if (scale > 1f) {
                            do {
                                val event = awaitPointerEvent()
                                val drag = event.changes.firstOrNull()?.positionChange() ?: Offset.Zero
                                val potentialOffsetX = offset.x + drag.x
                                val potentialOffsetY = offset.y + drag.y
                                // 计算理想的平移后位置
                                val newOffsetX = (offset.x + drag.x).coerceIn(-maxOffsetX, maxOffsetX)
                                // 如果水平已经接近边缘，则阻止垂直方向更新
                                val nearEdge = (kotlin.math.abs(newOffsetX - (-maxOffsetX)) < edgeThreshold) ||
                                        (kotlin.math.abs(newOffsetX - maxOffsetX) < edgeThreshold)
                                val newOffsetY = if (nearEdge) offset.y else potentialOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                offset = Offset(newOffsetX, newOffsetY)
                                // 只有在未超出边界时消费事件
                                if (potentialOffsetX in (-maxOffsetX..maxOffsetX) &&
                                    potentialOffsetY in (-maxOffsetY..maxOffsetY)
                                ) {
                                    event.changes.forEach { it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        } else {
                            // 图片未放大，放弃消费，交由父组件处理翻页
                            awaitPointerEvent(PointerEventPass.Final)
                        }
                        return@awaitEachGesture
                    } else {
                        // 多指分支：用于缩放与平移（完全消费事件，防止翻页）
                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
                            scale = newScale
                            if (newScale > 1f) {
                                val newOffsetX = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX)
                                val newOffsetY = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                                offset = Offset(newOffsetX, newOffsetY)
                            }
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
            // 单击退出
            .clickable { onClick() }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            .onSizeChanged { layoutSize = it },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Zoomable image",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}