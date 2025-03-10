//CustomRoundCheckbox.kt
package com.example.homestorage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomRoundCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
    selectedColor: Color = Color.Green
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange() },
        modifier = modifier.size(size)
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(size)
                    .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
                    .background(selectedColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.7f)
                )
            }
        } else {
            // 未选中时显示带边框的圆形
            Box(
                modifier = Modifier
                    .size(size)
                    .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
            )
        }
    }
}
