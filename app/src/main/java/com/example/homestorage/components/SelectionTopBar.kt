// SelectionTopBar.kt
package com.example.homestorage.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onBatchEdit: () -> Unit,
    onBatchDelete: () -> Unit,
    onCancelSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text("已选择$selectedCount 项", fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = onBatchEdit) {
                Icon(Icons.Default.Edit, contentDescription = "批量编辑")
            }
            IconButton(onClick = onBatchDelete) {
                Icon(Icons.Default.Delete, contentDescription = "批量删除")
            }
            IconButton(onClick = onCancelSelection) {
                Icon(Icons.Default.Close, contentDescription = "取消选择")
            }
        },
        modifier = modifier
    )
}
