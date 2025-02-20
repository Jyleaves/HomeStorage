package com.example.homestorage.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius

@Composable
fun ImageSelector(
    photoUri: Uri?,
    isEditing: Boolean, // true 表示可编辑（点击后可换图），false 表示只读（点击后查看大图）
    onSelectImage: () -> Unit, // 可编辑时点击触发（例如弹出图片选择对话框）
    onPreviewImage: () -> Unit, // 只读时点击触发（例如显示大图预览）
    modifier: Modifier = Modifier,
    borderThickness: Dp = 2.dp,
    cornerRadius: Dp = 8.dp,
    dashWidth: Dp = 10.dp,
    dashGap: Dp = 5.dp
) {
    // 在 Composable 范围内计算虚线参数
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { borderThickness.toPx() }
    val dashWidthPx = with(density) { dashWidth.toPx() }
    val dashGapPx = with(density) { dashGap.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    Box(
        modifier = modifier
            .clickable {
                if (isEditing) {
                    onSelectImage()
                } else {
                    onPreviewImage()
                }
            }
            // 使用 drawBehind 绘制虚线边框，仅当 photoUri 为空时绘制
            .drawBehind {
                if (photoUri == null) {
                    drawRoundRect(
                        color = borderColor,
                        style = Stroke(
                            width = strokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(dashWidthPx, dashGapPx),
                                0f
                            ),
                            cap = StrokeCap.Round
                        ),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (photoUri == null) {
            // 当没有图片时显示加号图标作为占位
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加图片",
                tint = borderColor,
                modifier = Modifier.size(36.dp)
            )
        } else {
            // 当有图片时显示图片
            AsyncImage(
                model = photoUri,
                contentDescription = "物品图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
