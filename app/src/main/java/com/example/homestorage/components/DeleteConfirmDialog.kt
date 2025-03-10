// DeleteConfirmDialog.kt
package com.example.homestorage.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homestorage.data.Item

@Composable
fun DeleteConfirmDialog(
    itemsToDelete: List<Item>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "确认删除",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("确定要永久删除选中的 ${itemsToDelete.size} 项物品吗？")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "包含物品：${itemsToDelete.take(3).joinToString { it.name }}${if (itemsToDelete.size > 3) " 等..." else ""}",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
