// AddActionDialog.kt
package com.example.homestorage.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AddActionDialog(
    selectedRoom: String,
    onDismiss: () -> Unit,
    onAddItem: () -> Unit,
    onAddContainer: () -> Unit,
    onManageRoom: () -> Unit,
    onManageItemCategory: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("请选择操作") },
        text = {
            androidx.compose.foundation.layout.Column {
                TextButton(onClick = onAddItem) { Text("添加物品") }
                if (selectedRoom != "全部") {
                    TextButton(onClick = onAddContainer) { Text("添加容器") }
                }
                TextButton(onClick = onManageRoom) { Text("管理房间") }
                TextButton(onClick = onManageItemCategory) { Text("管理物品类别") }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
