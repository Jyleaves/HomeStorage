// ItemListView.kt
package com.example.homestorage.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homestorage.data.Item

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ItemListView(
    items: List<Item>,
    selectedItems: List<Item>,
    isSelectionMode: Boolean,
    onItemClick: (Item) -> Unit,
    onItemLongClick: (Item) -> Unit,
    onDelete: (Item) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ItemRow(
                item = item,
                isSelected = selectedItems.contains(item),
                isSelectionMode = isSelectionMode,
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) },
                onDelete = { onDelete(item) }
            )
        }
    }
}
